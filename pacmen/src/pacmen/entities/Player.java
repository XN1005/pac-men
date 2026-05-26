package pacmen.entities;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.Set;
import javafx.scene.shape.Circle;
import pacmen.behaviorcomponents.Collision;
import pacmen.map.GameMap;

public class Player extends Entity implements Collision {
    public Circle sprite;
    public int score;
    public int num;
    public String state;
    public int combo = 0;   // start combo = 0
    private GameMap map;
    public int currentCol;
    public int currentRow;

    public double powerUpTime;

    private Set<KeyCode> keysPressed;

    public void setInput(Set<KeyCode> keysPressed) {
        this.keysPressed = keysPressed;
    }
    
    // initialize player
    public Player(GameMap map, double speed, int num, int col, int row) throws Exception {
        /** x-coordinates for players:
         * player num 1: x = 180
         * player num 3: x = 360
         * (basically different spawn points)
         */
        super((col * 20) + 10, (row * 20) + 10, speed);
    
        this.num = num;
        this.score = 0;
        this.state = "ACTIVE";
        this.combo = 0;
        this.powerUpTime = 0;
        this.map = map;
        this.direction = 1;
        this.currentCol = col;
        this.currentRow = row;

        
        if (this.num == 1) {
            this.sprite = new Circle(15, Color.YELLOW);
            this.sprite.setCenterX((col * 20) + 10); // 9 for multi, 14 for single
            this.sprite.setCenterY((row * 20) + 10); // 17 for multi and single
        }
        else if (this.num == 2) {
            this.sprite = new Circle(15, Color.RED);
            this.sprite.setCenterX((col * 20) + 10); // 18 for multi
            this.sprite.setCenterY((row * 20) + 10); // 17 for multi
        }
        else {
            throw new Exception();
        }
          
    }

    @Override
    public void update() {
        // check if movement is allowed
        if (this.state.equals("DISABLED") || this.state.equals("DEAD")) {
            return;     // no movements
        }

        if (this.state.equals("POWER_UP")) {
            long currentTime = System.nanoTime();
            double elapsedSeconds = (currentTime - this.powerUpTime) / 1_000_000_000.0;
            
            if (elapsedSeconds >= 10.0) {
                this.state = "ACTIVE";
                this.combo = 0; // Reset kill combo after power-up expires
        }
        }
        // set player movement
        // CELL_SIZE = 20
        // 1. Get current logical position
        this.currentCol = (int) Math.round((x - 10) / 20.0);
        this.currentRow = (int) Math.round((y - 10) / 20.0);

        // 2. Determine requested direction
        char dir = requestMovementDirection();
        this.direction = toDirection(dir);
        
        // 3. Predict the NEXT grid cell
        int targetCol = this.currentCol;
        int targetRow = this.currentRow;

        if (dir == 'l' || dir == 'r') {
            targetCol = calculateNewColPos(x, speed, dir);
        } else if (dir == 'u' || dir == 'd') {
            targetRow = calculateNewRowPos(y, speed, dir);
        }



        // 4. Validate with Map and Move
        if (this.map.isMoveValid(this, targetCol, targetRow)) {
            if (dir == 'r') x += speed;
            if (dir == 'l') x -= speed;
            
            if (dir == 'd') y += speed;
            if (dir == 'u') y -= speed;

            // Update the visual sprite
            this.sprite.setCenterX(x);
            this.sprite.setCenterY(y);

            if (dir == 'r' || dir == 'l') this.sprite.setCenterY((currentRow * 20) + 10);
            if (dir == 'u' || dir == 'd') this.sprite.setCenterX((currentCol * 20) + 10);
            if (dir == 'x') {
                x = (currentCol * 20) + 10;
                y = (currentRow * 20) + 10;
                this.sprite.setCenterX((currentCol * 20) + 10);
                this.sprite.setCenterY((currentRow * 20) + 10);
            }
            
            // 5. Trigger interactions (Pellets, etc.)
            map.getCell(targetCol, targetRow).onSteppedOn(this);
        }
    }
    
    // helper function for update(), determine users' requested movement direction
    private char requestMovementDirection() {
        if (this.num == 1) {
            if (keysPressed.contains(KeyCode.W) || keysPressed.contains(KeyCode.S)) {
                if (keysPressed.contains(KeyCode.W)) {
                    return 'u';
                }
                if (keysPressed.contains(KeyCode.S)) {
                    return 'd';
                }
            }
            else {
                if (keysPressed.contains(KeyCode.A)) {
                    return 'l';
                }
                if (keysPressed.contains(KeyCode.D)) {
                    return 'r';
                }
            }
        }
        else if (this.num == 2) {
            if (keysPressed.contains(KeyCode.UP) || keysPressed.contains(KeyCode.DOWN)) {
                if (keysPressed.contains(KeyCode.UP)) {
                    return 'u';
                }
                if (keysPressed.contains(KeyCode.DOWN)) {
                    return 'd';
                }      
            }
            else {
                if (keysPressed.contains(KeyCode.LEFT)) {
                    return 'l';
                }            
                if (keysPressed.contains(KeyCode.RIGHT)) {
                    return 'r';
                }
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
    
    @Override
    public void render() {
        // draw player
    }

    // ----- COLLISIONS -------------------------------------------
    public void consumePellet() {
        this.score += 10;
    }
    public void consumePowerPellet() {
        this.score += 50;
        this.state = "POWER_UP"; // Power up State
        this.powerUpTime = System.nanoTime();
    }
    public void collideCherry() {
        this.score += 5;
    }

    @Override
    public void collideGhost() {
        if (!this.state.equals("POWER_UP")) {
            this.state = "DEAD";
        }
    }

    public void collideGhost(Ghost ghost) {
        if (this.state.equals("POWER_UP")) {
            if (ghost.currentState != Ghost.GhostState.EATEN) {
                ghost.already_eaten = true;
                this.score += 200 * (int) (Math.pow(2, this.combo));    // stated requirement
                this.combo++;
            }
        } else {
            this.state = "DEAD";
        }
    }
    public void collidePlayer() {
        // acts as a wall? cannot bypass each other (can trap, block paths, ...)
        // No, players can cross each other
        // TODO: complete method
    }

    // ----- 3. STATES -------------------------------------------
    public void powerUp() {
        // Turn on power up mode for a set amount of time, changing collision interactions
    }

    public void disable() {
        // Disabled for before game play; Disable all movements
        this.state = "DISABLED";
    }

    public void enable() {
        // enables movement
        this.state = "ACTIVE";
    }

    public void die() {
        this.state = "DEAD";
        // TODO: Run Animation
    }
}
