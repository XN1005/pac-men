package pacmen.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import pacmen.scene.*;

import java.io.File;

/**
 * Central scene-switching utility.
 * Call SceneManager.init(stage) once from Main.java, then
 * SceneManager.goTo(SceneManager.MENU) from anywhere.
 */
public class SceneManager {

    // Scene name constants
    public static final String MENU        = "resources/fxml/Mainmenu.fxml";
    public static final String GAME        = "resources/fxml/Gamescene.fxml";
    public static final String GAME_OVER   = "resources/fxml/Gameoverscene.fxml";
    public static final String MULTIPLAYER = "resources/fxml/Multiplayerscene.fxml";
    public static final String LEADERBOARD = "resources/fxml/Leaderboard.fxml";

    private static Stage        primaryStage;
    private static final int    W = 900;
    private static final int    H = 700;
    private static final int    FADE_MS = 300;

    /** Call once from Main.java: SceneManager.init(primaryStage); */
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getStage() { return primaryStage; }

    /**
     * Navigate to a scene by FXML path constant.
     * Plays a quick fade-out → swap → fade-in transition.
     */
    public static void goTo(String fxmlPath) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager not initialised. Call SceneManager.init(stage) first.");
        }

        Scene current = primaryStage.getScene();

        if (current != null) {
            FadeTransition out = new FadeTransition(Duration.millis(FADE_MS), current.getRoot());
            out.setFromValue(1.0);
            out.setToValue(0.0);
            out.setOnFinished(e -> loadScene(fxmlPath));
            out.play();
        } else {
            loadScene(fxmlPath);
        }
    }

    // Game over data pass pipeline
    private static GameOverData pendingGameOverData;

    // Overloaded backup option if code elsewhere doesn't provide names yet
    public static void goToGameOver(int finalScore) {
        goToGameOver(finalScore, 0, 1, "Player1", "Player2");
    }

    /**
     * Primary navigate to Game Over shortcut.
     * Passes metrics and custom player names down to the controller lifecycle.
     */
    public static void goToGameOver(int finalScore, long elapsedMillis, int level, String p1Name, String p2Name) {
        pendingGameOverData = new GameOverData(finalScore, elapsedMillis, level, p1Name, p2Name);
        goTo(GAME_OVER);
    }

    public static GameOverData getPendingGameOverData() {
        return pendingGameOverData;
    }

    public static void clearPendingGameOverData() {
        pendingGameOverData = null;
    }

    // Internal
    private static void loadScene(String fxmlPath) {
        try {
            File fxmlFile = new File(fxmlPath);
            
            if (!fxmlFile.exists()) {
                System.err.println("FXML file not found: " + fxmlFile.getAbsolutePath());
                return;
            }
            
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(fxmlFile.toURI().toURL());
            Parent root = loader.load();
            root.setOpacity(0);

            Object controller = loader.getController();
            if (controller instanceof GameSceneController) {
                ((GameSceneController) controller).initAndStartGame();
            } else if (controller instanceof MultiplayerSceneController) {
                ((MultiplayerSceneController) controller).initAndStartGame();
            }

            Scene scene = new Scene(root, W, H);
            primaryStage.setScene(scene);
            primaryStage.show();

            FadeTransition in = new FadeTransition(Duration.millis(FADE_MS), root);
            in.setFromValue(0.0);
            in.setToValue(1.0);
            in.play();

        } catch (Exception ex) {
            System.err.println("Error loading FXML: " + fxmlPath);
            ex.printStackTrace();
        }
    }

    // Updated data bundle class to support string transmission
    public static class GameOverData {
        public final int finalScore;
        public final long elapsedMillis;
        public final int level;
        public final String player1Name;
        public final String player2Name;

        public GameOverData(int finalScore, long elapsedMillis, int level, String player1Name, String player2Name) {
            this.finalScore = finalScore;
            this.elapsedMillis = elapsedMillis;
            this.level = level;
            this.player1Name = player1Name;
            this.player2Name = player2Name;
        }
    }
}