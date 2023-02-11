package de.ventixxx.npcutilities.manager;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.Maps;
import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.utils.NPC;
import de.ventixxx.npcutilities.utils.Profile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class NPCPacketManager extends NPCPackets
{

    @Getter
    private final Map<Integer, NPC> npcs = Maps.newHashMap();
    private final Map<String, Profile> skins = Maps.newHashMap();
    private final Random RANDOM = new Random();

    public void create(Location location, String skinOwner, String displayName, String infoLine, Material itemInHand, boolean shouldLookAtPlayer, boolean shouldImitatePlayer)
    {
        Profile profile = new Profile(skinOwner);
        // create npc (id ,uuid, profile, wrappedGameProfile, location, displayName, infoLine, armorStandID, skinOwner, itemInHand, lookAtPlayer, imitatePlayer)
        NPC npc = new NPC(generateID(), generateUUID(), profile, location, displayName, generateID(), infoLine, generateID(), itemInHand, shouldLookAtPlayer, shouldImitatePlayer);
        // npc put in Map<ID, npc>
        npcs.put(npc.getId(), npc);
        // ProfileData put in Map<Owner, ProfileData>
        skins.put(profile.getName(), profile);

    }

    public void show(Player player, NPC npc)
    {
        npc.getPlayers().add(player);

        packetToList(armorStand(npc.getDisplayNameID(), npc.getLocation().getX(), (npc.getLocation().getY() - 0.175), npc.getLocation().getZ(), npc.getDisplayName()))
                .packetToList(armorStand(npc.getInfoLineID(), npc.getLocation().getX(), (npc.getLocation().getY() + 0.1), npc.getLocation().getZ(), npc.getInfoLine()))
                .packetToList(playerInfoAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER, npc))
                .packetToList(spawn(player, npc))
                .packetToList(itemInHand(0, npc.getItemInHand(), npc))
                .packetToList(createTeam(player, npc))
                .send(Collections.singletonList(player));

        Bukkit.getScheduler().runTaskLater(NPCUtilities.getNpcUtilities(), () ->
        {
            packetToList(playerInfoAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, npc)).send(Collections.singletonList(player));
        }, 20L*3);
    }

    public void hide(Player player, NPC npc)
    {
        npc.getPlayers().remove(player);
        packetToList(destroy(player, npc))
                .packetToList(removeTeam(player, npc))
                .send(Collections.singletonList(player));
    }

    public void swingMainArm(Player player, NPC npc)
    {
        packetToList(animation(player, npc, 0)).send(Collections.singletonList(player));
    }

    public void sneak(Player player, NPC npc)
    {
        packetToList(metaData(player, npc)).send(Collections.singletonList(player));
    }

    public void lookAtPlayer(Player player, NPC npc, float[] floats)
    {
        packetToList(entityHeadRotationEntityTeleport(player, npc, floats)[0])
                .packetToList(entityHeadRotationEntityTeleport(player, npc, floats)[1])
                .send(Collections.singletonList(player));
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
