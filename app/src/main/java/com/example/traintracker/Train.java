package com.example.traintracker;

import java.io.Serializable;

public class Train implements Serializable {

    private int number;
    private String type;
    private String start;
    private String destination;
    private String startTime;
    private String arrivalTime;

    public Train(int number, String type,   String start, String destination, String startTime, String arrivalTime){
        this.number = number;
        this.type = type;
        this.start = start;
        this.destination = destination;
        this.startTime = startTime;
        this.arrivalTime = arrivalTime;
    }

    public int getNumber(){ return number; }
    public String getType(){ return type; }
    public String getStart(){ return start; }
    public String getDestination(){ return destination; }
    public String getStartTime(){ return startTime; }
    public String getArrivalTime(){ return arrivalTime; }
    public String getNameFormated(){ return type + " " + number + ", " + start + " " + startTime  + " - " + destination + " " + arrivalTime;}

}
