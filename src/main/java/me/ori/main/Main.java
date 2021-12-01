package me.ori.main;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Location;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

import org.bukkit.event.Listener;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;


public final class Main extends JavaPlugin implements Listener {
    private static Main instance;

    private Location location1 = null;
    private Location location2 = null;
    public static Location lastLoc = null;

    @Override
    public void onEnable() {

        instance = this;

        File datafolder = new File(getDataFolder().getAbsolutePath());
        if(!datafolder.exists())
            datafolder.mkdir();
        getServer().getPluginManager().registerEvents(this,this);

        lastLoc = new Location(getServer().getWorld("world"),0,0,0);

        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Main getInstance(){
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("copy") && sender instanceof Player){
            Player p = (Player) sender;
            try {
                MCBuildsUtils.copyAndSave(location1, location2, args[0]);



            } catch (IOException e) {
                p.sendMessage(getDataFolder().getAbsolutePath());
                e.printStackTrace();
            }

        }
        else if(label.equalsIgnoreCase("paste") && sender instanceof Player) {
            Player p = (Player) sender;
            try {

                lastLoc = p.getLocation();

                //to paste from a custom center do:
                //MCBuildsUtils.pasteFromFile(new Location(p.getWorld(),Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])), Integer.parseInt(args[0]), "test", p, false);



                MCBuildsUtils.pasteFromFile(p.getLocation(), Integer.parseInt(args[1]), args[0], p, false);


            } catch (IOException | ClassNotFoundException | IllegalArgumentException | CommandSyntaxException e) {
                e.printStackTrace();
            }

        }
        else if((label.equalsIgnoreCase("pasterelative") || label.equalsIgnoreCase("pr")) && sender instanceof Player) {
            Player p = (Player) sender;
            try {
                if(lastLoc == null){
                    p.sendMessage("Please paste with /paste first!");
                    return false;
                }
                Location loc = new Location(p.getWorld(), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                MCBuildsUtils.pasteFromFile(loc, Integer.parseInt(args[1]), args[0], p, true);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if(label.equalsIgnoreCase("copystart") && sender instanceof Player) {
            Player p = (Player) sender;
            location1 = p.getLocation();
        }
        else if(label.equalsIgnoreCase("copyend") && sender instanceof Player) {
            Player p = (Player) sender;
            location2 = p.getLocation();

        }


        return false;
    }






}
