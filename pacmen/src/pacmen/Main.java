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
        // ----- 1. MAP INITIALIZATION ---------------------------
        gameMap = new GameMap();
        // Ensure level1.txt is in your project root or resources folder
        MapLoader.loadMap(gameMap, "level1.txt");

        // ----- 2. PLAYER INITIALIZATION -------------------------
        // We now pass the gameMap to the players so they can check collisions
        Player p1 = new Player(gameMap, 2.0, 1); 
        Player p2 = new Player(gameMap, 2.0, 2);
        
        p1.setInput(keysPressed);
        p2.setInput(keysPressed);

        // ----- 3. UI & SCENE SETUP ------------------------------
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");

        // ----- 4. DYNAMIC SPRITE LOADING ------------------------
        // Instead of adding sprites manually, we loop through the loaded grid
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 36; y++) {
                Cell cell = gameMap.getCell(x, y);
                
                // If it's a PelletCell, add the pellet's sprite to the screen
                if (cell instanceof PelletCell) {
                    root.getChildren().add(((PelletCell) cell).getPellet().sprite);
                }
                // TODO: If you implemented Wall entities with sprites, add them here similarly
            }
        }

        // Add players on top of the pellets
        root.getChildren().addAll(p1.sprite, p2.sprite);

        // Calculate window size based on grid (28 cols * 20px, 36 rows * 20px)
        Scene scene = new Scene(root, 28 * 20, 36 * 20);

        // ----- 5. INPUT HANDLING --------------------------------
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        // ----- 6. GAME LOOP -------------------------------------
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Players now use their internal 'map' reference to move correctly
                p1.update();
                p2.update();
                
                // Optional: Check for win/loss conditions here
                if (p1.state.equals("DEAD") || p2.state.equals("DEAD")) {
                    // This is where you'd call your future ScoreManager
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