package de.ventixxx.npcutilities.commands;

import com.comphenix.protocol.ProtocolManager;
import de.ventixxx.npcutilities.NPCUtilities;
import de.ventixxx.npcutilities.manager.NPCManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public final class NPCCommand implements CommandExecutor
{

    final Random RANDOM = new Random();

    final NPCManager npcManager = NPCUtilities.getNpcUtilities().getNpcManager();


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {

        if(!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        switch (args.length)
        {
            case 1:
                switch (args[0])
                {
                    case "create":
                        npcManager.create(player.getLocation(), "Kylotv", "§c§lBedWars", "§6Click me to join", Material.BED, true, true);
                        break;
                    case "cleanup":
                        npcManager.cleanUp(player);
                        break;
                }
                break;
        }


        return false;
    }

}
