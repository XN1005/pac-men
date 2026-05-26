package pacmen.map;

import pacmen.entities.Entity;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class WallCell implements Cell {
    private final Group sprite;
    private final Rectangle center;
    private final Rectangle leftConnection;
    private final Rectangle rightConnection;
    private final Rectangle upConnection;
    private final Rectangle downConnection;

    public WallCell(double px, double py) {
        this.sprite = new Group();

        this.center = new Rectangle(8, 8, Color.BLUE);
        this.center.setX(px - 4);
        this.center.setY(py - 4);
        this.center.setStroke(Color.DARKBLUE);

        this.leftConnection = createConnectionRect(px - 15, py - 3, 10, 6);
        this.rightConnection = createConnectionRect(px + 5, py - 3, 10, 6);
        this.upConnection = createConnectionRect(px - 3, py - 15, 6, 10);
        this.downConnection = createConnectionRect(px - 3, py + 5, 6, 10);

        setConnections(false, false, false, false);
        this.sprite.getChildren().addAll(leftConnection, rightConnection, upConnection, downConnection, center);
    }

    private Rectangle createConnectionRect(double x, double y, double width, double height) {
        Rectangle rect = new Rectangle(width, height, Color.BLUE);
        rect.setX(x);
        rect.setY(y);
        rect.setStroke(Color.DARKBLUE);
        rect.setVisible(false);
        return rect;
    }

    @Override
    public boolean canPass(Entity entity) {
        return false;   // cannot pass a wall cell
    }

    @Override
    public void onSteppedOn(Entity entity) {
        // nothing happens when an entity touches a wall
    }

    public Node getSprite() {
        return sprite;
    }

    public void setConnections(boolean left, boolean right, boolean up, boolean down) {
        leftConnection.setVisible(left);
        rightConnection.setVisible(right);
        upConnection.setVisible(up);
        downConnection.setVisible(down);
    }
}