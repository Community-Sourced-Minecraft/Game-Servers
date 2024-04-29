package com.github.communitysourcedminecraft.utils;

import com.github.communitysourcedminecraft.hosting.Hosting;
import com.github.communitysourcedminecraft.hosting.NATSConnection;
import io.nats.client.JetStreamApiException;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.io.IOException;

@RequiredArgsConstructor
public class ServerPicker {
	private final String gamemode;
	private final Hosting hosting;
	private final Material material;

	public Inventory getInventory() throws JetStreamApiException, IOException, InterruptedException {
		var inv = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Server Picker for " + gamemode));

		var menuBuilder = Menu.builder();

		var servers = hosting
			.getNats()
			.getServersForGamemode(gamemode)
			.stream()
			.sorted()
			.toList();

		var currentServer = hosting
			.getInfo()
			.podName();

		for (int i = 0; i < servers.size(); i++) {
			var server = servers.get(i);

			final var isCurrent = server.equals(currentServer);

			var itemBuilder = ItemStack
				.builder(material)
				.amount(1)
				.displayName(Component
					.text(server)
					.decoration(TextDecoration.ITALIC, false));

			if (isCurrent) {
				itemBuilder.meta(builder -> {
					builder.hideFlag(ItemHideFlag.HIDE_ENCHANTS);
					builder.enchantment(Enchantment.UNBREAKING, (short) 1);
				});

				itemBuilder.lore(Component
					.text("Current server")
					.color(NamedTextColor.GRAY));
			}

			menuBuilder.item(i, new Menu.Item(itemBuilder.build(), player -> hosting
				.getNats()
				.transferPlayer(player.getUuid(), server)));
		}

		var menu = menuBuilder.build();
		menu.apply(inv);

		return inv;
	}
}
