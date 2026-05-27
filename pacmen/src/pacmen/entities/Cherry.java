package pacmen.entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import pacmen.behaviorcomponents.Collision;

public class Cherry extends Entity implements Collision {
    public String state;
    
    public ImageView sprite;

    public Cherry(double x, double y) {
        super(x, y, 0); // Static item
        this.state = "ACTIVE";

        try {
            File file = new File("resources/assets/consumables/strawberry.png");
            
            if (!file.exists()) {
                System.err.println("CRITICAL ASSET ERROR: Cherry image file not found at: " + file.getAbsolutePath());
            }

            Image cherryImage = new Image(file.toURI().toString());
            this.sprite = new ImageView(cherryImage);
            
            // Explicit dimensions scaling to 20x20 pixels for the cherry
            this.sprite.setFitWidth(20);
            this.sprite.setFitHeight(20);

            // Position adjustment
            this.sprite.setX(x - 10);
            this.sprite.setY(y - 10);

        } catch (Exception e) {
            System.err.println("Failed to initialize Cherry graphical rendering node context:");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {} // No movement logic

    @Override
    public void render() {} // Render cherry template handler

    @Override public void consumePellet() {}
    @Override public void consumePowerPellet() {}
    @Override public void collideGhost() {}
    
    @Override 
    public void collidePlayer() {
        this.state = "CONSUMED";
        if (this.sprite != null) {
            this.sprite.setVisible(false); // Hide the cherry image texture node surface when consumed
        }
    }
    
    @Override public void collideCherry() {}
}