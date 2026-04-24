package pacMen.map;

public class GameMap {
    private int[][] grid = new int[28][36]; // Initialize 2D Array. Map is scaled 28 x 36.
    
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
