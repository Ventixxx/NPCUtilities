package de.ventixxx.npcutilities.events;

import com.comphenix.protocol.wrappers.EnumWrappers;
import de.ventixxx.npcutilities.utils.NPCData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NPCInteractEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final NPCData npcData;
    private final EnumWrappers.EntityUseAction entityUseAction;

    public NPCInteractEvent(Player player, NPCData npcData, EnumWrappers.EntityUseAction entityUseAction)
    {
        this.player = player;
        this.npcData = npcData;
        this.entityUseAction = entityUseAction;
    }

    public Player getPlayer()
    {
        return player;
    }

    public NPCData getNpcData()
    {
        return npcData;
    }

    public EnumWrappers.EntityUseAction getEntityUseAction()
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
