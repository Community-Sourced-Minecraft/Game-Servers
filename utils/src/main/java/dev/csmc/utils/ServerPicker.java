package dev.csmc.utils;

import dev.csmc.hosting.Hosting;
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
import java.util.Arrays;

@RequiredArgsConstructor
public class ServerPicker {
	private final String gamemode;
	private final Hosting hosting;

	public Inventory getInventory() throws JetStreamApiException, IOException, InterruptedException {
		var inv = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Server Picker for " + gamemode.substring(0, 1).toUpperCase() + gamemode.substring(1)));

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

			var namePrettified = server.substring(0, 1).toUpperCase() + server.substring(1).replace("-", " #");

			final var isCurrent = server.equals(currentServer);
			var material = isCurrent ? Material.LIME_CONCRETE : Material.WHITE_CONCRETE;

			var itemBuilder = ItemStack
				.builder(material)
				.amount(1)
				.displayName(Component
					.text(namePrettified)
					.decoration(TextDecoration.ITALIC, false));

			if (isCurrent) {
				itemBuilder.meta(builder -> {
					builder.hideFlag(ItemHideFlag.HIDE_ENCHANTS);
					builder.enchantment(Enchantment.UNBREAKING, (short) 1);
				});

				itemBuilder.lore(
						Arrays.asList(
								// TODO: Fetch player count
								Component
										.text("Players: TODO")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.GRAY),
								Component
										.text(""),
								Component
										.text("Current server")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.GRAY)
						)
				);
			} else {
				itemBuilder.lore(
						Arrays.asList(
								// TODO: Fetch player count
								Component
										.text("Players: TODO")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.GRAY),
								Component.text(""),
								Component
										.text("Click here to join!")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.YELLOW)
						)
				);
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
