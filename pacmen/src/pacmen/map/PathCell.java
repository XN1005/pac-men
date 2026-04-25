package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Ghost;

public class PathCell implements Cell {
    @Override
    public boolean canPass(Entity entity) {
        return true;    // can always pass a normal cell
    }

    @Override
    public void onSteppedOn(Entity entity) {
        if (entity instanceof Ghost) {
            Ghost g = (Ghost) entity;
            g.setSpeed(g.getBaseSpeed());
        }
    }
}
