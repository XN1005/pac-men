package pacmen.entities;
import javafx.scene.shape.Circle;
import pacmen.behaviorcomponents.Collision;
import javafx.scene.paint.Color;

public class Pellet implements Collision {
    public String state;
    public Circle sprite;

    public Pellet(double x, double y) {
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
