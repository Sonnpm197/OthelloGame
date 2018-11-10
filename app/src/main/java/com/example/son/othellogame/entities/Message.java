package com.example.son.othellogame.entities;

public class Message {

    public enum Type {
        INVITE("invite"), // Invite another player
        QUIT("quit"), // 1 user randomly quits
        DENY("deny"),// received invitation but opponent doesn't accept
        ACCEPT("accept"),// opponent accepted
        LOST_TURN("lost turn"),
        GAME_OVER_BY_NO_MOVES_BOTH_PLAYERS("game over by no more moves for both players"),
        GAME_OVER_BY_FULL_BOARD("game over by full board"); // game over by full board

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String senderName;
    private String senderId;
    private String receiverId;
    private String messageType;
    private Integer matchNumber;

    public Message(String senderName, String senderId, String receiverId, String messageType, Integer matchNumber) {
        this.senderName = senderName;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.matchNumber = matchNumber;
    }

    public Message() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderName='" + senderName + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", matchNumber=" + matchNumber +
                '}';
    }
}
