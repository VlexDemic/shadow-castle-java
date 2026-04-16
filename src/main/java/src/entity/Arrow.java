package src.entity;

import src.dungeon.Room;

import java.awt.*;

public class Arrow {
    private float x, y;
    private float vx, vy;
    private float speed = 15f;
    private int damage;
    private boolean active = true;
    private float lifeTime = 0;
    private final float MAX_LIFE = 1.5f;

    public Arrow(float startX, float startY, float angle, int damage) {
        this.x = startX;
        this.y = startY;
        this.vx = (float)Math.cos(angle) * speed;
        this.vy = (float)Math.sin(angle) * speed;
        this.damage = damage;
    }

    public void update(float delta, Room room) {
        lifeTime += delta;
        x += vx * delta;
        y += vy * delta;

        for (Enemy enemy : room.getEnemies()) {
            float dx = enemy.getX() - x;
            float dy = enemy.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 0.5f) {
                enemy.takeDamage(damage);
                active = false;
                return;
            }
        }

        if (x < -2 || x > 22 || y < -2 || y > 17 || lifeTime > MAX_LIFE) {
            active = false;
        }
    }

    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(new Color(200, 180, 100));
        g.fillRect(screenX - 2, screenY - 1, 12, 2);
        g.setColor(new Color(150, 130, 70));
        g.fillRect(screenX + 8, screenY - 2, 4, 4);
        g.setColor(new Color(200, 180, 100, 100));
        g.fillOval(screenX - 6, screenY - 3, 8, 6);
    }

    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
}