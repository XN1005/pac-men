package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Ghost;

public class TunnelCell implements Cell {
    // ghosts moves at 50% speed when passing through
    // these cells
    @Override
    public boolean canPass(Entity entity) {
        return true;    // anything can pass
    }

    @Override
    public void onSteppedOn(Entity entity) {
        if (entity instanceof Ghost) {
            // reduce speed to 50%, implement ghost first
            Ghost g = (Ghost) entity;
            g.setSpeed(g.getBaseSpeed() * 0.5);
        }
        
    }
}
