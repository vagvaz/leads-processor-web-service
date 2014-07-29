package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;

/**
 * Created by vagvaz on 3/10/14.
 */
public class GetQueryStatusHandler implements Handler<HttpServerRequest> {
    Cache<String, String> queriesCache;
    private ObjectMapper mapper;

    public GetQueryStatusHandler(EmbeddedCacheManager cacheManager) {
        mapper = new ObjectMapper();
        queriesCache = cacheManager.getCache("queries");
    }

    @Override
    public void handle(final HttpServerRequest httpServerRequest) {
        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-Type", "application/json");
        QueryStatus result = new QueryStatus();
        String queryId = httpServerRequest.params().get("id");
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
        } else {
            System.out.println("LEADS:Queries " + queriesCache.size());
            String queryJson = queriesCache.get(queryId);
            if (queryJson == null || queryJson.equals("")) {
                String message = "id " + queryId + "  not found";
                System.err.println(message);
                result.setMessage(message);
                try {
                    httpServerRequest.response().end(mapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = null;
                try {
                    root = mapper.readTree(queryJson);
//                    String status = mapper.readValue(root.path("queryState").textValue(),String.class);
                    String tmp = root.path("queryState").asText();
                    String status = mapper.readValue(tmp,String.class);
                    if(status == null)
                        status = "PENDING";
                    result.setStatus(status);
                    result.setMessage("Query Found");
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    String message = "io exception\n" + e.getMessage();
                    result.setMessage(message);
                    System.err.println(message);
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException ee) {
                        ee.printStackTrace();
                    }
                }
            }
        }
    }
}
