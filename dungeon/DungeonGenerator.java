package src.dungeon;

import java.util.*;

public class DungeonGenerator {
    private Map<String, Room> rooms;
    private Room startRoom;
    private Room currentRoom;
    private int floorLevel;
    private int gridSize;
    private boolean[][] roomGrid;
    private RoomTemplateLibrary library;
    private Random random;
    private boolean isTutorial;

    public DungeonGenerator(int floorLevel, boolean isTutorial) {
        this.floorLevel = floorLevel;
        this.isTutorial = isTutorial;
        this.library = RoomTemplateLibrary.getInstance();
        this.random = new Random();
        this.gridSize = isTutorial ? 5 : 7;
        this.rooms = new HashMap<>();
        generateDungeon();
    }

    private void generateDungeon() {
        roomGrid = new boolean[gridSize][gridSize];
        int startX = gridSize / 2;
        int startY = gridSize / 2;

        generateMaze(startX, startY);

        List<int[]> roomPositions = getRoomPositions();

        if (roomPositions.isEmpty()) {
            roomGrid[startX][startY] = true;
            roomPositions.add(new int[]{startX, startY});
        }

        sortRoomsByDistance(roomPositions, startX, startY);

        RoomTypeAssignment assignment = assignRoomTypes(roomPositions, startX, startY);

        createRooms(roomPositions, startX, startY, assignment);

        startRoom = rooms.get(startX + "," + startY);
        currentRoom = startRoom;
    }

    private void generateMaze(int startX, int startY) {
        roomGrid[startX][startY] = true;
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        int targetRooms = isTutorial ? 4 : Math.min(6 + floorLevel, 12);

        while (!stack.isEmpty() && countRooms() < targetRooms) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            List<int[]> neighbors = getUnvisitedNeighbors(x, y);

            if (!neighbors.isEmpty()) {
                int[] next = neighbors.get(random.nextInt(neighbors.size()));
                roomGrid[next[0]][next[1]] = true;
                stack.push(new int[]{next[0], next[1]});
            } else {
                stack.pop();
            }
        }
    }

    private List<int[]> getUnvisitedNeighbors(int x, int y) {
        List<int[]> neighbors = new ArrayList<>();
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (isValidCell(nx, ny) && !roomGrid[nx][ny]) {
                neighbors.add(new int[]{nx, ny});
            }
        }
        return neighbors;
    }

    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    private int countRooms() {
        int count = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (roomGrid[i][j]) count++;
            }
        }
        return count;
    }

    private List<int[]> getRoomPositions() {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (roomGrid[i][j]) {
                    positions.add(new int[]{i, j});
                }
            }
        }
        return positions;
    }

    private void sortRoomsByDistance(List<int[]> positions, int startX, int startY) {
        positions.sort((a, b) -> {
            int distA = Math.abs(a[0] - startX) + Math.abs(a[1] - startY);
            int distB = Math.abs(b[0] - startX) + Math.abs(b[1] - startY);
            return Integer.compare(distB, distA);
        });
    }

    private RoomTypeAssignment assignRoomTypes(List<int[]> positions, int startX, int startY) {
        RoomTypeAssignment assignment = new RoomTypeAssignment();

        assignment.bossIndex = 0;
        if (positions.get(assignment.bossIndex)[0] == startX &&
                positions.get(assignment.bossIndex)[1] == startY &&
                positions.size() > 1) {
            assignment.bossIndex = 1;
        }

        if (!isTutorial) {
            if (positions.size() > 3) {
                assignment.treasureIndex = 1;
                if (assignment.treasureIndex == assignment.bossIndex) {
                    assignment.treasureIndex++;
                }
            }

            if (positions.size() > 4 && floorLevel >= 1) {
                assignment.shopIndex = positions.size() / 2;
                if (assignment.shopIndex == assignment.bossIndex) assignment.shopIndex++;
                if (assignment.shopIndex == assignment.treasureIndex) assignment.shopIndex++;
            }
        }

        return assignment;
    }

    private void createRooms(List<int[]> positions, int startX, int startY, RoomTypeAssignment assignment) {
        // Сначала создаём все комнаты
        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            int x = pos[0];
            int y = pos[1];
            RoomType type = determineRoomType(x, y, startX, startY, i, assignment);

            boolean[] requiredDoors = getRequiredDoors(x, y);
            requiredDoors = fixDoorsIfNeeded(x, y, requiredDoors, type, positions);

            RoomTemplate template = selectTemplate(type, requiredDoors);
            Room room = new Room(template, floorLevel, x, y);
            rooms.put(x + "," + y, room);
        }

        // Затем устанавливаем связи и типы дверей между комнатами
        for (int i = 0; i < positions.size(); i++) {
            int[] pos = positions.get(i);
            int x = pos[0];
            int y = pos[1];
            Room room = rooms.get(x + "," + y);

            // Проверяем соседей
            if (y > 0 && rooms.containsKey(x + "," + (y-1))) {
                Room neighbor = rooms.get(x + "," + (y-1));
                if (neighbor.getType() == RoomType.BOSS) {
                    room.setDoorType(0, Room.DoorType.BOSS);
                } else if (neighbor.getType() == RoomType.SHOP) {
                    room.setDoorType(0, Room.DoorType.SHOP);
                } else if (neighbor.getType() == RoomType.TREASURE) {
                    room.setDoorType(0, Room.DoorType.TREASURE);
                }
                room.setDoorUpOpen(true);
            }

            if (y < gridSize-1 && rooms.containsKey(x + "," + (y+1))) {
                Room neighbor = rooms.get(x + "," + (y+1));
                if (neighbor.getType() == RoomType.BOSS) {
                    room.setDoorType(1, Room.DoorType.BOSS);
                } else if (neighbor.getType() == RoomType.SHOP) {
                    room.setDoorType(1, Room.DoorType.SHOP);
                } else if (neighbor.getType() == RoomType.TREASURE) {
                    room.setDoorType(1, Room.DoorType.TREASURE);
                }
                room.setDoorDownOpen(true);
            }

            if (x > 0 && rooms.containsKey((x-1) + "," + y)) {
                Room neighbor = rooms.get((x-1) + "," + y);
                if (neighbor.getType() == RoomType.BOSS) {
                    room.setDoorType(2, Room.DoorType.BOSS);
                } else if (neighbor.getType() == RoomType.SHOP) {
                    room.setDoorType(2, Room.DoorType.SHOP);
                } else if (neighbor.getType() == RoomType.TREASURE) {
                    room.setDoorType(2, Room.DoorType.TREASURE);
                }
                room.setDoorLeftOpen(true);
            }

            if (x < gridSize-1 && rooms.containsKey((x+1) + "," + y)) {
                Room neighbor = rooms.get((x+1) + "," + y);
                if (neighbor.getType() == RoomType.BOSS) {
                    room.setDoorType(3, Room.DoorType.BOSS);
                } else if (neighbor.getType() == RoomType.SHOP) {
                    room.setDoorType(3, Room.DoorType.SHOP);
                } else if (neighbor.getType() == RoomType.TREASURE) {
                    room.setDoorType(3, Room.DoorType.TREASURE);
                }
                room.setDoorRightOpen(true);
            }
        }
    }

    private RoomType determineRoomType(int x, int y, int startX, int startY, int index, RoomTypeAssignment assignment) {
        if (x == startX && y == startY) {
            return RoomType.START;
        } else if (index == assignment.bossIndex) {
            return RoomType.BOSS;
        } else if (index == assignment.shopIndex) {
            return RoomType.SHOP;
        } else if (index == assignment.treasureIndex) {
            return RoomType.TREASURE;
        }
        return RoomType.NORMAL;
    }

    private boolean[] getRequiredDoors(int x, int y) {
        boolean[] doors = new boolean[4];
        doors[0] = (y > 0 && roomGrid[x][y-1]);
        doors[1] = (y < gridSize-1 && roomGrid[x][y+1]);
        doors[2] = (x > 0 && roomGrid[x-1][y]);
        doors[3] = (x < gridSize-1 && roomGrid[x+1][y]);
        return doors;
    }

    private boolean[] fixDoorsIfNeeded(int x, int y, boolean[] doors, RoomType type, List<int[]> positions) {
        boolean[] result = doors.clone();

        if (type == RoomType.START) {
            boolean hasAnyDoor = result[0] || result[1] || result[2] || result[3];
            if (!hasAnyDoor && positions.size() > 1) {
                for (int[] otherPos : positions) {
                    if (otherPos[0] == x && otherPos[1] == y) continue;
                    int dx = otherPos[0] - x;
                    int dy = otherPos[1] - y;
                    if (Math.abs(dx) + Math.abs(dy) == 1) {
                        if (dy == -1) result[0] = true;
                        if (dy == 1) result[1] = true;
                        if (dx == -1) result[2] = true;
                        if (dx == 1) result[3] = true;
                        break;
                    }
                }
            }
        }

        if (type == RoomType.BOSS) {
            boolean hasAnyDoor = result[0] || result[1] || result[2] || result[3];
            if (!hasAnyDoor && positions.size() > 1) {
                result[0] = true;
            }
        }

        return result;
    }

    private RoomTemplate selectTemplate(RoomType type, boolean[] requiredDoors) {
        RoomTemplate template = library.getRandomTemplate(type, requiredDoors);

        if (template == null && type != RoomType.NORMAL) {
            template = library.getRandomTemplate(RoomType.NORMAL, requiredDoors);
        }

        if (template == null) {
            template = createFallbackTemplate(requiredDoors);
        }

        return template;
    }

    private RoomTemplate createFallbackTemplate(boolean[] requiredDoors) {
        RoomTemplate t = new RoomTemplate("fallback", "Fallback Room", 20, 15, RoomType.NORMAL);
        t.setDoor(0, requiredDoors[0]);
        t.setDoor(1, requiredDoors[1]);
        t.setDoor(2, requiredDoors[2]);
        t.setDoor(3, requiredDoors[3]);
        t.addEnemy(8, 7, "BLUE_SLIME");
        t.addEnemy(12, 7, "RED_SLIME");
        t.addEnemy(10, 5, "SKELETON_MELEE");
        return t;
    }

    public Room getStartRoom() {
        return startRoom;
    }

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
        }
        return nextRoom;
    }

    public boolean isFloorComplete() {
        for (Room room : rooms.values()) {
            if (room.getType() == RoomType.BOSS && !room.isCleared()) {
                return false;
            }
        }
        return true;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public Map<String, Room> getAllRooms() {
        return rooms;
    }

    private static class RoomTypeAssignment {
        int bossIndex = -1;
        int shopIndex = -1;
        int treasureIndex = -1;
    }
}