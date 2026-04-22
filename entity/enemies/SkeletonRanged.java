package src.entity.enemies;

import src.weapon.Projectile;

import java.awt.*;

public class SkeletonRanged extends Enemy {
    public SkeletonRanged(float x, float y) {
        super(x, y, 60, 2, 0f);
    }

    @Override
    protected void updateEnemy(float delta) {
        if (player == null) return;

        if (shootCooldown > 0) shootCooldown -= delta;

        updateCharge(delta);

        if (isStopped) return;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);

        if (dist > 9f) return;

        if (shootCooldown <= 0 && !isCharging) {
            float angle = (float)Math.atan2(dy, dx);
            startCharge(angle);
        }

        // Обновляем позицию снарядов
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            p.update(delta, player, null);
            if (!p.isActive()) {
                projectiles.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void performAttack() {
        if (player == null) return;
        Projectile p = new Projectile(x, y, player.getX(), player.getY(), damage, Color.ORANGE);
        projectiles.add(p);
        shootCooldown = 1.2f;
    }

    @Override
    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenX - 16, screenY - 6, 32, 12);
        g.setColor(new Color(180, 180, 180));
        g.fillOval(screenX - 14, screenY - 14, 28, 28);
        g.setColor(Color.WHITE);
        g.fillOval(screenX - 8, screenY - 8, 5, 5);
        g.fillOval(screenX + 3, screenY - 8, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(screenX - 7, screenY - 7, 2, 2);
        g.fillOval(screenX + 4, screenY - 7, 2, 2);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(screenX + 8, screenY - 10, 12, 4);
        g.fillRect(screenX + 15, screenY - 12, 3, 8);

        if (isCharging) {
            float intensity = (float)(Math.sin(chargeTimer * 20) * 0.5 + 0.5);
            g.setColor(new Color(255, 255, 100, (int)(150 * intensity)));
            g.fillOval(screenX + 22, screenY - 12, 10, 10);
            g.setColor(new Color(255, 200, 50, (int)(200 * intensity)));
            g.fillOval(screenX + 24, screenY - 10, 6, 6);
        }

        g.setColor(Color.RED);
        g.fillRect(screenX - 20, screenY - 26, 40, 5);
        g.setColor(Color.GREEN);
        int hpPercent = (int)((float)hp / maxHp * 40);
        g.fillRect(screenX - 20, screenY - 26, hpPercent, 5);
        g.setColor(Color.BLACK);
        g.drawRect(screenX - 20, screenY - 26, 40, 5);
    }
}