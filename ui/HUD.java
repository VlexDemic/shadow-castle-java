package src.ui;

import src.entity.Player;

import java.awt.*;

public class HUD {
    public void render(Graphics2D g, Player player, int floor, int screenWidth, int screenHeight) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, 380, 220);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("❤ HP: " + player.getHp() + "/" + player.getMaxHp(), 20, 40);
        g.drawString("💙 Shields: " + player.getShields() + "/3", 20, 70);
        g.drawString("✨ Mana: " + player.getMana() + "/" + player.getMaxMana(), 20, 100);
        g.drawString("🏹 Arrows: " + player.getArrows(), 20, 130);
        g.drawString("💰 Gold: " + player.getGold(), 20, 160);
        g.drawString("📊 Floor: " + floor, 20, 190);

        g.drawString("⚔️ " + player.getWeapon().name(), screenWidth - 200, 40);
        g.setFont(new Font("Arial", Font.PLAIN, 14));

        String attackType = (player.getWeapon() == src.core.Game.WeaponType.AXE) ? "Cone attack (60°)" : "Rectangular attack";
        g.drawString("📏 " + attackType, screenWidth - 200, 70);
        g.drawString("📐 Range: " + player.getWeaponRange(), screenWidth - 200, 90);

        g.setColor(new Color(200, 180, 100));
        g.drawString("🏹 Secondary: Bow (RMB)", screenWidth - 200, 120);
        g.drawString("💥 Damage: 5", screenWidth - 200, 140);

        if (player.isInvincible()) {
            g.setColor(new Color(255, 255, 100, 150));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            String invText = "✦ INVINCIBLE ✦";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(invText, screenWidth / 2 - fm.stringWidth(invText) / 2, 80);
        }

        if (player.isShadowMode()) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String shadowText = "✦ SHADOW MODE ✦";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(shadowText, screenWidth / 2 - fm.stringWidth(shadowText) / 2, 50);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        g.drawString("WASD - Move | LMB - Melee | RMB - Bow | Space - Shadow | E - Fireball", 20, screenHeight - 20);
    }
}