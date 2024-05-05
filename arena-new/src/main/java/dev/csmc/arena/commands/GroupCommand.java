package dev.csmc.arena.commands;

import dev.csmc.arena.ArenaGroup;
import dev.csmc.arena.ArenaPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.utils.entity.EntityFinder;

import java.util.StringJoiner;

import static net.minestom.server.command.builder.arguments.ArgumentType.Entity;
import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class GroupCommand extends Command {
    public GroupCommand() {
        super("group");
        addSyntax((sender, context) -> {
            ArenaPlayer player = (ArenaPlayer) sender;
            ArenaGroup group = player.getArenaGroup();

            if(group == null) {
                player.sendMessage("You are not in a group!");
            } else {
                TextComponent.Builder builder = Component.text()
                        .append(Component.newline())
                        .append(Component.text("Leader: ", NamedTextColor.YELLOW))
                        .append(Component.text(group.leader.getUsername(), NamedTextColor.GRAY))
                        .append(Component.newline())
                        .append(Component.text("Members (" + group.players.size() + 1 + ")", NamedTextColor.YELLOW))
                        .append(Component.newline());

                StringJoiner joiner = new StringJoiner(", ");
                for(ArenaPlayer p : group.players) {
                    joiner.add(p.getUsername());
                }

                builder.append(Component.text(joiner.toString(), NamedTextColor.GRAY));

                sender.sendMessage(builder.build());
            }
        });

        addSyntax((sender, context) -> {
            ArenaPlayer player = (ArenaPlayer) sender;
            ArenaGroup group = player.getArenaGroup();

            if(group == null) {
                player.sendMessage(Component.text("You are not in a group!", NamedTextColor.RED));
                return;
            }

            group.removePlayer(player);
            player.sendMessage(Component.text("You left group!", NamedTextColor.GREEN));
        }, Literal("leave"));

        addSyntax((sender, context) -> {
            ArenaPlayer player = (ArenaPlayer) sender;
            ArenaPlayer ctxPlayer = (ArenaPlayer) ((EntityFinder) context.get("player")).findFirstPlayer(sender);

            if(ctxPlayer == null) {
                player.sendMessage(Component.text("This player is not online!", NamedTextColor.RED));
                return;
            }

            if(ctxPlayer.getArenaGroup() != null) {
                player.sendMessage(Component.text(ctxPlayer.getUsername() + " is already in a group!", NamedTextColor.RED));
                return;
            }

            ArenaGroup group = player.getArenaGroup();

            if(group == null) {
                player.setArenaGroup(new ArenaGroup(player));
                group = player.getArenaGroup();
            }

            if(group.pendingInvites.contains(ctxPlayer.getUuid())) {
                player.sendMessage(Component.text(ctxPlayer.getUsername() + " is already invited to group!", NamedTextColor.RED));
                return;
            }

            group.invitePlayer(ctxPlayer);

            player.sendMessage(Component.text("Invited " + ctxPlayer.getUsername() + " to the group!", NamedTextColor.GREEN));
            ctxPlayer.sendMessage(Component.text(player.getUsername() + " has invited you to the group!", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/group accept " + player.getUsername())));
        }, Literal("invite"), Entity("player").onlyPlayers(true).singleEntity(true));

        addSyntax((sender, context) -> {
            ArenaPlayer player = (ArenaPlayer) sender;
            ArenaPlayer ctxPlayer = (ArenaPlayer) ((EntityFinder) context.get("player")).findFirstPlayer(sender);

            if(ctxPlayer == null) {
                player.sendMessage(Component.text("This group doesn't exist anymore!", NamedTextColor.RED));
                return;
            }

            ArenaGroup group = ctxPlayer.getArenaGroup();
            if(group == null) {
                player.sendMessage(Component.text("This group doesn't exist anymore!", NamedTextColor.RED));
                return;
            }

            if(!group.pendingInvites.contains(player.getUuid())) {
                player.sendMessage(Component.text("You are not invited to the group!", NamedTextColor.RED));
                return;
            }

            group.acceptInvite(player);

            player.sendMessage(Component.text("Joined group successfully!", NamedTextColor.GREEN));
            ctxPlayer.sendMessage(Component.text(player.getUsername() + " has joined the group!", NamedTextColor.GREEN));
        }, Literal("accept"), Entity("player").onlyPlayers(true).singleEntity(true));
    }
}
