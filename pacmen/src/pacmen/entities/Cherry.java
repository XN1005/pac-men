package pacmen.entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pacmen.behaviorcomponents.Collision;

public class Cherry extends Entity implements Collision {
    public String state;
    public Circle sprite;

    public Cherry(double x, double y) {
        super(x, y, 0); // Static item
        this.state = "ACTIVE";
        this.sprite = new Circle(8, Color.RED);
        this.sprite.setCenterX(x);
        this.sprite.setCenterY(y);
    }

    @Override
    public void update() {} // No movement logic

    @Override
    public void render() {} // render cherry

    @Override public void consumePellet() {}
    @Override public void consumePowerPellet() {}
    @Override public void collideGhost() {}
    @Override 
    public void collidePlayer() {
        this.state = "CONSUMED";
    }
    @Override public void collideCherry() {}
}
