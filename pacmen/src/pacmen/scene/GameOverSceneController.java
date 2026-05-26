package pacmen.scene;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pacmen.userinterface.TimerDisplay;
import pacmen.util.SceneManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the Game Over screen.
 *
 * Reads final score, level, and elapsed time from SceneManager pending data
 * (set by SceneManager.goToGameOver(score, elapsedMillis, level)).
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
        // Score and final time are passed via SceneManager pending data before navigation
        int finalScore = 0;
        long elapsedMillis = 0;
        int finalLevel = 1;

        SceneManager.GameOverData data = SceneManager.getPendingGameOverData();
        if (data != null) {
            finalScore = data.finalScore;
            elapsedMillis = data.elapsedMillis;
            finalLevel = data.level;
            SceneManager.clearPendingGameOverData();
        }

        // Update high score if beaten
        boolean isNewHigh = finalScore > storedHighScore;
        if (isNewHigh) storedHighScore = finalScore;

        populateStats(finalScore, storedHighScore, isNewHigh, finalLevel, elapsedMillis);
        animateEntrance(isNewHigh);
    }

    // ── Populate ──────────────────────────────────────────────────
    private void populateStats(int score, int highScore, boolean isNewHigh, int level, long elapsedMillis) {
        finalScoreLabel.setText(String.format("%06d", score));
        highScoreLabel.setText(String.format("%06d", highScore));

        levelLabel.setText(String.valueOf(level));
        timeLabel.setText(TimerDisplay.formatElapsedTime(elapsedMillis));

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
            PauseTransition pause = new PauseTransition(Duration.millis(700));
            pause.setOnFinished(e -> flash.play());
            pause.play();
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void onPlayAgain()   { SceneManager.goTo(SceneManager.GAME); }
    @FXML private void onMultiplayer() { SceneManager.goTo(SceneManager.MULTIPLAYER); }
    @FXML private void onMainMenu()    { SceneManager.goTo(SceneManager.MENU); }
}