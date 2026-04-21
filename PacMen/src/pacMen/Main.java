package pacMen;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

public class Main extends Application {

    // EXAMPLE CODE FOR TESTING PURPOSES, LIKELY NOT IMPLENTED IN THE FUTURE
    @Override
    public void start(Stage stage) {
        // Simple UI element
        Label label = new Label("Pac-Men is running!");

        // Root layout
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Scene
        Scene scene = new Scene(root, 800, 600);

        // Stage setup
        stage.setTitle("Pac-Men");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}