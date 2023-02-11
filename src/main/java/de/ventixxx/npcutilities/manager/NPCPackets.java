package de.ventixxx.npcutilities.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.utils.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NPCPackets
{

    private final ProtocolManager protocolManager = NPCUtilities.getNpcUtilities().getProtocolManager();
    private final int VERSION = protocolManager.getMinecraftVersion().getMinor();
    final List<PacketContainer> packetContainers = Lists.newArrayList();

    public NPCPackets packetToList(PacketContainer packetContainer)
    {
        this.packetContainers.add(packetContainer);
        return this;
    }

    public PacketContainer playerInfoAction(EnumWrappers.PlayerInfoAction playerInfoAction, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);
        PlayerInfoData playerInfoData = new PlayerInfoData(npc.getWrappedGameProfile(), 20, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(""));
        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        return packetContainer;
    }

    public PacketContainer spawn(Player player, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packetContainer.getIntegers().write(0, npc.getId());
        packetContainer.getUUIDs().write(0, npc.getUuid());
        Location location = npc.getLocation();
        if(VERSION < 9)
        {
            packetContainer.getIntegers()
                    .write(1, (int) Math.floor((location.getX() * 32.0D)))
                    .write(2, (int) Math.floor((location.getY() * 32.0D)))
                    .write(3, (int) Math.floor((location.getZ() * 32.0D)));

            WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
            wrappedDataWatcher.setObject(10, (byte) 127);
            packetContainer.getDataWatcherModifier().write(0, wrappedDataWatcher);

        } else
        {
            packetContainer.getDoubles()
                    .write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());
        }
        return packetContainer;
    }

    public PacketContainer itemInHand(int slot, Material material, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, npc.getId());
        packetContainer.getIntegers().write(1, slot);
        packetContainer.getItemModifier().write(0, new ItemStack(material));
        return packetContainer;
    }

    public PacketContainer destroy(Player player, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packetContainer.getIntegerArrays().write(0, new int[] { npc.getId(), npc.getInfoLineID(), npc.getDisplayNameID() });
        return packetContainer;
    }

    public PacketContainer armorStand(int id, double x, double y, double z, String customName)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING, false);
        packetContainer.getIntegers().write(0, id);
        packetContainer.getIntegers().write(1, 30);

        packetContainer.getIntegers()
                .write(2, (int) Math.floor((x * 32.0D)))
                .write(3, (int) Math.floor((y * 32.0D)))
                .write(4, (int) Math.floor((z * 32.0D)));

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setObject(0, (byte) (0x20));
        wrappedDataWatcher.setObject(2, customName);
        wrappedDataWatcher.setObject(3, (byte) 1);

        packetContainer.getDataWatcherModifier().write(0, wrappedDataWatcher);
        return packetContainer;
    }

    public PacketContainer animation(Player player, NPC npc, int animationID)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
        packetContainer.getIntegers().write(0, npc.getId());
        packetContainer.getIntegers().write(1, animationID);
        return packetContainer;
    }

    public PacketContainer metaData(Player player, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, npc.getId());
        packetContainer.getWatchableCollectionModifier().write(0, Collections.singletonList(new WrappedWatchableObject(0, (byte) (player.isSneaking() ? 6 : 0))));
        return packetContainer;
    }

    public PacketContainer[] entityHeadRotationEntityTeleport(Player player, NPC npc, float[] floats)
    {
        Location location = player.getLocation();
        PacketContainer entityHeadRotationContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        entityHeadRotationContainer.getIntegers().write(0, npc.getId());
        entityHeadRotationContainer.getBytes()
                .write(0, (byte) (floats[0] * 256.0F / 360.0F));
        Location npcLocation = npc.getLocation();
        PacketContainer entityTeleportContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        entityTeleportContainer.getIntegers().write(0, npc.getId());
        entityTeleportContainer.getIntegers()
                .write(1, (int) Math.floor((npcLocation.getX() * 32.0D)))
                .write(2, (int) Math.floor((npcLocation.getY() * 32.0D)))
                .write(3, (int) Math.floor((npcLocation.getZ() * 32.0D)));
        entityTeleportContainer.getBytes()
                .write(0, (byte) (floats[0] * 256.0F / 360.0F))
                .write(1, (byte) (floats[1] * 256.0F / 360.0F));
        entityTeleportContainer.getBooleans().write(0, true);
        return new PacketContainer[] {entityHeadRotationContainer, entityTeleportContainer};
    }

    public PacketContainer createTeam(Player player, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packetContainer.getStrings().write(0, player.getUniqueId().toString().substring(0,3)+npc.getUuid().toString().substring(0,3));
        packetContainer.getIntegers().write(1, 0);
        packetContainer.getStrings().write(4, "never");
        packetContainer.getSpecificModifier(Collection.class).write(0, Lists.newArrayList(npc.getUuid().toString().substring(0, 6)));
        return packetContainer;
    }

    public PacketContainer removeTeam(Player player, NPC npc)
    {
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        packetContainer.getStrings().write(0, player.getUniqueId().toString().substring(0,3)+npc.getUuid().toString().substring(0,3));
        packetContainer.getIntegers().write(1, 1);
        return packetContainer;
    }

    protected void send(Collection<? extends Player> players)
    {
        players.forEach(player ->
        {
            this.packetContainers.forEach(packetContainer ->
            {
                try
                {
                    protocolManager.sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            });
        });
        this.packetContainers.clear();
    }

}
