package pacmen.entities;
import pacmen.ghostai.*;

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
    private static final int HOUSE_CENTER_X = 11;
    private static final int HOUSE_CENTER_Y = 14;

    private final double baseSpeed;     // default speed, set later
    public  GhostState currentState;    // Public for player accessibility
    private Color baseColor;
    private int targetX, targetY;
    private final GameMap map;
    private final String ghostName;
    private Player targetPlayer;
    public Circle sprite;

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
        this.currentState = GhostState.CHASE;
        this.direction = 1; // Default starting direction (Right)
        this.sprite = new Circle(15, color);
        this.sprite.setCenterX(this.x);
        this.sprite.setCenterY(this.y);
        this.targetX = getGridX();
        this.targetY = getGridY();
    }

    @Override
    public void update() {
        // change direction at the center of a tile
        if (isAtCenterOfTile()) {
            if (currentState == GhostState.EATEN && isAtHouseTile()) {
                System.out.println("Recovered");
                currentState = GhostState.SCATTER;
            }
            System.out.println("At Center");
            updateTarget();
            chooseNextDirection();
        }
        move();
        updateVisuals();
    }

    private boolean isAtCenterOfTile() {
        return Math.abs((x - 10) % 20) <= speed && Math.abs((y - 10) % 20) <= speed;
    }

    private void updateTarget() {
        int[] target;
        System.out.println("Updated Target");
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
        System.out.println(this.targetX + " " + this.targetY);
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
    }

    public void collidePlayer(Player player) {
        if (player.state.equals("POWER_UP") && this.currentState != GhostState.EATEN) {
            setCurrentState(GhostState.EATEN);
        }
        else return;
    }

    @Override
    public void render() {}
}