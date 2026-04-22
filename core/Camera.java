package src.core;

import src.dungeon.Room;
import src.entity.Player;
import src.weapon.magic.Fireball;
import src.weapon.ranged.Arrow;

import java.awt.*;

public class Camera {
    private float camX, camY;
    private final int TILE_SIZE = 48;

    private int deadzoneX = 200;
    private int deadzoneY = 150;

    public void render(Graphics2D g, Room room, Player player, int screenWidth, int screenHeight) {
        int roomWidthPx = 20 * TILE_SIZE;
        int roomHeightPx = 15 * TILE_SIZE;

        float playerPxX = player.getX() * TILE_SIZE;
        float playerPxY = player.getY() * TILE_SIZE;

        float targetCamX, targetCamY;

        if (roomWidthPx <= screenWidth) {
            targetCamX = roomWidthPx / 2f;
        } else {
            float minCamX = screenWidth / 2f;
            float maxCamX = roomWidthPx - screenWidth / 2f;
            targetCamX = playerPxX;
            float currentCamX = camX;
            if (playerPxX > currentCamX + deadzoneX) {
                targetCamX = playerPxX - deadzoneX;
            } else if (playerPxX < currentCamX - deadzoneX) {
                targetCamX = playerPxX + deadzoneX;
            } else {
                targetCamX = currentCamX;
            }
            targetCamX = Math.min(maxCamX, Math.max(minCamX, targetCamX));
        }

        if (roomHeightPx <= screenHeight) {
            targetCamY = roomHeightPx / 2f;
        } else {
            float minCamY = screenHeight / 2f;
            float maxCamY = roomHeightPx - screenHeight / 2f;
            targetCamY = playerPxY;
            float currentCamY = camY;
            if (playerPxY > currentCamY + deadzoneY) {
                targetCamY = playerPxY - deadzoneY;
            } else if (playerPxY < currentCamY - deadzoneY) {
                targetCamY = playerPxY + deadzoneY;
            } else {
                targetCamY = currentCamY;
            }
            targetCamY = Math.min(maxCamY, Math.max(minCamY, targetCamY));
        }

        camX = camX * 0.9f + targetCamX * 0.1f;
        camY = camY * 0.9f + targetCamY * 0.1f;

        int offsetX = screenWidth / 2 - (int)camX;
        int offsetY = screenHeight / 2 - (int)camY;

        renderWithOffset(g, room, player, offsetX, offsetY, screenWidth, screenHeight);
    }

    private void renderWithOffset(Graphics2D g, Room room, Player player, int offsetX, int offsetY, int screenWidth, int screenHeight) {
        room.renderWithOffset(g, offsetX, offsetY, screenWidth, screenHeight);
        room.renderEntitiesWithOffset(g, offsetX, offsetY, screenWidth, screenHeight);

        renderFireballsWithOffset(g, player, offsetX, offsetY);
        renderArrowsWithOffset(g, player, offsetX, offsetY);

        renderPlayerWithOffset(g, player, offsetX, offsetY);
        renderCrosshair(g, screenWidth, screenHeight);
    }

    private void renderFireballsWithOffset(Graphics2D g, Player player, int offsetX, int offsetY) {
        for (Fireball f : player.getFireballs()) {
            int screenX = (int)(f.getX() * TILE_SIZE) + offsetX;
            int screenY = (int)(f.getY() * TILE_SIZE) + offsetY;
            f.render(g, screenX, screenY);
        }
    }

    private void renderArrowsWithOffset(Graphics2D g, Player player, int offsetX, int offsetY) {
        for (Arrow a : player.getArrowsList()) {
            int screenX = (int)(a.getX() * TILE_SIZE) + offsetX;
            int screenY = (int)(a.getY() * TILE_SIZE) + offsetY;
            a.render(g, screenX, screenY);
        }
    }

    private void renderPlayerWithOffset(Graphics2D g, Player player, int offsetX, int offsetY) {
        int screenX = (int)(player.getX() * TILE_SIZE) + offsetX;
        int screenY = (int)(player.getY() * TILE_SIZE) + offsetY;

        player.renderAttackIndicator(g, screenX, screenY);
        player.render(g, screenX, screenY);
    }

    private void renderCrosshair(Graphics2D g, int width, int height) {
        PointerInfo info = MouseInfo.getPointerInfo();
        Point p = info.getLocation();

        if (p.x > 0 && p.x < width && p.y > 0 && p.y < height) {
            g.setColor(new Color(255, 50, 50, 200));
            g.setStroke(new BasicStroke(2));
            g.drawLine(p.x - 15, p.y, p.x - 5, p.y);
            g.drawLine(p.x + 5, p.y, p.x + 15, p.y);
            g.drawLine(p.x, p.y - 15, p.x, p.y - 5);
            g.drawLine(p.x, p.y + 5, p.x, p.y + 15);
            g.drawOval(p.x - 8, p.y - 8, 16, 16);
            g.setStroke(new BasicStroke(1));
        }
    }

    public float getCamX() { return camX; }
    public float getCamY() { return camY; }
}