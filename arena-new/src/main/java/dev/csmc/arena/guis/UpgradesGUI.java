package dev.csmc.arena.guis;

import dev.csmc.arena.ArenaGame;
import dev.csmc.arena.ArenaPlayer;
import dev.csmc.arena.guis.upgrades.PersonalUpgrades;
import dev.csmc.arena.guis.upgrades.TeamUpgrades;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class UpgradesGUI extends Inventory {
    public UpgradesGUI(ArenaPlayer arenaPlayer) {
        super(InventoryType.CHEST_4_ROW, Component.text("Upgrades"));

        setItemStack(11,
                ItemStack.builder(Material.IRON_CHESTPLATE)
                        .displayName(Component.text("Team Upgrades", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES))
                        .build()
        );

        setItemStack(13,
                ItemStack.builder(Material.FLOWER_BANNER_PATTERN)
                        .displayName(Component.text("Personal Upgrades", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES, ItemHideFlag.HIDE_POTION_EFFECTS))
                        .build()
        );

        setItemStack(15,
                ItemStack.builder(Material.STONE_AXE)
                        .displayName(Component.text("Class Change", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES))
                        .build()
        );

        setItemStack(31,
                ItemStack.builder(Material.BARRIER)
                        .displayName(Component.text("Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        setItemStack(32,
                ItemStack.builder(Material.FEATHER)
                        .displayName(Component.text("Ready up!", arenaPlayer.getArenaGame().readyPlayers.get(arenaPlayer.getUuid()) ? NamedTextColor.RED : NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(true);
            switch (slot) {
                case 11 -> {
                    player.openInventory(new TeamUpgrades(arenaPlayer));
                }
                case 13 -> {
                    player.openInventory(new PersonalUpgrades(arenaPlayer));
                }
                case 31 -> {
                    player.closeInventory();
                }
                case 32 -> {
                    if(((ArenaPlayer) player).getArenaGame().gameState != ArenaGame.GameState.BREAK) return;
                    ((ArenaPlayer) player).getArenaGame().readyUp((ArenaPlayer) player);
                    setItemStack(32,
                            ItemStack.builder(Material.FEATHER)
                                    .displayName(Component.text("Ready up!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                                    .build()
                    );
                }
            }
        });
    }
}
