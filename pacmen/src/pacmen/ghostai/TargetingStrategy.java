package pacmen.ghostai;

import pacmen.entities.Ghost;
import pacmen.entities.Player;
import pacmen.map.GameMap;

public class TargetingStrategy {

    public static int[] getTargetTile(
            GameMap map,
            Ghost ghost,
            Player player,
            Ghost blinky,
            String ghostName,
            Ghost.GhostState state,
            int playerDirection
    ) {
        if (map == null) {
            return new int[]{0, 0};
        }

        int mapCols = map.getCols();
        int mapRows = map.getRows();

        String name = cleanName(ghostName);

        if (state == Ghost.GhostState.SCATTER) {
            return getScatterTarget(name, mapCols, mapRows);
        }

        if (state == Ghost.GhostState.EATEN) {
            return getGhostHouseTarget(mapCols, mapRows);
        }

        if (state == Ghost.GhostState.FRIGHTENED) {
            return getScatterTarget(name, mapCols, mapRows);
        }

        if (player == null || player.sprite == null) {
            return getScatterTarget(name, mapCols, mapRows);
        }

        int playerX = pixelToGrid(player.sprite.getCenterX());
        int playerY = pixelToGrid(player.sprite.getCenterY());

        playerDirection = BFSPathfinder.fixDirection(playerDirection);

        if (name.contains("pinky") || name.contains("pink")) {
            return getPinkyTarget(playerX, playerY, playerDirection, mapCols, mapRows);
        }

        if (name.contains("inky") || name.contains("blue")) {
            return getInkyTarget(playerX, playerY, playerDirection, blinky, mapCols, mapRows);
        }

        if (name.contains("clyde") || name.contains("orange")) {
            int ghostX = getGhostGridX(ghost);
            int ghostY = getGhostGridY(ghost);

            return getClydeTarget(
                    ghostX,
                    ghostY,
                    playerX,
                    playerY,
                    name,
                    mapCols,
                    mapRows
            );
        }

        return new int[]{
                clamp(playerX, 0, mapCols - 1),
                clamp(playerY, 0, mapRows - 1)
        };
    }

    private static int[] getPinkyTarget(
            int playerX,
            int playerY,
            int playerDirection,
            int mapCols,
            int mapRows
    ) {
        int[] delta = getDirectionDelta(playerDirection);

        int targetX = playerX + delta[0] * 4;
        int targetY = playerY + delta[1] * 4;

        return new int[]{
                clamp(targetX, 0, mapCols - 1),
                clamp(targetY, 0, mapRows - 1)
        };
    }

    private static int[] getInkyTarget(
            int playerX,
            int playerY,
            int playerDirection,
            Ghost blinky,
            int mapCols,
            int mapRows
    ) {
        if (blinky == null || blinky.sprite == null) {
            return new int[]{
                    clamp(playerX, 0, mapCols - 1),
                    clamp(playerY, 0, mapRows - 1)
            };
        }

        int[] delta = getDirectionDelta(playerDirection);

        int twoAheadX = playerX + delta[0] * 2;
        int twoAheadY = playerY + delta[1] * 2;

        int blinkyX = pixelToGrid(blinky.sprite.getCenterX());
        int blinkyY = pixelToGrid(blinky.sprite.getCenterY());

        int vectorX = twoAheadX - blinkyX;
        int vectorY = twoAheadY - blinkyY;

        int targetX = blinkyX + vectorX * 2;
        int targetY = blinkyY + vectorY * 2;

        return new int[]{
                clamp(targetX, 0, mapCols - 1),
                clamp(targetY, 0, mapRows - 1)
        };
    }

    private static int[] getClydeTarget(
            int ghostX,
            int ghostY,
            int playerX,
            int playerY,
            String ghostName,
            int mapCols,
            int mapRows
    ) {
        int distance = BFSPathfinder.distanceSquared(ghostX, ghostY, playerX, playerY);

        if (distance > 64) {
            return new int[]{
                    clamp(playerX, 0, mapCols - 1),
                    clamp(playerY, 0, mapRows - 1)
            };
        }

        return getScatterTarget(ghostName, mapCols, mapRows);
    }

    public static int[] getScatterTarget(String ghostName, int mapCols, int mapRows) {
        String name = cleanName(ghostName);

        if (name.contains("blinky") || name.contains("red")) {
            return new int[]{mapCols - 1, 0};
        }

        if (name.contains("pinky") || name.contains("pink")) {
            return new int[]{0, 0};
        }

        if (name.contains("inky") || name.contains("blue")) {
            return new int[]{mapCols - 1, mapRows - 1};
        }

        if (name.contains("clyde") || name.contains("orange")) {
            return new int[]{0, mapRows - 1};
        }

        return new int[]{0, 0};
    }

    public static int[] getGhostHouseTarget(int mapCols, int mapRows) {
        int targetX = 14;
        int targetY = 14;

        return new int[]{
                clamp(targetX, 0, mapCols - 1),
                clamp(targetY, 0, mapRows - 1)
        };
    }

    public static int getGhostGridX(Ghost ghost) {
        if (ghost == null || ghost.sprite == null) {
            return 0;
        }

        return pixelToGrid(ghost.sprite.getCenterX());
    }

    public static int getGhostGridY(Ghost ghost) {
        if (ghost == null || ghost.sprite == null) {
            return 0;
        }

        return pixelToGrid(ghost.sprite.getCenterY());
    }

    public static int getPlayerGridX(Player player) {
        if (player == null || player.sprite == null) {
            return 0;
        }

        return pixelToGrid(player.sprite.getCenterX());
    }

    public static int getPlayerGridY(Player player) {
        if (player == null || player.sprite == null) {
            return 0;
        }

        return pixelToGrid(player.sprite.getCenterY());
    }

    public static int pixelToGrid(double pixelValue) {
        return (int) Math.round((pixelValue - 10) / 20.0);
    }

    public static double gridToPixel(int gridValue) {
        return gridValue * 20.0 + 10.0;
    }

    private static int[] getDirectionDelta(int direction) {
        direction = BFSPathfinder.fixDirection(direction);

        if (direction == BFSPathfinder.UP) {
            return new int[]{0, -1};
        }

        if (direction == BFSPathfinder.RIGHT) {
            return new int[]{1, 0};
        }

        if (direction == BFSPathfinder.DOWN) {
            return new int[]{0, 1};
        }

        return new int[]{-1, 0};
    }

    private static String cleanName(String ghostName) {
        if (ghostName == null) {
            return "";
        }

        return ghostName.toLowerCase();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
