package de.ventixxx.npcutilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import de.ventixxx.npcutilities.commands.NPCCommand;
import de.ventixxx.npcutilities.events.NPCInteractEvent;
import de.ventixxx.npcutilities.listeners.InteractListener;
import de.ventixxx.npcutilities.listeners.JoinListener;
import de.ventixxx.npcutilities.manager.NPCManager;

import de.ventixxx.npcutilities.utils.NPCData;
import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
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
    private NPCManager npcManager;

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
                NPCData npcData = getNpcManager().getNpcs().get(entityID);
                assert npcData != null;
                Bukkit.getPluginManager().callEvent(new NPCInteractEvent(player, npcData, entityUseAction));
            }
        });
    }

    private void taskTimer()
    {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
        {
            assert !npcManager.getNpcs().isEmpty();
            Bukkit.getOnlinePlayers().forEach(player ->
            {
                for(NPCData npcData : npcManager.getNpcs().values())
                {

                    // hide NPC if NPCWorld != playerWorld && player is in NPCList
                    if((!(npcData.getLocation().getWorld().equals(player.getWorld()))) && npcData.getPlayers().contains(player))
                    {
                        npcManager.hide(player, npcData);
                        continue;
                    }

                    // distance of player to NPC
                    double distance = player.getLocation().distance(npcData.getLocation());

                    // show NPC in range of 50 blocks | hide NPC out of range of 50 blocks
                    if(distance <= 64 && (!npcData.getPlayers().contains(player))) npcManager.show(player, npcData);
                    else if(distance > 64 && npcData.getPlayers().contains(player)) npcManager.hide(player, npcData);

                    if(distance <= 32 && npcData.isImitatePlayer()) sneak(player, npcData);
                    // LookAtPlayer
                    if(distance <= 32 && npcData.isLookAtPlayer()) npcManager.lookAtPlayer(player, npcData, calculateView(player, npcData.getLocation()));


                }
            });
        }, 20L, 2L);
    }

    private void sneak(Player player, NPCData npcData)
    {
        PacketContainer packetContainer = npcManager.create(PacketType.Play.Server.ENTITY_METADATA, npcData);
        packetContainer.getWatchableCollectionModifier().write(0, Collections.singletonList(new WrappedWatchableObject(0, (byte) (player.isSneaking() ? 6 : 0))));
        npcManager.send(Collections.singletonList(player));
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
        pluginManager.registerEvents(new JoinListener(), this);
        pluginManager.registerEvents(new InteractListener(), this);

        getCommand("npcutilities").setExecutor(new NPCCommand());

    }


    private void init()
    {

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.npcManager = new NPCManager();

    }

    @Override
    public void onDisable()
    {

    }


}
