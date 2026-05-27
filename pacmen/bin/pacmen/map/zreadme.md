## Game map
The game map is made up of grids called Cell. The map is initiated as a 2D Array of size [36][28] (36 rows, 28 columns) of Cells, and is encapsulated as a class (public class GameMap) that has this property.

A Cell may be a PathCell (just a normal cell), a PelletCell, which contains a pellet, and so on. Cell is an interface, and PathCells, PelletCells, etc. are classes that implements Cell.

The map is stored as a text file (resources/maps/level1.txt), which is a 36x28 text block containing symbols representing different types of Cells. MapLoader.java reads the text file and assign Cells accordingly to a map.

In action: 
 - Main.java creates a new map, an instance of GameMap:
   ```java
   private GameMap gameMap;
   // some code in between
   gameMap = new GameMap();
   ```
 - Then, it calls MapLoader to load map data to the created map:
   ```java
   MapLoader.loadMap(gameMap, "resources\\maps\\level1.txt");
   ```

## Actual window
Each grid of the map is 20x20 pixels. This is set as a constant, CELL_SIZE:
   ```java
   private static final int CELL_SIZE = 20;
   ```
When the player moves, their position is calculated in pixels, not grids. This ensure a fairly continuous movement, and not discrete jumps between grids. The player's grid position is calculated separately to handle collision cases.

Since each grid is 20x20 pixels, the map size in pixels is 560x720 (28 columns, 36 rows of grids).