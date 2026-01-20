package com.smarttraffic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.smarttraffic.controller.SignalController;
import com.smarttraffic.model.Lane;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private Map<String, Lane> lanes = new HashMap<>();
    private Map<String, Circle> lights = new HashMap<>();
    private Map<Lane, LinkedList<Rectangle>> carQueues = new HashMap<>();
    private Map<Lane, Integer> carsPassed = new HashMap<>();
    private Random random = new Random();
    private SignalController controller = new SignalController();
    private static final int CAR_SPACING = 20;

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(700, 700);
        drawRoads(root);
        initializeLanes(root);

        // Metrics Panel
        VBox metricsBox = new VBox(10);
        metricsBox.setLayoutX(550);
        metricsBox.setLayoutY(20);
        metricsBox.setPadding(new Insets(10));
        metricsBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        for (Lane lane : lanes.values()) {
            Label metric = new Label(lane.getName() + " Passed: 0");
            metric.setUserData(lane);
            carsPassed.put(lane, 0);
            metricsBox.getChildren().add(metric);
        }
        root.getChildren().add(metricsBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Smart Traffic Light Control System");
        stage.show();

        controller.startAutomaticCycleSingleLane(lanes, lights);
        startCarArrival();
        startCarAnimation(metricsBox);
    }

    private void drawRoads(Pane root) {
        // Background
        root.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        // Horizontal and vertical roads
        Rectangle hRoad = new Rectangle(0, 300, 700, 100);
        hRoad.setFill(Color.DARKGRAY);
        root.getChildren().add(hRoad);

        Rectangle vRoad = new Rectangle(300, 0, 100, 700);
        vRoad.setFill(Color.DARKGRAY);
        root.getChildren().add(vRoad);

        // Lane markings (dashed)
        for (int i = 1; i < 4; i++) {
            Rectangle hLine = new Rectangle(0, 300 + i * 25, 700, 2);
            hLine.setFill(Color.WHITE);
            root.getChildren().add(hLine);

            Rectangle vLine = new Rectangle(300 + i * 25, 0, 2, 700);
            vLine.setFill(Color.WHITE);
            root.getChildren().add(vLine);
        }
    }

    private void initializeLanes(Pane root) {
        String[] directions = {"North", "South", "East", "West"};
        for (String dir : directions) {
            Lane lane = new Lane(dir);
            lanes.put(dir, lane);
            carQueues.put(lane, new LinkedList<>());

            // Traffic light as circle
            Circle light = new Circle(12, Color.RED);
            lights.put(dir, light);

            switch (dir) {
                case "North":
                    light.setCenterX(350);
                    light.setCenterY(280);
                    break;
                case "South":
                    light.setCenterX(350);
                    light.setCenterY(420);
                    break;
                case "East":
                    light.setCenterX(420);
                    light.setCenterY(350);
                    break;
                case "West":
                    light.setCenterX(280);
                    light.setCenterY(350);
                    break;
            }
            root.getChildren().add(light);
        }
    }

    private void startCarArrival() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        for (Lane lane : lanes.values()) {
                            if (random.nextBoolean()) {
                                int carsToAdd = random.nextInt(3) + 1;
                                for (int i = 0; i < carsToAdd; i++) {
                                    Rectangle car = new Rectangle(15, 15, getRandomCarColor());
                                    positionCar(car, lane, i);
                                    carQueues.get(lane).add(car);
                                    lane.addCar();
                                    ((Pane) lights.get(lane.getName()).getParent()).getChildren().add(car);
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Color getRandomCarColor() {
        Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.ORANGE, Color.GREEN};
        return colors[random.nextInt(colors.length)];
    }

    private void positionCar(Rectangle car, Lane lane, int index) {
        int offset = index * CAR_SPACING;
        switch (lane.getName()) {
            case "North":
                car.setLayoutX(350);
                car.setLayoutY(0 - offset);
                break;
            case "South":
                car.setLayoutX(350);
                car.setLayoutY(700 + offset);
                break;
            case "East":
                car.setLayoutX(700 + offset);
                car.setLayoutY(350);
                break;
            case "West":
                car.setLayoutX(0 - offset);
                car.setLayoutY(350);
                break;
        }
    }

    private void startCarAnimation(VBox metricsBox) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            Platform.runLater(() -> {
                for (Lane lane : lanes.values()) {
                    LinkedList<Rectangle> queue = carQueues.get(lane);
                    if (queue.isEmpty()) continue;

                    Circle light = lights.get(lane.getName());
                    Rectangle prev = null;

                    for (Rectangle car : queue) {
                        if (light.getFill() == Color.GREEN) {
                            switch (lane.getName()) {
                                case "North":
                                    if (prev == null || car.getLayoutY() + CAR_SPACING < prev.getLayoutY())
                                        car.setLayoutY(car.getLayoutY() + 2);
                                    if (car.getLayoutY() > 400) removeCar(queue, lane, car);
                                    break;
                                case "South":
                                    if (prev == null || car.getLayoutY() - CAR_SPACING > prev.getLayoutY())
                                        car.setLayoutY(car.getLayoutY() - 2);
                                    if (car.getLayoutY() < 300) removeCar(queue, lane, car);
                                    break;
                                case "East":
                                    if (prev == null || car.getLayoutX() - CAR_SPACING > prev.getLayoutX())
                                        car.setLayoutX(car.getLayoutX() - 2);
                                    if (car.getLayoutX() < 300) removeCar(queue, lane, car);
                                    break;
                                case "West":
                                    if (prev == null || car.getLayoutX() + CAR_SPACING < prev.getLayoutX())
                                        car.setLayoutX(car.getLayoutX() + 2);
                                    if (car.getLayoutX() > 400) removeCar(queue, lane, car);
                                    break;
                            }
                        }
                        prev = car;
                    }
                }

                // Update metrics
                metricsBox.getChildren().forEach(node -> {
                    if (node instanceof Label) {
                        Label lbl = (Label) node;
                        Lane lane = (Lane) lbl.getUserData();
                        lbl.setText(lane.getName() + " Passed: " + carsPassed.get(lane));
                    }
                });
            });
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void removeCar(LinkedList<Rectangle> queue, Lane lane, Rectangle car) {
        queue.remove(car);
        ((Pane) lights.get(lane.getName()).getParent()).getChildren().remove(car);
        lane.removeCar();
        carsPassed.put(lane, carsPassed.get(lane) + 1);
    }

    public static void main(String[] args) {
        launch();
    }
}
