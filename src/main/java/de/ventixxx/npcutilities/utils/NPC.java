package de.ventixxx.npcutilities.utils;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public final class NPC
{

    private UUID uuid;
    private int id;
    private String displayName;
    private int displayNameID;
    private String infoLine;
    private int infoLineID;
    private WrappedGameProfile wrappedGameProfile;
    private Profile profile;
    private Location location;
    private Material itemInHand;
    private boolean lookAtPlayer, imitatePlayer;
    private List<Player> players;
    private PacketContainer packetContainer;

    public NPC(int id, UUID uuid, Profile profile, Location location, String displayName, int displayNameID, String infoLine, int infoLineID, Material itemInHand, boolean lookAtPlayer, boolean imitatePlayer)
    {
        this.id = id;
        this.uuid = uuid;
        this.profile = profile;
        this.displayName = displayName;
        this.displayNameID = displayNameID;
        this.infoLine = infoLine;
        this.infoLineID = infoLineID;
        this.location = location;
        this.itemInHand = itemInHand;
        this.lookAtPlayer = lookAtPlayer;
        this.imitatePlayer = imitatePlayer;
        this.players = Lists.newArrayList();

        this.wrappedGameProfile = wrappedGameProfile();
    }

    private WrappedGameProfile wrappedGameProfile()
    {
        wrappedGameProfile = new WrappedGameProfile(this.uuid, this.uuid.toString().substring(0, 6));
        wrappedGameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", profile.getValue(), profile.getSignature()));
        return wrappedGameProfile;
    }

}
