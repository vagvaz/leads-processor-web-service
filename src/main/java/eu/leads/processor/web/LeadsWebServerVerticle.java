package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.leads.processor.conf.WP3Configuration;
import eu.leads.processor.pagerank.graph.LeadsPrGraph;
import eu.leads.processor.utils.InfinispanUtils;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

import java.io.IOException;

/**
 * Created by vagvaz on 3/7/14.
 */
public class LeadsWebServerVerticle extends Verticle {
    private EmbeddedCacheManager cacheManager = null;
    private LeadsWebModule module;
    public void start(){
        System.out.println("FooBar");
        RouteMatcher matcher = new RouteMatcher();
        String configFile = container.config().getValue("infinispanConfig");
        startEmbeddedCacheManager(configFile);
        startLeadsWebModule("propertiesFile");
        GetObjectHandler getObjectHandler = new GetObjectHandler(cacheManager);
        PutObjectHandler putObjectHandler = new PutObjectHandler(cacheManager);
        GetQueryStatusHandler getQueryStatusHandler = new GetQueryStatusHandler(cacheManager);
        GetResultsHandler getResultsHandler = new GetResultsHandler(cacheManager);
        SubmitQueryHandler submitQueryHandler = new SubmitQueryHandler(module);
        SubmitSpecialCallHandler submitSpecialCallHandler = new SubmitSpecialCallHandler(module);
        //object
        matcher.post("/rest/object/get/",getObjectHandler);
        matcher.post("/rest/object/put/",putObjectHandler);

        //query   [a-zA-Z0-9]+\-[a-zA-Z0-9]+\-[a-zA-Z0-9]+\-[a-zA-Z0-9]+\-[a-zA-Z0-9]+
        matcher.get("/rest/query/status/:id",getQueryStatusHandler);
        //id:[a-zA-Z0-9]+@[a-zA-Z0-9]+}/{min:[0-9]+}/{max:[0-9]+}
        matcher.get("/rest/query/results/:id/min/:min/max/:max",getResultsHandler);
        matcher.post("/rest/query/submit",submitQueryHandler);
        matcher.post("/rest/query/wgs/:type",submitSpecialCallHandler);

        matcher.get("/rest/checkOnline", new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                httpServerRequest.response().end("Yes I am online");
            }
        });

        try {
            LeadsPrGraph.init();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        QueryHandler handler = new QueryHandler();
        vertx.createHttpServer().requestHandler(matcher).listen(8080);
        container.logger().info("Webserver started");
    }

    private void startLeadsWebModule(String propertiesFile) {
        WP3Configuration.initialize();
        String url = WP3Configuration.getProperty("activemq_url");
        Integer port = WP3Configuration.getInt("activemq_port");
        try {
            module = new LeadsWebModule(url+":"+port.toString(),"WS:"+WP3Configuration.getNodeName());
            module.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startEmbeddedCacheManager(String configFile) {
        System.err.println("Starting cach manager from " + configFile);
        InfinispanUtils.start();
        cacheManager = InfinispanUtils.getManager();
//        cacheManager = null;
//        try {
//            cacheManager = new DefaultCacheManager(configFile);
//            cacheManager.start();
//
//        } catch (IOException e) {
//            cacheManager = null;
//            System.err.println("Could not start infinispan EmbeddedCacheManager");
//
//        }
    }
}
