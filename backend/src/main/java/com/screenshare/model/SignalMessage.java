package com.screenshare.model;

public class SignalMessage {
    private String type; // offer, answer, candidate, start, stop
    private String from;
    private String to;    // optional: "all" for broadcast
    private String data;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}