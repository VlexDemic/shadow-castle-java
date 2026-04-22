package src.weapon.ranged;

import src.entity.Player;
import src.entity.enemies.Enemy;
import src.dungeon.Room;
import src.weapon.Projectile;

import java.awt.*;

public class Arrow extends Projectile {
    private float speed = 15f;

    public Arrow(float startX, float startY, float angle, int damage) {
        super(startX, startY, startX + (float)Math.cos(angle) * 10, startY + (float)Math.sin(angle) * 10, damage, new Color(200, 180, 100));
        this.vx = (float)Math.cos(angle) * speed;
        this.vy = (float)Math.sin(angle) * speed;
    }

    @Override
    public void update(float delta, Player player, Room room) {
        lifeTime += delta;
        x += vx * delta;
        y += vy * delta;

        if (room != null) {
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
        }

        if (x < -2 || x > 22 || y < -2 || y > 17 || lifeTime > maxLife) {
            active = false;
        }
    }

    @Override
    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(new Color(200, 180, 100));
        g.fillRect(screenX - 2, screenY - 1, 12, 2);
        g.setColor(new Color(150, 130, 70));
        g.fillRect(screenX + 8, screenY - 2, 4, 4);
        g.setColor(new Color(200, 180, 100, 100));
        g.fillOval(screenX - 6, screenY - 3, 8, 6);
    }
}