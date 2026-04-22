package src.ui;

import src.core.InputHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MainMenu {
    private int selectedIndex = 0;
    private String[] menuItems = {"НОВАЯ ИГРА", "ОБУЧЕНИЕ", "НАСТРОЙКИ", "ВЫЙТИ"};
    private float time = 0;
    private Rectangle[] buttonRects = new Rectangle[4];

    public int update(InputHandler input, boolean tutorialCompleted) {
        time += 0.016f;

        int mouseX = input.getMouseX();
        int mouseY = input.getMouseY();

        for (int i = 0; i < menuItems.length; i++) {
            if (buttonRects[i] != null && buttonRects[i].contains(mouseX, mouseY)) {
                selectedIndex = i;
            }
        }

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = menuItems.length - 1;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selectedIndex++;
            if (selectedIndex >= menuItems.length) selectedIndex = 0;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            return selectedIndex;
        }

        if (input.isLeftMouseJustPressed()) {
            for (int i = 0; i < buttonRects.length; i++) {
                if (buttonRects[i] != null && buttonRects[i].contains(mouseX, mouseY)) {
                    if (i == 0 && !tutorialCompleted) return -1;
                    return i;
                }
            }
        }

        return -1;
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight, boolean tutorialCompleted) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(10, 10, 20),
                screenWidth, screenHeight, new Color(30, 10, 40));
        g.setPaint(gp);
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setColor(new Color(100, 50, 150, 50));
        for (int i = 0; i < 50; i++) {
            int x = (int)(Math.sin(time + i) * 100 + Math.cos(time * 0.5f + i) * 50);
            g.fillOval(x + i * 50, (int)(Math.sin(time * 0.7f + i) * 200 + screenHeight / 2), 3, 3);
        }

        g.setFont(new Font("Cinema", Font.BOLD, 96));
        String title = "GUNGEON";
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.BLACK);
        g.drawString(title, screenWidth / 2 - fm.stringWidth(title) / 2 + 5, 150 + 5);

        GradientPaint titleGp = new GradientPaint(screenWidth / 2 - 200, 100, Color.YELLOW,
                screenWidth / 2 + 200, 200, Color.ORANGE);
        g.setPaint(titleGp);
        g.drawString(title, screenWidth / 2 - fm.stringWidth(title) / 2, 150);

        g.setFont(new Font("Arial", Font.ITALIC, 24));
        g.setColor(Color.LIGHT_GRAY);
        String subtitle = "Roguelite Action Dungeon Crawler";
        fm = g.getFontMetrics();
        g.drawString(subtitle, screenWidth / 2 - fm.stringWidth(subtitle) / 2, 200);

        int buttonWidth = 350;
        int buttonHeight = 70;
        int startY = screenHeight / 2 - 100;
        int centerX = screenWidth / 2 - buttonWidth / 2;

        for (int i = 0; i < menuItems.length; i++) {
            int buttonY = startY + i * (buttonHeight + 20);
            buttonRects[i] = new Rectangle(centerX, buttonY, buttonWidth, buttonHeight);

            boolean isLocked = (i == 0 && !tutorialCompleted);
            boolean isHovered = buttonRects[i].contains(MouseInfo.getPointerInfo().getLocation());
            boolean isSelected = (i == selectedIndex);

            if (isLocked) {
                g.setColor(new Color(60, 60, 80, 150));
                g.fillRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(new Color(100, 100, 120));
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(new Color(150, 150, 170));
            } else if (isHovered || isSelected) {
                g.setColor(new Color(255, 215, 0, 100));
                g.fillRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(new Color(255, 215, 0));
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(new Color(150, 150, 200));
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(centerX, buttonY, buttonWidth, buttonHeight, 15, 15);
                g.setColor(Color.WHITE);
            }

            g.setFont(new Font("Arial", Font.BOLD, 32));
            fm = g.getFontMetrics();
            int textX = screenWidth / 2 - fm.stringWidth(menuItems[i]) / 2;
            int textY = buttonY + buttonHeight / 2 + fm.getAscent() / 2 - 5;
            g.drawString(menuItems[i], textX, textY);

            if (isLocked) {
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.setColor(new Color(200, 150, 50));
                String hint = "Сначала пройдите обучение!";
                fm = g.getFontMetrics();
                g.drawString(hint, screenWidth / 2 - fm.stringWidth(hint) / 2, buttonY + buttonHeight + 15);
            }
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(150, 150, 150));
        String controls = "W/S или ↑/↓ - навигация | ENTER/SPACE/МЫШЬ - выбор";
        fm = g.getFontMetrics();
        g.drawString(controls, screenWidth / 2 - fm.stringWidth(controls) / 2, screenHeight - 50);
        g.drawString("v1.0", screenWidth - 80, screenHeight - 30);

        g.setColor(new Color(255, 200, 100, 100));
        for (int i = 0; i < 20; i++) {
            int x = (int)(Math.sin(time * 2 + i) * 200 + screenWidth / 2);
            int y = (int)(Math.cos(time * 1.5f + i) * 100 + screenHeight - 100);
            g.fillOval(x, y, 4, 4);
        }
        g.setStroke(new BasicStroke(1));
    }
}