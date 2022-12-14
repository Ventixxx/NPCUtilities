package de.ventixxx.npcutilities.listeners;

import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.manager.NPCManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;

public final class JoinListener implements Listener
{

    final NPCManager npcManager = NPCUtilities.getNpcUtilities().getNpcManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if(event.getPlayer() == null) return;
        Player player = (Player) event.getPlayer();

        // get all NPCData's
        npcManager.getNpcs()
                .values()
                .forEach(npcData ->
                {
                    // hideNameTag of NPC for player
                    npcManager.hideNameTag(Collections.singletonList(player), npcData);
                });

    }

}
