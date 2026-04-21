package pacMen.map;

public class GameMap {
    private int[][] grid; // Initialize 2D Array

    public boolean isWalkable(int x, int y) {
        return grid[y][x] != 1; // wall
    }

    public boolean isWall(int x, int y) {
        return false;
    }

    public boolean isPellet(int x, int y) {
        return false;
    }
    
    public void consumePellet(int x, int y) {

    }
}
