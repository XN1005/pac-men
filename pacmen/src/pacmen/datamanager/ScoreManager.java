package pacmen.datamanager;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ScoreManager {
    private static ScoreManager instance;

    // active game scores
    private int player1Score = 0;
    private int player2Score = 0;

    // history tracking (up to 10 games)
    private LinkedList<String> gameHistory;
    private static final int MAX_HISTORY = 10;
    private static final String DATA_FILE = "resources/data/data.txt";

    private ScoreManager() {
        gameHistory = new LinkedList<>();
        loadHistory();
    }

    public static ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }

    // active game logic
    public void addScore(int playerNum, int points) {
        if (playerNum == 1) {
            player1Score += points;
        } else if (playerNum == 2) {
            player2Score += points;
        }
    }

    public int getPlayer1Score() {
        return player1Score;
    }
    public int getPlayer2Score() {
        return player2Score;
    }

    public void resetScores() {
        player1Score = 0;
        player2Score = 0;
    }

    // end of game logic
    // data is saved to history
    public void recordGameEnd(String mode, String status) {
        // create record string, CSV style
        String record = mode + "," + player1Score + "," + player2Score + "," + status;
        
        // add to top of history list, keep only 10 recent games
        gameHistory.addFirst(record);
        if (gameHistory.size() > MAX_HISTORY) {
            gameHistory.removeLast();
        }

        // save updated list to text file
        saveHistory();
    }

    // file input-output logic
    private void loadHistory() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                gameHistory.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to load history:");
            System.err.println(e.getMessage());
        }
    }

    public void saveHistory() {
        File file = new File(DATA_FILE);
        // ensure dir exists
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String record: gameHistory) {
                bw.write(record);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save history:");
            System.err.println(e.getMessage());
        }
    }

    // high score calculator (for ui)
    // this calculates the highest score in the past games
    // only for 2-player games
    public int getAbsoluteHighScore2Player() {
        int highest = 0;
        for (String record : gameHistory) {
            String[] parts = record.split(",");
            if (parts.length >= 3) {
                int p1 = Integer.parseInt(parts[1]);
                int p2 = Integer.parseInt(parts[2]);
                highest = Math.max(highest, Math.max(p1, p2));
            }
        }
        return highest;
    }

    public int getAbsoluteHighScore1Player() {
        int highest = 0;
        for (String record : gameHistory) {
            String[] parts = record.split(",");
            if (parts.length < 3) {
                int p1 = Integer.parseInt(parts[1]);
                highest = Math.max(highest, p1);
            }
        }
        return highest;
    }

    public List<String> getHistory() {
        return gameHistory;
    }
}
