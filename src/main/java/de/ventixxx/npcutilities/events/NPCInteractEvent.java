package de.ventixxx.npcutilities.events;

import com.comphenix.protocol.wrappers.EnumWrappers;
import de.ventixxx.npcutilities.utils.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NPCInteractEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final NPC npc;
    private final String entityUseAction;

    public NPCInteractEvent(Player player, NPC npc, EnumWrappers.EntityUseAction entityUseAction)
    {
        this.player = player;
        this.npc = npc;
        this.entityUseAction = entityUseAction.toString();
    }

    public Player getPlayer()
    {
        return player;
    }

    public NPC getnpc()
    {
        return npc;
    }

    public String getEntityUseAction()
    {
        return entityUseAction;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

}
