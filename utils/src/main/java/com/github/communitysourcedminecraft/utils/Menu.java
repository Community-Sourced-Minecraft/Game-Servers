package com.github.communitysourcedminecraft.utils;

import lombok.RequiredArgsConstructor;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Menu {
	private final HashMap<Integer, Item> items;

	public void apply(AbstractInventory inv) {
		inv.clear();

		for (var i : items.entrySet()) {
			inv.setItemStack(i.getKey(), i
				.getValue()
				.item());
		}

		inv.addInventoryCondition((player, slot, clickType, result) -> {
			var i = items.get(slot);
			if (i == null) return;

			result.setCancel(true);

			i
				.action()
				.accept(player);
		});
	}

	public static Builder builder() {
		return new Builder();
	}

	public record Item(ItemStack item, Consumer<Player> action) {
	}

	public static class Builder {
		private final HashMap<Integer, Item> items = new HashMap<>();

		public Builder item(int slot, Item item) {
			items.put(slot, item);
			return this;
		}

		public Menu build() {
			return new Menu(items);
		}
	}
}
