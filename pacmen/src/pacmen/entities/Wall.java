package pacmen.entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Wall extends Entity {
    public Rectangle sprite;

    public Wall(double x, double y) {
        // Walls don't move, so speed is 0
        super(x, y, 0);
        // CELL_SIZE is 20
        this.sprite = new Rectangle(20, 20, Color.BLUE);
        this.sprite.setX(x);
        this.sprite.setY(y);
    }

    @Override
    public void update() {
        // Walls are static; nothing to update
    }

    @Override
    public void render() {
        // JavaFX draws the rectangle
    }
}