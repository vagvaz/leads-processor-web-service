package eu.leads.processor.web;


import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedList;

/**
 * Created by vagvaz on 3/30/14.
 */
@JsonAutoDetect
public class User {
    private String name;
    private LinkedList<String> address;

    public User(){
        name ="default";
        address = new LinkedList<>();
        address.add("nowhere");
    }
    public User(String name, LinkedList<String> address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", address=" + address +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<String> getAddress() {
        return address;
    }

    public void setAddress(LinkedList<String> address) {
        this.address = address;
    }
}
