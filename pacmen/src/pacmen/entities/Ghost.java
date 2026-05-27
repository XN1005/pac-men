package pacmen.entities;
import pacmen.ghostai.*;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pacmen.map.GameMap;

public class Ghost extends Entity {
    // States
    public enum GhostState { SCATTER, CHASE, FRIGHTENED, EATEN }
    private static final int SPAWN_MIN_X = 9;
    private static final int SPAWN_MAX_X = 18;
    private static final int SPAWN_MIN_Y = 12;
    private static final int SPAWN_MAX_Y = 15;
    private static final int HOUSE_CENTER_X = 14;
    private static final int HOUSE_CENTER_Y = 14;
    private static final double CHASE_DURATION_SECONDS = 5.5; // -4s start = 1.5s
    private static final double SCATTER_DURATION_SECONDS = 5.0;
    private static final double CHASE_DURATION_AFTER_SCATTER_SECONDS = 8.0;

    private final double baseSpeed;     // default speed, set later
    public  GhostState currentState;    // Public for player accessibility
    private Color baseColor;
    private int targetX, targetY;
    private final GameMap map;
    private final String ghostName;
    private Player targetPlayer;
    public Circle sprite;
    private Label scoreDisplay;
    private Pane attachedPane;
    private boolean scoreDisplayActive = false;
    private long scoreDisplayEndNanos = 0;
    private long phaseStartNanos;
    private double chaseDurationSeconds = CHASE_DURATION_AFTER_SCATTER_SECONDS;

    // Acts as switchers
    public boolean already_eaten;
    public boolean already_frightened;
    public Ghost(GameMap map, double x, double y, double speed, Color color) {
        this(map, x, y, speed, color, null, "blinky");
        this.already_eaten = false;
        this.already_frightened = false;
    }

    public Ghost(GameMap map, double x, double y, double speed, Color color, Player targetPlayer, String ghostName) {
        super(snapToGrid(x), snapToGrid(y), speed);
        this.baseSpeed = speed;
        this.map = map;
        this.baseColor = color;
        this.targetPlayer = targetPlayer;
        this.ghostName = ghostName == null || ghostName.isBlank() ? "blinky" : ghostName;
        this.direction = 1; // Default starting direction (Right)
        this.sprite = new Circle(15, color);
        this.sprite.setCenterX(this.x);
        this.sprite.setCenterY(this.y);
        this.targetX = getGridX();
        this.targetY = getGridY();
        startScatterPhase();
    }

    @Override
    public void update() {
        if (scoreDisplayActive) {
            updateScoreDisplay();
            if (System.nanoTime() >= scoreDisplayEndNanos) {
                endScoreDisplay();
            }
            return;
        }

        applyGhostBehaviorState();
        updatePhaseTimer();

        if (isAtCenterOfTile()) {

            // --- THE SNAP ---
            // Wipe out any mathematical offset so they perfectly align with the grid
            this.x = (getGridX() * 20) + 10;
            this.y = (getGridY() * 20) + 10;

            if (currentState == GhostState.EATEN && isAtHouseTile()) {
                startScatterPhase();
            }

            updateTarget();
            chooseNextDirection();
        }
        move();
        updateVisuals();
    }

    public void attachToPane(Pane pane) {
        this.attachedPane = pane;
        if (scoreDisplay == null) {
            scoreDisplay = new Label();
            scoreDisplay.setStyle("-fx-font-family: 'Press Start 2P', 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-effect: dropshadow(gaussian, rgba(255,215,0,0.9), 12, 0.5, 0, 0);");
            scoreDisplay.setMouseTransparent(true);
            scoreDisplay.setVisible(false);
        }
        if (this.attachedPane != null && !this.attachedPane.getChildren().contains(scoreDisplay)) {
            this.attachedPane.getChildren().add(scoreDisplay);
        }
    }

    public void showScore(int earnedScore) {
        if (earnedScore <= 0 || scoreDisplay == null) {
            return;
        }
        this.scoreDisplayActive = true;
        this.scoreDisplayEndNanos = System.nanoTime() + 1_000_000_000L;
        this.scoreDisplay.setText("+" + earnedScore);
        this.scoreDisplay.setVisible(true);
        this.sprite.setVisible(false);
        updateScoreDisplay();
    }

    private void endScoreDisplay() {
        this.scoreDisplayActive = false;
        if (scoreDisplay != null) {
            scoreDisplay.setVisible(false);
        }
        this.sprite.setVisible(true);
    }

    private void updateScoreDisplay() {
        if (scoreDisplay == null) {
            return;
        }
        scoreDisplay.setLayoutX(x - 24);
        scoreDisplay.setLayoutY(y - 18);
    }

    private void applyGhostBehaviorState() {
        if (currentState == GhostState.EATEN) {
            return;
        }

        if (targetPlayer != null && "POWER_UP".equals(targetPlayer.state)) {
            if (currentState != GhostState.FRIGHTENED && !already_frightened) {
                already_frightened = true;
                setCurrentState(GhostState.FRIGHTENED);
            }
            return;
        }
        else already_frightened = false;

        if (currentState == GhostState.FRIGHTENED) {
            startScatterPhase();
        }
    }

    private void updatePhaseTimer() {
        if (currentState == GhostState.FRIGHTENED || currentState == GhostState.EATEN) {
            return;
        }

        double elapsedSeconds = (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0;

        if (currentState == GhostState.CHASE && elapsedSeconds >= chaseDurationSeconds) {
            startScatterPhase();
            return;
        }

        if (currentState == GhostState.SCATTER && elapsedSeconds >= SCATTER_DURATION_SECONDS) {
            startChasePhase(CHASE_DURATION_AFTER_SCATTER_SECONDS);
        }
    }

    private void startChasePhase(double durationSeconds) {
        System.out.println("Chase Phase");
        this.currentState = GhostState.CHASE;
        this.chaseDurationSeconds = durationSeconds;
        this.phaseStartNanos = System.nanoTime();
    }

    private void startScatterPhase() {
        System.out.println("Scatter Phase");
        this.currentState = GhostState.SCATTER;
        this.phaseStartNanos = System.nanoTime();
    }

    private boolean isAtCenterOfTile() {
        // 1. Calculate the exact, absolute center pixel of the ghost's current grid coordinate
        double exactCenterX = (getGridX() * 20) + 10;
        double exactCenterY = (getGridY() * 20) + 10;

        // 2. Find out exactly how far the ghost is from that center pixel
        double distX = Math.abs(this.x - exactCenterX);
        double distY = Math.abs(this.y - exactCenterY);

        // 3. Check if the ghost is close enough to snap. 
        // Use strictly less than (<) to prevent vibrating/double-triggering!
        return distX < speed && distY < speed;
    }

    private void updateTarget() {
        int[] target;
        switch (currentState) {
            case SCATTER:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
                speed = this.baseSpeed;
                break;
            case CHASE:
                target = getChaseTarget();
                speed = this.baseSpeed;
                break;
            case FRIGHTENED:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
                speed = this.baseSpeed;
                break;
            case EATEN:
                target = TargetingStrategy.getGhostHouseTarget(map.getCols(), map.getRows());
                speed = this.baseSpeed * 2;
                break;
            default:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
                speed = this.baseSpeed;
                break;
        }

        this.targetX = target[0];
        this.targetY = target[1];
    }

    private int[] getChaseTarget() {
        if (targetPlayer == null || targetPlayer.sprite == null) {
            return TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
        }

        return TargetingStrategy.getTargetTile(
                map,
                this,
                targetPlayer,
                null,
                ghostName,
                GhostState.CHASE,
                targetPlayer.direction
        );
    }

    private void chooseNextDirection() {
        // Allow reverse if EATEN -OR- if they are inside the ghost house trying to leave!
        boolean allowReverse = currentState == GhostState.EATEN || isInsideSpawnArea(getGridX(), getGridY());
        this.direction = BFSPathfinder.getNextDirection(
                map,
                this,
                getGridX(),
                getGridY(),
                this.targetX,
                this.targetY,
                direction,
                allowReverse
        );
    }

    private void move() {
        if (direction == 0) y -= speed;
        else if (direction == 1) x += speed;
        else if (direction == 2) y += speed;
        else if (direction == 3) x -= speed;
    }

    private void updateVisuals() {
        this.sprite.setCenterX(x);
        this.sprite.setCenterY(y);

        if (this.currentState == GhostState.FRIGHTENED) {
            this.sprite.setFill(Color.BLUE);
        } else if (this.currentState == GhostState.EATEN) {
            this.sprite.setRadius(5);
        } else {
            this.sprite.setFill(baseColor);
            this.sprite.setRadius(15);
        }
    }

    public int getGridX() {
        return TargetingStrategy.pixelToGrid(x);
    }

    public int getGridY() {
        return TargetingStrategy.pixelToGrid(y);
    }

    private static double snapToGrid(double pixel) {
        int grid = TargetingStrategy.pixelToGrid(pixel);
        return TargetingStrategy.gridToPixel(grid);
    }

    public double getBaseSpeed() {
        return this.baseSpeed;
    }

    public boolean isAtHouseTile() {
        return getGridX() == HOUSE_CENTER_X && getGridY() == HOUSE_CENTER_Y;
    }

    public boolean isInsideSpawnArea(int gridX, int gridY) {
        return gridX >= SPAWN_MIN_X
                && gridX <= SPAWN_MAX_X
                && gridY >= SPAWN_MIN_Y
                && gridY <= SPAWN_MAX_Y;
    }

    public boolean canOccupyTile(int gridX, int gridY) {
        if (currentState == GhostState.EATEN) {
            return true;
        }
        return !isInsideSpawnArea(gridX, gridY);
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }

    public void setCurrentState(GhostState state) {
        this.currentState = state;
        this.phaseStartNanos = System.nanoTime();

        if (state == GhostState.CHASE) {
            this.chaseDurationSeconds = CHASE_DURATION_AFTER_SCATTER_SECONDS;
        }
    }

    public void collidePlayer(Player player) {
        if (player.state.equals("POWER_UP") && this.currentState == GhostState.FRIGHTENED) {
            showScore(player.getLastGhostScoreAwarded());
            setCurrentState(GhostState.EATEN);
        }
        else return;
    }

    @Override
    public void render() {}
}