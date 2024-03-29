package eu.leads.processor.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by vagvaz on 3/7/14.
 */
@JsonAutoDetect
public class ActionResult {
    String result;
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ActionResult(String result){
        this.result = result;
    }
    public ActionResult(){
        result = "FAIL";
    }
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
