package pacmen.ghostai;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import pacmen.entities.Ghost;
import pacmen.entities.Player;
import pacmen.map.GameMap;

public class GhostAI {

    private static final Random random = new Random();
    private static final Map<Player, int[]> previousPlayerTiles = new IdentityHashMap<Player, int[]>();

    public static int chooseNextDirection(
            GameMap map,
            Ghost ghost,
            Player player,
            String ghostName,
            Ghost.GhostState state,
            int currentDirection,
            Ghost blinky
    ) {
        if (map == null || ghost == null) {
            return BFSPathfinder.fixDirection(currentDirection);
        }

        currentDirection = BFSPathfinder.fixDirection(currentDirection);

        int ghostX = TargetingStrategy.getGhostGridX(ghost);
        int ghostY = TargetingStrategy.getGhostGridY(ghost);

        boolean allowReverse = state == Ghost.GhostState.FRIGHTENED
                || state == Ghost.GhostState.EATEN;

        if (state == Ghost.GhostState.FRIGHTENED) {
            return chooseRandomDirection(
                    map,
                    ghost,
                    ghostX,
                    ghostY,
                    currentDirection,
                    allowReverse
            );
        }

        int playerDirection = getEstimatedPlayerDirection(player);

        int[] target = TargetingStrategy.getTargetTile(
                map,
                ghost,
                player,
                blinky,
                ghostName,
                state,
                playerDirection
        );

        rememberPlayerTile(player);

        if (state == Ghost.GhostState.EATEN) {
            return BFSPathfinder.getNextDirection(
                    map,
                    ghost,
                    ghostX,
                    ghostY,
                    target[0],
                    target[1],
                    currentDirection,
                    true
            );
        }

        return BFSPathfinder.chooseGreedyDirection(
                map,
                ghost,
                ghostX,
                ghostY,
                target[0],
                target[1],
                currentDirection,
                allowReverse
        );
    }

    public static int chooseNextDirectionToTile(
            GameMap map,
            Ghost ghost,
            int targetX,
            int targetY,
            Ghost.GhostState state,
            int currentDirection
    ) {
        if (map == null || ghost == null) {
            return BFSPathfinder.fixDirection(currentDirection);
        }

        currentDirection = BFSPathfinder.fixDirection(currentDirection);

        int ghostX = TargetingStrategy.getGhostGridX(ghost);
        int ghostY = TargetingStrategy.getGhostGridY(ghost);

        boolean allowReverse = state == Ghost.GhostState.FRIGHTENED
                || state == Ghost.GhostState.EATEN;

        if (state == Ghost.GhostState.FRIGHTENED) {
            return chooseRandomDirection(
                    map,
                    ghost,
                    ghostX,
                    ghostY,
                    currentDirection,
                    allowReverse
            );
        }

        if (state == Ghost.GhostState.EATEN) {
            return BFSPathfinder.getNextDirection(
                    map,
                    ghost,
                    ghostX,
                    ghostY,
                    targetX,
                    targetY,
                    currentDirection,
                    true
            );
        }

        return BFSPathfinder.chooseGreedyDirection(
                map,
                ghost,
                ghostX,
                ghostY,
                targetX,
                targetY,
                currentDirection,
                allowReverse
        );
    }

    public static Player findClosestPlayer(Ghost ghost, List<Player> players) {
        if (ghost == null || players == null || players.isEmpty()) {
            return null;
        }

        int ghostX = TargetingStrategy.getGhostGridX(ghost);
        int ghostY = TargetingStrategy.getGhostGridY(ghost);

        Player closestPlayer = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Player player : players) {
            if (player == null || player.sprite == null) {
                continue;
            }

            if ("DEAD".equals(player.state) || "DISABLED".equals(player.state)) {
                continue;
            }

            int playerX = TargetingStrategy.getPlayerGridX(player);
            int playerY = TargetingStrategy.getPlayerGridY(player);

            int distance = BFSPathfinder.distanceSquared(ghostX, ghostY, playerX, playerY);

            if (distance < bestDistance) {
                bestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    public static void rememberPlayerTile(Player player) {
        if (player == null || player.sprite == null) {
            return;
        }

        int playerX = TargetingStrategy.getPlayerGridX(player);
        int playerY = TargetingStrategy.getPlayerGridY(player);

        previousPlayerTiles.put(player, new int[]{playerX, playerY});
    }

    private static int getEstimatedPlayerDirection(Player player) {
        if (player == null || player.sprite == null) {
            return BFSPathfinder.RIGHT;
        }

        int currentX = TargetingStrategy.getPlayerGridX(player);
        int currentY = TargetingStrategy.getPlayerGridY(player);

        int[] previous = previousPlayerTiles.get(player);

        if (previous == null) {
            return BFSPathfinder.RIGHT;
        }

        int previousX = previous[0];
        int previousY = previous[1];

        if (currentY < previousY) {
            return BFSPathfinder.UP;
        }

        if (currentX > previousX) {
            return BFSPathfinder.RIGHT;
        }

        if (currentY > previousY) {
            return BFSPathfinder.DOWN;
        }

        if (currentX < previousX) {
            return BFSPathfinder.LEFT;
        }

        return BFSPathfinder.RIGHT;
    }

    private static int chooseRandomDirection(
            GameMap map,
            Ghost ghost,
            int ghostX,
            int ghostY,
            int currentDirection,
            boolean allowReverse
    ) {
        List<Integer> legalDirections = BFSPathfinder.getLegalDirections(
                map,
                ghost,
                ghostX,
                ghostY,
                currentDirection,
                allowReverse
        );

        if (legalDirections.isEmpty()) {
            return currentDirection;
        }

        return legalDirections.get(random.nextInt(legalDirections.size()));
    }
}
