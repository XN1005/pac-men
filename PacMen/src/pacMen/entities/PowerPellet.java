package pacMen.entities;

public class PowerPellet extends Pellet {
    public String state;

    PowerPellet() {
        this.state = "ACTIVE";
    }

    @Override
    public void consumePellet() {
        return;
    }

    @Override
    public void consumePowerPellet() {
        return;
    }

    @Override
    public void collideCherry() {
        return;
    }

    @Override
    public void collideGhost() {
        return;
    }

    @Override
    public void collidePlayer() {
        state = "CONSUMED";
    }
}
