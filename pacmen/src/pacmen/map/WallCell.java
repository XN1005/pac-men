package pacmen.map;

import pacmen.entities.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class WallCell implements Cell {
    private Rectangle sprite;

    public WallCell(double px, double py) {
        // Create a 20x20 blue square for the wall
        this.sprite = new Rectangle(20, 20, Color.BLUE);
        this.sprite.setX(px - 10); // Center it (pixel - half cell size)
        this.sprite.setY(py - 10);
        // For now, use provided asset so ignore
        this.sprite.setVisible(false); // Hide the default rectangle since we have a custom image
        
        // Optional: Add a stroke to make individual blocks visible
        this.sprite.setStroke(Color.DARKBLUE);
    }

    @Override
    public boolean canPass(Entity entity) {
        return false;   // cannot pass a wall cell
    }

    @Override
    public void onSteppedOn(Entity entity) {
        // nothing happens when an entity touches a wall
    }

    public Rectangle getSprite() {
        return sprite;
    }
}