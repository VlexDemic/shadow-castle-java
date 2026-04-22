package src.ui;

import src.core.InputHandler;
import src.core.KeyBindings;
import src.core.KeyBindings.Action;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ControlsMenu {
    private List<ControlItem> controls = new ArrayList<>();
    private int selectedIndex = 0;
    private boolean waitingForKey = false;
    private int waitingActionIndex = -1;
    private float time = 0;

    public ControlsMenu() {
        controls.add(new ControlItem("Движение вверх", Action.MOVE_UP));
        controls.add(new ControlItem("Движение вниз", Action.MOVE_DOWN));
        controls.add(new ControlItem("Движение влево", Action.MOVE_LEFT));
        controls.add(new ControlItem("Движение вправо", Action.MOVE_RIGHT));
        controls.add(new ControlItem("Атака (ЛКМ)", Action.ATTACK));
        controls.add(new ControlItem("Теневой режим", Action.SHADOW_MODE));
        controls.add(new ControlItem("Огненный шар", Action.FIREBALL));
        controls.add(new ControlItem("Взаимодействие", Action.INTERACT));
        controls.add(new ControlItem("Пауза/Меню", Action.PAUSE));
        controls.add(new ControlItem("Полноэкранный режим", Action.FULLSCREEN));
    }

    public int update(InputHandler input) {
        time += 0.016f;

        if (waitingForKey) {
            for (int i = 0; i < 256; i++) {
                if (input.isKeyJustPressed(i)) {
                    if (i == KeyEvent.VK_ESCAPE) {
                        waitingForKey = false;
                        waitingActionIndex = -1;
                    } else {
                        KeyBindings.getInstance().setKey(controls.get(waitingActionIndex).action, i);
                        waitingForKey = false;
                        waitingActionIndex = -1;
                    }
                    return -2;
                }
            }
            return -2;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_UP) || input.isKeyJustPressed(KeyEvent.VK_W)) {
            selectedIndex--;
            if (selectedIndex < 0) selectedIndex = controls.size() - 1;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_DOWN) || input.isKeyJustPressed(KeyEvent.VK_S)) {
            selectedIndex++;
            if (selectedIndex >= controls.size()) selectedIndex = 0;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ENTER) || input.isKeyJustPressed(KeyEvent.VK_SPACE)) {
            waitingForKey = true;
            waitingActionIndex = selectedIndex;
            return -2;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_R)) {
            KeyBindings.getInstance().resetAction(controls.get(selectedIndex).action);
            return -2;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_DELETE) || input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
            KeyBindings.getInstance().resetToDefault();
            return -2;
        }

        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            return 0;
        }

        return -1;
    }

    public void render(Graphics2D g, int screenWidth, int screenHeight) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "УПРАВЛЕНИЕ";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (screenWidth - fm.stringWidth(title)) / 2, 80);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        String instructions = "ENTER - изменить | R - сбросить действие | DELETE - сбросить всё | ESC - назад";
        fm = g.getFontMetrics();
        g.drawString(instructions, (screenWidth - fm.stringWidth(instructions)) / 2, 130);

        int startY = 180;
        int rowHeight = 45;
        int col1X = screenWidth / 2 - 250;
        int col2X = screenWidth / 2 + 50;

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.YELLOW);
        g.drawString("Действие", col1X, startY);
        g.drawString("Клавиша", col2X, startY);

        g.setColor(new Color(100, 100, 150));
        g.drawLine(col1X - 10, startY + 5, col2X + 150, startY + 5);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < controls.size(); i++) {
            ControlItem item = controls.get(i);
            int y = startY + (i + 1) * rowHeight;

            boolean isSelected = (i == selectedIndex) && !waitingForKey;
            boolean isWaiting = (i == waitingActionIndex) && waitingForKey;

            if (isSelected) {
                g.setColor(new Color(255, 215, 0, 50));
                g.fillRect(col1X - 10, y - 25, 350, rowHeight);
            }

            if (isWaiting) {
                g.setColor(Color.CYAN);
                g.drawString(item.name + ":", col1X, y);
                g.drawString("[НАЖМИТЕ КЛАВИШУ]", col2X, y);
            } else {
                g.setColor(Color.WHITE);
                g.drawString(item.name + ":", col1X, y);

                String keyName = KeyBindings.getInstance().getKeyName(item.action);
                if (isSelected) {
                    g.setColor(Color.YELLOW);
                    g.drawString("> " + keyName + " <", col2X, y);
                } else {
                    g.setColor(new Color(200, 200, 200));
                    g.drawString(keyName, col2X, y);
                }
            }
        }

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(150, 150, 150));
        String footer = "ESC - назад к настройкам";
        fm = g.getFontMetrics();
        g.drawString(footer, screenWidth - fm.stringWidth(footer) - 20, screenHeight - 30);

        if (waitingForKey) {
            float alpha = (float)(Math.sin(time * 10) * 0.5 + 0.5);
            g.setColor(new Color(0, 0, 0, (int)(alpha * 200)));
            g.fillRect(0, 0, screenWidth, screenHeight);
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String waiting = "НАЖМИТЕ ЛЮБУЮ КЛАВИШУ...";
            fm = g.getFontMetrics();
            g.drawString(waiting, (screenWidth - fm.stringWidth(waiting)) / 2, screenHeight / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String cancel = "ESC - отмена";
            fm = g.getFontMetrics();
            g.drawString(cancel, (screenWidth - fm.stringWidth(cancel)) / 2, screenHeight / 2 + 60);
        }
    }

    private static class ControlItem {
        String name;
        Action action;
        ControlItem(String name, Action action) {
            this.name = name;
            this.action = action;
        }
    }
}