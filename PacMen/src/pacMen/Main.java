package pacMen;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.shape.Circle;

import java.util.*;

public class Main extends Application {

    // store keys pressed as a hashset
    private Set<KeyCode> keysPressed = new HashSet<>();

    // EXAMPLE CODE FOR TESTING PURPOSES, LIKELY NOT IMPLENTED IN THE FUTURE
    @Override
    public void start(Stage stage) {
        Circle player = new Circle(10, Color.YELLOW);
        player.setCenterX(400);
        player.setCenterY(300);        

        // Simple UI element
        Label label = new Label("Pac-Men is running!");
        
        // Root layout
        Pane root = new Pane();
        root.getChildren().add(label);

        root.getChildren().add(player);

        // Scene
        Scene scene = new Scene(root, 800, 600);

        // Input Handling
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));


        // Movement Handling
        AnimationTimer gameLoop = new AnimationTimer() {
            public void handle(long now) {

                double speed = 1.5;

                // Set player movement; Restrict movement to either Vertical or Horizontal (no Diagonal)
                // Placeholder Commands, will fix accordingly to conform to 2D Arrays
                if (keysPressed.contains(KeyCode.W) || keysPressed.contains(KeyCode.S)) {
                    if (keysPressed.contains(KeyCode.W)) {
                        player.setCenterY(player.getCenterY() - speed);
                    }
                    if (keysPressed.contains(KeyCode.S)) {
                        player.setCenterY(player.getCenterY() + speed);
                    }      
                }
                else {
                    if (keysPressed.contains(KeyCode.A)) {
                        player.setCenterX(player.getCenterX() - speed);
                    }            
                    if (keysPressed.contains(KeyCode.D)) {
                        player.setCenterX(player.getCenterX() + speed);
                    }
                }   
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