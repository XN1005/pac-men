package pacmen.entities;

import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.util.Set;
import pacmen.behaviorcomponents.Collision;
import pacmen.map.GameMap;

public class Player extends Entity implements Collision {
    
    // LOAD SPRITE
    public ImageView sprite;
    private Image[][] directionalFrames = new Image[4][3];
    
    // CONSTANTS
    private int frameTick = 0;
    private int currentFrameIndex = 0;
    // ANIMATION FRAME SEQUENCE
    private static final int[] ANIMATION_SEQUENCE = {0, 1, 2, 1}; 

    // STATES AND PROPERTIES
    public int score;
    public int num;
    private String name;
    public String state;
    public int combo = 0;
    private int lastGhostScoreAwarded = 0;
    private GameMap map;
    public int currentCol;
    public int currentRow;

    public double powerUpTime;
    private Set<KeyCode> keysPressed;

    public void setInput(Set<KeyCode> keysPressed) {
        this.keysPressed = keysPressed;
    }
    
    public Player(GameMap map, double speed, int num, int col, int row, String name) throws Exception {
        super((col * 20) + 10, (row * 20) + 10, speed);
    
        this.num = num;
        this.score = 0;
        
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        } else {
            this.name = "Player" + num;
        }

        this.state = "ACTIVE";
        this.combo = 0;
        this.powerUpTime = 0;
        this.map = map;
        this.direction = 1; // Start with facing Right
        this.currentCol = col;
        this.currentRow = row;

        // Preload all directional assets into directionalFrames
        String[] dirFolders = {"up", "right", "down", "left"};
        String baseCharacterFolder = "pacman" + this.num; // Choose pacman folder for sprite loading

        for (int d = 0; d < 4; d++) {
            String fullPathFolder = baseCharacterFolder + "-" + dirFolders[d];
            for (int f = 0; f < 3; f++) {
                File file = new File("resources/assets/" + fullPathFolder + "/" + (f + 1) + ".png");
                if (!file.exists()) {
                    System.err.println("CRITICAL ANIMATION ERROR: Missing asset file at: " + file.getAbsolutePath());
                }
                directionalFrames[d][f] = new Image(file.toURI().toString());
            }
        }

        // Initialize sprite viewport
        this.sprite = new ImageView(directionalFrames[1][0]);
        this.sprite.setFitWidth(30);
        this.sprite.setFitHeight(30);
        
        updateVisuals();
    }

    public String getName() { return this.name; }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    @Override
    public void update() {
        if (this.state.equals("DISABLED") || this.state.equals("DEAD")) {
            return;
        }

        if (this.state.equals("POWER_UP")) {
            long currentTime = System.nanoTime();
            double elapsedSeconds = (currentTime - this.powerUpTime) / 1_000_000_000.0;
            if (elapsedSeconds >= 7.0) {
                this.state = "ACTIVE";
                this.combo = 0; 
            }
        }
        
        this.currentCol = (int) Math.round((x - 10) / 20.0);
        this.currentRow = (int) Math.round((y - 10) / 20.0);

        char dir = requestMovementDirection();
        this.direction = toDirection(dir);
        
        int targetCol = this.currentCol;
        int targetRow = this.currentRow;

        if (dir == 'l' || dir == 'r') targetCol = calculateNewColPos(x, speed, dir);
        else if (dir == 'u' || dir == 'd') targetRow = calculateNewRowPos(y, speed, dir);

        // Validate and Move
        if (this.map.isMoveValid(this, targetCol, targetRow)) {
            if (dir == 'r') x += speed;
            if (dir == 'l') x -= speed;
            if (dir == 'd') y += speed;
            if (dir == 'u') y -= speed;

            if (dir == 'r' || dir == 'l') y = (currentRow * 20) + 10;
            if (dir == 'u' || dir == 'd') x = (currentCol * 20) + 10;
            if (dir == 'x') {
                x = (currentCol * 20) + 10;
                y = (currentRow * 20) + 10;
            }
            
            map.getCell(targetCol, targetRow).onSteppedOn(this);
        }
        
        // Render
        updateVisuals();
    }
    
    private void updateVisuals() {
        // Repositioning
        this.sprite.setX(x - 15);
        this.sprite.setY(y - 15);

        this.sprite.setRotate(0);

        if (direction >= 0 && direction <= 3) {
            frameTick++;
            if (frameTick % 4 == 0) {
                currentFrameIndex = (currentFrameIndex + 1) % ANIMATION_SEQUENCE.length;
            }
            
            int activeFrameNumber = ANIMATION_SEQUENCE[currentFrameIndex];
            this.sprite.setImage(directionalFrames[direction][activeFrameNumber]);
        }
    }

    private char requestMovementDirection() {
        if (this.num == 1) {
            if (keysPressed.contains(KeyCode.W) || keysPressed.contains(KeyCode.S)) {
                if (keysPressed.contains(KeyCode.W)) return 'u';
                if (keysPressed.contains(KeyCode.S)) return 'd';
            } else {
                if (keysPressed.contains(KeyCode.A)) return 'l';
                if (keysPressed.contains(KeyCode.D)) return 'r';
            }
        } else if (this.num == 2) {
            if (keysPressed.contains(KeyCode.UP) || keysPressed.contains(KeyCode.DOWN)) {
                if (keysPressed.contains(KeyCode.UP)) return 'u';
                if (keysPressed.contains(KeyCode.DOWN)) return 'd';      
            } else {
                if (keysPressed.contains(KeyCode.LEFT)) return 'l';            
                if (keysPressed.contains(KeyCode.RIGHT)) return 'r';
            }
        }
        return 'x';
    }
    
    private int calculateNewColPos(double currentX, double speed, char dir) {
        double predictedX = (dir == 'r') ? currentX + speed : currentX - speed;
        return (int) Math.round((predictedX - 10) / 20.0);
    }

    private int calculateNewRowPos(double currentY, double speed, char dir) {
        double predictedY = (dir == 'd') ? currentY + speed : currentY - speed;
        return (int) Math.round((predictedY - 10) / 20.0);
    }

    private int toDirection(char dir) {
        if (dir == 'u') return 0;
        if (dir == 'r') return 1;
        if (dir == 'd') return 2;
        if (dir == 'l') return 3;
        return this.direction;
    }
    
    @Override public void render() {}
    @Override public void consumePellet() { this.score += 10; }
    
    @Override public void consumePowerPellet() {
        this.score += 50;
        this.state = "POWER_UP";
        this.powerUpTime = System.nanoTime();
    }
    
    @Override public void collideCherry() { this.score += 5; }
    
    @Override public void collideGhost() {
        if (!this.state.equals("POWER_UP")) {
            this.state = "DEAD";
        }
    }

    public void resetPosition(int col, int row) {
        this.currentCol = col;
        this.currentRow = row;
        this.x = (col * 20) + 10;
        this.y = (row * 20) + 10;
        this.direction = 1;
        this.state = "ACTIVE";
        this.sprite.setVisible(true);
        this.sprite.setOpacity(1.0);
        updateVisuals();
    }

    public int getLastGhostScoreAwarded() {
        return this.lastGhostScoreAwarded;
    }

    public void pausePowerUpTimer(long pauseNanos) {
        if (this.state.equals("POWER_UP")) {
            this.powerUpTime -= pauseNanos;
        }
    }

    public boolean collideGhost(Ghost ghost) {
        this.lastGhostScoreAwarded = 0;
        if (this.state.equals("POWER_UP")) {
            if (ghost.currentState == Ghost.GhostState.FRIGHTENED) {
                ghost.already_eaten = true;
                this.lastGhostScoreAwarded = 200 * (int) (Math.pow(2, this.combo));
                this.score += this.lastGhostScoreAwarded;
                if (this.combo < 2) this.combo++; // MAX COMBO: 0 -> 3
                return true;
            } else if (ghost.currentState == Ghost.GhostState.EATEN) {
                return false;
            }
            this.state = "DEAD";
            return false;
        }
        if (ghost.currentState == Ghost.GhostState.FRIGHTENED || ghost.currentState == Ghost.GhostState.EATEN) return false;
        this.state = "DEAD";
        return false;
    }
    
    public void collidePlayer() {}
    public void powerUp() {}
    public void disable() { this.state = "DISABLED"; }
    public void enable() { this.state = "ACTIVE"; }
    public void die() { 
        this.state = "DEAD"; 
        this.sprite.setVisible(false);
    }
}