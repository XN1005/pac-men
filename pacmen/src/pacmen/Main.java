package pacmen;

import javafx.application.Application;
import javafx.stage.Stage;
import pacmen.entities.Player;
import pacmen.map.GameMap;
import pacmen.map.MapLoader;
import pacmen.map.Cell;
import pacmen.map.PelletCell;
// import pacmen.map.WallCell; // if created a wall
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;

import java.util.*;

public class Main extends Application {

    private GameMap gameMap;
    private Set<KeyCode> keysPressed = new HashSet<>();

    @Override
    public void start(Stage stage) throws Exception {
        // map initialization
        gameMap = new GameMap();
        MapLoader.loadMap(gameMap, "resources\\maps\\level1.txt");

        // players initialization
        // TODO: need logic to initialize 1P or 2P depending on the play option (single/multiplayer)
        Player p1 = new Player(gameMap, 2.0, 1); 
        Player p2 = new Player(gameMap, 2.0, 2);
        
        p1.setInput(keysPressed);
        p2.setInput(keysPressed);

        // UI, Scene setup
        // TODO: Design UI
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");

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

        // Add players on top of the pellets
        root.getChildren().addAll(p1.sprite, p2.sprite);

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