package eu.leads.processor.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.leads.processor.conf.WP3Configuration;
import eu.leads.processor.utils.InfinispanUtils;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by vagvaz on 3/11/14.
 */
public class BenchMark {
    static PutAction putob;
    static QueryStatus ob;
    static ObjectMapper mapper = new ObjectMapper();
    static QueryStatus query;
    static QueryResults results = null;
    static EmbeddedCacheManager manager;
    public static void main(String[] args) {


        WP3Configuration.initialize();
//        String host = WP3Configuration.getProperty("wshost");
//        String port = WP3Configuration.getProperty("wsport");
        String host = "http://localhost";
        int port = 8080;
        try {
            LeadsWebServiceLibrary.initialize(host,port);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        InfinispanUtils.start();
        manager = InfinispanUtils.getManager();
        System.out.println("sleeping for 10 sec");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            query = LeadsWebServiceLibrary.submitQuery("wgs","select url from webpages");
        } catch (IOException e) {
            e.printStackTrace();
        }
        QueryStatus current = new QueryStatus();
        while(!current.getStatus().equals("COMPLETED"))
            {try {
               System.out.println("sleeping for 1 sec");
                Thread.sleep(1000);
               current = LeadsWebServiceLibrary.getQueryStatus(query.getId());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            }
        try {
            results = LeadsWebServiceLibrary.getQueryResults(query.getId(),0,10000);
            System.out.println("results size " + results.getTuples().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[] depth ={2,3};
        for(int i = 0; i < 10;i++){
            for(int j = 0; j < depth.length;j++){
                long s = System.currentTimeMillis();
                String url = getUrl(i*1);
                checkSpecial(url,depth[j]);
                long e = System.currentTimeMillis();
                System.out.println("depth"+depth[j]+" special " + String.valueOf(e - s));
                s = System.currentTimeMillis();
                checkRec(url,depth[j]);
                e = System.currentTimeMillis();
                System.out.println("depth"+depth[j]+" rec " + String.valueOf(e - s));
            }
        }

    }

    private static void checkRec(String url, int depth) {
        int cur = 0;
        HashSet<String> next = new HashSet<>();
        HashSet<String> current = new HashSet<>();
        Map<String,HashMap<String,String>> results = new HashMap<>();
        current.add(url);
        List<String> attributes = new LinkedList<>();
        attributes.add("url");
        attributes.add("pagerank");
        attributes.add("sentiment");
        attributes.add("links");
        while(cur <= depth){
            Iterator<String> iter = current.iterator();
            while(iter.hasNext()){
                String u = iter.next();
                try {
                    HashMap<String,String> tmp = (HashMap<String, String>) LeadsWebServiceLibrary.getObject("webpages:", u, attributes);
                    if(tmp == null)
                        continue;
                    String ltmp = tmp.get("links");
                    if(cur < depth){
                        next.addAll(Arrays.asList(ltmp.split(",")));
                    }
                    results.put(u,tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            current = next;
            next = new HashSet<>();
            cur++;
        }
    }

    private static String getUrl(int index){
        String url  = "";
        String tmp = results.getTuples().get(index);
        JsonNode root = null;
        try {
            root = mapper.readTree(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        url = root.path("url").textValue();
        return url;
    }
    private static boolean checkSpecial(String url,int depth) {
  
        HashMap<String,String> config = new HashMap<>();
        config.put("url",url);
        config.put("depth",Integer.toString(depth));
        HashMap<String,String> result = null;
        try {
            result = (HashMap<String, String>) LeadsWebServiceLibrary.submitSpecialQuery("wgs", "rec_call", config);
            if(result.containsKey("queryId") && result.containsKey("output"))
            {
                LinkedList<String> a = new LinkedList<>();
                a.add("result");
                System.out.println("sleeping for 5 sec");
//                Thread.sleep(5000);
                Cache<String,String> cacheResult = (Cache<String, String>) InfinispanUtils.getOrCreatePersistentMap(result.get("output"));
                for(int i = 0; i <= depth;i++){

//                    while(cacheResult.size() < depth){

//                        System.out.println("INFO: " + cacheResult.size() + result.get("output"));
//                    }
                    Map<String,String> tmpRes = null;
                    while(tmpRes == null)
                     tmpRes =   LeadsWebServiceLibrary.getObject(result.get("output"),Integer.toString(i),a);
                     if(tmpRes != null)
                     {
//                         System.out.println("i: "+i + tmpRes.get("result") + "\n");
                     }
                     else
                         Thread.sleep(1000);

                }
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
