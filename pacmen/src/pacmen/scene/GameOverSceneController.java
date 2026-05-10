package pacmen.scene;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pacmen.util.SceneManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the Game Over screen.
 *
 * Reads the final score from the scene root's UserData
 * (set by SceneManager.goToGameOver(score)).
 *
 * Compares against a stored high score (replace the stub
 * below with your ScoreManager when ready).
 */
public class GameOverSceneController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label     titleLabel;
    @FXML private Label     subtitleLabel;
    @FXML private Label     finalScoreLabel;
    @FXML private Label     highScoreLabel;
    @FXML private Label     levelLabel;
    @FXML private Label     timeLabel;
    @FXML private Label     newHighScoreLabel;

    // ── Replace with ScoreManager.getHighScore() when ready ──────
    private static int storedHighScore = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Score is passed via UserData set before navigation
        int finalScore = 0;
        if (rootPane.getUserData() instanceof Integer) {
            finalScore = (Integer) rootPane.getUserData();
        }

        // Update high score if beaten
        boolean isNewHigh = finalScore > storedHighScore;
        if (isNewHigh) storedHighScore = finalScore;

        populateStats(finalScore, storedHighScore, isNewHigh);
        animateEntrance(isNewHigh);
    }

    // ── Populate ──────────────────────────────────────────────────
    private void populateStats(int score, int highScore, boolean isNewHigh) {
        finalScoreLabel.setText(String.format("%06d", score));
        highScoreLabel.setText(String.format("%06d", highScore));

        // TODO: Replace placeholders with real values from Game state
        levelLabel.setText("1");
        timeLabel.setText("00:00");

        if (isNewHigh) {
            newHighScoreLabel.setVisible(true);
            newHighScoreLabel.setManaged(true);
        }
    }

    // ── Entrance animation ────────────────────────────────────────
    private void animateEntrance(boolean isNewHigh) {
        titleLabel.setOpacity(0);
        FadeTransition fadeTitle = new FadeTransition(Duration.millis(600), titleLabel);
        fadeTitle.setToValue(1.0);
        fadeTitle.play();

        if (isNewHigh) {
            // Pulsing flash on the new high score badge
            Timeline flash = new Timeline(
                new KeyFrame(Duration.millis(0),   new KeyValue(newHighScoreLabel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(400), new KeyValue(newHighScoreLabel.opacityProperty(), 0.2)),
                new KeyFrame(Duration.millis(800), new KeyValue(newHighScoreLabel.opacityProperty(), 1.0))
            );
            flash.setCycleCount(Animation.INDEFINITE);
            new PauseTransition(Duration.millis(700)) {{
                setOnFinished(e -> flash.play());
            }}.play();
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void onPlayAgain()   { SceneManager.goTo(SceneManager.GAME); }
    @FXML private void onMultiplayer() { SceneManager.goTo(SceneManager.MULTIPLAYER); }
    @FXML private void onMainMenu()    { SceneManager.goTo(SceneManager.MENU); }
}