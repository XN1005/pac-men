package pacmen.scene;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import pacmen.userinterface.TimerDisplay;
import pacmen.util.SceneManager;
import pacmen.datamanager.ScoreManager;

import java.net.URL;
import java.util.ResourceBundle;

public class GameOverSceneController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label     titleLabel;
    @FXML private Label     subtitleLabel;
    @FXML private Label     finalScoreLabel;
    @FXML private Label     highScoreLabel;
    @FXML private Label     levelLabel;
    @FXML private Label     timeLabel;
    @FXML private Label     newHighScoreLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int finalScore = 0;
        long elapsedMillis = 0;
        int finalLevel = 1;
        
        // Setup baseline safety defaults
        String p1Name = "Player1";
        String p2Name = "Player2";

        SceneManager.GameOverData data = SceneManager.getPendingGameOverData();
        if (data != null) {
            finalScore = data.finalScore;
            elapsedMillis = data.elapsedMillis;
            finalLevel = data.level;
            
            // Collect the passed names from SceneManager
            if (data.player1Name != null) p1Name = data.player1Name;
            if (data.player2Name != null) p2Name = data.player2Name;
            
            SceneManager.clearPendingGameOverData();
        }

        int storedHighScore = ScoreManager.getInstance().getAbsoluteHighScore();
        boolean isNewHigh = finalScore > storedHighScore;
        if (isNewHigh) storedHighScore = finalScore;

        // Commit high scores directly to the persistent file database
        ScoreManager.getInstance().submitScore(p1Name, finalScore);

        populateStats(finalScore, storedHighScore, isNewHigh, finalLevel, elapsedMillis);
        animateEntrance(isNewHigh);
    }

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

    private void animateEntrance(boolean isNewHigh) {
        titleLabel.setOpacity(0);
        FadeTransition fadeTitle = new FadeTransition(Duration.millis(600), titleLabel);
        fadeTitle.setToValue(1.0);
        fadeTitle.play();

        if (isNewHigh) {
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

    @FXML private void onPlayAgain()   { SceneManager.goTo(SceneManager.GAME); }
    @FXML private void onMultiplayer() { SceneManager.goTo(SceneManager.MULTIPLAYER); }
    @FXML private void onMainMenu()    { SceneManager.goTo(SceneManager.MENU); }
}