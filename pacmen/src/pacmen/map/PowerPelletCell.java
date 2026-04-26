package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Player;

public class PowerPelletCell implements Cell {
    @Override
    public boolean canPass(Entity entity) {
        return true;    // anything can pass
    }

    @Override
    public void onSteppedOn(Entity entity) {
        if (entity instanceof Player) {
            // change ghost to FRIGHTENED for 10 s
        }
    }
}
