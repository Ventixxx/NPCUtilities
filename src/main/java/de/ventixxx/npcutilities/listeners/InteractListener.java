package de.ventixxx.npcutilities.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.manager.NPCManager;
import de.ventixxx.npcutilities.utils.NPCData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;

public final class InteractListener implements Listener
{

    final NPCManager npcManager = NPCUtilities.getNpcUtilities().getNpcManager();

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
           get all NPCData's
           filter if distance from player to npc <= 25 blocks
           filter if npc should imitate player (true)
         */

        npcManager.getNpcs()
                .values()
                .stream()
                .filter(npcData -> player.getLocation().distance(npcData.getLocation()) <= 25)
                .filter(NPCData::isImitatePlayer)
                .forEach(npcData ->
                {
                    // swing main arm packet (Protocollib)
                    PacketContainer packetContainer = npcManager.create(PacketType.Play.Server.ANIMATION, npcData);
                    packetContainer.getIntegers().write(1, 0);
                    npcManager.send(Collections.singletonList(player));
                });
    }



}
