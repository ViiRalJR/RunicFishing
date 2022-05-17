package me.viiral.runicfishing.manager;

import lombok.Getter;
import me.viiral.runicfishing.gui.model.GUI;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class GUIManager {

    private final Map<UUID, GUI> playerGuis = new HashMap<>();

    public GUIManager() {
    }

    public GUI getPlayerGui(UUID uuid) {
        return this.playerGuis.get(uuid);
    }

    public GUI addPlayerGui(UUID uuid, GUI gui) {
        return this.playerGuis.put(uuid, gui);
    }

    public GUI removePlayerGui(UUID uuid) {
        return this.playerGuis.remove(uuid);
    }

}
