package pacmen.datamanager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ScoreManager {
    private static ScoreManager instance;

    // Active tracking variables
    private String player1Name = "Player1";
    private String player2Name = "Player2";
    private int player1Score = 0;
    private int player2Score = 0;
    private int selectedMapLevel = 1;

    private static final String DATA_FILE = "resources/data/data.txt";
    private Map<String, Integer> globalScores;

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

    // active game player names and scoring
    public void setPlayerNames(String p1Name, String p2Name) {
        String cleanP1 = sanitizeName(p1Name);
        String cleanP2 = sanitizeName(p2Name);
        if (cleanP1 != null) this.player1Name = cleanP1;
        if (cleanP2 != null) this.player2Name = cleanP2;
    }

    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }

    public void setSelectedMapLevel(int level) {
        this.selectedMapLevel = level == 2 ? 2 : 1;
    }

    public int getSelectedMapLevel() {
        return selectedMapLevel;
    }

    private String sanitizeName(String name) {
        if (name == null) return null;
        String cleaned = name.replaceAll("\\s+", "");
        return cleaned.isBlank() ? null : cleaned;
    }

    public void addScore(int playerNum, int points) {
        if (playerNum == 1) {
            player1Score += points;
        } else if (playerNum == 2) {
            player2Score += points;
        }
    }

    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }

    public void resetScores() {
        player1Score = 0;
        player2Score = 0;
        player1Name = "Player1";
        player2Name = "Player2";
    }

    // leaderboard data storage process
    public void loadGlobalScores() {
        globalScores.clear();
        List<String> lines = SaveSystem.loadLines(DATA_FILE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            int lastSpaceIndex = trimmed.lastIndexOf(' ');
            if (lastSpaceIndex == -1) continue;   

            try {
                String name = trimmed.substring(0, lastSpaceIndex).trim();
                int score = Integer.parseInt(trimmed.substring(lastSpaceIndex + 1).trim());
                globalScores.merge(name, score, Math::max);
            } catch (NumberFormatException e) {
                System.err.println("Skipping malformed score line: " + line);
            }
        }
    }

    public void submitScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) return;
        
        loadGlobalScores();
        globalScores.merge(playerName.trim(), score, Math::max);

        List<String> output = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : globalScores.entrySet()) {
            output.add(entry.getKey() + " " + entry.getValue());
        }
        SaveSystem.saveLines(DATA_FILE, output);
    }

    public void clearLeaderboard() {
        globalScores.clear();
        SaveSystem.saveLines(DATA_FILE, new ArrayList<>());
    }

    public int getAbsoluteHighScore() {
        loadGlobalScores();
        return globalScores.values().stream().max(Integer::compareTo).orElse(0);
    }

    public Map<String, Integer> getGlobalScores() {
        loadGlobalScores();
        return globalScores;
    }
}