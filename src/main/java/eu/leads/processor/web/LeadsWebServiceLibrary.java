package eu.leads.processor.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vagvaz on 3/10/14.
 */
public class LeadsWebServiceLibrary {
    private static String host;
    private static String port;
    private static URL address;
    private final static String prefix = "/rest/";
    private final static  ObjectMapper mapper = new ObjectMapper();

    public static boolean initialize(String url,int p) throws MalformedURLException {
        host= url;
        port = String.valueOf(p);
        address = new URL(host+":"+port);
        return checkIfOnline();
     }

    private static  boolean checkIfOnline() {
        HttpURLConnection connection = null;
        try {
            address = new URL(host+":"+port+prefix+"checkOnline");
            connection = (HttpURLConnection) address.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            InputStream is  = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuffer response = new StringBuffer();
            String line;
            while((line = rd.readLine())!=null){
                response.append(line);
            }
            rd.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally{
            if(connection!= null)
                connection.disconnect();
        }
    }
    private static HttpURLConnection setUp(HttpURLConnection connection, String type, String contentType, boolean hasInput, boolean hasOutput) throws ProtocolException {
        connection.setRequestMethod(type);
        connection.setRequestProperty("Content-Type",contentType);
        connection.setUseCaches(false);
        connection.setDoInput(hasInput);
        connection.setDoOutput(hasOutput);
        return connection;
    }

    private static String getResult(HttpURLConnection connection) throws IOException {
        InputStream is  = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuffer response = new StringBuffer();
        String line;
        while((line = rd.readLine())!=null){
            response.append(line);
        }
        rd.close();
        response.trimToSize();
        System.out.println("received: " + response);
        return response.toString();
    }

    private static void setBody(HttpURLConnection connection,Object body) throws IOException {
        String output = mapper.writeValueAsString(body);
        System.out.println("sending... " + output);
        DataOutputStream os = new DataOutputStream(connection.getOutputStream());
        os.writeBytes(output);
        os.flush();
        os.close();
    }

    public static Map<String,String> getObject(String table, String key, List<String> attributes) throws IOException {
        ObjectQuery ob = new ObjectQuery();
        ob.setAttributes(attributes);
        ob.setKey(key);
        ob.setTable(table);
        String atr = "" ;
        ob.setAttributes(attributes);
        address = new URL(host+":"+port+prefix+"object/get/");
        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection = setUp(connection,"POST", MediaType.APPLICATION_JSON,true,true);
        setBody(connection,ob);
        String response = getResult(connection);
//        System.out.println("getResponse " + response);
        if(response.length() < 5)
            return null;
        HashMap<String,String> res = (HashMap<String, String>) mapper.readValue(response, HashMap.class);
        HashMap<String,String> result = new HashMap<>();
//        for(Map.Entry<String,String> r : res.entrySet()){
////            if(!r.getValue().startsWith("["))
////               result.put(r.getKey(),mapper.readValue(r.getValue(),String.class));
////            else
//               result.put(r.getKey(),r.getValue());
//        }
        return res;
        }

    public static  boolean putObject(String table,String key,Object object) throws IOException {
        boolean result = false;
        PutAction action = new PutAction();
        action.setTable(table);
        action.setKey(key);
        action.setObject(mapper.writeValueAsString(object));
        address = new URL(host+":"+port+prefix+"object/put/");
        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection = setUp(connection,"POST", MediaType.APPLICATION_JSON,true,true);
//        setBody(connection,mapper.writeValueAsString(action));
        setBody(connection,action);
        String response = getResult(connection);
        ActionResult aresult =  mapper.readValue(response, ActionResult.class);
        result = aresult.getResult().equals("SUCCESS");
        return result;
    }

    public static  QueryStatus getQueryStatus(String id) throws IOException {
        QueryStatus result = new QueryStatus();
        address = new URL(host+":"+port+prefix+"query/status/"+id);
        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection = setUp(connection,"GET", MediaType.APPLICATION_JSON,true,true);
        String response = getResult(connection);
        System.err.println("responsed " + response);
        result =  mapper.readValue(response, QueryStatus.class);
        return result;
    }

    public static  QueryResults getQueryResults(String id , long min, long max) throws IOException {
        QueryResults result = new QueryResults();
        address = new URL(host+":"+port+prefix+"query/results/"+id+"/min/"+String.valueOf(min)+"/max/"+String.valueOf(max));
        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection = setUp(connection,"GET", MediaType.APPLICATION_JSON,true,true);
        String response = getResult(connection);
        result =  mapper.readValue(response, QueryResults.class);
        return result;
    }

    public static  QueryStatus submitQuery(String username, String SQL) throws IOException {
        QueryStatus result = null;
        WebServiceQuery query = new WebServiceQuery();
        query.setSql(SQL);
        query.setUser(username);
        address = new URL(host+":"+port+prefix+"query/submit");
        HttpURLConnection connection = (HttpURLConnection) address.openConnection();
        connection = setUp(connection,"POST", MediaType.APPLICATION_JSON,true,true);
        setBody(connection,query);
        String response = getResult(connection);
        result =  mapper.readValue(response, QueryStatus.class);
        return result;
    }

    public static  Map<String,String> submitSpecialQuery(String username, String type,Map<String,String> parameters) throws IOException {
        Map<String,String> result = new HashMap<String,String>();
        if(type.equals("rec_call")){
            RecursiveCallQuery query = new RecursiveCallQuery();
            query.setUser(username);
            query.setDepth(parameters.get("depth"));
            query.setUrl(parameters.get("url"));
            address = new URL(host+":"+port+prefix+"query/wgs/rec_call");
            HttpURLConnection connection = (HttpURLConnection) address.openConnection();
            connection = setUp(connection,"POST", MediaType.APPLICATION_JSON,true,true);
            setBody(connection,query);
            String response = getResult(connection);
            result =  mapper.readValue(response, Map.class);
            return result;
        }
        return result;
    }
}
