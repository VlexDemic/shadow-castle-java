package src.dungeon;

import src.entity.Player;
import src.entity.enemies.Enemy;
import src.entity.enemies.BlueSlime;
import src.entity.enemies.RedSlime;
import src.entity.enemies.SkeletonMelee;
import src.entity.enemies.SkeletonRanged;
import src.weapon.Projectile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private Pickup centerReward = null;

    // Состояния дверей
    private boolean doorUpOpen = false;
    private boolean doorDownOpen = false;
    private boolean doorLeftOpen = false;
    private boolean doorRightOpen = false;

    // Типы дверей для спецкомнат
    private DoorType doorUpType = DoorType.NORMAL;
    private DoorType doorDownType = DoorType.NORMAL;
    private DoorType doorLeftType = DoorType.NORMAL;
    private DoorType doorRightType = DoorType.NORMAL;

    public enum DoorType {
        NORMAL,      // Обычная дверь
        BOSS,        // Красная - путь к боссу
        SHOP,        // Зелёная - магазин
        CHALLENGE,   // Жёлтая - вызов
        TREASURE     // Синяя - сокровищница
    }

    public Room(RoomTemplate template, int floorLevel, int gridX, int gridY) {
        this.template = template;
        this.type = template.getType();
        this.gridX = gridX;
        this.gridY = gridY;
        this.floorLevel = floorLevel;

        if (type == RoomType.START) {
            cleared = true;
            openAllDoors();
        } else if (type == RoomType.SHOP || type == RoomType.TREASURE) {
            cleared = false;
            // Спецкомнаты не имеют врагов, но двери закрыты до входа
        } else {
            cleared = false;
        }
    }

    public void openAllDoors() {
        doorUpOpen = true;
        doorDownOpen = true;
        doorLeftOpen = true;
        doorRightOpen = true;
    }

    public void setDoorType(int direction, DoorType doorType) {
        switch (direction) {
            case 0: doorUpType = doorType; break;
            case 1: doorDownType = doorType; break;
            case 2: doorLeftType = doorType; break;
            case 3: doorRightType = doorType; break;
        }
    }

    public void spawnEnemiesIfNeeded() {
        if (hasSpawnedEnemies) return;
        if (type == RoomType.START) return;

        // Для SHOP и TREASURE комнат врагов нет, но они считаются очищенными при входе
        if (type == RoomType.SHOP || type == RoomType.TREASURE) {
            cleared = true;
            openAllDoors();
            hasSpawnedEnemies = true;
            return;
        }

        spawnEnemiesFromTemplate();
        hasSpawnedEnemies = true;

        if (enemies.isEmpty()) {
            addDefaultEnemies();
        }

        if (player != null) {
            for (Enemy enemy : enemies) {
                enemy.setPlayer(player);
            }
        }
    }

    private void addDefaultEnemies() {
        Random rand = new Random();
        int enemyCount = 3 + floorLevel;
        if (enemyCount > 6) enemyCount = 6;

        for (int i = 0; i < enemyCount; i++) {
            float x = 3 + rand.nextFloat() * 14;
            float y = 2 + rand.nextFloat() * 11;

            Enemy enemy = null;
            int typeRand = rand.nextInt(4);
            switch (typeRand) {
                case 0:
                    enemy = new BlueSlime(x, y);
                    break;
                case 1:
                    enemy = new RedSlime(x, y);
                    break;
                case 2:
                    enemy = new SkeletonMelee(x, y);
                    break;
                default:
                    enemy = new SkeletonRanged(x, y);
                    break;
            }

            if (enemy != null) {
                enemy.setFloorLevel(floorLevel);
                if (player != null) {
                    enemy.setPlayer(player);
                }
                enemies.add(enemy);
            }
        }
    }

    private void spawnEnemiesFromTemplate() {
        List<RoomTemplate.EnemySpawn> spawns = template.getEnemySpawns();

        if (spawns.isEmpty() && type == RoomType.NORMAL) {
            addDefaultEnemies();
            return;
        }

        for (RoomTemplate.EnemySpawn spawn : spawns) {
            Enemy enemy = null;

            switch (spawn.enemyType) {
                case "BLUE_SLIME":
                    enemy = new BlueSlime(spawn.x, spawn.y);
                    break;
                case "RED_SLIME":
                    enemy = new RedSlime(spawn.x, spawn.y);
                    break;
                case "SKELETON_MELEE":
                    enemy = new SkeletonMelee(spawn.x, spawn.y);
                    break;
                case "SKELETON_RANGED":
                    enemy = new SkeletonRanged(spawn.x, spawn.y);
                    break;
                case "BOSS":
                    enemy = new SkeletonMelee(spawn.x, spawn.y);
                    for (int i = 0; i < 20; i++) {
                        enemy.takeDamage(-50);
                    }
                    break;
                default:
                    continue;
            }

            if (enemy != null) {
                enemy.setFloorLevel(floorLevel);
                if (player != null) {
                    enemy.setPlayer(player);
                }
                enemies.add(enemy);
            }
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
                e.updateWithPlayer(delta, player);
                if (!e.isAlive()) {
                    enemies.remove(i);
                    i--;
                    player.addGold(10 + (int)(Math.random() * 20));
                }
            }

            if (enemies.isEmpty() && hasSpawnedEnemies && !cleared) {
                cleared = true;
                openAllDoors();
                spawnCenterReward();
            }
        }
    }

    private void spawnCenterReward() {
        if (centerReward != null) return;
        if (type == RoomType.START) return;
        if (type == RoomType.SHOP) return;
        if (type == RoomType.TREASURE) return;

        Random rand = new Random();
        int rewardType = rand.nextInt(4);

        Pickup reward = new Pickup();
        reward.x = 10;
        reward.y = 7.5f;

        switch (rewardType) {
            case 0:
                reward.type = Pickup.PickupType.HEALTH;
                break;
            case 1:
                reward.type = Pickup.PickupType.SHIELD;
                break;
            case 2:
                reward.type = Pickup.PickupType.ARROWS;
                break;
            case 3:
                reward.type = Pickup.PickupType.MANA;
                break;
        }

        centerReward = reward;
        pickups.add(reward);
    }

    public void setDoors(boolean up, boolean down, boolean left, boolean right) {
        doorUpOpen = up && cleared;
        doorDownOpen = down && cleared;
        doorLeftOpen = left && cleared;
        doorRightOpen = right && cleared;
    }

    public void setDoorOpen(int direction, boolean open) {
        switch (direction) {
            case 0: doorUpOpen = open; break;
            case 1: doorDownOpen = open; break;
            case 2: doorLeftOpen = open; break;
            case 3: doorRightOpen = open; break;
        }
    }

    public boolean checkDoorTransition(Player player) {
        int playerTileX = (int)player.getX();
        int playerTileY = (int)player.getY();

        if (playerTileX < 0 || playerTileX >= template.getWidth() ||
                playerTileY < 0 || playerTileY >= template.getHeight()) {
            return false;
        }

        // Проверяем, стоит ли игрок на месте двери
        if (playerTileY == 0 && doorUpOpen) return true;
        if (playerTileY == template.getHeight()-1 && doorDownOpen) return true;
        if (playerTileX == 0 && doorLeftOpen) return true;
        if (playerTileX == template.getWidth()-1 && doorRightOpen) return true;

        return false;
    }

    public String getTransitionDirection(Player player) {
        int playerTileX = (int)player.getX();
        int playerTileY = (int)player.getY();

        if (playerTileY == 0 && doorUpOpen) return "UP";
        if (playerTileY == template.getHeight()-1 && doorDownOpen) return "DOWN";
        if (playerTileX == 0 && doorLeftOpen) return "LEFT";
        if (playerTileX == template.getWidth()-1 && doorRightOpen) return "RIGHT";

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
        if (!rewardClaimed && type != RoomType.START && type != RoomType.SHOP && type != RoomType.TREASURE) {
            rewardClaimed = true;
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.invincibilityTimer = 1.5f;
        spawnEnemiesIfNeeded();

        for (Enemy enemy : enemies) {
            enemy.setPlayer(player);
        }
    }

    public boolean isWall(int x, int y) {
        if (x < 0 || x >= template.getWidth() || y < 0 || y >= template.getHeight()) {
            return true;
        }
        RoomTemplate.TileType tile = template.getTiles()[x][y];
        return tile == RoomTemplate.TileType.WALL;
    }

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

        // Рисуем двери с окантовкой
        renderDoors(g, offsetX, offsetY);
    }

    private void renderDoors(Graphics2D g, int offsetX, int offsetY) {
        int width = template.getWidth();
        int height = template.getHeight();

        // Верхняя дверь
        if (doorUpOpen) {
            int doorX = (width / 2) * TILE_SIZE + offsetX;
            int doorY = 0 * TILE_SIZE + offsetY;
            drawDoor(g, doorX, doorY, doorUpType, true);
        }

        // Нижняя дверь
        if (doorDownOpen) {
            int doorX = (width / 2) * TILE_SIZE + offsetX;
            int doorY = (height - 1) * TILE_SIZE + offsetY;
            drawDoor(g, doorX, doorY, doorDownType, true);
        }

        // Левая дверь
        if (doorLeftOpen) {
            int doorX = 0 * TILE_SIZE + offsetX;
            int doorY = (height / 2) * TILE_SIZE + offsetY;
            drawDoor(g, doorX, doorY, doorLeftType, true);
        }

        // Правая дверь
        if (doorRightOpen) {
            int doorX = (width - 1) * TILE_SIZE + offsetX;
            int doorY = (height / 2) * TILE_SIZE + offsetY;
            drawDoor(g, doorX, doorY, doorRightType, true);
        }
    }

    private void drawDoor(Graphics2D g, int x, int y, DoorType doorType, boolean isOpen) {
        // Цвет окантовки в зависимости от типа двери
        Color borderColor;
        switch (doorType) {
            case BOSS:
                borderColor = new Color(255, 50, 50);
                break;
            case SHOP:
                borderColor = new Color(50, 255, 50);
                break;
            case CHALLENGE:
                borderColor = new Color(255, 255, 50);
                break;
            case TREASURE:
                borderColor = new Color(50, 100, 255);
                break;
            default:
                borderColor = new Color(150, 120, 70);
        }

        // Внутренность двери
        if (isOpen) {
            g.setColor(new Color(60, 45, 35));
        } else {
            g.setColor(new Color(40, 30, 25));
        }
        g.fillRect(x + 8, y + 8, TILE_SIZE - 16, TILE_SIZE - 16);

        // Окантовка
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(3));
        g.drawRect(x + 6, y + 6, TILE_SIZE - 12, TILE_SIZE - 12);
        g.setStroke(new BasicStroke(1));

        // Иконка для спецдверей
        if (doorType != DoorType.NORMAL && isOpen) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String icon = "";
            switch (doorType) {
                case BOSS: icon = "B"; break;
                case SHOP: icon = "$"; break;
                case CHALLENGE: icon = "!"; break;
                case TREASURE: icon = "T"; break;
            }
            FontMetrics fm = g.getFontMetrics();
            int textX = x + TILE_SIZE / 2 - fm.stringWidth(icon) / 2;
            int textY = y + TILE_SIZE / 2 + fm.getAscent() / 2 - 3;
            g.drawString(icon, textX, textY);
        }
    }

    private Color getTileColor(RoomTemplate.TileType tile) {
        switch (tile) {
            case FLOOR:
                return new Color(40, 35, 45);
            case WALL:
                return new Color(30, 25, 35);
            case DOOR:
                return new Color(80, 60, 40);
            case PIT:
                return new Color(20, 15, 25);
            case ROCK:
                return new Color(70, 65, 55);
            default:
                return Color.BLACK;
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

        if (cleared && centerReward != null && !rewardClaimed) {
            int screenX = (int)(centerReward.x * TILE_SIZE) + offsetX;
            int screenY = (int)(centerReward.y * TILE_SIZE) + offsetY;
            g.setColor(new Color(255, 215, 0, 200));
            g.fillOval(screenX - 12, screenY - 12, 24, 24);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            switch (centerReward.type) {
                case HEALTH:
                    g.drawString("❤", screenX - 5, screenY + 5);
                    break;
                case SHIELD:
                    g.drawString("🛡", screenX - 5, screenY + 5);
                    break;
                case ARROWS:
                    g.drawString("🏹", screenX - 5, screenY + 5);
                    break;
                case MANA:
                    g.drawString("✨", screenX - 5, screenY + 5);
                    break;
                default:
                    g.drawString("?", screenX - 5, screenY + 5);
                    break;
            }
        }
    }

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

    public void setDoorUpOpen(boolean open) { this.doorUpOpen = open; }
    public void setDoorDownOpen(boolean open) { this.doorDownOpen = open; }
    public void setDoorLeftOpen(boolean open) { this.doorLeftOpen = open; }
    public void setDoorRightOpen(boolean open) { this.doorRightOpen = open; }

    private static class Pickup {
        float x, y;
        PickupType type;
        enum PickupType { HEALTH, SHIELD, ARROWS, MANA, GOLD }
        void collect(Player player) {
            switch (type) {
                case HEALTH:
                    player.heal(1);
                    break;
                case SHIELD:
                    player.addShield();
                    break;
                case ARROWS:
                    player.addArrows(10);
                    break;
                case MANA:
                    player.regenerateMana(2);
                    break;
                case GOLD:
                    player.addGold(15);
                    break;
            }
        }
    }
}