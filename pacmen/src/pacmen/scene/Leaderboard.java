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

        // ask ScoreManager for data
        Map<String, Integer> scores = ScoreManager.getInstance().getGlobalScores();

        if (scores.isEmpty()) {
            statusLabel.setText("NO SAVED SCORES FOUND");
            leaderboardRows.getChildren().add(makeEmptyRow("No saved scores yet."));
            return;
        }

        // sort the scores
        PriorityQueue<LeaderboardEntry> queue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            queue.offer(new LeaderboardEntry(entry.getKey(), entry.getValue()));
        }

        // add sorted scores to rank list
        List<LeaderboardEntry> ranked = new ArrayList<>();
        while (!queue.isEmpty()) {
            ranked.add(queue.poll());
        }

        statusLabel.setText("TOP: " + Math.min(ranked.size(), 10) + " ENTRIES");

        // display top 10
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
        // refresh UI visually
        loadLeaderboard();
    }

    private HBox createRow(int rank, LeaderboardEntry entry) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(620);
        row.setMaxWidth(620);
        row.setPadding(new javafx.geometry.Insets(0, 14, 0, 14));
        row.getStyleClass().add("leaderboard-row");

        Label rankLabel = new Label("#" + String.format("%02d", rank));
        rankLabel.getStyleClass().add("leaderboard-rank");
        rankLabel.setMinWidth(56);
        rankLabel.setPrefWidth(56);
        rankLabel.setMaxWidth(56);

        Label nameLabel = new Label(entry.name());
        nameLabel.getStyleClass().add("leaderboard-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label scoreLabel = new Label(String.format("%06d", entry.score()));
        scoreLabel.getStyleClass().add("leaderboard-score");
        scoreLabel.setMinWidth(110);
        scoreLabel.setPrefWidth(110);
        scoreLabel.setMaxWidth(110);
        scoreLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
        return row;
    }

    private HBox makeEmptyRow(String message) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setPrefWidth(620);
        row.setMaxWidth(620);
        row.setPadding(new javafx.geometry.Insets(0, 14, 0, 14));
        row.getStyleClass().add("leaderboard-row");

        Label label = new Label(message);
        label.getStyleClass().add("leaderboard-name");
        row.getChildren().add(label);
        return row;
    }

    private record LeaderboardEntry(String name, int score) implements Comparable<LeaderboardEntry> {
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
