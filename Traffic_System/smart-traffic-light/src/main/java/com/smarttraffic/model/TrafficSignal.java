package com.smarttraffic.model;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TrafficSignal extends Thread {

    private final Lane lane;
    private final Circle light;
    private int greenTime;

    public TrafficSignal(Lane lane, Circle light) {
        this.lane = lane;
        this.light = light;
    }

    @Override
    public void run() {
        try {
            // Green duration depends on car count
            greenTime = Math.min(Math.max(lane.getCarCount() * 2, 2), 8);
            if(lane.getWaitingTime() >= 15) greenTime = 5; // starvation override

            // GREEN
            Platform.runLater(() -> light.setFill(Color.GREEN));
            Thread.sleep(greenTime * 1000);

            // YELLOW
            Platform.runLater(() -> light.setFill(Color.YELLOW));
            Thread.sleep(2000);

            // RED
            Platform.runLater(() -> light.setFill(Color.RED));
            lane.resetWaitingTime();
        } catch (InterruptedException e) { e.printStackTrace(); }
    }
}
