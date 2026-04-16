package src.entity;

import src.core.Game.WeaponType;
import src.core.InputHandler;
import src.core.KeyBindings;
import src.core.KeyBindings.Action;
import src.dungeon.Room;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private float x, y;
    private int hp, maxHp;
    private int shields;
    private int mana, maxMana;
    private int arrows;

    private WeaponType weapon;
    private float attackCooldown = 0;
    private int comboCounter = 0;
    private float comboResetTimer = 0;
    private boolean isAttacking = false;
    private float attackTimer = 0;

    private boolean shadowMode = false;
    private float shadowModeTimer = 0;
    private float shadowModeCooldown = 0;

    private float invincibilityTimer = 0;
    private final float INVINCIBILITY_DURATION = 0.5f;

    private float speed = 6f;
    private int goldNuggets = 0;

    private List<Fireball> fireballs = new ArrayList<>();
    private List<Arrow> arrows_list = new ArrayList<>();

    private KeyBindings keyBindings;
    private float camX, camY;

    // Флаг для стрельбы из лука (ПКМ)
    private float bowCooldown = 0;
    private final float BOW_DELAY = 0.4f;

    public Player(float startX, float startY, WeaponType weapon) {
        this.x = startX;
        this.y = startY;
        this.hp = 6;
        this.maxHp = 20;
        this.shields = 0;
        this.mana = 10;
        this.maxMana = 10;
        this.arrows = 50;
        this.weapon = weapon;
        this.keyBindings = KeyBindings.getInstance();
    }

    public void setCameraPosition(float camX, float camY) {
        this.camX = camX;
        this.camY = camY;
    }

    public void update(float delta, InputHandler input, Room currentRoom) {
        if (invincibilityTimer > 0) invincibilityTimer -= delta;

        if (attackCooldown > 0) attackCooldown -= delta;
        if (bowCooldown > 0) bowCooldown -= delta;
        if (comboResetTimer > 0) {
            comboResetTimer -= delta;
            if (comboResetTimer <= 0) comboCounter = 0;
        }
        if (attackTimer > 0) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }

        // Обновляем снаряды
        for (int i = 0; i < fireballs.size(); i++) {
            Fireball f = fireballs.get(i);
            f.update(delta, this, currentRoom);
            if (!f.isActive()) {
                fireballs.remove(i);
                i--;
            }
        }

        for (int i = 0; i < arrows_list.size(); i++) {
            Arrow a = arrows_list.get(i);
            a.update(delta, currentRoom);
            if (!a.isActive()) {
                arrows_list.remove(i);
                i--;
            }
        }

        // Теневой режим (по пробелу)
        if (shadowModeTimer > 0) {
            shadowModeTimer -= delta;
            if (shadowModeTimer <= 0) shadowMode = false;
        }
        if (shadowModeCooldown > 0) shadowModeCooldown -= delta;

        int shadowKey = keyBindings.getKey(Action.SHADOW_MODE);
        if (shadowKey != 0 && input.isKeyJustPressed(shadowKey) && shadowModeCooldown <= 0 && !shadowMode) {
            shadowMode = true;
            shadowModeTimer = 1.0f;
            shadowModeCooldown = 1.0f;
        }

        // Движение
        float dx = 0, dy = 0;
        int upKey = keyBindings.getKey(Action.MOVE_UP);
        int downKey = keyBindings.getKey(Action.MOVE_DOWN);
        int leftKey = keyBindings.getKey(Action.MOVE_LEFT);
        int rightKey = keyBindings.getKey(Action.MOVE_RIGHT);

        if (upKey != 0 && input.isKeyPressed(upKey)) dy -= speed * delta;
        if (downKey != 0 && input.isKeyPressed(downKey)) dy += speed * delta;
        if (leftKey != 0 && input.isKeyPressed(leftKey)) dx -= speed * delta;
        if (rightKey != 0 && input.isKeyPressed(rightKey)) dx += speed * delta;

        if (dx != 0 && dy != 0) {
            dx *= 0.707f;
            dy *= 0.707f;
        }

        x += dx;
        y += dy;

        x = Math.max(0, Math.min(20, x));
        y = Math.max(0, Math.min(15, y));

        // Основная атака (ЛКМ) - только ближнее оружие
        if (input.isLeftMouseJustPressed() && attackCooldown <= 0 && weapon != WeaponType.BOW) {
            performAttack(currentRoom, input);
        }

        // Вспомогательная атака из лука (ПКМ)
        if (input.isRightMouseJustPressed() && bowCooldown <= 0 && arrows > 0) {
            shootBow(input);
        }

        // Заклинание огненного шара
        int fireballKey = keyBindings.getKey(Action.FIREBALL);
        if (fireballKey != 0 && input.isKeyJustPressed(fireballKey) && mana >= 2) {
            castFireball(input);
            mana -= 2;
        }

        currentRoom.checkPickups(x, y, this);
    }

    private void shootBow(InputHandler input) {
        float angle = getMouseAngle();
        arrows--;
        arrows_list.add(new Arrow(x, y, angle, 5));
        bowCooldown = BOW_DELAY;
    }

    private void castFireball(InputHandler input) {
        float angle = getMouseAngle();
        float targetX = x + (float)Math.cos(angle) * 10;
        float targetY = y + (float)Math.sin(angle) * 10;
        fireballs.add(new Fireball(x, y, targetX, targetY));
    }

    private void performAttack(Room room, InputHandler input) {
        attackCooldown = weapon.attackDelay;
        isAttacking = true;
        attackTimer = 0.3f;

        comboCounter = (comboCounter + 1) % 4;
        comboResetTimer = 1.0f;

        boolean isStrong = (comboCounter == 0);
        int damage = weapon.damage * (isStrong ? 2 : 1);
        float angle = getMouseAngle();

        if (weapon == WeaponType.AXE) {
            performConeAttack(room, angle, damage);
        } else {
            performRectAttack(room, angle, damage);
        }
    }

    private float getMouseAngle() {
        PointerInfo info = MouseInfo.getPointerInfo();
        Point mouseScreen = info.getLocation();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        float worldX = camX / 48f + (mouseScreen.x - screenSize.width / 2) / 48f;
        float worldY = camY / 48f + (mouseScreen.y - screenSize.height / 2) / 48f;

        float dx = worldX - x;
        float dy = worldY - y;
        return (float)Math.atan2(dy, dx);
    }

    private void performRectAttack(Room room, float angle, int damage) {
        float range = weapon.range;
        float width = 1.0f;
        float dirX = (float)Math.cos(angle);
        float dirY = (float)Math.sin(angle);
        float perpX = -dirY;
        float perpY = dirX;

        for (Enemy enemy : room.getEnemies()) {
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float proj = dx * dirX + dy * dirY;
            float perpDist = Math.abs(dx * perpX + dy * perpY);
            if (proj > 0 && proj < range && perpDist < width) {
                enemy.takeDamage(damage);
            }
        }
    }

    private void performConeAttack(Room room, float angle, int damage) {
        float range = weapon.range;
        float coneAngle = (float)Math.toRadians(60);
        float dirX = (float)Math.cos(angle);
        float dirY = (float)Math.sin(angle);

        for (Enemy enemy : room.getEnemies()) {
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < range) {
                float enemyDirX = dx / dist;
                float enemyDirY = dy / dist;
                float dot = dirX * enemyDirX + dirY * enemyDirY;
                float angleToEnemy = (float)Math.acos(Math.min(1, Math.max(-1, dot)));
                if (angleToEnemy < coneAngle / 2) {
                    enemy.takeDamage(damage);
                }
            }
        }
    }

    public void takeDamage(int damage) {
        if (invincibilityTimer > 0 || shadowMode) return;
        if (shields > 0) {
            shields--;
            invincibilityTimer = 0.3f;
        } else {
            hp -= damage;
            invincibilityTimer = INVINCIBILITY_DURATION;
            if (hp < 0) hp = 0;
        }
    }

    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }
    public void addShield() { if (shields < 3) shields++; }
    public void addArrows(int amount) { arrows = Math.min(1000, arrows + amount); }
    public void regenerateMana(int amount) { mana = Math.min(maxMana, mana + amount); }
    public void addGold(int amount) { goldNuggets += amount; }

    public List<Fireball> getFireballs() { return fireballs; }
    public List<Arrow> getArrowsList() { return arrows_list; }

    public void render(Graphics2D g, int screenX, int screenY) {
        float alpha = 1.0f;
        if (invincibilityTimer > 0) {
            alpha = (float)(Math.sin(System.currentTimeMillis() * 0.02) * 0.3 + 0.7);
        }
        if (shadowMode) alpha = 0.5f;

        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(screenX - 16, screenY - 8, 32, 16);
        g.setColor(new Color(60, 40, 100, (int)(200 * alpha)));
        g.fillRect(screenX - 10, screenY - 5, 20, 20);
        g.setColor(new Color(100, 150, 200, (int)(255 * alpha)));
        g.fillOval(screenX - 14, screenY - 14, 28, 28);
        g.setColor(Color.WHITE);
        g.fillOval(screenX - 8, screenY - 8, 6, 6);
        g.fillOval(screenX + 2, screenY - 8, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(screenX - 7, screenY - 7, 3, 3);
        g.fillOval(screenX + 3, screenY - 7, 3, 3);

        // Лук за спиной (всегда есть)
        g.setColor(new Color(160, 130, 70));
        g.fillRect(screenX + 10, screenY - 5, 15, 4);
        g.fillRect(screenX + 20, screenY - 8, 3, 10);

        g.setColor(Color.RED);
        g.fillRect(screenX - 25, screenY - 30, 50, 6);
        g.setColor(Color.GREEN);
        int hpPercent = (int)((float)hp / maxHp * 50);
        g.fillRect(screenX - 25, screenY - 30, hpPercent, 6);
        g.setColor(Color.BLACK);
        g.drawRect(screenX - 25, screenY - 30, 50, 6);

        if (shadowMode) {
            g.setColor(new Color(100, 150, 255, 100));
            g.drawOval(screenX - 20, screenY - 20, 40, 40);
        }
    }

    public void renderAttackIndicator(Graphics2D g, int screenX, int screenY) {
        float angle = getMouseAngle();
        float range = weapon.range;
        int pixelRange = (int)(range * 48);

        if (weapon == WeaponType.AXE) {
            float coneAngle = 60;
            int coneStartAngle = (int)Math.toDegrees(angle) - (int)(coneAngle / 2);
            g.setColor(new Color(255, 200, 100, 80));
            g.fillArc(screenX - pixelRange, screenY - pixelRange, pixelRange * 2, pixelRange * 2, coneStartAngle, (int)coneAngle);
        } else if (weapon != WeaponType.BOW) {
            int endX = screenX + (int)(Math.cos(angle) * pixelRange);
            int endY = screenY + (int)(Math.sin(angle) * pixelRange);
            g.setColor(new Color(255, 200, 100, 100));
            g.setStroke(new BasicStroke(8));
            g.drawLine(screenX, screenY, endX, endY);
            g.setStroke(new BasicStroke(1));
        }
    }

    public float getWeaponRange() { return weapon.range; }
    public boolean isInvincible() { return invincibilityTimer > 0; }
    public int getArrows() { return arrows; }
    public int getGold() { return goldNuggets; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getShields() { return shields; }
    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public boolean isShadowMode() { return shadowMode; }
    public WeaponType getWeapon() { return weapon; }
    public void setWeapon(WeaponType weapon) { this.weapon = weapon; }
}