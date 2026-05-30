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

public class GameSceneController implements Initializable {
    
    // ── FXML ───────────────────────────────────────────
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
    @FXML private Label     p1NameLabel;     
    
    private static final long FIXED_FRAME_NANOS = 1_000_000_000L / 60; // Frame Rate (60fps)

    private long elapsedTimerMillis = 0;
    private long lastTimerUpdateNanos = 0;
    private long accumulatorNanos = 0;
    private long lastFrameNanos = 0;
    private long ghostScorePauseUntilNanos = 0;

    // ── Game state ────────────────────────────────────────────────
    private String     state           = "ACTIVE"; 
    private GameMap    gameMap         = null;
    private int        currentScore    = 0;
    private int        currentLives    = 3;
    private int        currentLevel    = 1;
    private static int storedHighScore = 0;
    private String     activePlayer1Name = "Player1";
    private String     currentMapPath;

    private final Set<KeyCode> keysPressed = new HashSet<>();

    private AnimationTimer gameLoop;
    private Timeline       countdownTimeline;
    private Pane           gamePane;

    private Player         player;
    private Ghost          g1;
    private Ghost          g2;
    private Ghost          g3;
    private Ghost          g4;
    private static final int INITIAL_PLAYER_COL = 14;
    private static final int INITIAL_PLAYER_ROW = 17;

    // Take inputs
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        storedHighScore = ScoreManager.getInstance().getAbsoluteHighScore();
        currentLevel = ScoreManager.getInstance().getSelectedMapLevel();

        buildLivesDisplay(currentLives);
        updateHUD(0, storedHighScore, currentLevel);

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e  -> keysPressed.add(e.getCode()));
                newScene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));
            }
        });
    }

    // ── MAIN ENTRY POINT ─────────────────────────────────────────
    public void initAndStartGame() {
        gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: black;");
        canvasWrapper.getChildren().add(gamePane);

        activePlayer1Name = ScoreManager.getInstance().getPlayer1Name();
        currentLevel = ScoreManager.getInstance().getSelectedMapLevel();
        String mapPath = currentLevel == 2 ? "resources/maps/level2.txt" : "resources/maps/level1.txt";
        if (p1NameLabel != null) {
            p1NameLabel.setText(activePlayer1Name.toUpperCase());
        }

        this.gameMap = new GameMap();
        this.currentMapPath = mapPath;
        MapLoader.loadMap(this.gameMap, mapPath);
        MapLoader.connectWallCells(this.gameMap);
        fitMapToView();

        try {
            this.player = new Player(this.gameMap, 2.00, 1, INITIAL_PLAYER_COL, INITIAL_PLAYER_ROW, activePlayer1Name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.player != null) this.player.setInput(keysPressed);

        createGhosts();
        rebuildGamePane();

        startCountdown(() -> {
            elapsedTimerMillis = 0;
            lastTimerUpdateNanos = System.nanoTime();
            lastFrameNanos = 0;
            updateTimerDisplay();

            gameLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (lastFrameNanos == 0) {
                        lastFrameNanos = now;
                    }

                    long frameNanos = now - lastFrameNanos;
                    lastFrameNanos = now;
                    accumulatorNanos += frameNanos;

                    while (accumulatorNanos >= FIXED_FRAME_NANOS) {
                        if (ghostScorePauseUntilNanos > now) {
                            g1.update();
                            g2.update();
                            g3.update();
                            g4.update();
                            player.pausePowerUpTimer(FIXED_FRAME_NANOS);
                            updateHUD(player.score, storedHighScore, currentLevel);
                            accumulatorNanos -= FIXED_FRAME_NANOS;
                            continue;
                        }

                        if (state.equals("ACTIVE")) {
                            elapsedTimerMillis += FIXED_FRAME_NANOS / 1_000_000;
                            updateTimerDisplay();

                            player.update();
                            g1.update();
                            g2.update();
                            g3.update();
                            g4.update();

                            if (Math.pow(Math.pow(player.currentCol - g1.getGridX(), 2) + Math.pow(player.currentRow - g1.getGridY(), 2), 0.5) <= 1) {
                                boolean consumed = player.collideGhost(g1);
                                if (consumed) {
                                    g1.collidePlayer(player);
                                    ghostScorePauseUntilNanos = now + 500_000_000L;
                                }
                            }
                            if (Math.pow(Math.pow(player.currentCol - g2.getGridX(), 2) + Math.pow(player.currentRow - g2.getGridY(), 2), 0.5) <= 1) {
                                boolean consumed = player.collideGhost(g2);
                                if (consumed) {
                                    g2.collidePlayer(player);
                                    ghostScorePauseUntilNanos = now + 500_000_000L;
                                }
                            }
                            if (Math.pow(Math.pow(player.currentCol - g3.getGridX(), 2) + Math.pow(player.currentRow - g3.getGridY(), 2), 0.5) <= 1) {
                                boolean consumed = player.collideGhost(g3);
                                if (consumed) {
                                    g3.collidePlayer(player);
                                    ghostScorePauseUntilNanos = now + 500_000_000L;
                                }
                            }
                            if (Math.pow(Math.pow(player.currentCol - g4.getGridX(), 2) + Math.pow(player.currentRow - g4.getGridY(), 2), 0.5) <= 1) {
                                boolean consumed = player.collideGhost(g4);
                                if (consumed) {
                                    g4.collidePlayer(player);
                                    ghostScorePauseUntilNanos = now + 500_000_000L;
                                }
                            }

                            updateHUD(player.score, storedHighScore, currentLevel);
                            setLives(currentLives);

                            if (player.state.equals("DEAD") && !state.equals("LOSE")) {
                                state = "LOSE";
                                stop();
                                handleLose(player);
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

                        accumulatorNanos -= FIXED_FRAME_NANOS;
                    }
                }
            };
            gameLoop.start();
        });
    }

    public void updateHUD(int score, int highScore, int level) {
        currentScore = score;
        currentLevel = level;
        if (score > storedHighScore) storedHighScore = score;
        scoreLabel.setText(String.format("%06d", score));
        highScoreLabel.setText(String.format("%06d", Math.max(highScore, storedHighScore)));
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
            dot.setStyle("-fx-font-size:20px; -fx-text-fill:#FFD700;" +
                         "-fx-effect:dropshadow(gaussian,rgba(255,215,0,0.6),6,0.4,0,0);");
            livesBox.getChildren().add(dot);
        }
    }

    public void startCountdown(Runnable onGo) {
        countdownOverlay.setVisible(true);
        countdownOverlay.setManaged(true);
        countdownOverlay.setOpacity(1.0);
        String[] steps = {"3", "2", "1", "GO!"};

        countdownTimeline = new Timeline();
        for (int i = 0; i < steps.length; i++) {
            final String text = steps[i];
            countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i), e -> {
                countdownLabel.setText(text);
                ScaleTransition pop = new ScaleTransition(Duration.millis(300), countdownLabel);
                pop.setFromX(1.5); pop.setFromY(1.5);
                pop.setToX(1.0);   pop.setToY(1.0);
                pop.play();
            }));
        }
        countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(steps.length), e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(300), countdownOverlay);
            fade.setToValue(0);
            fade.setOnFinished(ev -> {
                countdownOverlay.setVisible(false);
                countdownOverlay.setManaged(false);
                if (onGo != null) onGo.run();
            });
            fade.play();
        }));
        countdownTimeline.play();
    }

    public void triggerGameOver() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, "Player2", "LOSE");
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
                if (cell instanceof PelletCell) {
                    if (((PelletCell) cell).getPellet().state.equals("ACTIVE")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void handleLose(Player p) {
        fadeOutPlayerSprite(p, () -> {
            if (currentLives > 1) {
                setLives(currentLives - 1);
                resetPlayerAfterDeath();
                startCountdown(() -> {
                    state = "ACTIVE";
                    lastFrameNanos = 0;
                    if (gameLoop != null) gameLoop.start();
                });
            } else {
                SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, "Player2", "LOSE");
            }
        });
    }

    private void handleWin() {
        PauseTransition freeze = new PauseTransition(Duration.seconds(2));
        freeze.setOnFinished(e -> {
            resetRoundKeepScore();
            startCountdown(() -> {
                state = "ACTIVE";
                elapsedTimerMillis = 0;
                updateTimerDisplay();
                lastFrameNanos = 0;
                if (gameLoop != null) gameLoop.start();
            });
        });
        freeze.play();
    }

    private void fadeOutPlayerSprite(Player p, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.seconds(2), p.sprite);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            p.die();
            if (onFinished != null) onFinished.run();
        });
        fade.play();
    }

    private void createGhosts() {
        if (gameMap == null || player == null) return;
        g1 = new Ghost(gameMap, 250, 310, 2.0, Color.RED, player, "blinky");
        g2 = new Ghost(gameMap, 230, 270, 2.0, Color.ORANGE, player, "clyde");
        g3 = new Ghost(gameMap, 310, 310, 2.0, Color.PINK, player, "pinky");
        g4 = new Ghost(gameMap, 330, 270, 2.0, Color.AQUA, player, "inky");
    }

    private void rebuildGamePane() {
        if (gamePane == null || gameMap == null) return;
        gamePane.getChildren().clear();
        for (int x = 0; x < gameMap.getCols(); x++) {
            for (int y = 0; y < gameMap.getRows(); y++) {
                Cell cell = gameMap.getCell(x, y);
                if (cell instanceof PelletCell)
                    gamePane.getChildren().add(((PelletCell) cell).getPellet().sprite);
                if (cell instanceof CherryCell)
                    gamePane.getChildren().add(((CherryCell) cell).getCherry().sprite);
                if (cell instanceof WallCell)
                    gamePane.getChildren().add(((WallCell) cell).getSprite());
            }
        }
        if (g1 != null) g1.attachToPane(gamePane);
        if (g2 != null) g2.attachToPane(gamePane);
        if (g3 != null) g3.attachToPane(gamePane);
        if (g4 != null) g4.attachToPane(gamePane);
        if (player != null) gamePane.getChildren().add(player.sprite);
        if (g1 != null && g2 != null && g3 != null && g4 != null)
            gamePane.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite, g4.sprite);
        Platform.runLater(this::fitMapToView);
    }

    private void resetPlayerAfterDeath() {
        if (player == null) return;
        player.resetPosition(INITIAL_PLAYER_COL, INITIAL_PLAYER_ROW);
        recreateGhosts();
        ghostScorePauseUntilNanos = 0;
    }

    private void recreateGhosts() {
        if (gamePane == null || gameMap == null || player == null) return;
        createGhosts();
        rebuildGamePane();
    }

    private void resetRoundKeepScore() {
        if (gameMap == null || currentMapPath == null) return;
        MapLoader.loadMap(gameMap, currentMapPath);
        MapLoader.connectWallCells(gameMap);
        createGhosts();
        rebuildGamePane();
        if (player != null) {
            player.resetPosition(INITIAL_PLAYER_COL, INITIAL_PLAYER_ROW);
        }
        ghostScorePauseUntilNanos = 0;
        state = "ACTIVE";
        setLives(currentLives);
    }

    @FXML 
    private void onMainMenu() {
        if (countdownTimeline != null) countdownTimeline.stop();
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goTo(SceneManager.MENU);
    }
}