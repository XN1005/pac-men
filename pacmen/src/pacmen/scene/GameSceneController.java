package pacmen.scene;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
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

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class GameSceneController implements Initializable {
    // ── FXML injections ───────────────────────────────────────────
    @FXML private StackPane rootPane;
    @FXML private Canvas    gameCanvas;      // kept in FXML but hidden behind gamePane
    @FXML private StackPane canvasWrapper;   // sprites go in here
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
    private String     state           = "ACTIVE"; // ACTIVE, PAUSED, WIN, LOSE     
    private GameMap    gameMap         = null;
    private int        currentScore    = 0;
    private int        currentLives    = 1;
    private int        currentLevel    = 2;
    private static int storedHighScore = 0;

    // ── Input ─────────────────────────────────────────────────────
    private final Set<KeyCode> keysPressed = new HashSet<>();

    // ── Game objects (filled in initAndStartGame) ─────────────────
    private AnimationTimer gameLoop;
    private Pane           gamePane;

    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildLivesDisplay(currentLives);
        updateHUD(0, storedHighScore, 2);

        // Wire keyboard input as soon as this scene is attached to a window
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(e  -> keysPressed.add(e.getCode()));
                newScene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // MAIN ENTRY POINT
    // Called by SceneManager after loading this scene.
    // Contains everything that used to live in Main.start().
    // ─────────────────────────────────────────────────────────────
    public void initAndStartGame() {

        // 1. Build the sprite pane and attach it to the FXML wrapper
        gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: black;");
        canvasWrapper.getChildren().add(gamePane);

        // 2. Map
        GameMap gameMap = new GameMap();
        this.gameMap = gameMap;
        MapLoader.loadMap(gameMap, "resources/maps/level2.txt");
        MapLoader.connectWallCells(gameMap);

        // 3. Players
        final Player[] players = new Player[2];
        try {
            players[0] = new Player(gameMap, 1.5, 1, 20, 14);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (players[0] != null) players[0].setInput(keysPressed);
        if (players[1] != null) players[1].setInput(keysPressed);
        
        final Player p1 = players[0];
        // final Player p2 = players[1];

        // 4. Ghosts
        Ghost g1 = new Ghost(gameMap, 250, 280, 1.5, Color.AQUA, p1, "blinky");
        Ghost g2 = new Ghost(gameMap, 270, 280, 1.5, Color.ORANGE, p1, "clyde");
        Ghost g3 = new Ghost(gameMap, 290, 280, 1.5, Color.PINK, p1, "pinky");

        // 5. Map cells (pellets, walls)
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

        // 6. Map border image
        // File imageFile = new File("resources/assets/MAP_Level1.png");
        // Image image = new Image(imageFile.toURI().toString());
        // ImageView imageView = new ImageView(image);
        // imageView.setFitWidth(560);
        // imageView.setPreserveRatio(true);

        // 7. Add everything to the pane (same order as original Main.java)
        gamePane.getChildren().addAll(p1.sprite);
        //gamePane.getChildren().add(imageView);
        gamePane.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite);

        // 8. Countdown → then start the game loop
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
                                // p2.update();
                                g1.update();
                                g2.update();
                                g3.update();

                                if (p1.currentCol == g1.getGridX() && p1.currentRow == g1.getGridY()) {
                                    p1.collideGhost(g1);
                                }
                                if (p1.currentCol == g2.getGridX() && p1.currentRow == g2.getGridY()) {
                                    p1.collideGhost(g2);
                                }
                                if (p1.currentCol == g3.getGridX() && p1.currentRow == g3.getGridY()) {
                                    p1.collideGhost(g3);
                                }

                                // Sync HUD score
                                updateHUD(p1.score, storedHighScore, currentLevel);

                                // Sync lives display (only redraws when value changes)
                                setLives(currentLives);

                                // Win/lose check
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

    /** Syncs score, high score, and level labels. Called every game tick. */
    public void updateHUD(int score, int highScore, int level) {
        currentScore = score;
        currentLevel = level;
        if (score > storedHighScore) storedHighScore = score;
        scoreLabel.setText(String.format("%06d", score));
        highScoreLabel.setText(String.format("%06d", Math.max(highScore, storedHighScore)));
        levelLabel.setText(String.valueOf(level));
    }

    /** Refreshes the ● life icons. Only redraws when the count actually changes. */
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

    // ── Countdown ─────────────────────────────────────────────────

    /** Shows 3 → 2 → 1 → GO!, then calls onGo. */
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

    // ── Game over ─────────────────────────────────────────────────

    /** Stops the loop and navigates to the Game Over screen. */
    public void triggerGameOver() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel);
    }

    // ── Pause ─────────────────────────────────────────────────────
    @FXML private void onPause() {
        // Enter PAUSED state; stop game rules
        state = "PAUSED";
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
        if (gameLoop != null) gameLoop.stop();
    }

    private void updateTimerDisplay() {
        timerLabel.setText(TimerDisplay.formatElapsedTime(elapsedTimerMillis));
    }

    @FXML private void onResume() {
        // Resume the game from PAUSED
        state = "ACTIVE";
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        if (gameLoop != null) {
            lastTimerUpdateNanos = System.nanoTime();
            gameLoop.start();
        }
    }

    // Check whether any pellets remain on the map
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
        // Game rules already stopped; play death animation after short delay,
        // then navigate to Game Over screen.
        PauseTransition beforeDie = new PauseTransition(Duration.millis(300));
        beforeDie.setOnFinished(e -> p.die());

        PauseTransition toStats = new PauseTransition(Duration.millis(1200));
        toStats.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel));

        SequentialTransition seq = new SequentialTransition(beforeDie, toStats);
        seq.play();
    }

    private void handleWin() {
        // Brief win delay/sound then show statistics
        PauseTransition winDelay = new PauseTransition(Duration.millis(800));
        winDelay.setOnFinished(e -> SceneManager.goToGameOver(currentScore, elapsedTimerMillis, currentLevel));
        winDelay.play();
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void onMainMenu() {
        if (gameLoop != null) gameLoop.stop();
        SceneManager.goTo(SceneManager.MENU);
    }
}
