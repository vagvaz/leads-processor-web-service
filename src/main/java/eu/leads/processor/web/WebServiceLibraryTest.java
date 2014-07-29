package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Created by vagvaz on 3/10/14.
 */
public class WebServiceLibraryTest {
    static PutAction putob;
    static QueryStatus ob;
    static ObjectMapper mapper = new ObjectMapper();
    static QueryStatus query;
    static QueryResults results = null;
    public static void main(String[] args) throws MalformedURLException {
        String host = "http://80.156.223.197";
        int port = 8080;
        if(args.length == 2)
        {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        if(LeadsWebServiceLibrary.initialize(host,port)){
            report("put object ", checkPut());
            report("get object", checkGet());
            report("submitQuery", checkSubmit());
            report("check query status", checkQueryStatus());
            report("getResults",checkGetResults());
            report("specialQuery",checkSpecial());
        }
    }

    private static boolean checkSpecial() {

        String url  = "";
        String tmp = results.getTuples().get(0);
        JsonNode root = null;
        try {
            root = mapper.readTree(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        url = "https://www.yahoo.com/tech";//root.path("url").textValue();
        int depth = 3;
        HashMap<String,String> config = new HashMap<>();
        config.put("url",url);
        config.put("depth",Integer.toString(depth));
        HashMap<String,String> result = null;
        try {
             result = (HashMap<String, String>) LeadsWebServiceLibrary.submitSpecialQuery("wgs", "rec_call", config);
            if(result.containsKey("id") && result.containsKey("output"))
            {
              LinkedList<String> a = new LinkedList<>();
                a.add("result");
              System.out.println("sleeping for 5 sec");
              Thread.sleep(5000);
              for(int i = 0; i <= depth;i++){
                  Map<String,String> tmpRes =   LeadsWebServiceLibrary.getObject(result.get("output"),Integer.toString(i),a);
                  System.out.println("i: " + tmpRes.get("result") + "\n");
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

    private static boolean checkGetResults() {
        try {
            results = LeadsWebServiceLibrary.getQueryResults(query.getId(),0L,1000L);
            System.out.println(results.toString());
            return results.getTuples().size() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean checkQueryStatus() {
        QueryStatus status = null;
        int retries = 0;
        try {
            while(status == null || !status.getStatus().equals("COMPLETED"))
            {
                status = LeadsWebServiceLibrary.getQueryStatus(query.getId());
                if(status == null)
                    continue;
                if(status.getStatus().equals("COMPLETED"))
                    return true;
                else
                {
                    Thread.sleep(1000);
                    retries++;
                    if(retries > 10)
                        return false;

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkSubmit() {
        try {
            query = LeadsWebServiceLibrary.submitQuery("wgs","select url,links from webpages");
//            query = LeadsWebServiceLibrary.submitQuery("wgs","select url,links from webpages");
            return query.getStatus().equals("PENDING") && !query.getId().equals("");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private static boolean checkGet() {
        boolean result = false;
        List<String> attributes = new LinkedList<>();
//        attributes.add("id");
//        attributes.add("status");
//        attributes.add("message");
        Map<String, String> response = null;
        try {
            response = LeadsWebServiceLibrary.getObject(putob.getTable(), putob.getKey(), attributes);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("received: " + response);
        if(response != null)
        {
            QueryStatus r = new QueryStatus();
            r.setId(response.get("id"));
            r.setMessage(response.get("message"));
            r.setStatus(response.get("status"));
            return r.toString().equals(ob.toString());
        }
        else
            return false;
    }

    private static boolean checkPut() {
        boolean result = false;
        putob = new PutAction();
        ob =new QueryStatus();
        ob.setId("100");
        ob.setStatus("TESTING");
        ob.setMessage("GREATE MESSAGE");
        try {
            putob.setObject(mapper.writeValueAsString(ob));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        putob.setKey("mykey");
        putob.setTable("testTable");
        try {
          return  LeadsWebServiceLibrary.putObject(putob.getTable(), putob.getKey(),putob.getObject());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    private static void report(String func, boolean result){
        System.err.println(func + " gave " + result);
    }


}
