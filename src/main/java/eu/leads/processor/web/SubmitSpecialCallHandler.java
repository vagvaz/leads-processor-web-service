package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by vagvaz on 3/10/14.
 */
public class SubmitSpecialCallHandler implements Handler<HttpServerRequest> {
    private LeadsWebModule module;
    private ObjectMapper mapper;
    private String type;
    public SubmitSpecialCallHandler(LeadsWebModule module) {
        this.module = module;
        mapper = new ObjectMapper();
        type = "";

    }

    @Override
    public void handle(final HttpServerRequest httpServerRequest) {
        httpServerRequest.response().setStatusCode(200);
        httpServerRequest.response().putHeader("Content-type", "application/json");
        System.out.println("submit special handler");
        type = httpServerRequest.params().get("type");
        httpServerRequest.bodyHandler(new Handler<Buffer>() {
            HashMap<String,String> result = new HashMap<>();
            @Override
            public void handle(Buffer body) {
                byte[] bt = body.getBytes();
//                String jsonObject = new String(bt);
                if(type.equals("rec_call")){

                    try {
                        RecursiveCallQuery query = mapper.readValue(bt, RecursiveCallQuery.class);
                        HashMap<String,String> parameters = new HashMap<>();
                        parameters.put("depth",query.getDepth());
                        parameters.put("url",query.getUrl());
                        result = module.sumbitQuery(query.getUser(),type,parameters);

                        try {
                            httpServerRequest.response().end(mapper.writeValueAsString(result));
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                        }
                        type ="";


                    } catch (IOException e) {
                        try {
                            httpServerRequest.response().end(mapper.writeValueAsString(result));
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("unknown web graph service query " + type);
                }


            }
        });
    }
}
