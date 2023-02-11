package de.ventixxx.npcutilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.ventixxx.npcutilities.events.NPCInteractEvent;
import de.ventixxx.npcutilities.listeners.InteractListener;

import de.ventixxx.npcutilities.manager.NPCPacketManager;
import de.ventixxx.npcutilities.utils.NPC;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NPCUtilities extends JavaPlugin
{

    @Getter
    private static NPCUtilities npcUtilities;
    @Getter
    private final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    @Getter
    private  ProtocolManager protocolManager;
    @Getter
    private NPCPacketManager npcPacketManager;

    @Getter
    private final String PREFIX = " §f§lNPCUtilities §8┃ ",
                        NO_PERMS = PREFIX+"§7You don§8'§7t have permission to do this§8.";

    @Override
    public void onEnable()
    {

        npcUtilities = this;
        init();
        register();

        protocolLibUseEntity();
        taskTimer();

    }

    private void protocolLibUseEntity()
    {
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                PacketContainer packetContainer = event.getPacket();
                int entityID = packetContainer.getIntegers().read(0);
                Player player = (Player) event.getPlayer();
                EnumWrappers.EntityUseAction entityUseAction = packetContainer.getEntityUseActions().read(0);
                NPC npc = getNpcPacketManager().getNpcs().get(entityID);
                if(npc == null) return;
                Bukkit.getPluginManager().callEvent(new NPCInteractEvent(player, npc, entityUseAction));
            }
        });
    }

    private void taskTimer()
    {
        Bukkit.getScheduler().runTaskTimer(this, () ->
        {
            assert !npcPacketManager.getNpcs().isEmpty();
            Bukkit.getOnlinePlayers().forEach(player ->
            {
                for(NPC npc : npcPacketManager.getNpcs().values())
                {

                    // hide NPC if NPCWorld != playerWorld && player is in NPCList
                    if((!(npc.getLocation().getWorld().equals(player.getWorld()))) && npc.getPlayers().contains(player))
                    {
                        npcPacketManager.hide(player, npc);
                        continue;
                    }

                    // distance of player to NPC
                    double distance = player.getLocation().distance(npc.getLocation());

                    // show NPC in range of 24 blocks | hide NPC out of range of 24 blocks
                    if(distance <= 24 && (!npc.getPlayers().contains(player))) npcPacketManager.show(player, npc);
                    else if(distance > 24 && npc.getPlayers().contains(player)) npcPacketManager.hide(player, npc);

                    if(distance <= 24 && npc.isImitatePlayer()) npcPacketManager.sneak(player, npc);
                    // LookAtPlayer
                    if(distance <= 24 && npc.isLookAtPlayer()) npcPacketManager.lookAtPlayer(player, npc, calculateView(player, npc.getLocation()));

                }
            });
        }, 20L, 2L);
    }

    private float[] calculateView(Player player, Location location)
    {
        float[] floatArray = new float [2];
        double xDifference = player.getLocation().getX() - location.getX();
        double yDifference = player.getLocation().getY() - location.getY();
        double zDifference = player.getLocation().getZ() - location.getZ();
        double r = Math.sqrt(Math.pow(xDifference, 2.0D) + Math.pow(yDifference, 2.0D) + Math.pow(zDifference, 2.0D));
        floatArray[0] = (float)(-Math.atan2(xDifference, zDifference) / Math.PI * 180.0D);
        floatArray[0] = (floatArray[0] < 0.0F) ? (floatArray[0] + 360.0F) : floatArray[0];
        floatArray[1] = (float)(-Math.asin(yDifference / r) / Math.PI * 180.0D);
        return floatArray;
    }

    private void register()
    {

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new InteractListener(), this);


    }


    private void init()
    {

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.npcPacketManager = new NPCPacketManager();

    }

    @Override
    public void onDisable()
    {

    }


}
