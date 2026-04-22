package src.entity.enemies;

import java.awt.*;

public class SkeletonMelee extends Enemy {
    private float dashCooldown = 0;
    private float dashDuration = 0;
    private float dashSpeed = 0;
    private float dashDirX = 0, dashDirY = 0;
    private float restTimer = 0;
    private boolean isResting = false;

    public SkeletonMelee(float x, float y) {
        super(x, y, 80, 2, 2.5f);
    }

    @Override
    protected void updateEnemy(float delta) {
        if (player == null) return;

        if (attackCooldown > 0) attackCooldown -= delta;

        updateCharge(delta);

        if (isStopped) return;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);

        if (dist > 8f) return;

        if (dashDuration > 0) {
            dashDuration -= delta;
            x += dashDirX * dashSpeed * delta;
            y += dashDirY * dashSpeed * delta;
            return;
        }

        if (isResting) {
            restTimer -= delta;
            if (restTimer <= 0) {
                isResting = false;
                dashCooldown = 0.3f;
            }
            return;
        }

        if (dashCooldown > 0) {
            dashCooldown -= delta;
            return;
        }

        if (dist < 1.2f && attackCooldown <= 0 && !isCharging) {
            float angle = (float)Math.atan2(dy, dx);
            startCharge(angle);
            attackCooldown = 0.8f;
            return;
        }

        if (dist > 0.8f && dist < 5f) {
            dashDirX = dx / dist;
            dashDirY = dy / dist;
            dashSpeed = speed * 3f;
            dashDuration = 0.3f;
        }
    }

    @Override
    protected void performAttack() {
        if (player == null) return;
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist < 1.2f) {
            player.takeDamage(damage);
        }
    }

    @Override
    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenX - 16, screenY - 6, 32, 12);
        g.setColor(new Color(150, 150, 150));
        g.fillOval(screenX - 14, screenY - 14, 28, 28);
        g.setColor(Color.WHITE);
        g.fillOval(screenX - 8, screenY - 8, 5, 5);
        g.fillOval(screenX + 3, screenY - 8, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(screenX - 7, screenY - 7, 2, 2);
        g.fillOval(screenX + 4, screenY - 7, 2, 2);

        if (isCharging) {
            int radius = 30;
            int startAngle = (int)((attackAngle - Math.PI / 3) * 180 / Math.PI);
            int arcAngle = 120;
            float pulse = (float)(Math.sin(chargeTimer * 20) * 0.3 + 0.7);
            g.setColor(new Color(255, 50, 50, (int)(100 * pulse)));
            g.fillArc(screenX - radius, screenY - radius, radius * 2, radius * 2, startAngle, arcAngle);
            g.setColor(new Color(255, 100, 50, (int)(150 * pulse)));
            g.drawArc(screenX - radius, screenY - radius, radius * 2, radius * 2, startAngle, arcAngle);
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