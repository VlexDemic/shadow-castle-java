package src.ui;

import src.dungeon.Room;
import src.dungeon.DungeonGenerator;
import src.dungeon.RoomType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Minimap {
    private int minimapSize = 13;
    private int cellSize = 20;
    private int offsetX;
    private int offsetY;
    private int padding = 5;

    private Color colorUnvisited = new Color(30, 30, 40, 150);
    private Color colorVisited = new Color(60, 60, 80, 200);
    private Color colorCleared = new Color(80, 140, 80, 200);
    private Color colorBoss = new Color(180, 50, 50, 200);
    private Color colorShop = new Color(50, 150, 200, 200);
    private Color colorTreasure = new Color(200, 150, 50, 200);
    private Color colorSecret = new Color(150, 50, 200, 200);
    private Color colorStart = new Color(80, 180, 80, 200);

    private Map<String, RoomInfo> roomMap = new HashMap<>();
    private int currentX, currentY;

    private static class RoomInfo {
        RoomType type;
        boolean visited;
        boolean cleared;
        int x, y;

        RoomInfo(RoomType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.visited = false;
            this.cleared = false;
        }
    }

    public void updatePosition(int screenWidth, int screenHeight) {
        int mapWidth = minimapSize * cellSize + padding * 2;
        offsetX = screenWidth - mapWidth - 20;
        offsetY = 20;
    }

    public void updateMap(DungeonGenerator dungeon, Room currentRoom) {
        if (dungeon == null) return;
        currentX = currentRoom.getGridX();
        currentY = currentRoom.getGridY();
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight) {
        updatePosition(screenWidth, screenHeight);

        int mapWidth = minimapSize * cellSize + padding * 2;
        int mapHeight = minimapSize * cellSize + padding * 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(offsetX - padding, offsetY - padding, mapWidth, mapHeight, 10, 10);
        g.setColor(new Color(100, 80, 50, 150));
        g.drawRoundRect(offsetX - padding, offsetY - padding, mapWidth, mapHeight, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("MAP", offsetX, offsetY - 5);

        int startX = Math.max(0, currentX - 4);
        int endX = Math.min(minimapSize - 1, currentX + 4);
        int startY = Math.max(0, currentY - 4);
        int endY = Math.min(minimapSize - 1, currentY + 4);

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                int screenX = offsetX + (i - (currentX - 4)) * cellSize;
                int screenY = offsetY + (j - (currentY - 4)) * cellSize;

                if (screenX < offsetX - cellSize || screenX > offsetX + mapWidth ||
                        screenY < offsetY - cellSize || screenY > offsetY + mapHeight) {
                    continue;
                }

                String key = i + "," + j;
                RoomInfo info = roomMap.get(key);

                if (info != null && info.visited) {
                    Color color = getRoomColor(info.type, info.cleared);
                    g.setColor(color);
                    g.fillRect(screenX, screenY, cellSize - 2, cellSize - 2);
                    g.setColor(new Color(255, 255, 255, 100));
                    g.drawRect(screenX, screenY, cellSize - 2, cellSize - 2);

                    if (i == currentX && j == currentY) {
                        g.setColor(Color.WHITE);
                        g.drawRect(screenX - 1, screenY - 1, cellSize, cellSize);
                        g.drawRect(screenX - 2, screenY - 2, cellSize + 2, cellSize + 2);
                    }

                    drawRoomIcon(g, screenX, screenY, info.type, info.cleared);
                } else if (info != null) {
                    g.setColor(colorUnvisited);
                    g.fillRect(screenX, screenY, cellSize - 2, cellSize - 2);
                } else {
                    g.setColor(colorUnvisited);
                    g.fillRect(screenX, screenY, cellSize - 2, cellSize - 2);
                }
            }
        }

        g.setColor(new Color(150, 120, 70, 180));
        g.drawRoundRect(offsetX - padding, offsetY - padding, mapWidth, mapHeight, 10, 10);
    }

    private Color getRoomColor(RoomType type, boolean cleared) {
        if (cleared) return colorCleared;
        switch (type) {
            case START: return colorStart;
            case BOSS: return colorBoss;
            case SHOP: return colorShop;
            case TREASURE: return colorTreasure;
            case SECRET: return colorSecret;
            default: return colorVisited;
        }
    }

    private void drawRoomIcon(Graphics2D g, int x, int y, RoomType type, boolean cleared) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));

        if (cleared) {
            g.setColor(new Color(100, 200, 100));
            g.fillRect(x + cellSize / 2 - 2, y + cellSize / 2 - 2, 4, 4);
            return;
        }

        switch (type) {
            case START: g.drawString("S", x + cellSize / 2 - 3, y + cellSize / 2 + 3); break;
            case BOSS: g.drawString("B", x + cellSize / 2 - 3, y + cellSize / 2 + 3); break;
            case SHOP: g.drawString("$", x + cellSize / 2 - 3, y + cellSize / 2 + 3); break;
            case TREASURE: g.drawString("T", x + cellSize / 2 - 3, y + cellSize / 2 + 3); break;
            case SECRET: g.drawString("?", x + cellSize / 2 - 3, y + cellSize / 2 + 3); break;
            default: break;
        }
    }

    public void addRoom(int x, int y, RoomType type) {
        String key = x + "," + y;
        if (!roomMap.containsKey(key)) {
            roomMap.put(key, new RoomInfo(type, x, y));
        }
    }

    public void markRoomVisited(int x, int y, boolean cleared) {
        String key = x + "," + y;
        RoomInfo info = roomMap.get(key);
        if (info != null) {
            info.visited = true;
            info.cleared = cleared;
        }
    }

    public void reset() {
        roomMap.clear();
    }
}