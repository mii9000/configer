package com.mii9000.configer;

public class EventMessageBody {
    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    String Type;

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        this.Subject = subject;
    }

    String Subject;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }

    String Message;
}
