package src.core;

import src.core.KeyBindings.Action;

import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private Set<Integer> pressedKeys = new HashSet<>();
    private Set<Integer> justPressedKeys = new HashSet<>();
    private Set<Integer> releasedKeys = new HashSet<>();

    private int mouseX, mouseY;
    private boolean leftMousePressed, rightMousePressed;
    private boolean leftMouseJustPressed, rightMouseJustPressed;

    private KeyBindings keyBindings;

    public InputHandler() {
        keyBindings = KeyBindings.getInstance();
    }

    public void update() {
        justPressedKeys.clear();
        releasedKeys.clear();
        leftMouseJustPressed = false;
        rightMouseJustPressed = false;
    }

    public boolean isActionPressed(Action action) {
        return pressedKeys.contains(keyBindings.getKey(action));
    }

    public boolean isActionJustPressed(Action action) {
        return justPressedKeys.contains(keyBindings.getKey(action));
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isKeyJustPressed(int keyCode) {
        return justPressedKeys.contains(keyCode);
    }

    public boolean isLeftMousePressed() { return leftMousePressed; }
    public boolean isLeftMouseJustPressed() { return leftMouseJustPressed; }
    public boolean isRightMousePressed() { return rightMousePressed; }
    public boolean isRightMouseJustPressed() { return rightMouseJustPressed; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!pressedKeys.contains(e.getKeyCode())) {
            justPressedKeys.add(e.getKeyCode());
        }
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        releasedKeys.add(e.getKeyCode());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
            leftMouseJustPressed = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMousePressed = true;
            rightMouseJustPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            rightMousePressed = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
}