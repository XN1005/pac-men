package pacmen.ghostai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pacmen.entities.Ghost;
import pacmen.map.GameMap;

public class BFSPathfinder {

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    public static int getNextDirection(
            GameMap map,
            Ghost ghost,
            int startX,
            int startY,
            int targetX,
            int targetY,
            int currentDirection,
            boolean allowReverse
    ) {
        if (map == null || ghost == null) {
            return fixDirection(currentDirection);
        }

        currentDirection = fixDirection(currentDirection);

        if (!isInsideMap(map, startX, startY)) {
            return currentDirection;
        }

        targetX = clamp(targetX, 0, map.getCols() - 1);
        targetY = clamp(targetY, 0, map.getRows() - 1);

        boolean[][] visited = new boolean[map.getRows()][map.getCols()];
        int[][] firstDirection = new int[map.getRows()][map.getCols()];

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                firstDirection[row][col] = -1;
            }
        }

        Queue<int[]> queue = new LinkedList<int[]>();
        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();

            int currentX = current[0];
            int currentY = current[1];

            if (currentX == targetX && currentY == targetY) {
                int answer = firstDirection[currentY][currentX];

                if (answer == -1) {
                    return currentDirection;
                }

                return answer;
            }

            for (int direction = 0; direction < 4; direction++) {
                if (currentX == startX
                        && currentY == startY
                        && !allowReverse
                        && direction == opposite(currentDirection)) {
                    continue;
                }

                int nextX = currentX + DX[direction];
                int nextY = currentY + DY[direction];

                if (!isInsideMap(map, nextX, nextY)) {
                    continue;
                }

                if (visited[nextY][nextX]) {
                    continue;
                }

                if (!map.isMoveValid(ghost, nextX, nextY)) {
                    continue;
                }

                visited[nextY][nextX] = true;

                if (currentX == startX && currentY == startY) {
                    firstDirection[nextY][nextX] = direction;
                } else {
                    firstDirection[nextY][nextX] = firstDirection[currentY][currentX];
                }

                queue.add(new int[]{nextX, nextY});
            }
        }

        return chooseGreedyDirection(
                map,
                ghost,
                startX,
                startY,
                targetX,
                targetY,
                currentDirection,
                allowReverse
        );
    }

    public static int chooseGreedyDirection(
            GameMap map,
            Ghost ghost,
            int startX,
            int startY,
            int targetX,
            int targetY,
            int currentDirection,
            boolean allowReverse
    ) {
        if (map == null || ghost == null) {
            return fixDirection(currentDirection);
        }

        currentDirection = fixDirection(currentDirection);

        List<Integer> legalDirections = getLegalDirections(
                map,
                ghost,
                startX,
                startY,
                currentDirection,
                allowReverse
        );

        if (legalDirections.isEmpty()) {
            return currentDirection;
        }

        int bestDirection = legalDirections.get(0);
        int bestDistance = Integer.MAX_VALUE;

        for (int direction : legalDirections) {
            int nextX = startX + DX[direction];
            int nextY = startY + DY[direction];

            int distance = distanceSquared(nextX, nextY, targetX, targetY);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

    public static List<Integer> getLegalDirections(
            GameMap map,
            Ghost ghost,
            int gridX,
            int gridY,
            int currentDirection,
            boolean allowReverse
    ) {
        List<Integer> legalDirections = new ArrayList<Integer>();

        currentDirection = fixDirection(currentDirection);

        int[] priorityOrder = {UP, LEFT, DOWN, RIGHT};

        for (int direction : priorityOrder) {
            if (!allowReverse && direction == opposite(currentDirection)) {
                continue;
            }

            int nextX = gridX + DX[direction];
            int nextY = gridY + DY[direction];

            if (!isInsideMap(map, nextX, nextY)) {
                continue;
            }

            if (map.isMoveValid(ghost, nextX, nextY)) {
                legalDirections.add(direction);
            }
        }

        if (legalDirections.isEmpty()) {
            int reverse = opposite(currentDirection);

            int nextX = gridX + DX[reverse];
            int nextY = gridY + DY[reverse];

            if (isInsideMap(map, nextX, nextY)
                    && map.isMoveValid(ghost, nextX, nextY)) {
                legalDirections.add(reverse);
            }
        }

        return legalDirections;
    }

    public static int[] getNextTile(int gridX, int gridY, int direction) {
        direction = fixDirection(direction);

        return new int[]{
                gridX + DX[direction],
                gridY + DY[direction]
        };
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

    public static int distanceSquared(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;

        return dx * dx + dy * dy;
    }

    private static boolean isInsideMap(GameMap map, int x, int y) {
        return x >= 0 && x < map.getCols() && y >= 0 && y < map.getRows();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
