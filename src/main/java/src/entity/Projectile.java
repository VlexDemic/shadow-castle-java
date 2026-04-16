package src.entity;

import java.awt.*;

public class Projectile {
    private float x, y;
    private float targetX, targetY;
    private float startX, startY;
    private float speed = 8f;
    private int damage;
    private Color color;
    private boolean active = true;
    private float lifeTime = 0;
    private final float MAX_LIFE = 3f;

    public Projectile(float startX, float startY, float targetX, float targetY, int damage, Color color) {
        this.startX = startX;
        this.startY = startY;
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.color = color;

        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist > 0) {
            this.speed = 8f;
        }
    }

    public void update(float delta, Player player) {
        if (!active) return;
        lifeTime += delta;
        if (lifeTime > MAX_LIFE) {
            active = false;
            return;
        }

        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist > 0.01f) {
            dx /= dist;
            dy /= dist;
            x += dx * speed * delta;
            y += dy * speed * delta;
        }

        float playerDx = player.getX() - x;
        float playerDy = player.getY() - y;
        float playerDist = (float)Math.sqrt(playerDx*playerDx + playerDy*playerDy);
        if (playerDist < 0.5f) {
            player.takeDamage(damage);
            active = false;
        }

        if (x < -2 || x > 22 || y < -2 || y > 17) {
            active = false;
        }
    }

    public void render(Graphics2D g, int screenX, int screenY) {
        if (!active) return;
        g.setColor(color);
        g.fillOval(screenX - 6, screenY - 6, 12, 12);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.fillOval(screenX - 3, screenY - 3, 6, 6);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g.fillOval(screenX - 10, screenY - 10, 20, 20);
    }

    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
}