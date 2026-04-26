package pacmen.map;

import pacmen.entities.Entity;

public class GameMap {
    private static final int CELL_SIZE = 20;    
    private static final int COLS = 28;         // x-coordinate
    private static final int ROWS = 36;         // y-coordinate

    private Cell[][] grid;

    public GameMap() {
        this.grid = new Cell[ROWS][COLS];
        initializeMap();
    }

    // fill grid with default PathCells
    private void initializeMap() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                grid[y][x] = new PathCell();
            }
        }
    }

    private boolean isValidCoordinates(int x, int y) {
        return (x >= 0 && x < COLS) && (y >= 0 && y < ROWS);
    }
    
    public Cell getCell(int x, int y) {
        if (!isValidCoordinates(x, y)) {
            return new WallCell();
        }
        return grid[y][x];  // row y, column x
    }

    public void setCell(int x, int y, Cell newCell) {
        if (isValidCoordinates(x,y)) {
            grid[y][x] = newCell;
        }
    }

    public boolean isMoveValid(Entity entity, int targetX, int targetY) {
        Cell currentCell = getCell(targetX, targetY);
        return currentCell.canPass(entity);
    }

    // getters for map dimensions for rendering logic
    public int getCols() { return COLS; }
    public int getRows() { return ROWS; }
    public int getCellSize() { return CELL_SIZE; }
}
