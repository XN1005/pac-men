package pacmen.entities;
import pacmen.ghostai.*;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pacmen.map.GameMap;

public class Ghost extends Entity {
    // States
    public enum GhostState { SCATTER, CHASE, FRIGHTENED, EATEN }
    
    private final double baseSpeed;     // default speed, set later
    public  GhostState currentState;    // Public for player accessibility
    private Color baseColor;
    private int targetX, targetY;
    private final GameMap map;
    private final String ghostName;
    private Player targetPlayer;
    public Circle sprite;
    boolean already_eaten;

    public Ghost(GameMap map, double x, double y, double speed, Color color) {
        this(map, x, y, speed, color, null, "blinky");
        this.already_eaten = false;
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

        switch (currentState) {
            case SCATTER:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
                break;
            case CHASE:
                target = getChaseTarget();
                break;
            case FRIGHTENED:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
                break;
            case EATEN:
                target = TargetingStrategy.getGhostHouseTarget(map.getCols(), map.getRows());
                break;
            default:
                target = TargetingStrategy.getScatterTarget(ghostName, map.getCols(), map.getRows());
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
        boolean allowReverse = currentState == GhostState.EATEN;
        this.direction = BFSPathfinder.getNextDirection(
                map,
                this,
                getGridX(),
                getGridY(),
                targetX,
                targetY,
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