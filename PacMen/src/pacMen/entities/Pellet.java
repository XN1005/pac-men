package pacMen.entities;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import pacMen.behaviorComponents.Collision;

public class Pellet implements Collision {
    public String state;
    public Circle sprite;

    public Pellet() {
        this.state = "ACTIVE";
        this.sprite = new Circle(5, Color.BEIGE);
        this.sprite.setCenterX(100);
        this.sprite.setCenterY(100); 
    }

    @Override
    public void consumePellet() {
        return;
    }

    @Override
    public void consumePowerPellet() {
        return;
    }

    @Override
    public void collideCherry() {
        return;
    }

    @Override
    public void collideGhost() {
        return;
    }

    @Override
    public void collidePlayer() {
        state = "CONSUMED";
    }

}
