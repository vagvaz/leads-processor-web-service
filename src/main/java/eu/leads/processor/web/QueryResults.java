package eu.leads.processor.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

/**
 * Created by vagvaz on 3/7/14.
 */
@JsonAutoDetect
public class QueryResults {
    private String id;
    private long min;
    private long max;
    private long size;
    private List<String> tuples;
    private String message;



    public QueryResults(){
        id = "";
        min = -1;
        max = -1;
        tuples = null;
    }
    public QueryResults(String queryId) {
        id = queryId;
        min = -1;
        max = -1;
        tuples = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public List<String> getTuples() {
        return tuples;
    }

    public void setTuples(List<String> tuples) {
        this.tuples = tuples;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    @Override
    public String toString(){
        String result = id+"["+Long.toString(min)+":"+Long.toString(max)+"]\n"+message+"\n";
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for(String tuple : tuples){
            builder.append(Integer.toString(counter++)+ tuple+"\n");
        }
        result += builder.toString();
        return result;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
