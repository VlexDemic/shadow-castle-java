package src.dungeon;

import java.util.*;

public class DungeonGenerator {
    private Map<String, Room> rooms = new HashMap<>();
    private Room startRoom;
    private Room currentRoom;
    private int floorLevel;
    private int gridSize = 13;
    private boolean[][] roomGrid;
    private RoomTemplateLibrary library;

    public DungeonGenerator(int floorLevel) {
        this.floorLevel = floorLevel;
        this.library = RoomTemplateLibrary.getInstance();
        generateDungeon();
    }

    private void generateDungeon() {
        roomGrid = new boolean[gridSize][gridSize];
        Random rand = new Random();
        int startX = gridSize / 2;
        int startY = gridSize / 2;

        generateMaze(startX, startY, rand);

        List<int[]> roomPositions = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (roomGrid[i][j]) {
                    roomPositions.add(new int[]{i, j});
                }
            }
        }

        if (roomPositions.isEmpty()) {
            System.err.println("ERROR: No rooms generated!");
            roomGrid[startX][startY] = true;
            roomPositions.add(new int[]{startX, startY});
        }

        // Вычисляем расстояния от старта
        List<RoomDistance> roomDistances = new ArrayList<>();
        for (int[] pos : roomPositions) {
            int dist = Math.abs(pos[0] - startX) + Math.abs(pos[1] - startY);
            roomDistances.add(new RoomDistance(pos[0], pos[1], dist));
        }

        // Сортируем по расстоянию (от дальних к ближним)
        roomDistances.sort((a, b) -> Integer.compare(b.distance, a.distance));

        // Назначаем комнаты
        boolean bossAssigned = false;
        boolean shopAssigned = false;
        boolean treasureAssigned = false;

        for (int i = 0; i < roomDistances.size(); i++) {
            RoomDistance rd = roomDistances.get(i);
            int x = rd.x;
            int y = rd.y;
            Room.RoomType type = Room.RoomType.NORMAL;

            // START комната
            if (x == startX && y == startY) {
                type = Room.RoomType.START;
            }
            // BOSS комната - самая дальняя от старта (первая в отсортированном списке, исключая START)
            else if (!bossAssigned && (x != startX || y != startY)) {
                type = Room.RoomType.BOSS;
                bossAssigned = true;
                System.out.println("BOSS room assigned at (" + x + "," + y + ") distance=" + rd.distance);
            }
            // TREASURE комната - вторая по дальности (или случайная)
            else if (!treasureAssigned && i > 0 && i < roomDistances.size() - 2) {
                type = Room.RoomType.TREASURE;
                treasureAssigned = true;
                System.out.println("TREASURE room assigned at (" + x + "," + y + ")");
            }
            // SHOP комната - на среднем расстоянии
            else if (!shopAssigned && floorLevel > 1 && rd.distance > 3 && rd.distance < 8) {
                type = Room.RoomType.SHOP;
                shopAssigned = true;
                System.out.println("SHOP room assigned at (" + x + "," + y + ")");
            }

            boolean[] requiredDoors = new boolean[4];
            requiredDoors[0] = (y > 0 && roomGrid[x][y-1]);
            requiredDoors[1] = (y < gridSize-1 && roomGrid[x][y+1]);
            requiredDoors[2] = (x > 0 && roomGrid[x-1][y]);
            requiredDoors[3] = (x < gridSize-1 && roomGrid[x+1][y]);

            // Для START комнаты убеждаемся, что есть двери
            if (type == Room.RoomType.START) {
                boolean hasAnyDoor = requiredDoors[0] || requiredDoors[1] || requiredDoors[2] || requiredDoors[3];
                if (!hasAnyDoor && roomPositions.size() > 1) {
                    // Ищем ближайшую комнату
                    for (RoomDistance other : roomDistances) {
                        if (other.x == x && other.y == y) continue;
                        int dx = other.x - x;
                        int dy = other.y - y;
                        if (Math.abs(dx) + Math.abs(dy) == 1) {
                            if (dy == -1) requiredDoors[0] = true;
                            if (dy == 1) requiredDoors[1] = true;
                            if (dx == -1) requiredDoors[2] = true;
                            if (dx == 1) requiredDoors[3] = true;
                            break;
                        }
                    }
                }
            }

            RoomTemplate template = library.getRandomTemplate(type, requiredDoors);

            if (template == null && type != Room.RoomType.NORMAL) {
                template = library.getRandomTemplate(Room.RoomType.NORMAL, requiredDoors);
            }

            if (template == null) {
                boolean[] anyDoors = new boolean[4];
                template = library.getRandomTemplate(Room.RoomType.NORMAL, anyDoors);
            }

            if (template == null) {
                template = createFallbackTemplate(requiredDoors);
            }

            Room room = new Room(template, floorLevel, x, y);
            rooms.put(x + "," + y, room);

            System.out.println("Created room at (" + x + "," + y + ") type: " + type +
                    " doors: U=" + requiredDoors[0] + " D=" + requiredDoors[1] +
                    " L=" + requiredDoors[2] + " R=" + requiredDoors[3]);
        }

        // Проверяем, что босс назначен
        if (!bossAssigned && roomPositions.size() > 1) {
            System.err.println("WARNING: Boss not assigned! Assigning to farthest room.");
            for (RoomDistance rd : roomDistances) {
                if (rd.x != startX || rd.y != startY) {
                    Room room = rooms.get(rd.x + "," + rd.y);
                    if (room != null) {
                        // Пересоздаём комнату как BOSS
                        RoomTemplate bossTemplate = library.getRandomTemplate(Room.RoomType.BOSS, new boolean[4]);
                        if (bossTemplate == null) {
                            bossTemplate = createFallbackTemplate(new boolean[4]);
                        }
                        Room newRoom = new Room(bossTemplate, floorLevel, rd.x, rd.y);
                        rooms.put(rd.x + "," + rd.y, newRoom);
                        System.out.println("FORCED BOSS room at (" + rd.x + "," + rd.y + ")");
                        break;
                    }
                }
            }
        }

        startRoom = rooms.get(startX + "," + startY);
        currentRoom = startRoom;

        // Выводим статистику
        int bossCount = 0;
        for (Room room : rooms.values()) {
            if (room.getType() == Room.RoomType.BOSS) bossCount++;
        }
        System.out.println("Dungeon generated: " + rooms.size() + " rooms, BOSS rooms: " + bossCount);
    }

    private RoomTemplate createFallbackTemplate(boolean[] requiredDoors) {
        RoomTemplate t = new RoomTemplate("fallback", "Fallback Room", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, requiredDoors[0]);
        t.setDoor(1, requiredDoors[1]);
        t.setDoor(2, requiredDoors[2]);
        t.setDoor(3, requiredDoors[3]);
        t.addEnemy(8, 7, "BLUE_SLIME");
        t.addEnemy(12, 7, "RED_SLIME");
        return t;
    }

    private void generateMaze(int startX, int startY, Random rand) {
        roomGrid[startX][startY] = true;
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        int maxDepth = 8 + floorLevel;

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            List<int[]> neighbors = new ArrayList<>();
            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize && !roomGrid[nx][ny]) {
                    if (stack.size() < maxDepth) {
                        neighbors.add(new int[]{nx, ny});
                    }
                }
            }

            if (!neighbors.isEmpty()) {
                int[] next = neighbors.get(rand.nextInt(neighbors.size()));
                roomGrid[next[0]][next[1]] = true;
                stack.push(new int[]{next[0], next[1]});
            } else {
                stack.pop();
            }
        }

        addExtraConnections(rand);
    }

    private void addExtraConnections(Random rand) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (roomGrid[i][j] && rand.nextFloat() < 0.25f) {
                    int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
                    for (int[] dir : dirs) {
                        int nx = i + dir[0];
                        int ny = j + dir[1];
                        if (nx >= 0 && nx < gridSize && ny >= 0 && ny < gridSize && !roomGrid[nx][ny]) {
                            roomGrid[nx][ny] = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    public Room getStartRoom() { return startRoom; }

    public Room moveToRoom(String direction) {
        int x = currentRoom.getGridX();
        int y = currentRoom.getGridY();
        switch (direction) {
            case "UP": y--; break;
            case "DOWN": y++; break;
            case "LEFT": x--; break;
            case "RIGHT": x++; break;
            default: return null;
        }
        Room nextRoom = rooms.get(x + "," + y);
        if (nextRoom != null) {
            currentRoom = nextRoom;
            System.out.println("Moved to room (" + x + "," + y + ")");
        }
        return nextRoom;
    }

    public boolean isFloorComplete() {
        for (Room room : rooms.values()) {
            if (room.getType() == Room.RoomType.BOSS && !room.isCleared()) {
                System.out.println("BOSS room not cleared yet");
                return false;
            }
        }
        System.out.println("Floor " + floorLevel + " complete!");
        return true;
    }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }

    private static class RoomDistance {
        int x, y, distance;
        RoomDistance(int x, int y, int distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }
    }
}