package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vagvaz on 3/10/14.
 */
public class PutObjectHandler implements Handler<HttpServerRequest> {
    private EmbeddedCacheManager manager;
    private ObjectMapper mapper;

    public PutObjectHandler(EmbeddedCacheManager cacheManager) {
        manager = cacheManager;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(final HttpServerRequest httpServerRequest) {
        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-type","application/json");
        httpServerRequest.bodyHandler(new Handler<Buffer>() {

            @Override
            public void handle(Buffer body) {
                byte[] bt = body.getBytes();
//                String jsonObject = new String(bt);
                ActionResult result = new ActionResult();
                result.setResult("FAILED");
                try {
                    PutAction  putAction = mapper.readValue(bt,PutAction.class);
                    Cache<String,String> cache = manager.getCache(putAction.getTable());
                    System.err.println(putAction.toString());
                    if(cache == null)
                    {   System.err.println("cache not found");
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    }
                    if(putAction.getKey().equals(""))
                    {      System.err.println("key equals empty");
                        httpServerRequest.response().end(mapper.writeValueAsString(result));

                    }
                    try{
                        //unwrap
//                        String  obj = mapper.readValue(putAction.getObject(),String.class);
                        cache.put(putAction.getKey(),putAction.getObject());
//                        printCache(cache);
                        result.setResult("SUCCESS");
                    }catch(Exception e){
                        result.setMessage(e.getMessage());

                    }finally{
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    }

                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
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
}

