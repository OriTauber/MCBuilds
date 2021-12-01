package me.ori.main;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.Block;

import org.bukkit.block.data.Bisected;

import org.bukkit.block.data.Directional;

import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Furnace;

import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MCBuildsUtils {

    public static void copyAndSave(Location loc1, Location loc2, String filename) throws IOException {
        ArrayList<String> blockTypesAsString = new ArrayList<>();
        ArrayList<Location> serBlocksOrgLoc = new ArrayList<>();

        ArrayList<String> serBlockData = new ArrayList<>();


        ArrayList<String> entityTypesAsString = new ArrayList<>();
        ArrayList<Location> entitiesOrgLoc = new ArrayList<>();

        ArrayList<Entity> entities = new ArrayList<>();


        //make file
        File buildFile = new File(Main.getInstance().getDataFolder(), filename + ".mcbuilds");


        //define x, y, z in the right direction
        int topBlockX = (Math.max(loc1.getBlockX(), loc2.getBlockX()));
        int bottomBlockX = (Math.min(loc1.getBlockX(), loc2.getBlockX()));

        int topBlockY = (Math.max(loc1.getBlockY(), loc2.getBlockY()));
        int bottomBlockY = (Math.min(loc1.getBlockY(), loc2.getBlockY()));

        int topBlockZ = (Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        int bottomBlockZ = (Math.min(loc1.getBlockZ(), loc2.getBlockZ()));


        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);

                    if (!block.getType().isAir()) {
                        blockTypesAsString.add(block.getType().toString());
                        serBlocksOrgLoc.add(block.getLocation());
                        if (block.getState() instanceof TileState) {
                            TileBlocksSerializer tbs = new TileBlocksSerializer(block);
                            serBlockData.add(block.getState().getBlockData().getAsString() + "~~spliter~~" + tbs.getBlockData());
//                            Block sddas = block.getWorld().getBlockAt(block.getLocation());
//                            sddas.setType(block.getType());


                        } else {
                            serBlockData.add(block.getState().getBlockData().getAsString());
                        }
                    }
                }
            }
        }

        //entity check
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    for (Entity e : block.getChunk().getEntities()) {
                        if (block.getLocation().distance(e.getLocation()) < 1.5) {
                            if (!(entityTypesAsString.contains(e.getType().toString()) && entitiesOrgLoc.contains(e.getLocation()))) {
                                if (e.getType().isSpawnable()) {
                                    entityTypesAsString.add(e.getType().toString());
                                    entitiesOrgLoc.add(e.getLocation());

                                    entities.add(e);

                                }
                            }

                        }
                    }
                }
            }
        }

        if (!buildFile.exists())
            buildFile.createNewFile();

        ArrayList<String> serBlockLocs = new ArrayList<>();
        ArrayList<String> serEntityLocs = new ArrayList<>();
        ArrayList<EntitySerializer> serEntities = new ArrayList<>();

        for (Location loc : serBlocksOrgLoc) {
            serBlockLocs.add(LocationSerializer.getSerializedLocation(loc));
        }
        for (Location loc : entitiesOrgLoc) {
            serEntityLocs.add(LocationSerializer.getSerializedLocation(loc));
        }
        //adds every serialized entity to the list
        serEntities.addAll(new ArrayList<>(entities.stream().map(entity -> new EntitySerializer(entity)).collect(Collectors.toList())));


        MCBuilds build = new MCBuilds(blockTypesAsString, serBlockLocs, entityTypesAsString, serEntityLocs, serBlockData, serEntities);

        FileOutputStream f = new FileOutputStream(buildFile);
        ObjectOutputStream o = new ObjectOutputStream(f);

        // Write objects to file
        o.writeObject(build);

        o.close();
        f.close();


    }

    //be careful with the blockface as the direction may generate exception as you cant rotate a bed down for example
    public static void pasteFromFile(Location middle, int degrees, String filename, Player sender, boolean isRelative) throws IOException, ClassNotFoundException, IllegalArgumentException, CommandSyntaxException {
        File buildFile = new File(Main.getInstance().getDataFolder(), filename + ".mcbuilds");


        if (!buildFile.exists()) {
            return;
        }







        FileInputStream fi = new FileInputStream(buildFile);
        ObjectInputStream oi = new ObjectInputStream(fi);

        MCBuilds mcBuilds = (MCBuilds) oi.readObject();



        if(isRelative){



            middle.add(Main.lastLoc);
        }




        oi.close();
        fi.close();
        //makes every type string his type for both blocks and entities
        List<Material> blockTypes = mcBuilds.getBlockTypesAsString().stream().map(string -> Material.valueOf(string)).collect(Collectors.toList());
        List<EntityType> entityTypes = mcBuilds.getEntityTypesAsString().stream().map(string -> EntityType.valueOf(string)).collect(Collectors.toList());
        List<Location> blockLocations = mcBuilds.getNewBlockLocations(middle, degrees);
        List<Location> entityLocations = mcBuilds.getNewEntityLocations(middle, degrees);
        //List<String> serTileBlockData = mcBuilds.getSerTileBlockData();


        if (blockTypes.isEmpty())
            return;


        int blockLocCount = 0;

        for (Material type : blockTypes) {
            Block targetBlock = middle.getWorld().getBlockAt(blockLocations.get(blockLocCount));


            if (type != Material.BEDROCK && type.name().toLowerCase().contains("bed")) {
                String data = mcBuilds.getSerBlockData().get(blockLocCount);
                if (data.contains("~~spliter~~")) {
                    data = data.split("~~spliter~~")[0];
                }


                targetBlock.setBlockData(Bukkit.createBlockData(data));
                if (targetBlock instanceof Directional) {
                    setBed(targetBlock, ((Directional) targetBlock).getFacing(), type);
                }


            } else {
                if (!(targetBlock.getBlockData() instanceof Bisected))
                    targetBlock.setType(type);
            }


            if (targetBlock.getState() instanceof TileState) {
                String[] data = mcBuilds.getSerBlockData().get(blockLocCount).split("~~spliter~~");
                String blockData = data[0];
                String tileBlockData = data[1];



                new BukkitRunnable() {


                    @Override
                    public void run() {
                        try {


                            WorldServer ws = ((CraftWorld) targetBlock.getWorld()).getHandle(); //W is your normal bukkit world . . . I'm using player.getWorld()
                            NBTTagCompound ntc = null;
                            try {
                                ntc = MojangsonParser.parse(tileBlockData);
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }

                            TileEntity te = ws.getTileEntity(new BlockPosition(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ()));

                            targetBlock.setBlockData(Bukkit.createBlockData(blockData));

                            ntc.setInt("x", targetBlock.getX());
                            ntc.setInt("y", targetBlock.getY());
                            ntc.setInt("z", targetBlock.getZ());

                            te.load(ntc);


                        } catch (Exception e) {
                            e.printStackTrace();

                        }


                    }
                }.runTaskLater(Main.getInstance(), 1L);


            } else {

                String data = mcBuilds.getSerBlockData().get(blockLocCount);
                if (!data.contains("~~spliter~~"))
                    targetBlock.setBlockData(Bukkit.createBlockData(mcBuilds.getSerBlockData().get(blockLocCount)));
                else
                    targetBlock.setBlockData(Bukkit.createBlockData(data.split("~~spliter~~")[0]));
            }



            if (targetBlock.getBlockData() instanceof Bisected) {
                String name = targetBlock.getType().name().toLowerCase();
                if (!(name.contains("stairs") || name.contains("door"))) {
                    Bisected blockData = (Bisected) targetBlock.getBlockData();


                    if (blockData.getHalf() == Bisected.Half.BOTTOM) {

                        Block flowerBlockLower = targetBlock;
                        Block flowerBlockUpper = flowerBlockLower.getRelative(BlockFace.UP);
                        flowerBlockLower.setType(type, false);
                        flowerBlockUpper.setType(type, false);

// Changing Bisected data to respective halfs
                        Bisected dataLower = (Bisected) flowerBlockLower.getBlockData();
                        dataLower.setHalf(Bisected.Half.BOTTOM);
                        Bisected dataHigher = (Bisected) flowerBlockUpper.getBlockData();
                        dataHigher.setHalf(Bisected.Half.TOP);


// Setting data on blocks
                        flowerBlockLower.setBlockData(dataLower, false);
                        flowerBlockUpper.setBlockData(dataHigher, false);
                    }
                }


            }
            if(targetBlock.getBlockData() instanceof Directional && degrees != 0){
                try {






                    if(targetBlock.getBlockData() instanceof Furnace){
                        Furnace furnace = (Furnace) targetBlock.getBlockData();
                        furnace.setFacing(degreeToFacing(degrees, furnace.getFacing()));
                    }

                    else{

                        Directional block = (Directional) targetBlock.getBlockData();


                        block.setFacing(degreeToFacing(degrees, block.getFacing()));
                        targetBlock.setBlockData(block);

                    }




                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }


            blockLocCount++;
        }

        if (entityTypes.isEmpty())
            return;
        int entityLocCount = 0;
        for (EntityType type : entityTypes) {
            Block targetBlock = middle.getWorld().getBlockAt(entityLocations.get(entityLocCount));

            Entity spawnedEntity = targetBlock.getWorld().spawnEntity(targetBlock.getLocation(), type);


            net.minecraft.server.v1_15_R1.Entity entity = ((CraftEntity) spawnedEntity).getHandle();
            EntitySerializer es = mcBuilds.getSerEntities().get(entityLocCount);


            if (es != null) {
                entity.f(MojangsonParser.parse(es.getEntity()));

                spawnedEntity.teleport(targetBlock.getLocation());

            }


            entityLocCount++;
        }

    }

    public static void setBed(Block start, BlockFace facing, Material material) {
        for (Bed.Part part : Bed.Part.values()) {
            start.setBlockData(Bukkit.createBlockData(material, (data) -> {
                ((Bed) data).setPart(part);
                ((Bed) data).setFacing(facing);
            }));
            start = start.getRelative(facing.getOppositeFace());
        }
    }

    private static BlockFace degreeToFacing(int degress, BlockFace currentBF){
        switch (degress){
            case (270):
                switch (currentBF){
                    case NORTH:
                        return BlockFace.EAST;
                    case EAST:
                        return BlockFace.SOUTH;
                    case SOUTH:
                        return BlockFace.WEST;
                    case WEST:
                        return BlockFace.NORTH;
                    default:
                        return BlockFace.SELF;
                }
            case (180):
                switch (currentBF){
                    case NORTH:
                        return BlockFace.SOUTH;
                    case EAST:
                        return BlockFace.WEST;
                    case SOUTH:
                        return BlockFace.NORTH;
                    case WEST:
                        return BlockFace.EAST;
                    default:
                        return BlockFace.SELF;
                }
            case (90):
                switch (currentBF){
                    case NORTH:
                        return BlockFace.WEST;
                    case EAST:
                        return BlockFace.NORTH;
                    case SOUTH:
                        return BlockFace.EAST;
                    case WEST:
                        return BlockFace.SOUTH;
                    default:
                        return BlockFace.SELF;
                }


        }
        //gave self
        return BlockFace.SELF;

    }




}
