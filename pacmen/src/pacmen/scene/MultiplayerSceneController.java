package pacmen.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import pacmen.entities.Ghost;
import pacmen.entities.Player;
import pacmen.map.Cell;
import pacmen.map.CherryCell;
import pacmen.map.GameMap;
import pacmen.map.MapLoader;
import pacmen.map.PelletCell;
import pacmen.map.WallCell;
import pacmen.userinterface.TimerDisplay;
import pacmen.util.SceneManager;
import pacmen.datamanager.ScoreManager;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class MultiplayerSceneController implements Initializable {
    
    // ── FXML injections ───────────────────────────────────────────
    @FXML private StackPane rootPane;
    @FXML private Canvas    gameCanvas;      
    @FXML private StackPane canvasWrapper;   
    @FXML private StackPane pauseOverlay;
    @FXML private StackPane countdownOverlay;
    @FXML private Label     countdownLabel;
    @FXML private Label     scoreLabel;
    @FXML private Label     highScoreLabel;
    @FXML private Label     levelLabel;
    @FXML private Label     timerLabel;
    @FXML private HBox      livesBox;

    private long elapsedTimerMillis = 0;
    private long lastTimerUpdateNanos = 0;

    // ── Game state ────────────────────────────────────────────────
    private String     state           = "ACTIVE";    
    private GameMap    gameMap         = null;
    private int        currentScore    = 0;
    private int        currentLives    = 1;
    private int        currentLevel    = 2;
    private static int storedHighScore = 0;
    
    // Track localized player session strings
    private String activePlayer1Name = "Player1";
    private String activePlayer2Name = "Player2";

    // ── Input ─────────────────────────────────────────────────────
    private final Set<KeyCode> keysPressed = new HashSet<>();

    // ── Game objects (filled in initAndStartGame) ─────────────────
    private AnimationTimer gameLoop;
    private Pane           gamePane;

    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        storedHighScore = ScoreManager.getInstance().getAbsoluteHighScore();
        
        buildLivesDisplay(currentLives);
        updateHUD(0, storedHighScore, 2);

        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        rootPane.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(rootPane.getOnKeyPressed());
                newScene.setOnKeyReleased(rootPane.getOnKeyReleased());
                Platform.runLater(rootPane::requestFocus);
            }
        });
    }

    // ── MAIN ENTRY POINT ─────────────────────────────────────────
    public void initAndStartGame() {
        gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: black;");
        canvasWrapper.getChildren().add(gamePane);
        Platform.runLater(rootPane::requestFocus);

        // 1. Gather configured tracking details out of global instances
        activePlayer1Name = ScoreManager.getInstance().getPlayer1Name();
        activePlayer2Name = ScoreManager.getInstance().getPlayer2Name();

        // 2. Map
        GameMap gameMap = new GameMap();
        this.gameMap = gameMap;
        MapLoader.loadMap(gameMap, "resources/maps/level1.txt");
        MapLoader.connectWallCells(gameMap);

        // 3. Players
        final Player[] players = new Player[2];
        try {
            players[0] = new Player(gameMap, 1.5, 1, 8, 14, activePlayer1Name);
            players[1] = new Player(gameMap, 1.5, 2, 20, 14, activePlayer2Name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (players[0] != null) players[0].setInput(keysPressed);
        if (players[1] != null) players[1].setInput(keysPressed);
        
        final Player p1 = players[0];
        final Player p2 = players[1];

        // 4. Ghosts
        Ghost g1 = new Ghost(gameMap, 250, 280, 1.5, Color.RED, p1, "blinky");
        Ghost g2 = new Ghost(gameMap, 270, 280, 1.5, Color.ORANGE, p1, "clyde");
        Ghost g3 = new Ghost(gameMap, 290, 280, 1.5, Color.PINK, p1, "pinky");
        Ghost g4 = new Ghost(gameMap, 230, 280, 1.5, Color.AQUA, p2, "inky");

        // 5. Map cells
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 36; y++) {
                Cell cell = gameMap.getCell(x, y);
                if (cell instanceof PelletCell)
                    gamePane.getChildren().add(((PelletCell) cell).getPellet().sprite);
                if (cell instanceof CherryCell)
                    gamePane.getChildren().add(((CherryCell) cell).getCherry().sprite);
                if (cell instanceof WallCell)
                    gamePane.getChildren().add(((WallCell) cell).getSprite());
            }
        }

        // 7. Render components
        gamePane.getChildren().addAll(p1.sprite, p2.sprite);
        gamePane.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite, g4.sprite);

        // 8. Countdown loop
        startCountdown(() -> {
            elapsedTimerMillis = 0;
            lastTimerUpdateNanos = System.nanoTime();
            updateTimerDisplay();

            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    long deltaMillis = (now - lastTimerUpdateNanos) / 1_000_000;
                    if (deltaMillis > 0) {
                        lastTimerUpdateNanos = now;

                        if (state.equals("ACTIVE")) {
                            elapsedTimerMillis += deltaMillis;
                            updateTimerDisplay();

<<<<<<< HEAD
                            p1.update();
                            p2.update();
                            g1.update();
                            g2.update();
                            g3.update();
=======
                                p1.update();
                                p2.update();
                                g1.update();
                                g2.update();
                                g3.update();
                                g4.update();
>>>>>>> 03f73b9a342976204f87103e32467042182d7507

                            // Collision matrix boundary evaluations
                            if (p1.currentCol == g1.getGridX() && p1.currentRow == g1.getGridY()) p1.collideGhost(g1);
                            if (p1.currentCol == g2.getGridX() && p1.currentRow == g2.getGridY()) p1.collideGhost(g2);
                            if (p1.currentCol == g3.getGridX() && p1.currentRow == g3.getGridY()) p1.collideGhost(g3);
                            if (p1.currentCol == g4.getGridX() && p1.currentRow == g4.getGridY()) p1.collideGhost(g4);
                            
                            if (p2.currentCol == g1.getGridX() && p2.currentRow == g1.getGridY()) p2.collideGhost(g1);
                            if (p2.currentCol == g2.getGridX() && p2.currentRow == g2.getGridY()) p2.collideGhost(g2);
                            if (p2.currentCol == g3.getGridX() && p2.currentRow == g3.getGridY()) p2.collideGhost(g3);
                            if (p2.currentCol == g4.getGridX() && p2.currentRow == g4.getGridY()) p2.collideGhost(g4);

                            updateHUD(p1.score, storedHighScore, currentLevel);
                            setLives(currentLives);

                            if (p1.state.equals("DEAD") && !state.equals("LOSE")) {
                                state = "LOSE";
                                stop();
                                handleLose(p1);
                                return;
                            }

                            if (allPelletsConsumed()) {
                                if (!state.equals("WIN")) {
                                    state = "WIN";
                                    stop();
                                    handleWin();
                                    return;
                                }
                            }
                        }
                    }
                }
            };
            gameLoop.start();
        });
    }

    // ── HUD ───────────────────────────────────────────────────────
    public void updateHUD(int score, int highScore, int level) {
        currentScore = score;
        currentLevel = level;
        if (score > storedHighScore) storedHighScore = score;
        scoreLabel.setText(String.format("%06d", score));
        highScoreLabel.setText(String.format("%06d", Math.max(highScore, storedHighScore)));
        levelLabel.setText(String.valueOf(level));
    }

    public void setLives(int lives) {
        if (lives != currentLives) {
            currentLives = lives;
            buildLivesDisplay(lives);
        }
    }

    private void buildLivesDisplay(int lives) {
        livesBox.getChildren().clear();
        for (int i = 0; i < lives; i++) {
            Label dot = new Label("●");
            dot.setStyle("-fx-font-size:20px; -fx-text-fill:#FFD700;");
            livesBox.getChildren().add(dot);
        }
    }

    // ── Countdown ─────────────────────────────────────────────────
    public void startCountdown(Runnable onGo) {
        countdownOverlay.setVisible(true);
        countdownOverlay.setManaged(true);
        String[] steps = {"3", "2", "1", "GO!"};

        Timeline tl = new Timeline();
        for (int i = 0; i < steps.length; i++) {
            final String text = steps[i];
            tl.getKeyFrames().add(new KeyFrame(Duration.seconds(i), e -> {
                countdownLabel.setText(text);
                ScaleTransition pop = new ScaleTransition(Duration.millis(300), countdownLabel);
                pop.setFromX(1.5); pop.setFromY(1.5);
                pop.setToX(1.0);   pop.setToY(1.0);
                pop.play();
            }));
        }
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(steps.length), e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(300), countdownOverlay);
            fade.setToValue(0);
            fade.setOnFinished(ev -> {
                countdownOverlay.setVisible(false);
                countdownOverlay.setManaged(false);
                if (onGo != null) onGo.run();
            });
            fade.play();
        }));
        tl.play();
    }

    // ── Game over triggers ────────────────────────────────────────
    public void triggerGameOver() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name);
    }

    @FXML 
    private void onPause() {
        state = "PAUSED";
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
        if (gameLoop != null) gameLoop.stop();
    }

    private void updateTimerDisplay() {
        timerLabel.setText(TimerDisplay.formatElapsedTime(elapsedTimerMillis));
    }

    @FXML 
    private void onResume() {
        state = "ACTIVE";
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        if (gameLoop != null) {
            lastTimerUpdateNanos = System.nanoTime();
            gameLoop.start();
        }
    }

    private boolean allPelletsConsumed() {
        if (this.gameMap == null) return false;
        for (int x = 0; x < this.gameMap.getCols(); x++) {
            for (int y = 0; y < this.gameMap.getRows(); y++) {
                Cell cell = this.gameMap.getCell(x, y);
                if (cell instanceof PelletCell && ((PelletCell) cell).getPellet().state.equals("ACTIVE")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleLose(Player p) {
        PauseTransition beforeDie = new PauseTransition(Duration.millis(300));
        beforeDie.setOnFinished(e -> p.die());

        PauseTransition toStats = new PauseTransition(Duration.millis(1200));
        toStats.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name));

        new SequentialTransition(beforeDie, toStats).play();
    }

    private void handleWin() {
        PauseTransition winDelay = new PauseTransition(Duration.millis(800));
        winDelay.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name));
        winDelay.play();
    }

    @FXML 
    private void onMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goTo(SceneManager.MENU);
    }
}