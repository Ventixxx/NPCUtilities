package de.ventixxx.npcutilities.listeners;

import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.manager.NPCPacketManager;
import de.ventixxx.npcutilities.utils.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class InteractListener implements Listener
{

    final NPCPacketManager npcPacketManager = NPCUtilities.getNpcUtilities().getNpcPacketManager();

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if(event.getPlayer() == null) return;
        Player player = (Player) event.getPlayer();

        // if player left click in the air or on a block -> swingMainArm();
        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) swingMainArm(player);

    }

    private void swingMainArm(Player player)
    {

        /*
           get all npc's
           filter if distance from player to npc <= 25 blocks
           filter if npc should imitate player (true)
         */

        npcPacketManager.getNpcs()
                .values()
                .stream()
                .filter(npc -> player.getLocation().distance(npc.getLocation()) <= 25)
                .filter(NPC::isImitatePlayer)
                .forEach(npc ->
                {
                    // swing main arm
                    npcPacketManager.swingMainArm(player, npc);
                });

    }



}
