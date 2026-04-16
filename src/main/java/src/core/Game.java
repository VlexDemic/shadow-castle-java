package src.core;

import src.dungeon.DungeonGenerator;
import src.dungeon.Room;
import src.entity.Player;
import src.ui.HUD;
import src.ui.MainMenu;
import src.ui.Lobby;
import src.ui.SettingsMenu;
import src.core.KeyBindings.Action;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class Game implements Runnable {
    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bs;
    private Graphics2D g;

    private boolean running = false;
    private Thread gameThread;

    private Player player;
    private Room currentRoom;
    private DungeonGenerator dungeon;
    private HUD hud;
    private InputHandler input;
    private Camera camera;
    private MainMenu mainMenu;
    private Lobby lobby;
    private SettingsMenu settingsMenu;
    private KeyBindings keyBindings;

    public enum GameState { MAIN_MENU, LOBBY, EXPLORING, SHOP, GAME_OVER, VICTORY, SETTINGS }
    private GameState state = GameState.MAIN_MENU;

    private int currentFloor = 1;

    private boolean fullscreen = true;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.7f;

    private final int TICKS_PER_SECOND = 120;
    private final long TICK_TIME = 1000000000 / TICKS_PER_SECOND;
    private float deltaPerTick = 1.0f / TICKS_PER_SECOND;

    private long lastTick = System.nanoTime();

    private int fps = 0, tps = 0;
    private int fpsCounter = 0, tpsCounter = 0;
    private long fpsTimer = System.currentTimeMillis();

    private boolean isTransitioning = false;
    private float transitionTimer = 0;
    private String targetDirection = "";

    public Game() {
        keyBindings = KeyBindings.getInstance();
        initWindow();
        input = new InputHandler();
        canvas.addKeyListener(input);
        canvas.addMouseListener(input);
        canvas.addMouseMotionListener(input);
        canvas.setFocusable(true);

        hud = new HUD();
        camera = new Camera();
        mainMenu = new MainMenu();
        settingsMenu = new SettingsMenu();

        initLobby();
    }

    private void initWindow() {
        frame = new JFrame("Gungeon-style Roguelite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        canvas = new Canvas();
        canvas.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        canvas.setBackground(Color.BLACK);

        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        canvas.createBufferStrategy(3);
        bs = canvas.getBufferStrategy();
    }

    public void start() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        lastTick = System.nanoTime();

        while (running) {
            long now = System.nanoTime();

            if (now - lastTick >= TICK_TIME) {
                int ticksToProcess = (int)((now - lastTick) / TICK_TIME);
                if (ticksToProcess > 5) ticksToProcess = 5;

                for (int i = 0; i < ticksToProcess; i++) {
                    update(deltaPerTick);
                    tpsCounter++;
                }

                lastTick += ticksToProcess * TICK_TIME;
            }

            render();
            fpsCounter++;

            if (System.currentTimeMillis() - fpsTimer >= 1000) {
                fps = fpsCounter;
                tps = tpsCounter;
                fpsCounter = 0;
                tpsCounter = 0;
                fpsTimer = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update(float delta) {
        if (isTransitioning) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                completeTransition();
            }
            input.update();
            return;
        }

        switch (state) {
            case MAIN_MENU:
                int selected = mainMenu.update(input);
                if (selected == 0) {
                    resetLobby();
                    state = GameState.LOBBY;
                } else if (selected == 1) {
                    state = GameState.SETTINGS;
                } else if (selected == 2) {
                    System.exit(0);
                }
                break;

            case SETTINGS:
                int settingsResult = settingsMenu.update(input);
                if (settingsResult == 0) {
                    state = GameState.MAIN_MENU;
                }
                break;

            case LOBBY:
                lobby.update(delta, input, player);
                if (lobby.isWeaponSelected()) {
                    startGameWithWeapon(lobby.getChosenWeapon());
                }
                if (keyBindings.getKey(Action.PAUSE) != 0 && input.isKeyJustPressed(keyBindings.getKey(Action.PAUSE))) {
                    state = GameState.MAIN_MENU;
                }
                break;

            case EXPLORING:
                player.update(delta, input, currentRoom);
                currentRoom.update(delta, player);

                if (player.getHp() <= 0) {
                    state = GameState.GAME_OVER;
                    return;
                }

                if (currentRoom.isCleared() && !currentRoom.isRewardClaimed()) {
                    currentRoom.claimReward(player);
                    player.regenerateMana(1);
                    player.addArrows(5 + (int)(Math.random() * 6));
                }

                if (currentRoom.checkDoorTransition(player)) {
                    targetDirection = currentRoom.getTransitionDirection(player);
                    if (targetDirection != null) {
                        isTransitioning = true;
                        transitionTimer = 0.3f;
                    }
                }

                if (dungeon.isFloorComplete()) {
                    currentFloor++;
                    if (currentFloor > 5) {
                        state = GameState.VICTORY;
                    } else {
                        loadFloor(currentFloor);
                    }
                }
                break;

            case GAME_OVER:
                if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isLeftMouseJustPressed()) {
                    state = GameState.MAIN_MENU;
                    initLobby();
                }
                break;

            case VICTORY:
                if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isLeftMouseJustPressed()) {
                    state = GameState.MAIN_MENU;
                    initLobby();
                }
                break;
        }

        input.update();
    }

    private void completeTransition() {
        Room nextRoom = dungeon.moveToRoom(targetDirection);
        if (nextRoom != null) {
            currentRoom = nextRoom;
            currentRoom.setPlayer(player);

            switch (targetDirection) {
                case "UP":
                    player.setPosition(10, 13);
                    break;
                case "DOWN":
                    player.setPosition(10, 2);
                    break;
                case "LEFT":
                    player.setPosition(18, 7.5f);
                    break;
                case "RIGHT":
                    player.setPosition(2, 7.5f);
                    break;
            }
            System.out.println("Transition complete, new room: " + currentRoom.getGridX() + "," + currentRoom.getGridY());
        } else {
            System.out.println("Transition failed: no room in direction " + targetDirection);
        }
        isTransitioning = false;
    }

    private void render() {
        g = (Graphics2D) bs.getDrawGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case MAIN_MENU:
                mainMenu.render(g, canvas.getWidth(), canvas.getHeight());
                break;
            case SETTINGS:
                settingsMenu.render(g, canvas.getWidth(), canvas.getHeight());
                break;
            case LOBBY:
                lobby.render(g, player, canvas.getWidth(), canvas.getHeight());
                break;
            case EXPLORING:
                camera.render(g, currentRoom, player, canvas.getWidth(), canvas.getHeight());
                player.setCameraPosition(camera.getCamX(), camera.getCamY());  // Добавьте эту строку
                hud.render(g, player, currentFloor, canvas.getWidth(), canvas.getHeight());

                if (isTransitioning) {
                    float alpha = Math.min(1.0f, transitionTimer / 0.3f);
                    g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                    g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                }
                break;
            case GAME_OVER:
                renderGameOver();
                break;
            case VICTORY:
                renderVictory();
                break;
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("FPS: " + fps + " | TPS: " + tps, canvas.getWidth() - 150, 20);

        bs.show();
        g.dispose();
    }

    private void renderGameOver() {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (canvas.getWidth() - fm.stringWidth(msg)) / 2, canvas.getHeight() / 2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        String restart = "Нажмите ENTER или ЛКМ для возврата в главное меню";
        fm = g.getFontMetrics();
        g.drawString(restart, (canvas.getWidth() - fm.stringWidth(restart)) / 2, canvas.getHeight() / 2 + 50);
    }

    private void renderVictory() {
        GradientPaint gp = new GradientPaint(0, 0, new Color(255, 215, 0),
                canvas.getWidth(), canvas.getHeight(), new Color(255, 100, 0));
        g.setPaint(gp);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        String msg = "ПОБЕДА!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (canvas.getWidth() - fm.stringWidth(msg)) / 2, canvas.getHeight() / 2 - 50);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        String restart = "Нажмите ENTER или ЛКМ для возврата в главное меню";
        fm = g.getFontMetrics();
        g.drawString(restart, (canvas.getWidth() - fm.stringWidth(restart)) / 2, canvas.getHeight() / 2 + 50);
    }

    private void startGameWithWeapon(WeaponType type) {
        player = new Player(10, 10, type);
        currentFloor = 1;
        loadFloor(1);
        state = GameState.EXPLORING;
    }

    private void loadFloor(int floor) {
        dungeon = new DungeonGenerator(floor);
        currentRoom = dungeon.getStartRoom();
        currentRoom.setPlayer(player);
        player.setPosition(10, 10);
        isTransitioning = false;
    }

    private void resetLobby() {
        lobby = new Lobby();
        player = new Player(10, 7.5f, WeaponType.DAGGER);
        player.setPosition(10, 7.5f);
    }

    private void initLobby() {
        lobby = new Lobby();
        player = new Player(10, 7.5f, WeaponType.DAGGER);
        player.setPosition(10, 7.5f);
    }

    public enum WeaponType {
        DAGGER(5, 0.4f, 1.2f, false),
        SWORD(10, 0.6f, 1.8f, false),
        SPEAR(20, 0.9f, 3.0f, false),
        AXE(15, 0.7f, 2.2f, true),
        BOW(5, 0.7f, 10.0f, false);

        public final int damage;
        public final float attackDelay;
        public final float range;
        public final boolean isCone;

        WeaponType(int damage, float attackDelay, float range, boolean isCone) {
            this.damage = damage;
            this.attackDelay = attackDelay;
            this.range = range;
            this.isCone = isCone;
        }
    }
}