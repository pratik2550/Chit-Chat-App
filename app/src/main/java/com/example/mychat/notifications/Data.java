package com.example.mychat.notifications;

public class Data {

    private String users, body, title, sent;
    private Integer icon;

    public Data () {
    }

    public Data(String users, String body, String title, String sent, Integer icon) {
        this.users = users;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.icon = icon;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
