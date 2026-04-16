package src.dungeon;

import java.util.*;

public class RoomTemplateLibrary {
    private static RoomTemplateLibrary instance;
    private Map<String, List<RoomTemplate>> templatesByType;
    private Map<String, RoomTemplate> templatesById;

    private RoomTemplateLibrary() {
        templatesByType = new HashMap<>();
        templatesById = new HashMap<>();
        initTemplates();
    }

    public static RoomTemplateLibrary getInstance() {
        if (instance == null) {
            instance = new RoomTemplateLibrary();
        }
        return instance;
    }

    private void initTemplates() {
        createStartRoom();
        createNormalRoom1();
        createNormalRoom2();
        createNormalRoom3();
        createNormalRoom4();
        createNormalRoom5();
        createNormalRoom6();
        createNormalRoom7();
        createNormalRoom8();
        createPitRoom();
        createRockRoom();
        createBossRoom();
        createShopRoom();
        createTreasureRoom();
    }

    public RoomTemplate getTemplateById(String id) {
        return templatesById.get(id);
    }

    private void createStartRoom() {
        RoomTemplate t = new RoomTemplate("start_01", "Starting Room", 20, 15, Room.RoomType.START);
        // START комната должна иметь двери во все стороны, чтобы быть связанной
        t.setDoor(0, true); // UP
        t.setDoor(1, true); // DOWN
        t.setDoor(2, true); // LEFT
        t.setDoor(3, true); // RIGHT
        registerTemplate(t);
    }

    private void createNormalRoom1() {
        RoomTemplate t = new RoomTemplate("normal_01", "Basic Room 1", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true); t.setDoor(2, true); t.setDoor(3, true);
        t.addEnemy(5, 5, "BLUE_SLIME");
        t.addEnemy(14, 5, "BLUE_SLIME");
        t.addEnemy(5, 9, "RED_SLIME");
        t.addEnemy(14, 9, "RED_SLIME");
        registerTemplate(t);
    }

    private void createNormalRoom2() {
        RoomTemplate t = new RoomTemplate("normal_02", "Basic Room 2", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true);
        t.addEnemy(10, 4, "SKELETON_MELEE");
        t.addEnemy(10, 10, "SKELETON_RANGED");
        registerTemplate(t);
    }

    private void createNormalRoom3() {
        RoomTemplate t = new RoomTemplate("normal_03", "Four Corners", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true); t.setDoor(2, true); t.setDoor(3, true);
        t.addEnemy(4, 4, "BLUE_SLIME");
        t.addEnemy(15, 4, "BLUE_SLIME");
        t.addEnemy(4, 10, "RED_SLIME");
        t.addEnemy(15, 10, "SKELETON_MELEE");
        registerTemplate(t);
    }

    private void createNormalRoom4() {
        RoomTemplate t = new RoomTemplate("normal_04", "Cross Room", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true); t.setDoor(2, true); t.setDoor(3, true);
        t.addEnemy(10, 5, "SKELETON_RANGED");
        t.addEnemy(7, 7, "BLUE_SLIME");
        t.addEnemy(13, 7, "BLUE_SLIME");
        t.addEnemy(10, 9, "RED_SLIME");
        registerTemplate(t);
    }

    private void createNormalRoom5() {
        RoomTemplate t = new RoomTemplate("normal_05", "L-Shape", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(2, true); t.setDoor(1, true);
        t.addEnemy(8, 5, "SKELETON_MELEE");
        t.addEnemy(12, 9, "RED_SLIME");
        registerTemplate(t);
    }

    private void createNormalRoom6() {
        RoomTemplate t = new RoomTemplate("normal_06", "Dead End", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true);
        t.addEnemy(10, 7, "SKELETON_MELEE");
        t.addEnemy(7, 7, "BLUE_SLIME");
        t.addEnemy(13, 7, "BLUE_SLIME");
        registerTemplate(t);
    }

    private void createNormalRoom7() {
        RoomTemplate t = new RoomTemplate("normal_07", "Vertical Hall", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true);
        t.addEnemy(10, 4, "SKELETON_RANGED");
        t.addEnemy(10, 10, "SKELETON_RANGED");
        registerTemplate(t);
    }

    private void createNormalRoom8() {
        RoomTemplate t = new RoomTemplate("normal_08", "Horizontal Hall", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(2, true); t.setDoor(3, true);
        t.addEnemy(5, 7, "BLUE_SLIME");
        t.addEnemy(14, 7, "RED_SLIME");
        registerTemplate(t);
    }

    private void createPitRoom() {
        RoomTemplate t = new RoomTemplate("pit_01", "Pit Room", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true); t.setDoor(2, true); t.setDoor(3, true);
        for (int i = 3; i < 7; i++) {
            for (int j = 3; j < 7; j++) t.setTile(i, j, RoomTemplate.TileType.PIT);
            for (int j = 13; j < 17; j++) t.setTile(i, j, RoomTemplate.TileType.PIT);
        }
        t.addEnemy(10, 5, "SKELETON_RANGED");
        t.addEnemy(7, 9, "BLUE_SLIME");
        t.addEnemy(13, 9, "BLUE_SLIME");
        registerTemplate(t);
    }

    private void createRockRoom() {
        RoomTemplate t = new RoomTemplate("rock_01", "Rock Room", 20, 15, Room.RoomType.NORMAL);
        t.setDoor(0, true); t.setDoor(1, true);
        t.setTile(5, 7, RoomTemplate.TileType.ROCK);
        t.setTile(14, 7, RoomTemplate.TileType.ROCK);
        t.addEnemy(10, 4, "SKELETON_RANGED");
        t.addEnemy(10, 10, "SKELETON_RANGED");
        registerTemplate(t);
    }

    private void createBossRoom() {
        RoomTemplate t = new RoomTemplate("boss_01", "Boss Room", 20, 15, Room.RoomType.BOSS);
        t.setDoor(0, true);
        t.addEnemy(10, 7, "BOSS");
        registerTemplate(t);
    }

    private void createShopRoom() {
        RoomTemplate t = new RoomTemplate("shop_01", "Shop", 20, 15, Room.RoomType.SHOP);
        t.setDoor(0, true); t.setDoor(1, true); t.setDoor(2, true); t.setDoor(3, true);
        registerTemplate(t);
    }

    private void createTreasureRoom() {
        RoomTemplate t = new RoomTemplate("treasure_01", "Treasure Room", 20, 15, Room.RoomType.TREASURE);
        t.setDoor(0, true);
        t.addDecoration(10, 7, "CHEST");
        registerTemplate(t);
    }

    private void registerTemplate(RoomTemplate template) {
        templatesById.put(template.getId(), template);
        String typeKey = template.getType().toString();
        if (!templatesByType.containsKey(typeKey)) {
            templatesByType.put(typeKey, new ArrayList<>());
        }
        templatesByType.get(typeKey).add(template);
    }

    public RoomTemplate getRandomTemplate(Room.RoomType type, boolean[] requiredDoors) {
        String typeKey = type.toString();
        List<RoomTemplate> candidates = templatesByType.getOrDefault(typeKey, new ArrayList<>());
        List<RoomTemplate> matching = new ArrayList<>();

        for (RoomTemplate template : candidates) {
            boolean[] templateDoors = template.getDoors();
            boolean matches = true;
            for (int i = 0; i < 4; i++) {
                if (requiredDoors[i] && !templateDoors[i]) {
                    matches = false;
                    break;
                }
            }
            if (matches) matching.add(template);
        }

        if (matching.isEmpty() && !candidates.isEmpty()) return candidates.get(0);
        if (matching.isEmpty()) return null;

        Random rand = new Random();
        return matching.get(rand.nextInt(matching.size()));
    }
}