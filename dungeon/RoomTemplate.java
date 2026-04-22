package src.dungeon;

import java.util.ArrayList;
import java.util.List;

public class RoomTemplate {
    private String id;
    private String name;
    private int width;
    private int height;
    private RoomType type;
    private boolean[] doors;
    private TileType[][] tiles;
    private List<EnemySpawn> enemySpawns;
    private List<Decoration> decorations;

    public enum TileType {
        FLOOR, WALL, DOOR, PIT, ROCK, POOP
    }

    public static class EnemySpawn {
        public int x, y;
        public String enemyType;
        public EnemySpawn(int x, int y, String enemyType) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
        }
    }

    public static class Decoration {
        public int x, y;
        public String type;
        public Decoration(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    public RoomTemplate(String id, String name, int width, int height, RoomType type) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.type = type;
        this.doors = new boolean[4];
        this.tiles = new TileType[width][height];
        this.enemySpawns = new ArrayList<>();
        this.decorations = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || i == width-1 || j == 0 || j == height-1) {
                    tiles[i][j] = TileType.WALL;
                } else {
                    tiles[i][j] = TileType.FLOOR;
                }
            }
        }
    }

    public void setDoor(int direction, boolean hasDoor) {
        doors[direction] = hasDoor;
        if (hasDoor) {
            int doorX = width / 2;
            int doorY = height / 2;
            switch (direction) {
                case 0: tiles[doorX][0] = TileType.DOOR; break;
                case 1: tiles[doorX][height-1] = TileType.DOOR; break;
                case 2: tiles[0][doorY] = TileType.DOOR; break;
                case 3: tiles[width-1][doorY] = TileType.DOOR; break;
            }
        }
    }

    public void setTile(int x, int y, TileType tileType) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = tileType;
        }
    }

    public void addEnemy(int x, int y, String enemyType) {
        enemySpawns.add(new EnemySpawn(x, y, enemyType));
    }

    public void addDecoration(int x, int y, String type) {
        decorations.add(new Decoration(x, y, type));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public RoomType getType() { return type; }
    public boolean[] getDoors() { return doors; }
    public TileType[][] getTiles() { return tiles; }
    public List<EnemySpawn> getEnemySpawns() { return enemySpawns; }
    public List<Decoration> getDecorations() { return decorations; }
}