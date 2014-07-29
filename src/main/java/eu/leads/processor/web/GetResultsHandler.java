package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.leads.processor.utils.InfinispanUtils;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vagvaz on 3/10/14.
 */
public class GetResultsHandler implements Handler<HttpServerRequest> {
    private Cache<String, String> queriesCache;
    private EmbeddedCacheManager manager;
    private ObjectMapper mapper;

    public GetResultsHandler(EmbeddedCacheManager cacheManager) {
        queriesCache = cacheManager.getCache("queries");
        manager = cacheManager;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-type", "application/json");
        QueryResults result = new QueryResults();
        String queryId = httpServerRequest.params().get("id");
        long min = Long.parseLong(httpServerRequest.params().get("min"));
        long max = Long.parseLong(httpServerRequest.params().get("max"));
        result.setId(queryId);
//        manager = (EmbeddedCacheManager) ctx.getAttribute("manager");
        if (queriesCache == null) {
            String message = "queries cache has not been initialized check infinispan configuration";
            result.setMessage(message);
            System.err.println(message);
            try {
                httpServerRequest.response().end(mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return;
        } else {
//            System.out.println("LEADS:Queries " + queriesCache.size());
//            printCache(queriesCache);
            String queryJson = queriesCache.get(queryId);
            if (queryId == null) {
                try {
                    httpServerRequest.response().end(mapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {


                String readFrom = "";
                //        try {
            JsonNode root =null;
                try{
                    root = mapper.readTree(queryJson);
                    readFrom = mapper.readValue(root.path("output").textValue(), String.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException ee) {
                        ee.printStackTrace();
                    }
                    return;
                }
                System.err.println("READFROM " + readFrom + " " + min + " " + max);
                Cache<String, String> resultCache = manager.getCache(readFrom);
                if (resultCache == null)
                {
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                else{
                    List<String> tuples = new LinkedList<String>();
                    long i = 0;
                    result.setMin(min);
                    for (i = min; i <= max; i++) {
                        if (resultCache.size() <= i) {
                            System.err.println("ResultCache " + resultCache.size() + " " + i);
                            break;
                        }
                        tuples.add(resultCache.get(Long.toString(i)));
                    }
                    result.setMax(i);
                    result.setTuples(tuples);
                    result.setId(queryId);
                    result.setSize(resultCache.size());
                    if(result.getMax()==result.getSize())
                    {
                        InfinispanUtils.removeCache(readFrom);
                    }
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

        }
    }

    private void printCache(Cache<String, String> cache) {

        System.err.println("############");
        for (String key : cache.keySet()) {
            System.out.println(key + ": " + cache.get(key));
        }
        System.err.println("############");
    }
}

