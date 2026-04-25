package pacmen.behaviorcomponents;

public interface Collision {
    public void consumePellet();
    public void consumePowerPellet();
    public void collideCherry();
    public void collideGhost();
    public void collidePlayer();
}
