package pacMen.entities;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.Set;
import javafx.scene.shape.Circle;
import pacMen.behaviorComponents.Collision;

public class Player extends abstractBase_Entity implements Collision {
    public Circle sprite;
    public int score;
    
    public double speed;
    public int num;
    public String state;
    public int combo;

    public int x, y;

    public double powerUpTime;

    private Set<KeyCode> keysPressed;

    public void setInput(Set<KeyCode> keysPressed) {
        this.keysPressed = keysPressed;
    }
    
    public Player(double speed, int num) throws Exception {
        this.speed = speed;
        this.num = num;
        this.score = 0;
        this.state = "ACTIVE";
        this.combo = 0;
        this.powerUpTime = 0;
        

        if (this.num == 1) {
            this.sprite = new Circle(15, Color.YELLOW);
            this.sprite.setCenterX(300);
            this.sprite.setCenterY(300);  
        }
        else if (this.num == 2) {
            this.sprite = new Circle(15, Color.RED);
            this.sprite.setCenterX(500);
            this.sprite.setCenterY(300);  
        }
        else {
            throw new Exception();
        }
          
    }

    @Override
    public void update() {
        // ----- 1. MOVEMENTS -------------------------------------------
        // Set player movement; Restrict movement to either Vertical or Horizontal (no Diagonal)
        // Placeholder Commands, will fix accordingly to conform to 2D Arrays
        if (this.num == 1) {
            if (keysPressed.contains(KeyCode.W) || keysPressed.contains(KeyCode.S)) {
                if (keysPressed.contains(KeyCode.W)) {
                    this.sprite.setCenterY(this.sprite.getCenterY() - this.speed);
                }
                if (keysPressed.contains(KeyCode.S)) {
                    this.sprite.setCenterY(this.sprite.getCenterY() + this.speed);
                }      
            }
            else {
                if (keysPressed.contains(KeyCode.A)) {
                    this.sprite.setCenterX(this.sprite.getCenterX() - this.speed);
                }            
                if (keysPressed.contains(KeyCode.D)) {
                    this.sprite.setCenterX(this.sprite.getCenterX() + speed);
                }
            }
        }
        else if (this.num == 2) {
            if (keysPressed.contains(KeyCode.UP) || keysPressed.contains(KeyCode.DOWN)) {
                if (keysPressed.contains(KeyCode.UP)) {
                    this.sprite.setCenterY(this.sprite.getCenterY() - this.speed);
                }
                if (keysPressed.contains(KeyCode.DOWN)) {
                    this.sprite.setCenterY(this.sprite.getCenterY() + this.speed);
                }      
            }
            else {
                if (keysPressed.contains(KeyCode.LEFT)) {
                    this.sprite.setCenterX(this.sprite.getCenterX() - this.speed);
                }            
                if (keysPressed.contains(KeyCode.RIGHT)) {
                    this.sprite.setCenterX(this.sprite.getCenterX() + this.speed);
                }
            }
        }
    }

    @Override
    public void render() {
        // draw player
    }

    // ----- 2. COLLISIONS -------------------------------------------
    public void consumePellet() {
        this.score += 1;
    }
    public void consumePowerPellet() {
        this.score += 10;
        this.state = "POWER_UP"; // Power up State
        this.powerUpTime = System.nanoTime();
    }
    public void collideCherry() {
        this.score += 5;
    }
    public void collideGhost() {
        if (this.state.equals("POWER_UP")) {
            this.score += 30 * (Math.pow(2,combo));
        }
        else {
            this.state = "DEAD";
        }
    }
    public void collidePlayer() {
        // acts as a wall? cannot bypass each other (can trap, block paths, ...)
    }

    // ----- 3. STATES -------------------------------------------
    public void powerUp() {
        // Turn on power up mode for a set amount of time, changing collision interactions
    }

    public void disable() {
        // Disabled for before game play; Disable all movements
    }

    public void die() {
        // Run Animation
    }
}
