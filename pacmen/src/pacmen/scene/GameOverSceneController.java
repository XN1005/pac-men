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
    @FXML private Label     finalScoreTitleLabel;
    @FXML private Label     highScoreTitleLabel;
    @FXML private Label     levelLabel;
    @FXML private Label     timeLabel;
    @FXML private Label     newHighScoreLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int finalScore = 0;
        int player1Score = 0;
        int player2Score = 0;
        long elapsedMillis = 0;
        int finalLevel = 1;

        String p1Name = "Player1";
        String p2Name = "Player2";
        String resultStatus = null;
        boolean isMultiplayer = false;

        SceneManager.GameOverData data = SceneManager.getPendingGameOverData();
        if (data != null) {
            finalScore = data.finalScore;
            player1Score = data.player1Score;
            player2Score = data.player2Score;
            elapsedMillis = data.elapsedMillis;
            finalLevel = data.level;
            resultStatus = data.resultStatus;
            isMultiplayer = data.isMultiplayer;

            if (data.player1Name != null) p1Name = data.player1Name;
            if (data.player2Name != null) p2Name = data.player2Name;

            SceneManager.clearPendingGameOverData();
        }

        boolean isNewHigh = !isMultiplayer && finalScore > ScoreManager.getInstance().getAbsoluteHighScore();
        if (!isMultiplayer && isNewHigh) {
            ScoreManager.getInstance().submitScore(p1Name, finalScore);
        }

        if (isMultiplayer) {
            ScoreManager.getInstance().submitScore(p1Name, player1Score);
            ScoreManager.getInstance().submitScore(p2Name, player2Score);
        }

        populateStats(player1Score, player2Score, isNewHigh, finalLevel, elapsedMillis, isMultiplayer, resultStatus, p1Name, p2Name);
        animateEntrance(isNewHigh);
    }

    private void populateStats(int player1Score, int player2Score, boolean isNewHigh, int level, long elapsedMillis, boolean isMultiplayer, String resultStatus, String p1Name, String p2Name) {
        if (isMultiplayer) {
            finalScoreTitleLabel.setText("PLAYER 1 SCORE");
            highScoreTitleLabel.setText("PLAYER 2 SCORE");
            finalScoreLabel.setText(String.format("%06d", player1Score));
            highScoreLabel.setText(String.format("%06d", player2Score));

            String winnerName = player1Score >= player2Score ? p1Name : p2Name;
            subtitleLabel.setText((winnerName + " WINS!").toUpperCase());
            newHighScoreLabel.setVisible(false);
            newHighScoreLabel.setManaged(false);
        } else {
            finalScoreTitleLabel.setText("FINAL SCORE");
            highScoreTitleLabel.setText("HIGH SCORE");
            finalScoreLabel.setText(String.format("%06d", player1Score));
            highScoreLabel.setText(String.format("%06d", Math.max(player1Score, ScoreManager.getInstance().getAbsoluteHighScore())));

            if ("WIN".equals(resultStatus)) {
                subtitleLabel.setText("LEVEL CLEAR, YOU WIN!");
            } else {
                subtitleLabel.setText("BETTER LUCK NEXT TIME");
            }

            if (isNewHigh) {
                newHighScoreLabel.setVisible(true);
                newHighScoreLabel.setManaged(true);
            }
        }

        levelLabel.setText(String.valueOf(level));
        timeLabel.setText(TimerDisplay.formatElapsedTime(elapsedMillis));
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