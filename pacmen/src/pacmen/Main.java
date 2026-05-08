package pacmen;

import javafx.application.Application;
import javafx.stage.Stage;
import pacmen.entities.Player;
import pacmen.entities.Ghost;
import pacmen.map.GameMap;
import pacmen.map.MapLoader;
import pacmen.map.Cell;
import pacmen.map.PelletCell;
// import pacmen.map.WallCell; // if created a wall


import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class Main extends Application {

    // ----------- TEST FOR MULTIPLAYER SCENE ----------------------------------------------------------

    private GameMap gameMap;
    private Set<KeyCode> keysPressed = new HashSet<>();

    @Override
    public void start(Stage stage) throws Exception {
        // map initialization
        gameMap = new GameMap();
        MapLoader.loadMap(gameMap, "resources\\maps\\level1.txt");

        // players initialization
        // TODO: need logic to initialize 1P or 2P depending on the play option (single/multiplayer).
        /* If choose SINGLEPLAYER, ONLY P1 is spawned; P2 becomes null.
           If choose MULTIPLAYER, BOTH P1 AND P2 are spawned. */
        Player p1 = new Player(gameMap, 1.5, 1); 
        Player p2 = new Player(gameMap, 1.5, 2);

        Ghost g1 = new Ghost(gameMap, 250, 280, 1.5, Color.AQUA);
        Ghost g2 = new Ghost(gameMap, 270, 280, 1.5, Color.ORANGE);
        Ghost g3 = new Ghost(gameMap, 270, 280, 1.5, Color.PINK);
        
        p1.setInput(keysPressed);
        p2.setInput(keysPressed);

        // UI, Scene setup
        // TODO: Design UI
        
        // TEXT
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");
        Label scoreLabelP1 = new Label("Score: " + p1.score);
        Label scoreLabelP2 = new Label("Score: " + p2.score);

        // 2. Customize the Label (Optional)
        // Change the font size and style
        scoreLabelP1.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabelP2.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Change the text color (import javafx.scene.paint.Color)
        scoreLabelP1.setTextFill(Color.WHITE);
        scoreLabelP2.setTextFill(Color.WHITE);

        scoreLabelP1.setLayoutX(50.0);
        scoreLabelP1.setLayoutY(630.0);
        scoreLabelP2.setLayoutX(410.0);
        scoreLabelP2.setLayoutY(630.0);

        // load sprites by looping through the map
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 36; y++) {
                Cell cell = gameMap.getCell(x, y);
                
                // If it's a PelletCell, add the pellet's sprite to the screen
                if (cell instanceof PelletCell) {
                    root.getChildren().add(((PelletCell) cell).getPellet().sprite);
                }
            }
        }
        
        // load map border (walls)
        // We use a leading slash "/" to tell Java to start looking at the root of the 'src' folder
        File imageFile = new File("resources/assets/MAP_Level1.png");

        // 2. Automatically convert it to the perfect absolute URI that JavaFX requires
        String imageUrl = imageFile.toURI().toString();

        // 3. Load the image and set up your ImageView
        Image image = new Image(imageUrl);

        // Create the view and set properties
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(560);
        imageView.setPreserveRatio(true);




        // Add elements
        root.getChildren().addAll(p1.sprite, p2.sprite);
        root.getChildren().addAll(scoreLabelP1,scoreLabelP2);
        root.getChildren().add(imageView);
        root.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite);
        

        // Calculate window size based on grid (28 cols * 20px, 36 rows * 20px)
        Scene scene = new Scene(root, 28 * 20, 36 * 20);

        // input handling
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        // game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // track movements of players
                p1.update();
                p2.update();
                g1.update();
                g2.update();
                g3.update();
                scoreLabelP1.setText("Score: " + p1.score);
                scoreLabelP2.setText("Score: " + p2.score);

                
                // check for win-lose condition
                // TODO: complete this
                if (p1.state.equals("DEAD") || p2.state.equals("DEAD")) {
                    // call score manager for actions
                }
            }
        };

        gameLoop.start();

        stage.setTitle("Pac-Men Multiplayer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}