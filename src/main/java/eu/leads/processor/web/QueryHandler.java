package eu.leads.processor.web;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;

/**
 * Created by vagvaz on 3/9/14.
 */
public class QueryHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest httpServerRequest) {

    }
//    EmbeddedCacheManager cacheManager;
//
//    public QueryHandler(){
//        cacheManager = null;
//        try {
//                cacheManager = new DefaultCacheManager("/home/vagvaz/infinispan-clustered-tcp.xml");
//                cacheManager.start();
//            } catch (IOException e) {
//                cacheManager = null;
//                System.err.println("Could not start infinispan EmbeddedCacheManager");
//
//            }
//    }
//
//    @Override
//    public void handle(HttpServerRequest httpServerRequest) {
//        Cache<String,String> queriesCache = cacheManager.getCache("queries");
//        Cache<String,String> webCache = cacheManager.getCache("webpages:");
//
//        httpServerRequest.response().setStatusCode(200);
//        httpServerRequest.response().end("queries + " + queriesCache.size() +"\nwebpages: " + webCache.size());
//    }
}
