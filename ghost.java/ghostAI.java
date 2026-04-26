package ghost.java;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import pacmen.entities.Ghost;
import pacmen.map.GameMap;

public class ghostAI {

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

    private static final Random random = new Random();

    public static int chooseDirection(
            GameMap map,
            Ghost ghost,
            int ghostGridX,
            int ghostGridY,
            int currentDirection,
            int targetGridX,
            int targetGridY,
            int mode
    ) {
        if (map == null || ghost == null) {
            return BehaviorLogic.fixDirection(currentDirection);
        }

        currentDirection = BehaviorLogic.fixDirection(currentDirection);

        if (mode == FRIGHTENED) {
            return chooseRandomDirection(map, ghost, ghostGridX, ghostGridY, currentDirection);
        }

        return chooseBfsDirection(
                map,
                ghost,
                ghostGridX,
                ghostGridY,
                targetGridX,
                targetGridY,
                currentDirection,
                mode == EATEN
        );
    }

    private static int chooseBfsDirection(
            GameMap map,
            Ghost ghost,
            int startX,
            int startY,
            int targetX,
            int targetY,
            int currentDirection,
            boolean allowReverse
    ) {
        int cols = map.getCols();
        int rows = map.getRows();

        targetX = clamp(targetX, 0, cols - 1);
        targetY = clamp(targetY, 0, rows - 1);

        if (!inside(startX, startY, cols, rows)) {
            return currentDirection;
        }

        boolean[][] visited = new boolean[rows][cols];
        int[][] firstDirection = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
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
                        && direction == BehaviorLogic.opposite(currentDirection)) {
                    continue;
                }

                int nextX = currentX + DX[direction];
                int nextY = currentY + DY[direction];

                if (!inside(nextX, nextY, cols, rows)) {
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

    private static int chooseGreedyDirection(
            GameMap map,
            Ghost ghost,
            int ghostGridX,
            int ghostGridY,
            int targetGridX,
            int targetGridY,
            int currentDirection,
            boolean allowReverse
    ) {
        int bestDirection = currentDirection;
        int bestDistance = Integer.MAX_VALUE;

        int[] priorityOrder = {UP, LEFT, DOWN, RIGHT};

        for (int direction : priorityOrder) {
            if (!allowReverse && direction == BehaviorLogic.opposite(currentDirection)) {
                continue;
            }

            int nextX = ghostGridX + DX[direction];
            int nextY = ghostGridY + DY[direction];

            if (!inside(nextX, nextY, map.getCols(), map.getRows())) {
                continue;
            }

            if (!map.isMoveValid(ghost, nextX, nextY)) {
                continue;
            }

            int distance = BehaviorLogic.distanceSquared(nextX, nextY, targetGridX, targetGridY);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestDirection = direction;
            }
        }

        if (bestDistance == Integer.MAX_VALUE) {
            int reverse = BehaviorLogic.opposite(currentDirection);
            int reverseX = ghostGridX + DX[reverse];
            int reverseY = ghostGridY + DY[reverse];

            if (inside(reverseX, reverseY, map.getCols(), map.getRows())
                    && map.isMoveValid(ghost, reverseX, reverseY)) {
                return reverse;
            }
        }

        return bestDirection;
    }

    private static int chooseRandomDirection(
            GameMap map,
            Ghost ghost,
            int ghostGridX,
            int ghostGridY,
            int currentDirection
    ) {
        int[] possibleDirections = new int[4];
        int count = 0;

        for (int direction = 0; direction < 4; direction++) {
            int nextX = ghostGridX + DX[direction];
            int nextY = ghostGridY + DY[direction];

            if (!inside(nextX, nextY, map.getCols(), map.getRows())) {
                continue;
            }

            if (!map.isMoveValid(ghost, nextX, nextY)) {
                continue;
            }

            possibleDirections[count] = direction;
            count++;
        }

        if (count == 0) {
            return currentDirection;
        }

        return possibleDirections[random.nextInt(count)];
    }

    public static int[] getNextGridPosition(int gridX, int gridY, int direction) {
        direction = BehaviorLogic.fixDirection(direction);

        return new int[]{
                gridX + DX[direction],
                gridY + DY[direction]
        };
    }

    public static int pixelToGrid(double pixelValue) {
        return (int) Math.round((pixelValue - 10) / 20.0);
    }

    public static double gridToPixel(int gridValue) {
        return gridValue * 20.0 + 10.0;
    }

    private static boolean inside(int x, int y, int cols, int rows) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
