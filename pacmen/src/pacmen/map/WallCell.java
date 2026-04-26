package pacmen.map;

import pacmen.entities.Entity;

public class WallCell implements Cell {
    @Override
    public boolean canPass(Entity entity) {
        return false;   // cannot pass a wall cell
    }

    @Override
    public void onSteppedOn(Entity entity) {
        // nothing happens when an entity touches a wall
    }
}