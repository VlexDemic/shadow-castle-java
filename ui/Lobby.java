package src.ui;

import src.entity.Player;
import src.core.Game.WeaponType;
import src.core.InputHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private List<WeaponPedestal> weaponPedestals = new ArrayList<>();
    private int selectedWeaponIndex = 0;
    private float time = 0;
    private boolean weaponSelected = false;
    private WeaponType chosenWeapon = null;
    private float cameraX = 0, cameraY = 0;
    private final int TILE_SIZE = 48;

    public Lobby() {
        WeaponType[] weapons = {WeaponType.DAGGER, WeaponType.SWORD, WeaponType.SPEAR, WeaponType.AXE};
        String[] weaponNames = {"Кинжал", "Меч", "Копьё", "Топор"};
        String[] descriptions = {
                "Быстрые атаки, маленький радиус",
                "Сбалансированное оружие среднего радиуса",
                "Медленные, но мощные атаки с большим радиусом",
                "Конусные атаки по площади"
        };
        int[] damages = {5, 10, 20, 15};

        float centerX = 10;
        float centerY = 7.5f;
        float radius = 3.5f;

        for (int i = 0; i < weapons.length; i++) {
            float angle = (float)(Math.PI * 2 * i / weapons.length);
            float x = centerX + (float)Math.cos(angle) * radius;
            float y = centerY + (float)Math.sin(angle) * radius;
            weaponPedestals.add(new WeaponPedestal(x, y, weapons[i], weaponNames[i], descriptions[i], damages[i]));
        }
    }

    public void update(float delta, InputHandler input, Player player) {
        time += delta;
        if (weaponSelected) return;

        float speed = 6f;
        float dx = 0, dy = 0;
        if (input.isKeyPressed(KeyEvent.VK_W)) dy -= speed * delta;
        if (input.isKeyPressed(KeyEvent.VK_S)) dy += speed * delta;
        if (input.isKeyPressed(KeyEvent.VK_A)) dx -= speed * delta;
        if (input.isKeyPressed(KeyEvent.VK_D)) dx += speed * delta;
        if (dx != 0 && dy != 0) { dx *= 0.707f; dy *= 0.707f; }

        float newX = player.getX() + dx;
        float newY = player.getY() + dy;
        newX = Math.max(3, Math.min(17, newX));
        newY = Math.max(2, Math.min(13, newY));
        player.setPosition(newX, newY);

        selectedWeaponIndex = -1;
        for (int i = 0; i < weaponPedestals.size(); i++) {
            WeaponPedestal pedestal = weaponPedestals.get(i);
            float distToPlayer = (float)Math.hypot(player.getX() - pedestal.x, player.getY() - pedestal.y);
            if (distToPlayer < 1.2f) {
                selectedWeaponIndex = i;
                if (input.isKeyJustPressed(KeyEvent.VK_E)) {
                    chosenWeapon = pedestal.weaponType;
                    weaponSelected = true;
                }
            }
        }
        cameraX = player.getX();
        cameraY = player.getY();
    }

    public void render(Graphics2D g, Player player, int screenWidth, int screenHeight) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(15, 10, 20),
                screenWidth, screenHeight, new Color(35, 20, 45));
        g.setPaint(gp);
        g.fillRect(0, 0, screenWidth, screenHeight);

        int offsetX = screenWidth / 2 - (int)(cameraX * TILE_SIZE);
        int offsetY = screenHeight / 2 - (int)(cameraY * TILE_SIZE);

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 15; j++) {
                int screenX = i * TILE_SIZE + offsetX;
                int screenY = j * TILE_SIZE + offsetY;
                g.setColor((i + j) % 2 == 0 ? new Color(50, 40, 60) : new Color(60, 45, 70));
                g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(80, 60, 90));
                g.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
            }
        }

        for (WeaponPedestal pedestal : weaponPedestals) {
            pedestal.render(g, cameraX, cameraY, screenWidth, screenHeight,
                    selectedWeaponIndex == weaponPedestals.indexOf(pedestal));
        }

        renderPlayer(g, player, screenWidth, screenHeight);
        renderUI(g, screenWidth, screenHeight, player);
        renderParticles(g, screenWidth, screenHeight);
    }

    private void renderPlayer(Graphics2D g, Player player, int screenWidth, int screenHeight) {
        int offsetX = screenWidth / 2 - (int)(cameraX * TILE_SIZE);
        int offsetY = screenHeight / 2 - (int)(cameraY * TILE_SIZE);
        int screenX = (int)(player.getX() * TILE_SIZE) + offsetX;
        int screenY = (int)(player.getY() * TILE_SIZE) + offsetY;
        player.render(g, screenX, screenY);
    }

    private void renderUI(Graphics2D g, int screenWidth, int screenHeight, Player player) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(20, screenHeight - 150, 420, 130);
        g.setColor(new Color(200, 150, 50));
        g.drawRect(20, screenHeight - 150, 420, 130);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("ЛОББИ - ВЫБОР ОСНОВНОГО ОРУЖИЯ", 30, screenHeight - 120);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Подойдите к пьедесталу и нажмите E", 30, screenHeight - 90);
        g.drawString("WASD - Движение", 30, screenHeight - 65);
        g.drawString("ESC - Выйти в меню", 30, screenHeight - 40);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 180, 100));
        g.drawString("🏹 Лук доступен как вспомогательное оружие (ПКМ)", screenWidth - 350, screenHeight - 40);

        if (selectedWeaponIndex >= 0 && !weaponSelected) {
            WeaponPedestal selected = weaponPedestals.get(selectedWeaponIndex);

            g.setColor(new Color(0, 0, 0, 220));
            g.fillRect(screenWidth - 450, screenHeight - 200, 430, 180);
            g.setColor(new Color(255, 215, 0));
            g.drawRect(screenWidth - 450, screenHeight - 200, 430, 180);

            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.setColor(Color.YELLOW);
            g.drawString(selected.name, screenWidth - 430, screenHeight - 160);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.WHITE);
            g.drawString(selected.description, screenWidth - 430, screenHeight - 125);

            g.setColor(Color.CYAN);
            g.drawString("Урон: " + selected.damage, screenWidth - 430, screenHeight - 95);

            g.setColor(new Color(100, 200, 100));
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("[E] - ВЗЯТЬ ОРУЖИЕ", screenWidth - 430, screenHeight - 35);
        }

        if (!weaponSelected) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(new Color(255, 200, 100));
            String msg = "ВЫБЕРИТЕ ОСНОВНОЕ ОРУЖИЕ";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, screenWidth/2 - fm.stringWidth(msg)/2, 50);
        }
    }

    private void renderParticles(Graphics2D g, int screenWidth, int screenHeight) {
        for (int i = 0; i < 30; i++) {
            float angle = (float)(time * 0.5f + i);
            float x = (float)(Math.sin(angle) * 100 + Math.cos(time + i) * 50);
            float y = (float)(Math.cos(angle * 1.3f) * 80 + Math.sin(time * 0.7f + i) * 40);
            g.setColor(new Color(150, 100, 200, 100));
            g.fillOval((int)(screenWidth/2 + x), (int)(screenHeight/2 + y), 4, 4);
        }
    }

    public boolean isWeaponSelected() { return weaponSelected; }
    public WeaponType getChosenWeapon() { return chosenWeapon; }

    private class WeaponPedestal {
        float x, y;
        WeaponType weaponType;
        String name;
        String description;
        int damage;
        float animationTime = 0;

        WeaponPedestal(float x, float y, WeaponType type, String name, String desc, int damage) {
            this.x = x;
            this.y = y;
            this.weaponType = type;
            this.name = name;
            this.description = desc;
            this.damage = damage;
        }

        void render(Graphics2D g, float camX, float camY, int screenWidth, int screenHeight, boolean isSelected) {
            animationTime += 0.05f;
            int offsetX = screenWidth / 2 - (int)(camX * TILE_SIZE);
            int offsetY = screenHeight / 2 - (int)(camY * TILE_SIZE);
            int screenX = (int)(x * TILE_SIZE) + offsetX;
            int screenY = (int)(y * TILE_SIZE) + offsetY;

            if (isSelected) {
                g.setColor(new Color(255, 215, 0, 100));
                g.fillOval(screenX - 30, screenY - 30, 60, 60);
            }

            g.setColor(new Color(100, 70, 40));
            g.fillRect(screenX - 18, screenY - 8, 36, 20);
            g.setColor(new Color(80, 50, 30));
            g.fillRect(screenX - 14, screenY - 14, 28, 8);

            float floatY = (float)Math.sin(animationTime) * 4;
            Color weaponColor;
            switch (weaponType) {
                case DAGGER: weaponColor = Color.CYAN; break;
                case SWORD: weaponColor = Color.GREEN; break;
                case SPEAR: weaponColor = Color.ORANGE; break;
                case AXE: weaponColor = Color.RED; break;
                default: weaponColor = Color.YELLOW;
            }
            g.setColor(weaponColor);
            g.fillRect(screenX - 8, screenY - 28 + (int)floatY, 16, 20);
            g.setColor(Color.WHITE);
            g.drawRect(screenX - 8, screenY - 28 + (int)floatY, 16, 20);

            if (isSelected) {
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.setColor(Color.YELLOW);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(name, screenX - fm.stringWidth(name)/2, screenY - 35);
            }
        }
    }
}