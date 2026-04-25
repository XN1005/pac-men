package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Player;

public class GhostHouseDoorCell implements Cell {
    // ghosts pass through these cells to enter the map
    @Override
    public boolean canPass(Entity entity) {
        if (entity instanceof Player) {
            return false;
        }
        return true;    // is a ghost
    }

    @Override
    public void onSteppedOn(Entity entity) {
        //
    }
}
