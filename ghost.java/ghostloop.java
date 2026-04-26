package ghost.java;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import pacmen.entities.Ghost;
import pacmen.entities.Player;
import pacmen.map.GameMap;

public class ghostloop {

    private final GameMap map;
    private final List<GhostData> ghosts;
    private final Map<Player, int[]> previousPlayerTiles;

    private int frightenedCounter;

    public ghostloop(GameMap map) {
        this.map = map;
        this.ghosts = new ArrayList<GhostData>();
        this.previousPlayerTiles = new IdentityHashMap<Player, int[]>();
        this.frightenedCounter = 0;
    }

    public void addGhost(String name, Ghost ghost) {
        if (ghost == null) {
            return;
        }

        int gridX = ghostAI.pixelToGrid(ghost.sprite.getCenterX());
        int gridY = ghostAI.pixelToGrid(ghost.sprite.getCenterY());

        GhostData data = new GhostData();
        data.name = name;
        data.ghost = ghost;
        data.gridX = gridX;
        data.gridY = gridY;
        data.homeX = gridX;
        data.homeY = gridY;
        data.direction = ghostAI.RIGHT;
        data.mode = ghostAI.CHASE;

        ghosts.add(data);
        writeGhostState(data);
    }

    public void update(Player... players) {
        if (players == null || players.length == 0) {
            updateWithoutPlayers();
            return;
        }

        updatePlayerHistory(players);

        boolean powerMode = isAnyPlayerPowered(players);

        if (powerMode) {
            frightenedCounter = 300;
        } else if (frightenedCounter > 0) {
            frightenedCounter--;
        }

        GhostData blinky = findGhost("blinky");

        for (GhostData data : ghosts) {
            Player targetPlayer = findClosestPlayer(data, players);

            if (targetPlayer == null) {
                moveToScatter(data, blinky);
            } else {
                moveWithTarget(data, targetPlayer, blinky, frightenedCounter > 0);
            }

            writeGhostState(data);
        }

        checkCollisions(players);
    }

    private void updateWithoutPlayers() {
        GhostData blinky = findGhost("blinky");

        for (GhostData data : ghosts) {
            moveToScatter(data, blinky);
            writeGhostState(data);
        }
    }

    private void moveToScatter(GhostData data, GhostData blinky) {
        int blinkyX = blinky == null ? data.gridX : blinky.gridX;
        int blinkyY = blinky == null ? data.gridY : blinky.gridY;

        int[] target = BehaviorLogic.getTargetTile(
                data.name,
                data.gridX,
                data.gridY,
                data.gridX,
                data.gridY,
                data.direction,
                blinkyX,
                blinkyY,
                map.getCols(),
                map.getRows(),
                ghostAI.SCATTER
        );

        data.mode = ghostAI.SCATTER;
        moveOneStep(data, target[0], target[1]);
    }

    private void moveWithTarget(
            GhostData data,
            Player targetPlayer,
            GhostData blinky,
            boolean frightened
    ) {
        int playerX = ghostAI.pixelToGrid(targetPlayer.sprite.getCenterX());
        int playerY = ghostAI.pixelToGrid(targetPlayer.sprite.getCenterY());
        int playerDirection = getPlayerDirection(targetPlayer);

        int blinkyX = blinky == null ? data.gridX : blinky.gridX;
        int blinkyY = blinky == null ? data.gridY : blinky.gridY;

        if (data.mode != ghostAI.EATEN) {
            data.mode = frightened ? ghostAI.FRIGHTENED : ghostAI.CHASE;
        }

        int[] target = BehaviorLogic.getTargetTile(
                data.name,
                data.gridX,
                data.gridY,
                playerX,
                playerY,
                playerDirection,
                blinkyX,
                blinkyY,
                map.getCols(),
                map.getRows(),
                data.mode
        );

        moveOneStep(data, target[0], target[1]);

        if (data.mode == ghostAI.EATEN && data.gridX == data.homeX && data.gridY == data.homeY) {
            data.mode = ghostAI.CHASE;
        }
    }

    private void moveOneStep(GhostData data, int targetX, int targetY) {
        int nextDirection = ghostAI.chooseDirection(
                map,
                data.ghost,
                data.gridX,
                data.gridY,
                data.direction,
                targetX,
                targetY,
                data.mode
        );

        int[] next = ghostAI.getNextGridPosition(data.gridX, data.gridY, nextDirection);

        if (map.isMoveValid(data.ghost, next[0], next[1])) {
            data.direction = nextDirection;
            data.gridX = next[0];
            data.gridY = next[1];
        }
    }

    private void checkCollisions(Player... players) {
        for (GhostData data : ghosts) {
            for (Player player : players) {
                if (player == null) {
                    continue;
                }

                if ("DEAD".equals(player.state) || "DISABLED".equals(player.state)) {
                    continue;
                }

                int playerX = ghostAI.pixelToGrid(player.sprite.getCenterX());
                int playerY = ghostAI.pixelToGrid(player.sprite.getCenterY());

                if (data.gridX == playerX && data.gridY == playerY) {
                    if ("POWER_UP".equals(player.state) && data.mode != ghostAI.EATEN) {
                        player.collideGhost();
                        data.mode = ghostAI.EATEN;
                    } else if (data.mode != ghostAI.EATEN) {
                        player.collideGhost();
                    }
                }
            }
        }
    }

    private Player findClosestPlayer(GhostData data, Player... players) {
        Player closest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Player player : players) {
            if (player == null) {
                continue;
            }

            if ("DEAD".equals(player.state) || "DISABLED".equals(player.state)) {
                continue;
            }

            int playerX = ghostAI.pixelToGrid(player.sprite.getCenterX());
            int playerY = ghostAI.pixelToGrid(player.sprite.getCenterY());

            int distance = BehaviorLogic.distanceSquared(data.gridX, data.gridY, playerX, playerY);

            if (distance < bestDistance) {
                bestDistance = distance;
                closest = player;
            }
        }

        return closest;
    }

    private boolean isAnyPlayerPowered(Player... players) {
        for (Player player : players) {
            if (player != null && "POWER_UP".equals(player.state)) {
                return true;
            }
        }

        return false;
    }

    private void updatePlayerHistory(Player... players) {
        for (Player player : players) {
            if (player == null) {
                continue;
            }

            int x = ghostAI.pixelToGrid(player.sprite.getCenterX());
            int y = ghostAI.pixelToGrid(player.sprite.getCenterY());

            previousPlayerTiles.put(player, new int[]{x, y});
        }
    }

    private int getPlayerDirection(Player player) {
        int currentX = ghostAI.pixelToGrid(player.sprite.getCenterX());
        int currentY = ghostAI.pixelToGrid(player.sprite.getCenterY());

        int[] previous = previousPlayerTiles.get(player);

        if (previous == null) {
            return ghostAI.RIGHT;
        }

        int oldX = previous[0];
        int oldY = previous[1];

        if (currentY < oldY) {
            return ghostAI.UP;
        }

        if (currentX > oldX) {
            return ghostAI.RIGHT;
        }

        if (currentY > oldY) {
            return ghostAI.DOWN;
        }

        if (currentX < oldX) {
            return ghostAI.LEFT;
        }

        return ghostAI.RIGHT;
    }

    private GhostData findGhost(String name) {
        for (GhostData data : ghosts) {
            if (data.name != null && data.name.toLowerCase().contains(name)) {
                return data;
            }
        }

        return null;
    }

    private void writeGhostState(GhostData data) {
        double pixelX = ghostAI.gridToPixel(data.gridX);
        double pixelY = ghostAI.gridToPixel(data.gridY);

        data.ghost.sprite.setCenterX(pixelX);
        data.ghost.sprite.setCenterY(pixelY);

        setDoubleField(data.ghost, "x", pixelX);
        setDoubleField(data.ghost, "y", pixelY);
        setIntField(data.ghost, "direction", data.direction);
    }

    private void setDoubleField(Object object, String fieldName, double value) {
        try {
            Field field = findField(object.getClass(), fieldName);
            field.setAccessible(true);
            field.setDouble(object, value);
        } catch (Exception ignored) {
        }
    }

    private void setIntField(Object object, String fieldName, int value) {
        try {
            Field field = findField(object.getClass(), fieldName);
            field.setAccessible(true);
            field.setInt(object, value);
        } catch (Exception ignored) {
        }
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private static class GhostData {
        String name;
        Ghost ghost;
        int gridX;
        int gridY;
        int homeX;
        int homeY;
        int direction;
        int mode;
    }
}
