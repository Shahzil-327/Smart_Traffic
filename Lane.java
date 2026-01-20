package com.smarttraffic.model;

public class Lane {
    private final String name;
    private int carCount;
    private int waitingTime;

    public Lane(String name) {
        this.name = name;
        this.carCount = 0;
        this.waitingTime = 0;
    }

    public String getName() { return name; }
    public int getCarCount() { return carCount; }
    public void addCar() { carCount++; }
    public void removeCar() { if(carCount>0) carCount--; }

    public int getWaitingTime() { return waitingTime; }
    public void incrementWaitingTime() { waitingTime++; }
    public void resetWaitingTime() { waitingTime = 0; }
}
