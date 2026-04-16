package src.entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Enemy {
    private float x, y;
    private int hp;
    private int damage;
    private float speed;
    private EnemyType type;
    private float attackCooldown = 0;
    private float shootCooldown = 0;
    private List<Projectile> projectiles = new ArrayList<>();

    public enum EnemyType {
        BLUE_SLIME(50, 1, 4f, true, false, new Color(50, 100, 200), Color.CYAN),
        RED_SLIME(30, 1, 0, false, true, new Color(200, 50, 50), Color.RED),
        SKELETON_MELEE(80, 2, 5f, true, false, new Color(150, 150, 150), Color.GRAY),
        SKELETON_RANGED(60, 2, 0, false, true, new Color(180, 180, 180), Color.ORANGE);

        public final int hp;
        public final int damage;
        public final float speed;
        public final boolean isMelee;
        public final boolean isRanged;
        public final Color color;
        public final Color projectileColor;

        EnemyType(int hp, int damage, float speed, boolean isMelee, boolean isRanged, Color color, Color projectileColor) {
            this.hp = hp;
            this.damage = damage;
            this.speed = speed;
            this.isMelee = isMelee;
            this.isRanged = isRanged;
            this.color = color;
            this.projectileColor = projectileColor;
        }
    }

    public Enemy(float x, float y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.hp = type.hp;
        this.damage = type.damage;
        this.speed = type.speed;
    }

    public void update(float delta, Player player) {
        if (attackCooldown > 0) attackCooldown -= delta;
        if (shootCooldown > 0) shootCooldown -= delta;

        for (int i = 0; i < projectiles.size(); i++) {
            Projectile p = projectiles.get(i);
            p.update(delta, player);
            if (!p.isActive()) {
                projectiles.remove(i);
                i--;
            }
        }

        if (type.isMelee) {
            float dx = player.getX() - x;
            float dy = player.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);

            if (dist > 0.01f && dist > 0.8f) {
                dx /= dist;
                dy /= dist;
                x += dx * speed * delta;
                y += dy * speed * delta;
            }

            // Атака при касании хитбокса игрока (дистанция меньше 0.6)
            if (dist < 0.6f && attackCooldown <= 0) {
                player.takeDamage(damage);
                attackCooldown = 0.8f;
            }
        } else if (type.isRanged) {
            if (shootCooldown <= 0) {
                shootAt(player);
                shootCooldown = 1.5f;
            }
        }
    }

    private void shootAt(Player player) {
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist < 7.0f) {
            Projectile p = new Projectile(x, y, player.getX(), player.getY(), damage, type.projectileColor);
            projectiles.add(p);
        }
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() { return hp > 0; }
    public float getX() { return x; }
    public float getY() { return y; }
    public EnemyType getType() { return type; }
    public List<Projectile> getProjectiles() { return projectiles; }

    public void render(Graphics2D g, int screenX, int screenY) {
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(screenX - 16, screenY - 6, 32, 12);
        g.setColor(type.color);
        g.fillOval(screenX - 14, screenY - 14, 28, 28);
        g.setColor(Color.WHITE);
        g.fillOval(screenX - 8, screenY - 8, 5, 5);
        g.fillOval(screenX + 3, screenY - 8, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(screenX - 7, screenY - 7, 2, 2);
        g.fillOval(screenX + 4, screenY - 7, 2, 2);

        if (type.isRanged) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(screenX + 8, screenY - 10, 12, 4);
            g.fillRect(screenX + 15, screenY - 12, 3, 8);
        }

        g.setColor(Color.RED);
        g.fillRect(screenX - 20, screenY - 26, 40, 5);
        g.setColor(Color.GREEN);
        int hpPercent = (int)((float)hp / type.hp * 40);
        g.fillRect(screenX - 20, screenY - 26, hpPercent, 5);
        g.setColor(Color.BLACK);
        g.drawRect(screenX - 20, screenY - 26, 40, 5);
    }
}