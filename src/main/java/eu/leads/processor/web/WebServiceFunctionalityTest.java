package eu.leads.processor.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by vagvaz on 3/30/14.
 */
public class WebServiceFunctionalityTest {
    public static void main(String[] args) throws IOException {
        String host = "http://80.156.223.197";
        int port = 8080;
        if(args.length == 2)
        {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        try {
            if(!LeadsWebServiceLibrary.initialize(host,port)){
                System.out.println("Error exit");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HashMap<String,String> d1 = new HashMap<>();
        HashMap<String,String> d2 = new HashMap<>();
        d1.put("1","1");
        d1.put("2","2");
        d2.put("11","11");
        d2.put("22","22");
        LinkedList<String> a1 = new LinkedList<String>();
        LinkedList<String> a2 = new LinkedList<String>();
        a1.add("a");
        a1.add("b");
        a2.add("1a");
        a2.add("2a");
        User u1 = new User("name",a1);
        User u2 = new User("name",a2);
        LinkedList<Integer> q1 = new LinkedList<>();
        q1.add(1);
        q1.add(2);
        LinkedList<Integer> q2 = new LinkedList<>();
        q2.add(11);
        q2.add(22);

        TestObject t1 = new TestObject();
        Map<String, String> t2 = new HashMap();
        t1.setCost(100.0f);
        t1.setDictionary(d1);
        t1.setPercent(0.5f);
        t1.setQueue(q1);
        t1.setRank(1);
        t1.setUser(u1);
        System.out.println("t1: " + t1.toString());
        try {
            LeadsWebServiceLibrary.putObject("testTable","t1",t1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedList<String> attrs;
        attrs = new LinkedList<>();
        attrs.add("rank");
        attrs.add("queue");
        attrs.add("user");
        attrs.add("percent");
        LeadsWebServiceLibrary.putObject("testTable","u1",u1);
        Map<String,String> asd = LeadsWebServiceLibrary.getObject("testTable","u1",new LinkedList<String>());
        for(Map.Entry<String,String> e : asd.entrySet()){
            System.out.println("k: " + e.getKey() + "\n" + e.getValue());
        }
        try {
            t2 = LeadsWebServiceLibrary.getObject("testTable", "t1", attrs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry<String,String> e : t2.entrySet()){
            System.out.println("k: " + e.getKey() + "\n" + e.getValue());
        }
        ObjectMapper mapper = new ObjectMapper();
        u2 = mapper.readValue(t2.get("user"),User.class);
        System.out.println("final " + u2);
    }
}
