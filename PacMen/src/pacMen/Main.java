package pacMen;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;

import pacMen.entities.Pellet;
import pacMen.entities.Player;

import java.util.*;

public class Main extends Application {

    // EXAMPLE CODE FOR TESTING PURPOSES, LIKELY NOT IMPLENTED IN THE FUTURE
    @Override
    public void start(Stage stage) throws Exception {
        // ----- 1. PLAYER -------------------------------
        Set<KeyCode> keysPressed = new HashSet<>();

        Player p1 = new Player(3, 1);
        Player p2 = new Player(3, 2);
        Pellet pel1 = new Pellet();
        p1.setInput(keysPressed);
        p2.setInput(keysPressed);

        // Simple UI element
        Label label = new Label("Pac-Men is running!");
        
        // Root layout
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");
        root.getChildren().add(label);

        root.getChildren().add(pel1.sprite);
        root.getChildren().add(p1.sprite);
        root.getChildren().add(p2.sprite);

        // Scene
        Scene scene = new Scene(root, 600, 800);

        // Input Handling
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));


        // Movement Handling
        AnimationTimer gameLoop = new AnimationTimer() {
            public void handle(long now) {
                p1.update();
                p2.update();
            }
        };

        gameLoop.start();

        // Stage setup
        stage.setTitle("Pac-Men");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}