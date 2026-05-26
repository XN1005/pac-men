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
    @FXML private Label     p1NameLabel;
    @FXML private Label     p2NameLabel;
    @FXML private Label     p2ScoreLabel;
    @FXML private Label     currentLeadLabel;

    private long elapsedTimerMillis = 0;
    private long lastTimerUpdateNanos = 0;

    // ── Game state ────────────────────────────────────────────────
    private String     state           = "ACTIVE";    
    private GameMap    gameMap         = null;
    private int        currentScore    = 0;
    private int        currentP2Score  = 0;
    private int        currentLives    = 1;
    private int        currentLevel    = 1;
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
        currentLevel = ScoreManager.getInstance().getSelectedMapLevel();

        updateHUD(0, 0, currentLevel);

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
        currentLevel = ScoreManager.getInstance().getSelectedMapLevel();

        if (p1NameLabel != null) {
            p1NameLabel.setText(activePlayer1Name.toUpperCase());
        }
        if (p2NameLabel != null) {
            p2NameLabel.setText(activePlayer2Name.toUpperCase());
        }

        // 2. Map
        GameMap gameMap = new GameMap();
        this.gameMap = gameMap;
        String mapPath = currentLevel == 2 ? "resources/maps/level2.txt" : "resources/maps/level1.txt";
        MapLoader.loadMap(gameMap, mapPath);
        MapLoader.connectWallCells(gameMap);
        fitMapToView();

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
        Ghost g1 = new Ghost(gameMap, 250, 300, 1.5, Color.RED, p1, "blinky");
        Ghost g2 = new Ghost(gameMap, 230, 260, 1.5, Color.ORANGE, p1, "clyde");
        Ghost g3 = new Ghost(gameMap, 310, 300, 1.5, Color.PINK, p1, "pinky");
        Ghost g4 = new Ghost(gameMap, 330, 260, 1.5, Color.AQUA, p2, "inky");
        g1.attachToPane(gamePane);
        g2.attachToPane(gamePane);
        g3.attachToPane(gamePane);
        g4.attachToPane(gamePane);

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
        Platform.runLater(this::fitMapToView);

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

                            p1.update();
                            p2.update();
                            g1.update();
                            g2.update();
                            g3.update();
                            g4.update();

                            // Collision matrix boundary evaluations
                            if (Math.pow(Math.pow(p1.currentCol - g1.getGridX(), 2) + Math.pow(p1.currentRow - g1.getGridY(), 2), 0.5) <= 1) {
                                p1.collideGhost(g1);
                                g1.collidePlayer(p1);
                            }
                            if (Math.pow(Math.pow(p1.currentCol - g2.getGridX(), 2) + Math.pow(p1.currentRow - g2.getGridY(), 2), 0.5) <= 1) {
                                p1.collideGhost(g2);
                                g2.collidePlayer(p1);
                            }
                            if (Math.pow(Math.pow(p1.currentCol - g3.getGridX(), 2) + Math.pow(p1.currentRow - g3.getGridY(), 2), 0.5) <= 1) {
                                p1.collideGhost(g3);
                                g3.collidePlayer(p1);
                            }
                            if (Math.pow(Math.pow(p1.currentCol - g4.getGridX(), 2) + Math.pow(p1.currentRow - g4.getGridY(), 2), 0.5) <= 1) {
                                p1.collideGhost(g4);
                                g4.collidePlayer(p1);
                            }

                            if (Math.pow(Math.pow(p2.currentCol - g1.getGridX(), 2) + Math.pow(p2.currentRow - g1.getGridY(), 2), 0.5) <= 1) {
                                p2.collideGhost(g1);
                                g1.collidePlayer(p2);
                            }
                            if (Math.pow(Math.pow(p2.currentCol - g2.getGridX(), 2) + Math.pow(p2.currentRow - g2.getGridY(), 2), 0.5) <= 1) {
                                p2.collideGhost(g2);
                                g2.collidePlayer(p2);
                            }
                            if (Math.pow(Math.pow(p2.currentCol - g3.getGridX(), 2) + Math.pow(p2.currentRow - g3.getGridY(), 2), 0.5) <= 1) {
                                p2.collideGhost(g3);
                                g3.collidePlayer(p2);
                            }
                            if (Math.pow(Math.pow(p2.currentCol - g4.getGridX(), 2) + Math.pow(p2.currentRow - g4.getGridY(), 2), 0.5) <= 1) {
                                p2.collideGhost(g4);
                                g4.collidePlayer(p2);
                            }

                            updateHUD(p1.score, p2.score, currentLevel);

                            if (p1.state.equals("DEAD") && p2.state.equals("DEAD") && !state.equals("LOSE")) {
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
    public void updateHUD(int p1Score, int p2Score, int level) {
        currentScore = p1Score;
        currentP2Score = p2Score;
        currentLevel = level;
        scoreLabel.setText(String.format("%06d", p1Score));
        p2ScoreLabel.setText(String.format("%06d", p2Score));
        currentLeadLabel.setText(String.format("%+06d", p1Score - p2Score));
        levelLabel.setText(String.valueOf(level));
    }

    private void fitMapToView() {
        if (gamePane == null || canvasWrapper == null || gameMap == null) {
            return;
        }

        double mapWidth = gameMap.getCols() * 20.0;
        double mapHeight = gameMap.getRows() * 20.0;
        double availableWidth = canvasWrapper.getWidth();
        double availableHeight = canvasWrapper.getHeight();

        if (availableWidth <= 0 || availableHeight <= 0) {
            return;
        }

        double scale = Math.min(availableWidth / mapWidth, availableHeight / mapHeight);
        gamePane.setScaleX(scale);
        gamePane.setScaleY(scale);
        gamePane.setTranslateX((availableWidth - (mapWidth * scale)) / 2.0);
        gamePane.setTranslateY((availableHeight - (mapHeight * scale)) / 2.0);
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
        SceneManager.goToGameOver(currentScore, currentP2Score, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name);
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
        toStats.setOnFinished(e -> SceneManager.goToGameOver(currentScore, currentP2Score, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name));

        new SequentialTransition(beforeDie, toStats).play();
    }

    private void handleWin() {
        PauseTransition winDelay = new PauseTransition(Duration.millis(800));
        winDelay.setOnFinished(e -> SceneManager.goToGameOver(currentScore, currentP2Score, elapsedTimerMillis, currentLevel, activePlayer1Name, activePlayer2Name));
        winDelay.play();
    }

    @FXML 
    private void onMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goTo(SceneManager.MENU);
    }
}