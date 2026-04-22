package src.core;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyBindings {
    private static KeyBindings instance;
    private Map<Action, Integer> bindings;
    private Map<Action, Integer> defaultBindings;

    public enum Action {
        MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT,
        ATTACK, SHADOW_MODE, FIREBALL, INTERACT,
        PAUSE, FULLSCREEN
    }

    private KeyBindings() {
        bindings = new HashMap<>();
        defaultBindings = new HashMap<>();
        initDefaultBindings();
        loadBindings();
    }

    public static KeyBindings getInstance() {
        if (instance == null) {
            instance = new KeyBindings();
        }
        return instance;
    }

    private void initDefaultBindings() {
        defaultBindings.put(Action.MOVE_UP, KeyEvent.VK_W);
        defaultBindings.put(Action.MOVE_DOWN, KeyEvent.VK_S);
        defaultBindings.put(Action.MOVE_LEFT, KeyEvent.VK_A);
        defaultBindings.put(Action.MOVE_RIGHT, KeyEvent.VK_D);
        defaultBindings.put(Action.ATTACK, KeyEvent.VK_SPACE);
        defaultBindings.put(Action.SHADOW_MODE, KeyEvent.VK_SPACE);
        defaultBindings.put(Action.FIREBALL, KeyEvent.VK_E);
        defaultBindings.put(Action.INTERACT, KeyEvent.VK_E);
        defaultBindings.put(Action.PAUSE, KeyEvent.VK_ESCAPE);
        defaultBindings.put(Action.FULLSCREEN, KeyEvent.VK_F11);

        bindings.putAll(defaultBindings);
    }

    public int getKey(Action action) {
        return bindings.getOrDefault(action, defaultBindings.get(action));
    }

    public String getKeyName(Action action) {
        return KeyEvent.getKeyText(getKey(action));
    }

    public void setKey(Action action, int keyCode) {
        for (Map.Entry<Action, Integer> entry : bindings.entrySet()) {
            if (entry.getValue() == keyCode && entry.getKey() != action) {
                bindings.put(entry.getKey(), defaultBindings.get(entry.getKey()));
            }
        }
        bindings.put(action, keyCode);
        saveBindings();
    }

    public void resetToDefault() {
        bindings.clear();
        bindings.putAll(defaultBindings);
        saveBindings();
    }

    public void resetAction(Action action) {
        bindings.put(action, defaultBindings.get(action));
        saveBindings();
    }

    private void saveBindings() {
        Properties props = new Properties();
        for (Map.Entry<Action, Integer> entry : bindings.entrySet()) {
            props.setProperty(entry.getKey().name(), String.valueOf(entry.getValue()));
        }
        try (FileOutputStream out = new FileOutputStream("keybindings.properties")) {
            props.store(out, "Game Key Bindings");
        } catch (IOException e) {
            System.err.println("Failed to save key bindings: " + e.getMessage());
        }
    }

    private void loadBindings() {
        File file = new File("keybindings.properties");
        if (!file.exists()) return;

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            for (Action action : Action.values()) {
                String value = props.getProperty(action.name());
                if (value != null) {
                    try {
                        int keyCode = Integer.parseInt(value);
                        bindings.put(action, keyCode);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load key bindings: " + e.getMessage());
        }
    }

    public Map<Action, Integer> getAllBindings() {
        return new HashMap<>(bindings);
    }
}