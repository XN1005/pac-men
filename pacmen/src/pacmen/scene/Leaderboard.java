package pacmen.scene;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pacmen.util.SceneManager;
import pacmen.datamanager.ScoreManager;

import java.net.URL;
import java.util.*;

public class Leaderboard implements Initializable {

    @FXML private VBox leaderboardRows;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        leaderboardRows.getChildren().clear();
        statusLabel.setText("LOADING SCORES...");

        Map<String, ScoreManager.ScoreData> scores = ScoreManager.getInstance().getGlobalScores();

        if (scores.isEmpty()) {
            statusLabel.setText("NO SAVED SCORES FOUND");
            leaderboardRows.getChildren().add(makeEmptyRow("No saved scores yet."));
            return;
        }

        PriorityQueue<LeaderboardEntry> queue = new PriorityQueue<>();
        for (ScoreManager.ScoreData scoreData : scores.values()) {
            queue.offer(new LeaderboardEntry(scoreData.name, scoreData.timestamp, scoreData.score));
        }

        List<LeaderboardEntry> ranked = new ArrayList<>();
        while (!queue.isEmpty()) {
            ranked.add(queue.poll());
        }

        statusLabel.setText("TOP " + Math.min(ranked.size(), 10) + " ENTRIES");

        int limit = Math.min(ranked.size(), 10);
        for (int i = 0; i < limit; i++) {
            leaderboardRows.getChildren().add(createRow(i + 1, ranked.get(i)));
        }
    }

    @FXML
    private void onBack() {
        SceneManager.goTo(SceneManager.MENU);
    }

    @FXML
    private void onClearLeaderboard() {
        ScoreManager.getInstance().clearLeaderboard();
        loadLeaderboard();
    }

    private HBox createRow(int rank, LeaderboardEntry entry) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(620);
        row.setMaxWidth(620);
        row.setPadding(new javafx.geometry.Insets(0, 14, 0, 14));
        row.getStyleClass().add("leaderboard-row");

        // Rank column
        Label rankLabel = new Label("#" + String.format("%02d", rank));
        rankLabel.getStyleClass().add("leaderboard-rank");
        rankLabel.setMinWidth(56); rankLabel.setPrefWidth(56); rankLabel.setMaxWidth(56);

        // 2. Player Name Column
        Label nameLabel = new Label(entry.name());
        nameLabel.getStyleClass().add("leaderboard-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // 3. Timestamp Column
        String readableTime = entry.timestamp().replace("_", " "); // Changes 2026-05-28_13:15 to 2026-05-28 13:15
        Label timeLabel = new Label(readableTime);
        timeLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px; -fx-text-fill: #aaaaaa;");
        timeLabel.setMinWidth(150);
        timeLabel.setPrefWidth(150);
        timeLabel.setMaxWidth(150);
        timeLabel.setAlignment(Pos.CENTER);

        // Score Column
        Label scoreLabel = new Label(String.format("%06d", entry.score()));
        scoreLabel.getStyleClass().add("leaderboard-score");
        scoreLabel.setMinWidth(110); scoreLabel.setPrefWidth(110); scoreLabel.setMaxWidth(110);
        scoreLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(rankLabel, nameLabel, timeLabel, scoreLabel);
        return row;
    }

    private HBox makeEmptyRow(String message) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPrefWidth(620);
        row.setMaxWidth(620);
        row.getStyleClass().add("leaderboard-row");

        Label label = new Label(message);
        label.getStyleClass().add("leaderboard-name");
        row.getChildren().add(label);
        return row;
    }

    // Record expanded to encapsulate timestamp strings
    private record LeaderboardEntry(String name, String timestamp, int score) implements Comparable<LeaderboardEntry> {
        @Override
        public int compareTo(LeaderboardEntry other) {
            int scoreCompare = Integer.compare(other.score, this.score);
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return this.name.compareToIgnoreCase(other.name);
        }
    }
}