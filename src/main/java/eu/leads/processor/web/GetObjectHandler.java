package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.leads.processor.execute.Tuple;
import eu.leads.processor.pagerank.graph.LeadsPrGraph;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vagvaz on 3/10/14.
 */
public class GetObjectHandler implements Handler<HttpServerRequest> {
    private EmbeddedCacheManager manager;
    private ObjectMapper mapper ;

    public GetObjectHandler(EmbeddedCacheManager cacheManager) {
        manager = cacheManager;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(final HttpServerRequest httpServerRequest) {

        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-Type", "application/json");
        System.err.println("request handler");
        System.err.flush();
        httpServerRequest.bodyHandler(new Handler<Buffer>() {

            @Override
            public void handle(Buffer body) {
                byte[] bt = body.getBytes();
//                String json = new String(bt);
//                System.err.println("received: " + json );
                HashMap<String,String> result = new HashMap<>();
                try {
                    ObjectQuery  query = mapper.readValue(bt,ObjectQuery.class);

//                    ObjectQuery query =new ObjectQuery();
//                    query.setTable(httpServerRequest.params().get("table"));
//                    query.setKey(httpServerRequest.params().get("key"));
//                    String[] attris = httpServerRequest.params().get("attributes").split(",");
//                    query.setAttributes(Arrays.asList(attris));
                    System.err.println(query.toString());
                    Cache<String, String> cache = manager.getCache(query.getTable());

                    if (cache == null) {
                        System.err.println("cache not found");
                        httpServerRequest.response().end(mapper.writeValueAsString("{}"));
                    } else {
                        //printCache(cache);
//                        System.out.println("requesting: " +query.toString());


                        if (!cache.containsKey(query.getKey())) {
                            System.err.println("cache does not contain  key " + query.getKey() + " val " + cache.get(query.getKey()));
                            httpServerRequest.response().end(mapper.writeValueAsString("{}"));
                            return;
                        }
                        String tupleString = cache.get(query.getKey());
                        JsonNode root = null;
//                        System.out.println("Reaching zero " + query.getAttributes().size());
//                        if(query.getTable().endsWith(":result:")&& query.getAttributes().size()==1&& query.getAttributes().contains("result"))
//                            cache.remove(query.getKey());
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            root = mapper.readTree(tupleString);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(query.getAttributes().size()==0){
                            Iterator<String> iterator = root.fieldNames();
                            List<String> attrs = query.getAttributes();
                            while (iterator.hasNext()) {
                                String field = iterator.next();
                                result.put(field,root.path(field).toString());
                            }
                        }
                        boolean tohandle = (query.getTable().equals("webpages:") && query.getAttributes().contains("pagerank"));
                        Iterator<String> iterator = root.fieldNames();
                        List<String> attrs = query.getAttributes();
                        while (iterator.hasNext()) {
                            String field = iterator.next();
                            if(field.equals("pagerank")){
                                if(!tohandle){
                                    if (attrs.contains(field)) {
                                        result.put(field, root.path(field).toString());
                                    }
                                }
                                else{
                                    if (attrs.contains(field)) {
                                        handlePagerank(root);
                                        result.put(field,root.path(field).toString());
                                    }
                                }
                            }
                            else{
                                if (attrs.contains(field)) {
                                    result.put(field, root.path(field).toString());
                                }
                            }

                        }
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    }
            } catch (JsonMappingException e) {
                    httpServerRequest.response().end("{}");
                    e.printStackTrace();
                }
                catch (JsonParseException e) {
                    httpServerRequest.response().end("{}");
                    e.printStackTrace();
                } catch (IOException e) {
                    httpServerRequest.response().end("{}");
                    e.printStackTrace();
                }
            }
    });
    }
    private void printCache(Cache<String, String> cache) {

        System.err.println("############");
        for (String key : cache.keySet()) {
            System.out.println(key + ": " + cache.get(key));
        }
        System.err.println("############");
    }

    private void handlePagerank(JsonNode t) {
            String pagerankStr = t.path("pagerank").asText();
            Double d = Double.parseDouble(pagerankStr);
            if (d < 0.0) {

                try {
                    d = LeadsPrGraph.getPageDistr(t.path("url").asText());
//                    d = (double) LeadsPrGraph.getPageVisitCount(t.getAttribute("url"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ObjectNode ob = (ObjectNode) t;
                ob.put("pagerank",d.toString());


        }
    }
}
