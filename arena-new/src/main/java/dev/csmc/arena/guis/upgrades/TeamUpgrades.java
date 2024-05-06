package dev.csmc.arena.guis.upgrades;

import dev.csmc.arena.ArenaPlayer;
import dev.csmc.arena.guis.UpgradesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class TeamUpgrades extends Inventory {
    public TeamUpgrades(ArenaPlayer arenaPlayer) {
        super(InventoryType.CHEST_4_ROW, Component.text("Team Upgrades"));

        setItemStack(11,
                ItemStack.builder(Material.IRON_SWORD)
                        .displayName(Component.text("More Damage", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES))
                        .build()
        );

        setItemStack(13,
                ItemStack.builder(Material.CHAINMAIL_CHESTPLATE)
                        .displayName(Component.text("More Defense", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES))
                        .build()
        );

        setItemStack(15,
                ItemStack.builder(Material.GOLDEN_APPLE)
                        .displayName(Component.text("More Hearts", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .meta(builder -> builder.hideFlag(ItemHideFlag.HIDE_ATTRIBUTES))
                        .build()
        );

        setItemStack(31,
                ItemStack.builder(Material.BARRIER)
                        .displayName(Component.text("Go Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(true);
            switch (slot) {
                case 31 -> {
                    player.openInventory(new UpgradesGUI((ArenaPlayer) player));
                }
            }
        });
    }
}
