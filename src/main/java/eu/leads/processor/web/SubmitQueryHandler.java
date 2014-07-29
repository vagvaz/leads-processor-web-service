package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infinispan.Cache;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;

/**
 * Created by vagvaz on 3/10/14.
 */
public class SubmitQueryHandler implements Handler<HttpServerRequest> {
    private LeadsWebModule module;
    private ObjectMapper mapper;
    public SubmitQueryHandler(LeadsWebModule module) {
        this.module = module;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(final HttpServerRequest httpServerRequest) {
        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-type", "application/json");
        System.out.println("submit handler");
        httpServerRequest.bodyHandler(new Handler<Buffer>() {
            QueryStatus result = new QueryStatus();
            @Override
            public void handle(Buffer body) {
                byte[] bt = body.getBytes();
//                String jsonObject = new String(bt);

                WebServiceQuery query = null;
                try {
                     query = mapper.readValue(bt, WebServiceQuery.class);
                    String queryId = module.submitSQLQuery(query.getSql(),query.getUser());
                    result.setId(queryId);
                    result.setStatus("PENDING");
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }

                } catch (IOException e) {
                    try {
                        httpServerRequest.response().end(mapper.writeValueAsString(result));
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }

            }
        });
        }
}

