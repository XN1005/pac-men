package pacmen.entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PowerPellet extends Pellet {
    public PowerPellet(double x, double y) {
        super(x, y);
        this.state = "ACTIVE";
        // Power Pellets are larger
        this.sprite = new Circle(10, Color.WHITE);

        // Position it based on the map coordinates passed from MapLoader
        this.sprite.setCenterX(x);
        this.sprite.setCenterY(y);
    }

    // no action for these methods
    @Override public void consumePellet() {}
    @Override public void consumePowerPellet() {}
    @Override public void collideCherry() {}
    @Override public void collideGhost() {}

    @Override
    public void collidePlayer() {
        // collide player => CONSUMED
        this.state = "CONSUMED";
        this.sprite.setVisible(false); // Hide it from the screen
    }
}