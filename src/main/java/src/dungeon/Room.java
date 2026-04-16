package src.dungeon;

import src.entity.Enemy;
import src.entity.Player;
import src.entity.Projectile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private RoomTemplate template;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Pickup> pickups = new ArrayList<>();
    private boolean cleared = false;
    private boolean rewardClaimed = false;
    private boolean hasSpawnedEnemies = false;
    private RoomType type;
    private Player player;

    private float invincibilityTimer = 0;
    public static final int TILE_SIZE = 48;
    private int gridX, gridY;
    private int floorLevel;

    public enum RoomType {
        START, NORMAL, SHOP, TREASURE, SECRET, BOSS
    }

    public Room(RoomTemplate template, int floorLevel, int gridX, int gridY) {
        this.template = template;
        this.type = template.getType();
        this.gridX = gridX;
        this.gridY = gridY;
        this.floorLevel = floorLevel;

        if (type == RoomType.START) {
            cleared = true;
        } else {
            cleared = false;
        }
    }

    public void spawnEnemiesIfNeeded() {
        if (hasSpawnedEnemies) return;
        if (type == RoomType.START) return;

        spawnEnemiesFromTemplate();
        hasSpawnedEnemies = true;
    }

    private void spawnEnemiesFromTemplate() {
        for (RoomTemplate.EnemySpawn spawn : template.getEnemySpawns()) {
            if (spawn.enemyType.equals("BOSS")) {
                Enemy boss = new Enemy(spawn.x, spawn.y, Enemy.EnemyType.SKELETON_MELEE);
                for (int i = 0; i < 20; i++) {
                    boss.takeDamage(-50);
                }
                enemies.add(boss);
                continue;
            }

            Enemy.EnemyType enemyType;
            switch (spawn.enemyType) {
                case "BLUE_SLIME": enemyType = Enemy.EnemyType.BLUE_SLIME; break;
                case "RED_SLIME": enemyType = Enemy.EnemyType.RED_SLIME; break;
                case "SKELETON_MELEE": enemyType = Enemy.EnemyType.SKELETON_MELEE; break;
                default: enemyType = Enemy.EnemyType.SKELETON_RANGED;
            }

            Enemy enemy = new Enemy(spawn.x, spawn.y, enemyType);
            for (int i = 0; i < floorLevel - 1; i++) {
                enemy.takeDamage(-10);
            }
            enemies.add(enemy);
        }
    }

    public void update(float delta, Player player) {
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
            if (invincibilityTimer > 0) return;
        }

        if (!cleared) {
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                e.update(delta, player);
                if (!e.isAlive()) {
                    enemies.remove(i);
                    i--;
                    player.addGold(10 + (int)(Math.random() * 20));
                }
            }

            if (enemies.isEmpty() && !cleared) {
                cleared = true;
            }
        }
    }

    public boolean checkDoorTransition(Player player) {
        if (!cleared) return false;

        int playerTileX = (int)player.getX();
        int playerTileY = (int)player.getY();

        if (playerTileX < 0 || playerTileX >= template.getWidth() ||
                playerTileY < 0 || playerTileY >= template.getHeight()) {
            return false;
        }

        RoomTemplate.TileType tile = template.getTiles()[playerTileX][playerTileY];
        if (tile == RoomTemplate.TileType.DOOR) {
            if (playerTileY == 0) return true;
            if (playerTileY == template.getHeight()-1) return true;
            if (playerTileX == 0) return true;
            if (playerTileX == template.getWidth()-1) return true;
        }
        return false;
    }

    public String getTransitionDirection(Player player) {
        int playerTileX = (int)player.getX();
        int playerTileY = (int)player.getY();

        if (playerTileX < 0 || playerTileX >= template.getWidth() ||
                playerTileY < 0 || playerTileY >= template.getHeight()) {
            return null;
        }

        RoomTemplate.TileType tile = template.getTiles()[playerTileX][playerTileY];
        if (tile == RoomTemplate.TileType.DOOR) {
            if (playerTileY == 0) return "UP";
            if (playerTileY == template.getHeight()-1) return "DOWN";
            if (playerTileX == 0) return "LEFT";
            if (playerTileX == template.getWidth()-1) return "RIGHT";
        }
        return null;
    }

    public void checkPickups(float playerX, float playerY, Player player) {
        for (int i = 0; i < pickups.size(); i++) {
            Pickup p = pickups.get(i);
            float dx = p.x - playerX;
            float dy = p.y - playerY;
            if (Math.abs(dx) < 0.5f && Math.abs(dy) < 0.5f) {
                p.collect(player);
                pickups.remove(i);
                i--;
            }
        }
    }

    public void claimReward(Player player) {
        if (!rewardClaimed) {
            if (Math.random() < 0.7) {
                player.heal(Math.random() < 0.25 ? 2 : 1);
            } else {
                player.addShield();
            }
            rewardClaimed = true;
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.invincibilityTimer = 1.5f;
        spawnEnemiesIfNeeded();
    }

    // Метод для проверки стен (нужен для Fireball)
    public boolean isWall(int x, int y) {
        if (x < 0 || x >= template.getWidth() || y < 0 || y >= template.getHeight()) {
            return true;
        }
        RoomTemplate.TileType tile = template.getTiles()[x][y];
        return tile == RoomTemplate.TileType.WALL;
    }

    // Методы рендера с оффсетом для камеры
    public void renderWithOffset(Graphics2D g, int offsetX, int offsetY, int screenWidth, int screenHeight) {
        int width = template.getWidth();
        int height = template.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int screenX = i * TILE_SIZE + offsetX;
                int screenY = j * TILE_SIZE + offsetY;

                if (screenX + TILE_SIZE > 0 && screenX < screenWidth &&
                        screenY + TILE_SIZE > 0 && screenY < screenHeight) {
                    g.setColor(getTileColor(template.getTiles()[i][j]));
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(60, 55, 70));
                    g.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public void renderEntitiesWithOffset(Graphics2D g, int offsetX, int offsetY, int screenWidth, int screenHeight) {
        for (Enemy e : enemies) {
            int screenX = (int)(e.getX() * TILE_SIZE) + offsetX;
            int screenY = (int)(e.getY() * TILE_SIZE) + offsetY;
            if (screenX + 50 > 0 && screenX - 50 < screenWidth &&
                    screenY + 50 > 0 && screenY - 50 < screenHeight) {
                e.render(g, screenX, screenY);
            }
        }

        for (Enemy e : enemies) {
            for (Projectile p : e.getProjectiles()) {
                int screenX = (int)(p.getX() * TILE_SIZE) + offsetX;
                int screenY = (int)(p.getY() * TILE_SIZE) + offsetY;
                p.render(g, screenX, screenY);
            }
        }
    }

    private Color getTileColor(RoomTemplate.TileType tile) {
        switch (tile) {
            case FLOOR: return new Color(40, 35, 45);
            case WALL: return new Color(30, 25, 35);
            case DOOR: return new Color(80, 60, 40);
            case PIT: return new Color(20, 15, 25);
            case ROCK: return new Color(70, 65, 55);
            default: return Color.BLACK;
        }
    }

    // Старый метод рендера для обратной совместимости
    public void render(Graphics2D g, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        int offsetX = screenWidth / 2 - (int)(cameraX * TILE_SIZE);
        int offsetY = screenHeight / 2 - (int)(cameraY * TILE_SIZE);
        renderWithOffset(g, offsetX, offsetY, screenWidth, screenHeight);
    }

    public void renderEntities(Graphics2D g, float cameraX, float cameraY, int screenWidth, int screenHeight) {
        int offsetX = screenWidth / 2 - (int)(cameraX * TILE_SIZE);
        int offsetY = screenHeight / 2 - (int)(cameraY * TILE_SIZE);
        renderEntitiesWithOffset(g, offsetX, offsetY, screenWidth, screenHeight);
    }

    public boolean isCleared() { return cleared; }
    public boolean isRewardClaimed() { return rewardClaimed; }
    public List<Enemy> getEnemies() { return enemies; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public RoomType getType() { return type; }
    public RoomTemplate getTemplate() { return template; }

    private static class Pickup {
        float x, y;
        PickupType type;
        enum PickupType { HEALTH, SHIELD, ARROWS, MANA, GOLD }
        void collect(Player player) {
            switch (type) {
                case HEALTH: player.heal(1); break;
                case SHIELD: player.addShield(); break;
                case ARROWS: player.addArrows(10); break;
                case MANA: player.regenerateMana(2); break;
                case GOLD: player.addGold(15); break;
            }
        }
    }
}