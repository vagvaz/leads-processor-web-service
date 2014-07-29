package eu.leads.processor.web;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.ws.rs.Produces;
import java.io.IOException;

/**
 * Created by vagvaz on 3/7/14.
 */
public class AppBinder extends AbstractBinder {

    @Produces
    EmbeddedCacheManager getCacheManager(){
        EmbeddedCacheManager manager = null;
        try {
            manager = new DefaultCacheManager("/home/vagvaz/infinispan-clustered-tcp.xml");
            manager.start();
        } catch (IOException e) {
            manager = null;
            System.err.println("Could not start infinispan EmbeddedCacheManager");

        }
        finally {
            return manager;
        }
    }

    @Override
    protected void configure() {

    }
}
