package dev.csmc.arena.commands;

import dev.csmc.arena.guis.ArenaGUI;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class ArenaCommand extends Command {
    public ArenaCommand() {
        super("arena");

        setDefaultExecutor((sender, context) -> {
            ((Player) sender).openInventory(new ArenaGUI());
        });
    }
}
