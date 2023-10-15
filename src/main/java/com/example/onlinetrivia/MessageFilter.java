package com.example.onlinetrivia;


public class MessageFilter {
    private String sender;
    private String recipient;

    // Constructor
    public MessageFilter(String sender, String recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
