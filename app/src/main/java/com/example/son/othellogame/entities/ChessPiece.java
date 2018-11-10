package com.example.son.othellogame.entities;


public class ChessPiece {

    private int x, y; // location
    private String color;
    private String title; // TODO: replace this with image later
    private int position;

    public enum PieceColor {
        BLACK("black"),
        WHITE("white");

        private String xColor;

        PieceColor(String xColor) {
            this.xColor = xColor;
        }

        public String getValue() {
            return xColor;
        }
    }

    public ChessPiece(String title, int x, int y, int position) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.position = position;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
