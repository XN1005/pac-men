package pacmen.entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pacmen.map.GameMap;

public class Ghost extends Entity {
    // States
    public enum GhostState { SCATTER, CHASE, FRIGHTENED, EATEN }
    
    private final double baseSpeed;     // default speed, set later
    private GhostState currentState;
    private Color baseColor;
    private int targetX, targetY; // Target tile in grid coordinates
    private GameMap map;
    public Circle sprite;

    public Ghost(GameMap map, double x, double y, double speed, Color color) {
        super(x, y, speed);
        this.baseSpeed = speed;
        this.map = map;
        this.baseColor = color;
        this.currentState = GhostState.SCATTER;
        this.direction = 1; // Default starting direction (Right)
        this.sprite = new Circle(15, color);
        this.sprite.setCenterX(x);
        this.sprite.setCenterY(y);
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
        return Math.abs((x - 10) % 20) < speed && Math.abs((y - 10) % 20) < speed;
    }

    private void updateTarget() {
        switch (currentState) {
            case SCATTER:
                // Each ghost should have a unique corner
                this.targetX = 0; 
                this.targetY = 0;
                break;
            case CHASE:
                // TODO: Chase closest player (BFS)
                break;
            case FRIGHTENED:
                // Handles random turns, no chasing
                break;
            case EATEN:
                // BFS to find path back to ghost house
                this.targetX = 14; 
                this.targetY = 14;
                break;
        }
    }

    private void chooseNextDirection() {
        if (currentState == GhostState.EATEN) {
            // TODO: direction = BFSPackage.getNextStep(currentPos, targetPos);
            return;
        }

        double minDistance = Double.MAX_VALUE;
        int bestDir = direction;

        // Check all 4 directions: 0=Up, 1=Right, 2=Down, 3=Left
        for (int i = 0; i < 4; i++) {
            // cannot turn 180 deg except in FRIGHTEN state
            if (i == (direction + 2) % 4) continue;

            int nextCol = getGridX();
            int nextRow = getGridY();

            if (i == 0) nextRow--;
            else if (i == 1) nextCol++;
            else if (i == 2) nextRow++;
            else if (i == 3) nextCol--;

            if (map.isMoveValid(this, nextCol, nextRow)) {
                double dist = calculateDistance(nextCol, nextRow, targetX, targetY);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestDir = i;
                }
            }
        }
        this.direction = bestDir;
    }

    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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
        
        if (currentState == GhostState.FRIGHTENED) {
            this.sprite.setFill(Color.BLUE);
        } else if (currentState == GhostState.EATEN) {
            this.sprite.setRadius(5); // Just eyes
        } else {
            this.sprite.setFill(baseColor);
            this.sprite.setRadius(15);
        }
    }

    // Helper to get logical grid coordinates
    private int getGridX() { return (int) Math.round((x - 10) / 20.0); }
    private int getGridY() { return (int) Math.round((y - 10) / 20.0); }

    // methods for TunnelCell.java (reduce speed to 50%)
    public double getBaseSpeed() {
        return this.baseSpeed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public void render() {}
}