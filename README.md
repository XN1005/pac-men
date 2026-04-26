# pac-men
This is the git repository for the project's development.

## Main program
Main.java starts the game.

## Game characters
Characters (Players and Ghosts) and objects (Pellet, Cherry, etc.) are called entities. They are written in src/pacmen/entities and ghostai.
Entities inherit the base abstract class Entity.

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
 - Calls MapLoader to load map data to the created map:
    ```java
    MapLoader.loadMap(gameMap, "resources\\maps\\level1.txt");
    ```

## Other properties
Other properties like scene or userinterface can be found inside src/pacmen.
