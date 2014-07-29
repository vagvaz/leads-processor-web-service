package eu.leads.processor.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;

/**
 * Created by vagvaz on 3/7/14.
 */
@Path("rest/")
public class LeadsWebServiceResource {


    private EmbeddedCacheManager manager;
    private Cache<String,String> queriesCache;
    public LeadsWebServiceResource(@Context EmbeddedCacheManager manager){
        this.manager = manager;
        if(this.manager != null)
        queriesCache = this.manager.getCache("queries");
    }
    @GET
    @Path("/object/get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String,String> getObject(ObjectQuery query){
        System.err.println("LEADS:getObject " + query.toString());
        Cache<String, String> cache = manager.getCache(query.getTable());
        HashMap<String, String> result = new HashMap<String, String>();
        if (cache == null) {
            return result;
        } else {
            String tupleString = cache.get(query.getKey());
            if (tupleString == null || tupleString.equals("")) {
                return result;
            }
            JsonNode root = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                root = mapper.readTree(tupleString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator<String> iterator = root.fieldNames();
            List<String> attrs = query.getAttributes();
            while (iterator.hasNext()) {
                String field = iterator.next();
                if (attrs.contains(field)) {
                    result.put(field, root.path(field).asText());
                }

            }
            return result;
        }
    }

    @POST
    @Path("/object/put")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionResult putObject(PutAction putAction){
        System.out.println("LEADS:putObject");
        ActionResult result = new ActionResult();

        Cache<String,String> cache = manager.getCache(putAction.getTable());
        if(cache == null)
            return result;
        if(putAction.getKey().equals(""))
            return result;
        try{
           cache.put(putAction.getKey(),putAction.getObject());
           result.setResult("SUCCESS");
        }catch(Exception e){
          result.setMessage(e.getMessage());

        }finally {
            return result;
        }

    }

    @GET
    @Path("/query/status/{id:[a-zA-Z0-9]+\\-[a-zA-Z0-9]+\\-[a-zA-Z0-9]+\\-[a-zA-Z0-9]+\\-[a-zA-Z0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public QueryStatus getQueryStatus(@PathParam("id") String queryId){
        System.err.println("LEADS:getQueryStatus");
        QueryStatus result = new QueryStatus();
        result.setId(queryId);
//        manager = (EmbeddedCacheManager) ctx.getAttribute("manager");
        System.out.println("After new " + manager.getMembers().size());


        if (queriesCache == null) {
            String message =  "queries cache has not been initialized check infinispan configuration";
            result.setMessage(message);
            System.err.println(message);
            return result;
        }
        System.out.println("LEADS:Queries " + queriesCache.size());
        String queryJson = queriesCache.get(queryId);
        if (queryJson == null || queryJson.equals("")) {
            String message = "id " + queryId + "  not found";
            System.err.println(message);
            result.setMessage(message);
            return result;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(queryJson);
            String status = root.path("queryState").asText();
            result.setStatus(status);
            result.setMessage("Query Found");
        } catch (IOException e) {
            String message = "io exception\n" + e.getMessage();
            result.setMessage(message);
            System.err.println(message);
            return result;
        }finally{
            return result;
        }
    }


    @GET
    @Path("/query/results/{id:[a-zA-Z0-9]+@[a-zA-Z0-9]+}/{min:[0-9]+}/{max:[0-9]+}")
    @Produces("application/json")
    public QueryResults getQueryResults(@PathParam("id") String queryId, @PathParam("min") long min, @PathParam("max") long max) {
        System.err.println("LEADS:Results " + (manager == null));
//        populateSampleData();
        QueryResults result = new QueryResults(queryId);
        if (min > max)
            return result;

        if (queriesCache == null) {
            String message =  "queries cache has not been initialized check infinispan configuration";
            result.setMessage(message);
            System.err.println(message);
            return result;
        }
        String queryJson = queriesCache.get(queryId);
        if (queryId == null)
            return result;

        String readFrom = "";
        System.err.println("READFROM " + min + " " + max);
        readFrom = "testData";
        Cache<String, String> resultCache = manager.getCache(readFrom);
        if (resultCache == null)
            return result;
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
        return result;
    }
}
