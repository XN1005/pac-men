package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Player;
import pacmen.entities.PowerPellet;
import pacmen.entities.Pellet;

public class PelletCell implements Cell {
    private Pellet pellet;

    public PelletCell(Pellet pellet) {
        this.pellet = pellet;
    }

    @Override
    public boolean canPass(Entity entity) {
        return true;    // can pass over pellets
    }

    @Override
    public void onSteppedOn(Entity entity) {
        // Only interact if the entity is a Player and the pellet isn't eaten yet
        if (entity instanceof Player && pellet.state.equals("ACTIVE")) {
            Player p = (Player) entity;
            
            // Check if the pellet is a PowerPellet to trigger the state change
            if (this.pellet instanceof PowerPellet) {
                p.consumePowerPellet(); // + 50 points & start 10s timer
            } else {
                p.consumePellet();      // + 10 points
            }

            pellet.collidePlayer(); 
        }
    }

    public Pellet getPellet() {
        return pellet;
    }
}
