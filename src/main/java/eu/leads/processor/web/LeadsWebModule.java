package eu.leads.processor.web;

import com.google.common.base.Strings;
import eu.leads.processor.Module;
import eu.leads.processor.conf.WP3Configuration;
import eu.leads.processor.utils.StringConstants;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by vagvaz on 3/10/14.
 */
public class LeadsWebModule extends Module {

    private LinkedList<Message> incoming;
    private final Object mutex = new Object();
    private String ackQueryId = "";
    private String ackQueryText = "";
    private CCJSqlParserManager validator;
    private String location;
    private final String queue = StringConstants.UIMANAGERQUEUE;
    private Destination destination;
    public LeadsWebModule(String url, String name) throws Exception {
        super(url, name + WP3Configuration.getNodeName());
        location = WP3Configuration.getHostname();
        com.createQueuePublisher(queue);
        validator = new CCJSqlParserManager();
        com.setTopicMessageListener(this);
        com.setQueueMessageListener(this);
        incoming = new LinkedList<Message>();
        destination = null;
    }

    @Override
    public void onMessage(Message message) {
        synchronized (mutex){
            if(message != null){
                incoming.add(message);
                mutex.notify();
            }
        }
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            LinkedList<Message> toprocess = null;
            synchronized (mutex) {
                if (incoming.size() > 0) {
                    toprocess = incoming;
                    incoming = new LinkedList<Message>();
                } else {
                    mutex.wait();
                }

            if(toprocess != null){
                while(toprocess.size() > 0){
                    Message message = toprocess.poll();
                    if (message.getStringProperty("type").equals("ack")) {
                        TextMessage msg = (TextMessage)message;
                        ackQueryId = msg.getText();
                        ackQueryText = msg.getStringProperty("sqlText");
                        mutex.notify();
                    }
                }
            }
            }
        }
    }
    public String submitSQLQuery(String sqlText,String username){
        if (this.validateSQL(sqlText)) {
//                console.show("your query was\n" + query);
            submitQuery(sqlText,username);
            waitForAck();
            String result = ackQueryId;
            ackQueryText = "";
            ackQueryId = "";
            return  result;
        } else {
            return "";
        }
    }

    public HashMap<String,String> sumbitQuery(String username, String type, HashMap<String,String> parameters){
        HashMap<String,String>  result = new HashMap<>();
        submitSpecialQuery(username,type,parameters);
        waitForAck();
        result.put("id",ackQueryId);
        result.put("status","PENDING");
        result.put("output",ackQueryId+":result:");
        ackQueryId = "";
        ackQueryText = "";
        return result;
    }

    private void submitSpecialQuery(String username, String type, HashMap<String, String> parameters) {
        System.out.println("Submiting special query from " + username + " "  + type + parameters.size());
        TextMessage message = new ActiveMQTextMessage();
        try{
            message.setStringProperty("user", username);
            message.setStringProperty("location", location);
            message.setStringProperty("type","specialQueryMessage");
            message.setStringProperty("special_type",type);
            message.setText(type);
            destination = com.getSession().createQueue(username + "@" + location);
            com.subscribeToQueue(destination.toString());
            com.setQueueMessageListener(this, destination.toString());
            message.setJMSReplyTo(destination);
            message.setJMSRedelivered(false);
            message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //put_parameters
            for(Map.Entry<String,String> entry : parameters.entrySet()){
                message.setStringProperty(entry.getKey(),entry.getValue());
            }
            com.publishToQueue(message,queue);
        }catch(JMSException e){
            e.printStackTrace();
        }
    }

    private void submitQuery(String query, String username) {
        TextMessage message = new ActiveMQTextMessage();
        try {
            message.setText(query);
            message.setStringProperty("user", username);
            message.setStringProperty("location", location);
            message.setStringProperty("type", "SQLQueryMessage");
            destination = com.getSession().createQueue(username + "@" + location);
            com.subscribeToQueue(destination.toString());
            com.setQueueMessageListener(this, destination.toString());
            message.setJMSReplyTo(destination);
            message.setJMSRedelivered(false);
            message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        this.com.publishToQueue(message, queue);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private boolean validateSQL(String sqlText) {
        try {
            StringReader reader = new StringReader(sqlText);
            validator.parse(reader);
            return true;
        } catch (JSQLParserException e) {
            return false;
        }
    }

    private void waitForAck() {
        synchronized (mutex) {
            if (Strings.isNullOrEmpty(ackQueryId) && Strings.isNullOrEmpty(ackQueryText)) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void triggerShutdown() {
        super.triggerShutdown();
    }

}
