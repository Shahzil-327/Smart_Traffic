package com.smarttraffic.controller;

import java.util.Map;

import com.smarttraffic.model.Lane;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class SignalController {

    private static final int MIN_GREEN = 2;
    private static final int MAX_GREEN = 8;
    private static final int STARVATION_TIME = 15; // seconds

    public void startAutomaticCycleSingleLane(Map<String, Lane> lanes,
                                              Map<String, Circle> lights) {

        new Thread(() -> {
            while (true) {
                try {
                    // Increment waiting time for all lanes
                    for (Lane lane : lanes.values()) lane.incrementWaitingTime();

                    // Select lane with most cars or starvation
                    Lane selected = lanes.values().stream()
                            .max((l1, l2) -> {
                                if (l1.getWaitingTime() >= STARVATION_TIME) return 1;
                                if (l2.getWaitingTime() >= STARVATION_TIME) return -1;
                                return Integer.compare(l1.getCarCount(), l2.getCarCount());
                            }).orElse(lanes.get("North"));

                    Circle light = lights.get(selected.getName());

                    // Calculate green time
                    int greenTime = Math.min(Math.max(selected.getCarCount()*2, MIN_GREEN), MAX_GREEN);
                    if(selected.getWaitingTime() >= STARVATION_TIME) greenTime = MIN_GREEN + 3;

                    // GREEN
                    Platform.runLater(() -> light.setFill(Color.GREEN));
                    Thread.sleep(greenTime * 1000);

                    // YELLOW
                    Platform.runLater(() -> light.setFill(Color.YELLOW));
                    Thread.sleep(2000);

                    // RED
                    Platform.runLater(() -> light.setFill(Color.RED));
                    selected.resetWaitingTime();

                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }).start();
    }
}
