package eu.leads.processor.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by vagvaz on 3/30/14.
 */
@JsonAutoDetect
public class TestObject {
    private  String text;
    private  int rank;
    private float percent;
    private double cost;
    private HashMap<String,String> dictionary;
    private LinkedList<Integer> queue;
    private User user;

    public TestObject(){
        text = "default";
        rank = 1;
        percent = 0.1f;
        cost = 10.5f;
        dictionary = new HashMap<>();
        dictionary.put("defaultKey","defaultValue");
        queue = new LinkedList<>();
        queue.add(1);
        queue.add(2);
        user = new User();
    }

    public TestObject(String t, int r, float p , double c , HashMap<String,String> d, LinkedList<Integer> q, User u){
        text = t;
        rank = r;
        percent = p;
        cost = c;
        dictionary = d;
        queue = q;
        user = u;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public HashMap<String, String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(HashMap<String, String> dictionary) {
        this.dictionary = dictionary;
    }

    public LinkedList<Integer> getQueue() {
        return queue;
    }

    public void setQueue(LinkedList<Integer> queue) {
        this.queue = queue;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "text='" + text + '\'' +
                ", rank=" + rank +
                ", percent=" + percent +
                ", cost=" + cost +
                ", dictionary=" + dictionary +
                ", queue=" + queue +
                ", user=" + user +
                '}';
    }
}
