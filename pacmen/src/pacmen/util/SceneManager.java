package pacmen.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import pacmen.scene.GameSceneController;

/**
 * Central scene-switching utility.
 * Call SceneManager.init(stage) once from Main.java, then
 * SceneManager.goTo(SceneManager.MENU) from anywhere.
 */
public class SceneManager {

    // ── Scene name constants ──────────────────────────────────────
    public static final String MENU        = "fxml/MainMenu.fxml";
    public static final String GAME        = "fxml/GameScene.fxml";
    public static final String GAME_OVER   = "fxml/GameOverScene.fxml";
    public static final String MULTIPLAYER = "fxml/MultiplayerScene.fxml";

    private static Stage        primaryStage;
    private static final int    W = 900;
    private static final int    H = 650;
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

    /**
     * Shortcut: navigate to Game Over and pass the final score.
     * The GameOverSceneController reads it from UserData.
     */
    public static void goToGameOver(int finalScore) {
        primaryStage.getScene().getRoot().setUserData(finalScore);
        goTo(GAME_OVER);
    }

    // ── Internal ──────────────────────────────────────────────────
    private static void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getClassLoader().getResource("resources/" + fxmlPath)
            );
            Parent root = loader.load();
            root.setOpacity(0);

            Object controller = loader.getController();
            if (controller instanceof GameSceneController) {
                ((GameSceneController) controller).initAndStartGame();
            }

            Scene scene = new Scene(root, W, H);
            primaryStage.setScene(scene);
            primaryStage.show();

            FadeTransition in = new FadeTransition(Duration.millis(FADE_MS), root);
            in.setFromValue(0.0);
            in.setToValue(1.0);
            in.play();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}