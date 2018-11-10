package com.example.son.othellogame.entities;

public class PlayingPiece {

    private String senderId;
    private String receiverId;
    private int location;
    private String pieceColor;

    public PlayingPiece(String senderId, String receiverId, int location, String pieceColor) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.location = location;
        this.pieceColor = pieceColor;
    }

    public PlayingPiece() {
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

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public String getPieceColor() {
        return pieceColor;
    }

    public void setPieceColor(String pieceColor) {
        this.pieceColor = pieceColor;
    }

    @Override
    public String toString() {
        return " location: " + location + "; color: " + pieceColor;
    }
}
