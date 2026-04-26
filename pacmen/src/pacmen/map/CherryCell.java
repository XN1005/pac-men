package pacmen.map;

import pacmen.entities.Entity;
import pacmen.entities.Player;
import pacmen.entities.Cherry;

public class CherryCell implements Cell {
    private Cherry cherry;

    public CherryCell(Cherry cherry) {
        this.cherry = cherry;
    }

    @Override
    public boolean canPass(Entity entity) {
        return true;
    }

    @Override
    public void onSteppedOn(Entity entity) {
        if (entity instanceof Player && cherry.state.equals("ACTIVE")) {
            Player p = (Player) entity;
            p.collideCherry(); // +5 points
            cherry.collidePlayer(); // Deactivate cherry
        }
    }
}