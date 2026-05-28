package pacmen.entities;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Wall extends Entity {
    public Rectangle sprite;

    public Wall(double x, double y) {
        // Walls don't move
        super(x, y, 0);
        // Size to 10 then connect later
        this.sprite = new Rectangle(10, 10, Color.BLUE);
        this.sprite.setX(x);
        this.sprite.setY(y);
    }

    @Override
    public void update() {}

    @Override
    public void render() {}
}