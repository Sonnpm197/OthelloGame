package com.example.son.othellogame.entities;

public class Player {

    public enum Status {
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

    private String playerColor;

    public Player() {
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }
}
