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

    private long elapsedTimerMillis = 0;
    private long lastTimerUpdateNanos = 0;

    private String     state           = "ACTIVE"; 
    private GameMap    gameMap         = null;
    private int        currentScore    = 0;
    private int        currentLives    = 1;
    private int        currentLevel    = 1;
    private static int storedHighScore = 0;
    private String     activePlayer1Name = "Player1"; 

    private final Set<KeyCode> keysPressed = new HashSet<>();

    private AnimationTimer gameLoop;
    private Pane           gamePane;

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

        GameMap gameMap = new GameMap();
        this.gameMap = gameMap;
        MapLoader.loadMap(gameMap, mapPath);
        MapLoader.connectWallCells(gameMap);
        fitMapToView();

        final Player[] players = new Player[2];
        try {
            players[0] = new Player(gameMap, 1.55, 1, 14, 17, activePlayer1Name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (players[0] != null) players[0].setInput(keysPressed);
        
        final Player p1 = players[0];

        Ghost g1 = new Ghost(gameMap, 250, 280, 1.5, Color.RED, p1, "blinky");
        Ghost g2 = new Ghost(gameMap, 290, 280, 1.5, Color.ORANGE, p1, "clyde");
        Ghost g3 = new Ghost(gameMap, 330, 280, 1.5, Color.PINK, p1, "pinky");
        g1.attachToPane(gamePane);
        g2.attachToPane(gamePane);
        g3.attachToPane(gamePane);

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

        gamePane.getChildren().addAll(p1.sprite);
        gamePane.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite);
        Platform.runLater(this::fitMapToView);

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
                            g1.update();
                            g2.update();
                            g3.update();

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

    public void triggerGameOver() {
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
        PauseTransition beforeDie = new PauseTransition(Duration.millis(300));
        beforeDie.setOnFinished(e -> p.die());

        PauseTransition toStats = new PauseTransition(Duration.millis(1200));
        toStats.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, "Player2", "LOSE"));

        SequentialTransition seq = new SequentialTransition(beforeDie, toStats);
        seq.play();
    }

    private void handleWin() {
        PauseTransition winDelay = new PauseTransition(Duration.millis(800));
        winDelay.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel, activePlayer1Name, "Player2", "WIN"));
        winDelay.play();
    }

    @FXML 
    private void onMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goTo(SceneManager.MENU);
    }
}