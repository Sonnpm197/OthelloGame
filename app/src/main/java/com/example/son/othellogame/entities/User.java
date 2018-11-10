package com.example.son.othellogame.entities;

public class User {

    private String id, userName, status;
    public enum Status{
        ONLINE("online"),
        OFFLINE("offline");

        private String value;
        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public User(String id, String userName, String status) {
        this.id = id;
        this.userName = userName;
        this.status = status;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
