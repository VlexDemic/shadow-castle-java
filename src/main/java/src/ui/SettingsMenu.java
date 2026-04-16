package src.ui;

import src.core.InputHandler;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SettingsMenu {
    private int selectedIndex = 0;
    private String[] menuItems = {"Управление", "Видео", "Звук", "Назад"};
    private boolean inControlsMenu = false;
    private ControlsMenu controlsMenu;

    public SettingsMenu() {
        controlsMenu = new ControlsMenu();
    }

    public int update(InputHandler input) {
        if (inControlsMenu) {
            int result = controlsMenu.update(input);
            if (result == 0) {
                inControlsMenu = false;
            }
            return -2;
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
            if (selectedIndex == 0) {
                inControlsMenu = true;
                return -2;
            } else if (selectedIndex == 3) {
                return 0;
            }
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            return 0;
        }

        return -1;
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight) {
        if (inControlsMenu) {
            controlsMenu.render(g, screenWidth, screenHeight);
            return;
        }

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "НАСТРОЙКИ";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (screenWidth - fm.stringWidth(title)) / 2, 100);

        int startY = 200;
        int buttonWidth = 300;
        int buttonHeight = 60;
        int centerX = screenWidth / 2 - buttonWidth / 2;

        for (int i = 0; i < menuItems.length; i++) {
            int buttonY = startY + i * (buttonHeight + 20);
            boolean isSelected = (i == selectedIndex);

            if (isSelected) {
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

            g.setFont(new Font("Arial", Font.BOLD, 28));
            fm = g.getFontMetrics();
            int textX = screenWidth / 2 - fm.stringWidth(menuItems[i]) / 2;
            int textY = buttonY + buttonHeight / 2 + fm.getAscent() / 2 - 5;
            g.drawString(menuItems[i], textX, textY);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(150, 150, 150));
        String footer = "ESC - назад | W/S - навигация | ENTER - выбор";
        fm = g.getFontMetrics();
        g.drawString(footer, screenWidth / 2 - fm.stringWidth(footer) / 2, screenHeight - 50);

        g.setStroke(new BasicStroke(1));
    }
}