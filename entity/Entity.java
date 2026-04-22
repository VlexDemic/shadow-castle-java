package src.entity;

import java.awt.*;

public abstract class Entity {
    protected float x, y;
    protected int hp, maxHp;
    protected float speed;
    protected boolean active = true;

    public Entity(float x, float y, int hp, float speed) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
    }

    // Базовый метод update - будет переопределён в дочерних классах
    public abstract void update(float delta);

    // Дополнительный метод для обновления с игроком (для врагов)
    public void updateWithPlayer(float delta, Player player) {
        update(delta);
    }

    public abstract void render(Graphics2D g, int screenX, int screenY);

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    public boolean isAlive() { return hp > 0; }
    public boolean isActive() { return active; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
}