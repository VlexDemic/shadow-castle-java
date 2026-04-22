package src.entity.enemies;

import src.entity.Entity;
import src.entity.Player;
import src.weapon.Projectile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Enemy extends Entity {
    protected int damage;
    protected float attackCooldown = 0;
    protected float shootCooldown = 0;
    protected List<Projectile> projectiles = new ArrayList<>();

    protected boolean isCharging = false;
    protected float chargeTimer = 0;
    protected final float CHARGE_DURATION = 0.5f;
    protected float attackAngle = 0;
    protected boolean isStopped = false;

    protected int floorLevel = 1;
    protected Player player;

    public Enemy(float x, float y, int baseHp, int baseDamage, float speed) {
        super(x, y, baseHp, speed);
        this.damage = baseDamage;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
        float hpMultiplier = 1 + (floorLevel - 1) * 0.25f;
        float damageMultiplier = 1 + (floorLevel - 1) * 0.2f;
        this.maxHp = (int)(this.maxHp * hpMultiplier);
        this.hp = this.maxHp;
        this.damage = (int)(this.damage * damageMultiplier);
    }

    // Переопределяем метод updateWithPlayer для обновления с игроком
    @Override
    public void updateWithPlayer(float delta, Player player) {
        this.player = player;
        updateEnemy(delta);
    }

    // Абстрактный метод для обновления врага
    protected abstract void updateEnemy(float delta);

    protected void startCharge(float angle) {
        isCharging = true;
        isStopped = true;
        chargeTimer = CHARGE_DURATION;
        attackAngle = angle;
    }

    protected void updateCharge(float delta) {
        if (isCharging) {
            chargeTimer -= delta;
            if (chargeTimer <= 0) {
                isCharging = false;
                isStopped = false;
                performAttack();
            }
        }
    }

    protected abstract void performAttack();

    public List<Projectile> getProjectiles() { return projectiles; }

    @Override
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
        isCharging = false;
        isStopped = false;
        chargeTimer = 0;
    }

    @Override
    public void update(float delta) {
        // Пустая реализация, так как используем updateWithPlayer
    }
}