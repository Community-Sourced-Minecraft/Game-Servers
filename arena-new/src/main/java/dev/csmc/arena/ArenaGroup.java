package dev.csmc.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaGroup {
    public ArenaPlayer leader;
    public List<ArenaPlayer> players;
    public List<UUID> pendingInvites;

    public ArenaGroup(ArenaPlayer leader) {
        this.leader = leader;
        this.players = new ArrayList<>();
        this.pendingInvites = new ArrayList<>();
    }

    public void addPlayer(ArenaPlayer player) {
        this.players.add(player);
        player.setArenaGroup(this);
    }

    public void removePlayer(ArenaPlayer player) {
        if(this.leader.equals(player)) {
            if(this.players.isEmpty()) return;
            this.leader = this.players.getFirst();
        }
        this.players.remove(player);
        player.setArenaGroup(null);
    }

    public void disbandGroup(ArenaGroup group) {
        for (ArenaPlayer player : players) {
            group.removePlayer(player);
        }
    }

    public void invitePlayer(ArenaPlayer player) {
        pendingInvites.add(player.getUuid());
    }

    public void acceptInvite(ArenaPlayer player) {
        pendingInvites.remove(player.getUuid());
        this.addPlayer(player);
    }

    public void revokeInvite(ArenaPlayer player) {
        pendingInvites.remove(player.getUuid());
    }
}
