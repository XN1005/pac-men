package ghost.java;

public class BehaviorLogic {

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public static final int CHASE = 0;
    public static final int SCATTER = 1;
    public static final int FRIGHTENED = 2;
    public static final int EATEN = 3;

    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    public static int[] getTargetTile(
            String ghostName,
            int ghostGridX,
            int ghostGridY,
            int playerGridX,
            int playerGridY,
            int playerDirection,
            int blinkyGridX,
            int blinkyGridY,
            int mapCols,
            int mapRows,
            int mode
    ) {
        playerDirection = fixDirection(playerDirection);

        if (mode == EATEN) {
            return new int[]{14, 14};
        }

        if (mode == SCATTER || mode == FRIGHTENED) {
            return getScatterTarget(ghostName, mapCols, mapRows);
        }

        String name = cleanName(ghostName);

        if (name.contains("pinky") || name.contains("pink")) {
            return getPinkyTarget(playerGridX, playerGridY, playerDirection, mapCols, mapRows);
        }

        if (name.contains("inky") || name.contains("blue")) {
            return getInkyTarget(
                    playerGridX,
                    playerGridY,
                    playerDirection,
                    blinkyGridX,
                    blinkyGridY,
                    mapCols,
                    mapRows
            );
        }

        if (name.contains("clyde") || name.contains("orange")) {
            return getClydeTarget(
                    ghostGridX,
                    ghostGridY,
                    playerGridX,
                    playerGridY,
                    ghostName,
                    mapCols,
                    mapRows
            );
        }

        return new int[]{
                clamp(playerGridX, 0, mapCols - 1),
                clamp(playerGridY, 0, mapRows - 1)
        };
    }

    private static int[] getPinkyTarget(
            int playerGridX,
            int playerGridY,
            int playerDirection,
            int mapCols,
            int mapRows
    ) {
        int targetX = playerGridX + DX[playerDirection] * 4;
        int targetY = playerGridY + DY[playerDirection] * 4;

        return new int[]{
                clamp(targetX, 0, mapCols - 1),
                clamp(targetY, 0, mapRows - 1)
        };
    }

    private static int[] getInkyTarget(
            int playerGridX,
            int playerGridY,
            int playerDirection,
            int blinkyGridX,
            int blinkyGridY,
            int mapCols,
            int mapRows
    ) {
        int twoAheadX = playerGridX + DX[playerDirection] * 2;
        int twoAheadY = playerGridY + DY[playerDirection] * 2;

        int vectorX = twoAheadX - blinkyGridX;
        int vectorY = twoAheadY - blinkyGridY;

        int targetX = blinkyGridX + vectorX * 2;
        int targetY = blinkyGridY + vectorY * 2;

        return new int[]{
                clamp(targetX, 0, mapCols - 1),
                clamp(targetY, 0, mapRows - 1)
        };
    }

    private static int[] getClydeTarget(
            int ghostGridX,
            int ghostGridY,
            int playerGridX,
            int playerGridY,
            String ghostName,
            int mapCols,
            int mapRows
    ) {
        int distance = distanceSquared(ghostGridX, ghostGridY, playerGridX, playerGridY);

        if (distance > 64) {
            return new int[]{
                    clamp(playerGridX, 0, mapCols - 1),
                    clamp(playerGridY, 0, mapRows - 1)
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

    public static int distanceSquared(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;

        return dx * dx + dy * dy;
    }

    public static int opposite(int direction) {
        direction = fixDirection(direction);

        if (direction == UP) {
            return DOWN;
        }

        if (direction == DOWN) {
            return UP;
        }

        if (direction == RIGHT) {
            return LEFT;
        }

        return RIGHT;
    }

    public static int fixDirection(int direction) {
        if (direction < 0 || direction > 3) {
            return RIGHT;
        }

        return direction;
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
