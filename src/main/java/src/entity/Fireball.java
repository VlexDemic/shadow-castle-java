package src.entity;

import src.dungeon.Room;
import src.dungeon.RoomTemplate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Fireball {
    private float x, y;
    private float targetX, targetY;
    private float startX, startY;
    private float vx, vy;
    private float speed = 12f;
    private int damage = 30;
    private boolean active = true;
    private float lifeTime = 0;
    private final float MAX_LIFE = 3f;
    private boolean hasExploded = false;
    private List<Particle> particles = new ArrayList<>();

    public Fireball(float startX, float startY, float targetX, float targetY) {
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;

        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist > 0) {
            this.vx = dx / dist * speed;
            this.vy = dy / dist * speed;
        }
    }

    public void update(float delta, Player player, Room room) {
        if (!active) return;
        lifeTime += delta;

        x += vx * delta;
        y += vy * delta;

        if (Math.random() < 0.3f && !hasExploded) {
            particles.add(new Particle(x, y, new Color(255, 100 + (int)(Math.random() * 155), 50),
                    (float)(Math.random() - 0.5f), (float)(Math.random() - 0.5f)));
        }

        // Проверка столкновения с врагами
        for (Enemy enemy : room.getEnemies()) {
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 0.6f && !hasExploded) {
                explode(room);
                hasExploded = true;
                return;
            }
        }

        // Проверка столкновения со стенами
        int tileX = (int)x;
        int tileY = (int)y;
        if (tileX >= 0 && tileX < 20 && tileY >= 0 && tileY < 15) {
            if (room.isWall(tileX, tileY) && !hasExploded) {
                explode(room);
                hasExploded = true;
                return;
            }
        }

        if (x < -1 || x > 21 || y < -1 || y > 16 || lifeTime > MAX_LIFE) {
            if (!hasExploded) {
                explode(room);
                hasExploded = true;
            }
        }

        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) {
                particles.remove(i);
                i--;
            }
        }

        if (hasExploded && particles.isEmpty()) {
            active = false;
        }
    }

    private void explode(Room room) {
        // Наносим урон врагам в радиусе
        for (Enemy enemy : room.getEnemies()) {
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 2.5f) {
                enemy.takeDamage(damage);
            }
        }

        // Создаем частицы взрыва
        for (int i = 0; i < 40; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float speedX = (float)Math.cos(angle) * (float)(Math.random() * 4 + 1);
            float speedY = (float)Math.sin(angle) * (float)(Math.random() * 4 + 1);
            Color color;
            int rand = (int)(Math.random() * 3);
            if (rand == 0) color = new Color(255, 50, 0);
            else if (rand == 1) color = new Color(255, 150, 0);
            else color = new Color(255, 255, 100);
            particles.add(new Particle(x, y, color, speedX, speedY, 0.6f));
        }

        for (int i = 0; i < 20; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float speedX = (float)Math.cos(angle) * (float)(Math.random() * 6 + 2);
            float speedY = (float)Math.sin(angle) * (float)(Math.random() * 6 + 2);
            particles.add(new Particle(x, y, Color.YELLOW, speedX, speedY, 0.4f));
        }

        for (int i = 0; i < 15; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float speedX = (float)Math.cos(angle) * (float)(Math.random() * 2);
            float speedY = (float)Math.sin(angle) * (float)(Math.random() * 2);
            particles.add(new Particle(x, y, new Color(80, 80, 80), speedX, speedY, 0.8f));
        }
    }

    public void render(Graphics2D g, int screenX, int screenY) {
        if (!active && particles.isEmpty()) return;

        for (Particle p : particles) {
            int particleX = screenX + (int)((p.x - x) * 48);
            int particleY = screenY + (int)((p.y - y) * 48);
            p.render(g, particleX, particleY);
        }

        if (!hasExploded && active) {
            float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.015) * 0.3 + 0.7);
            g.setColor(new Color(255, 80, 0, 100));
            g.fillOval(screenX - 14, screenY - 14, 28, 28);
            g.setColor(new Color(255, 80 + (int)(50 * pulse), 0));
            g.fillOval(screenX - 9, screenY - 9, 18, 18);
            g.setColor(new Color(255, 150 + (int)(50 * pulse), 0));
            g.fillOval(screenX - 6, screenY - 6, 12, 12);
            g.setColor(Color.YELLOW);
            g.fillOval(screenX - 4, screenY - 4, 8, 8);
            g.setColor(Color.WHITE);
            g.fillOval(screenX - 2, screenY - 2, 4, 4);
            g.setColor(new Color(255, 200, 100, 150));
            g.drawOval(screenX - 12, screenY - 12, 24, 24);
        }
    }

    public boolean isActive() { return active || !particles.isEmpty(); }
    public float getX() { return x; }
    public float getY() { return y; }

    private class Particle {
        float x, y;
        float vx, vy;
        Color color;
        float life = 1.0f;
        float maxLife;

        Particle(float x, float y, Color color, float vx, float vy) {
            this(x, y, color, vx, vy, 0.5f);
        }

        Particle(float x, float y, Color color, float vx, float vy, float maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLife = maxLife;
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            life -= delta / maxLife;
            vy += delta * 8;
        }

        boolean isDead() { return life <= 0; }

        void render(Graphics2D g, int screenX, int screenY) {
            int alpha = (int)(life * 200);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(alpha, 255)));
            int size = (int)(8 * life);
            g.fillOval(screenX - size/2, screenY - size/2, size, size);
        }
    }
}