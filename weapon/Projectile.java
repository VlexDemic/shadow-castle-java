package src.weapon;

import src.entity.Player;
import src.dungeon.Room;

import java.awt.*;

public class Projectile {
    protected float x, y;
    protected float vx, vy;
    protected int damage;
    protected boolean active = true;
    protected float lifeTime = 0;
    protected float maxLife = 3f;
    protected Color color;

    public Projectile(float startX, float startY, float targetX, float targetY, int damage, Color color) {
        this.x = startX;
        this.y = startY;
        this.damage = damage;
        this.color = color;

        float dx = targetX - startX;
        float dy = targetY - startY;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist > 0) {
            float speed = 8f;
            this.vx = dx / dist * speed;
            this.vy = dy / dist * speed;
        }
    }

    public void update(float delta, Player player, Room room) {
        lifeTime += delta;
        x += vx * delta;
        y += vy * delta;

        if (player != null) {
            float dx = player.getX() - x;
            float dy = player.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist < 0.5f) {
                player.takeDamage(damage);
                active = false;
                return;
            }
        }

        if (x < -2 || x > 22 || y < -2 || y > 17 || lifeTime > maxLife) {
            active = false;
        }
    }

    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(color);
        g.fillOval(screenX - 6, screenY - 6, 12, 12);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g.fillOval(screenX - 3, screenY - 3, 6, 6);
    }

    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
}