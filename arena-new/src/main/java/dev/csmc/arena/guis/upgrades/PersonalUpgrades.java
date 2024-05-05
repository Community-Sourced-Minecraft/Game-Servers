package dev.csmc.arena.guis.upgrades;

import dev.csmc.arena.ArenaPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class PersonalUpgrades extends Inventory {
    public PersonalUpgrades(ArenaPlayer arenaPlayer) {
        super(InventoryType.CHEST_4_ROW, Component.text("Personal Upgrades"));

        setItemStack(11,
                ItemStack.builder(Material.REDSTONE)
                        .displayName(Component.text("Lifesteal", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        setItemStack(13,
                ItemStack.builder(Material.STRUCTURE_VOID)
                        .displayName(Component.text("SOON", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        setItemStack(15,
                ItemStack.builder(Material.STRUCTURE_VOID)
                        .displayName(Component.text("SOON", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        setItemStack(31,
                ItemStack.builder(Material.BARRIER)
                        .displayName(Component.text("Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                        .build()
        );

        addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(true);
            switch (slot) {
                case 11 -> {
                    
                }
            }
        });
    }
}
