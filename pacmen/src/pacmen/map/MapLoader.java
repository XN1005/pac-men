package pacmen.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import pacmen.entities.Pellet;
import pacmen.entities.PowerPellet;
import pacmen.entities.Cherry;

public class MapLoader {
    private static final int CELL_SIZE = 20;

    public static void loadMap(GameMap gameMap, String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < 36) {
                for (int col = 0; col < line.length() && col < 28; col++) {
                    char symbol = line.charAt(col);
                    
                    // Calculate pixel center for sprites
                    double pixelX = col * CELL_SIZE + (CELL_SIZE / 2.0);
                    double pixelY = row * CELL_SIZE + (CELL_SIZE / 2.0);

                    // Create the appropriate cell based on the symbol
                    Cell cell = createCell(symbol, pixelX, pixelY);
                    gameMap.setCell(col, row, cell);
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("Error loading map file: " + e.getMessage());
            // Fallback: fill with walls or paths so the game doesn't crash
        }
    }

    private static Cell createCell(char symbol, double px, double py) {
        switch (symbol) {
            case '#': 
                return new WallCell();
            case '.': 
                return new PelletCell(new Pellet(px, py));
            case '*': 
                return new PelletCell(new PowerPellet(px, py));
            case 'C': 
                return new CherryCell(new Cherry(px, py));
            case 'T': 
                return new TunnelCell();
            case '-': 
                return new GhostHouseDoorCell();
            case ' ': 
            case '_': 
                return new PathCell();
            default:  
                return new PathCell(); // Default to path for unknown symbols
        }
    }
}