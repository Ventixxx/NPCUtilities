package de.ventixxx.npcutilities.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.utils.NPCData;
import de.ventixxx.npcutilities.utils.ProfileData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class NPCManager
{

    @Getter
    private final Map<Integer, NPCData> npcs = Maps.newHashMap();

    private final Map<String, ProfileData> skins = Maps.newHashMap();

    private final ProtocolManager protocolManager = NPCUtilities.getNpcUtilities().getProtocolManager();
    private final int VERSION = protocolManager.getMinecraftVersion().getMinor();

    private List<PacketContainer> packetContainers = Lists.newArrayList();
    private final Random RANDOM = new Random();

    public void create(Location location, String skinOwner, String displayName, String infoLine, Material itemInHand, boolean shouldLookAtPlayer, boolean shouldImitatePlayer)
    {
        Bukkit.getScheduler().runTaskLater(NPCUtilities.getNpcUtilities(), () ->
        {
            // if displayName longer than 16 characters return
            if(displayName.length() > 16) return;
            // create ProfileData of NPC (SkinData)
            ProfileData profile;
            // if skinOwner is not in Map<Owner, ProfileData>
            if(!skins.containsKey(skinOwner))
            {
                profile = new ProfileData(skinOwner);
                // if skinOwner not exists or skinData of skinOwner not exists -> return
                if (!profile.hasProfile()) return;
            } else
            {
                // get ProfileData of Map<Owner, ProfileData>
                profile = skins.get(skinOwner);
            }
            // create NPCData (id ,uuid, profile, wrappedGameProfile, location, displayName, infoLine, armorStandID, skinOwner, itemInHand, lookAtPlayer, imitatePlayer)
            NPCData npcData = new NPCData(generateID(), generateUUID(), profile, location, displayName, generateID(), infoLine, generateID(), itemInHand, shouldLookAtPlayer, shouldImitatePlayer);
            // hideNameTag of NPC for all online players
            hideNameTag(Bukkit.getOnlinePlayers(), npcData);
            // NPCData put in Map<ID, NPCData>
            npcs.put(npcData.getId(), npcData);
            // ProfileData put in Map<Owner, ProfileData>
            skins.put(profile.getName(), profile);

        }, 2L);
    }


    public void hideNameTag(Collection<? extends Player> players, NPCData npcData)
    {
        PacketContainer scoreBoardTeam = create(PacketType.Play.Server.SCOREBOARD_TEAM, npcData, false);
        scoreBoardTeam.getStrings().write(0, npcData.getUuid().toString().substring(0, 6));
        scoreBoardTeam.getIntegers().write(0, 0);
        scoreBoardTeam.getStrings().write(4, "never");
        scoreBoardTeam.getSpecificModifier(Collection.class).write(0, Collections.singletonList(npcData.getUuid().toString().substring(0, 6)));
        send(players);
    }

    public void lookAtPlayer(Player player, NPCData npcData, float[] floats)
    {
        Location location = player.getLocation();
        PacketContainer entityHeadRotationContainer = create(PacketType.Play.Server.ENTITY_HEAD_ROTATION, npcData);
        entityHeadRotationContainer.getBytes()
                        .write(0, (byte) (floats[0] * 256.0F / 360.0F));
        Location npcLocation = npcData.getLocation();
        PacketContainer entityTeleportContainer = create(PacketType.Play.Server.ENTITY_TELEPORT, npcData);
        entityTeleportContainer.getIntegers()
                .write(1, (int) Math.floor((npcLocation.getX() * 32.0D)))
                .write(2, (int) Math.floor((npcLocation.getY() * 32.0D)))
                .write(3, (int) Math.floor((npcLocation.getZ() * 32.0D)));
        entityTeleportContainer.getBytes()
                .write(0, (byte) (floats[0] * 256.0F / 360.0F))
                .write(1, (byte) (floats[1] * 256.0F / 360.0F));
        entityTeleportContainer.getBooleans().write(0, true);
        send(Collections.singletonList(player));
    }

    public void show(Player player, NPCData npcData)
    {
        npcData.getPlayers().add(player);
        playerInfoAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER, npcData);
        spawnEntity(player, npcData);
        itemInHand(0, npcData.getItemInHand(), npcData);
        send(Collections.singletonList(player));
        Bukkit.getScheduler().runTaskLater(NPCUtilities.getNpcUtilities(), () ->
        {
            playerInfoAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, npcData);
            send(Collections.singletonList(player));
        }, 20L);
    }

    public void hide(Player player, NPCData npcData)
    {
        npcData.getPlayers().remove(player);
        destroy(player, npcData);
        send(Collections.singletonList(player));
    }

    public void cleanUp(Player player)
    {
        getNpcs()
                .values()
                .forEach(npcData ->
                {
                    destroy(player, npcData);
                    send();
                });
        getNpcs().clear();
    }

    public void destroy(Player player, NPCData npcData)
    {
        PacketContainer npc = create(PacketType.Play.Server.ENTITY_DESTROY, npcData, false);
        npc.getIntegerArrays().write(0, new int[] { npcData.getId(), npcData.getInfoLineID(), npcData.getDisplayNameID() });
    }

    private void itemInHand(int slot, Material material, NPCData npcData)
    {
        PacketContainer packetContainer = create(PacketType.Play.Server.ENTITY_EQUIPMENT, npcData);
        packetContainer.getIntegers().write(1, slot);
        packetContainer.getItemModifier().write(0, new ItemStack(material));
    }

    private void spawnEntity(Player player, NPCData npcData)
    {
        PacketContainer packetContainer = create(PacketType.Play.Server.NAMED_ENTITY_SPAWN, npcData);
        packetContainer.getUUIDs().write(0, npcData.getUuid());
        Location location = npcData.getLocation();
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
        armorStand(npcData.getDisplayNameID(), location.getX(), (location.getY() - 0.175), location.getZ(), npcData.getDisplayName());
        armorStand(npcData.getInfoLineID(), location.getX(), (location.getY() + 0.1), location.getZ(), npcData.getInfoLine());
    }

    private void armorStand(int id, double x, double y, double z, String customName)
    {

        PacketContainer armorStand = create(PacketType.Play.Server.SPAWN_ENTITY_LIVING, false);
        armorStand.getIntegers().write(0, id);
        armorStand.getIntegers().write(1, 30);

        armorStand.getIntegers()
                .write(2, (int) Math.floor((x * 32.0D)))
                .write(3, (int) Math.floor((y * 32.0D)))
                .write(4, (int) Math.floor((z * 32.0D)));

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
        wrappedDataWatcher.setObject(0, (byte) (0x20));
        wrappedDataWatcher.setObject(2, customName);
        wrappedDataWatcher.setObject(3, (byte) 1);

        armorStand.getDataWatcherModifier().write(0, wrappedDataWatcher);

    }

    private void playerInfoAction(EnumWrappers.PlayerInfoAction playerInfoAction, NPCData npcData)
    {
        PacketContainer packetContainer = create(PacketType.Play.Server.PLAYER_INFO, npcData,false);
        packetContainer.getPlayerInfoAction().write(0, playerInfoAction);
        PlayerInfoData playerInfoData = new PlayerInfoData(npcData.getWrappedGameProfile(), 20, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(""));
        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
    }

    public PacketContainer create(PacketType packetType, boolean withEntityID)
    {
        return create(packetType, null,withEntityID);
    }

    public PacketContainer create(PacketType packetType, NPCData npcData)
    {
        return create(packetType, npcData,true);
    }

    public PacketContainer create(PacketType packetType, NPCData npcData, boolean withEntityID)
    {
        PacketContainer packetContainer = protocolManager.createPacket(packetType);
        if(withEntityID) packetContainer.getIntegers().write(0, npcData.getId());
        this.packetContainers.add(packetContainer);
        return packetContainer;
    }

    public void send()
    {
        send(Bukkit.getOnlinePlayers());
    }

    public void send(Collection<? extends Player> players)
    {
        players.forEach(player ->
        {
            this.packetContainers.forEach(packetContainer ->
            {
                try
                {
                    protocolManager.sendServerPacket(player, packetContainer);
                } catch (InvocationTargetException exception)
                {
                    exception.printStackTrace();
                }
            });
        });
        packetContainers.clear();
    }

    private UUID generateUUID()
    {
        return UUID.randomUUID();
    }

    private Integer generateID()
    {
        int id;
        do
        {
            id = RANDOM.nextInt(Integer.MAX_VALUE);
        } while (npcs.containsKey(id));
        return id;
    }

}
