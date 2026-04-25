package pacmen.entities;

public abstract class Entity {
    protected double x, y;         // Position
    protected double speed;
    protected int direction;    // 0=Up, 1=Right, 2=Down, 3=Left

    public Entity(double x, double y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    // update its logic every frame
    public abstract void update();

    public abstract void render();

    // getters for collision detection (deciding if implement or not)
}
