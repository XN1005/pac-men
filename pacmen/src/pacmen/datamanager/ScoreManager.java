package pacmen.datamanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreManager {
    private static ScoreManager instance;

    // Active game variables
    private String player1Name = "Player1";
    private String player2Name = "Player2";
    private int player1Score = 0;
    private int player2Score = 0;

    private int selectedMapLevel = 1; // Default map level

    private static final String DATA_FILE = "resources/data/data.txt";
    
    // Map structure to store all data
    // Key: player name + timestamp, Value: ScoreData (name + timestamp + score)
    private Map<String, ScoreData> globalScores;

    private ScoreManager() {
        globalScores = new HashMap<>();
        loadGlobalScores();
    }

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }

    // map selection methods
    public int getSelectedMapLevel() {
        return this.selectedMapLevel;
    }

    public void setSelectedMapLevel(int level) {
        this.selectedMapLevel = level;
    }

    // Active game session player name and score management
    public void setPlayerNames(String p1Name, String p2Name) {
        if (p1Name != null && !p1Name.trim().isEmpty()) this.player1Name = p1Name.trim();
        if (p2Name != null && !p2Name.trim().isEmpty()) this.player2Name = p2Name.trim();
    }

    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }

    public void addScore(int playerNum, int points) {
        if (playerNum == 1) player1Score += points;
        else if (playerNum == 2) player2Score += points;
    }

    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }

    public void resetScores() {
        player1Score = 0;
        player2Score = 0;
        player1Name = "Player1";
        player2Name = "Player2";
    }

    // Updated global score management with timestamp handling
    public void loadGlobalScores() {
        globalScores.clear();
        List<String> lines = SaveSystem.loadLines(DATA_FILE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Format: name timestamp score (separated by spaces)
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 3) {
                try {
                    int score = Integer.parseInt(parts[parts.length - 1]);
                    String timestamp = parts[parts.length - 2];

                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 2; i++) {
                        if (i > 0) nameBuilder.append(" ");
                        nameBuilder.append(parts[i]);
                    }
                    String name = nameBuilder.toString().trim();

                    String key = name + "|" + timestamp;
                    ScoreData existing = globalScores.get(key);
                    if (existing == null || score > existing.score) {
                        globalScores.put(key, new ScoreData(name, timestamp, score));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping malformed score line: " + line);
                }
            }
        }
    }

    public void submitScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) return;
        String cleanName = playerName.trim();

        loadGlobalScores();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        String currentTimestamp = now.format(formatter);
        String key = cleanName + "|" + currentTimestamp;

        ScoreData existing = globalScores.get(key);
        if (existing == null || score > existing.score) {
            globalScores.put(key, new ScoreData(cleanName, currentTimestamp, score));
        }

        List<String> output = new ArrayList<>();
        for (ScoreData scoreData : globalScores.values()) {
            output.add(scoreData.name + " " + scoreData.timestamp + " " + scoreData.score);
        }

        SaveSystem.saveLines(DATA_FILE, output);
    }

    public void clearLeaderboard() {
        globalScores.clear();
        SaveSystem.saveLines(DATA_FILE, new ArrayList<>());
    }

    public int getAbsoluteHighScore() {
        loadGlobalScores();
        return globalScores.values().stream().mapToInt(sd -> sd.score).max().orElse(0);
    }

    public Map<String, ScoreData> getGlobalScores() {
        loadGlobalScores();
        return globalScores;
    }

    // helper class to store full saved score details
    public static class ScoreData {
        public final String name;
        public final String timestamp;
        public final int score;

        public ScoreData(String name, String timestamp, int score) {
            this.name = name;
            this.timestamp = timestamp;
            this.score = score;
        }
    }
}