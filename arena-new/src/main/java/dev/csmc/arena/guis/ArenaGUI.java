package dev.csmc.arena.guis;

import dev.csmc.arena.ArenaGame;
import dev.csmc.arena.ArenaGroup;
import dev.csmc.arena.ArenaPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class ArenaGUI extends Inventory {
    public ArenaGUI() {
        super(InventoryType.CHEST_3_ROW, Component.text("Arena"));

        setItemStack(13, ItemStack.builder(Material.ZOMBIE_HEAD)
                .displayName(Component.text("ᴘʟᴀʏ ᴀʀᴇɴᴀ", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
                .build()
        );

        addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(true);
            player.closeInventory();
            ArenaPlayer aplayer = (ArenaPlayer) player;
            ArenaGame arenaGame;

            if(slot == 13) {
                if (aplayer.getArenaGroup() != null && aplayer.getArenaGroup().leader.getUuid().equals(aplayer.getUuid())) {
                    // TODO: Fix this braindead code

                    aplayer.arenaGroup.players.add(aplayer);
                    arenaGame = new ArenaGame(aplayer.arenaGroup.players);

                    for (ArenaPlayer plr: aplayer.arenaGroup.players) {
                        plr.arenaGame = arenaGame;
                    }

                    aplayer.arenaGroup.players.remove(aplayer);
                } else {
                    arenaGame = new ArenaGame(List.of(aplayer));
                    aplayer.arenaGame = arenaGame;
                }
            }
        });
    }
}
