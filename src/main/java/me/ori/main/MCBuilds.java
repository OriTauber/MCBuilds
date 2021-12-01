package me.ori.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;


import java.io.Serializable;
import java.util.ArrayList;


public class MCBuilds implements Serializable {
    //You maybe need to change this
    private static final long serialVersionUID = -3050147762118768399L;
    //above




    private ArrayList<String> blockTypesAsString;
    private ArrayList<String> serBlocksOrgLoc;



    private ArrayList<String> serBlockData;

    private ArrayList<String> entityTypesAsString;
    private ArrayList<String> entitiesOrgLoc;






    public ArrayList<EntitySerializer> getSerEntities() {
        return serEntities;
    }

    private ArrayList<EntitySerializer> serEntities;

    public ArrayList<String> getEntityTypesAsString() {
        return entityTypesAsString;
    }





    public MCBuilds(ArrayList<String> blockTypesAsString, ArrayList<String> blocksOrgLoc, ArrayList<String> entityTypesAsString,
                    ArrayList<String> entitiesOrgLoc, ArrayList<String> serBlockData, ArrayList<EntitySerializer> serEntities) {
        this.blockTypesAsString = blockTypesAsString;
        this.serBlocksOrgLoc = blocksOrgLoc;
        this.entityTypesAsString = entityTypesAsString;
        this.entitiesOrgLoc = entitiesOrgLoc;
        this.serBlockData = serBlockData;
        this.serEntities = serEntities;


    }



    public ArrayList<String> getBlockTypesAsString() {
        return blockTypesAsString;
    }
    public ArrayList<String> getSerBlockData() { return serBlockData; }





    public  ArrayList<Location> getNewBlockLocations(Location midLoc, int degrees){
        ArrayList<Location> locations = new ArrayList<>();
        if (serBlocksOrgLoc.size() == 0)
            return locations;


        double centerX = 0, centerY = 0, centerZ = 0;
        ArrayList<Location> orgLocs = new ArrayList<>();
        //find center of mass with average
        for (String s : serBlocksOrgLoc){
            Location desLoc = LocationSerializer.getDeserializedLocation(s);
            centerX += desLoc.getBlockX();
            centerY += desLoc.getBlockY();
            centerZ += desLoc.getBlockZ();
            orgLocs.add(desLoc);


        }

        centerX /= serBlocksOrgLoc.size();
        centerY /= serBlocksOrgLoc.size();
        centerZ /= serBlocksOrgLoc.size();


        //set a new location list around the new center




        return rotateBuild(degrees, orgLocs, centerX,centerY, centerZ, midLoc);


    }

    public  ArrayList<Location> getNewEntityLocations(Location midLoc, int degrees){
        ArrayList<Location> locations = new ArrayList<>();
        if (serBlocksOrgLoc.size() == 0)
            return locations;


        double centerX = 0, centerY = 0, centerZ = 0;
        ArrayList<Location> orgLocs = new ArrayList<>();
        //find center of mass with average
        for (String s : serBlocksOrgLoc){
            Location desLoc = LocationSerializer.getDeserializedLocation(s);
            centerX += desLoc.getBlockX();
            centerY += desLoc.getBlockY();
            centerZ += desLoc.getBlockZ();
            orgLocs.add(desLoc);
        }
        centerX /= serBlocksOrgLoc.size();
        centerY /= serBlocksOrgLoc.size();
        centerZ /= serBlocksOrgLoc.size();











        return rotateBuild(degrees, orgLocs, centerX,centerY, centerZ, midLoc);

    }

    private ArrayList<Location> rotateBuild(int degrees, ArrayList<Location> locations, double centerX, double centerY, double centerZ, Location midLoc){


        ArrayList<Location> newLocations = new ArrayList<>();


        double minX = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Location loc : locations) {
            if (loc.getBlockX() < minX)
                minX = loc.getBlockX();
            if (loc.getBlockZ() < minZ)
                minZ = loc.getBlockZ();

            if (loc.getBlockX() > maxX)
                maxX = loc.getBlockX();
            if (loc.getBlockZ() > maxZ)
                maxZ = loc.getBlockZ();

            if (loc.getBlockY() < minY)
                minY = loc.getBlockY();
            if (loc.getBlockY() > maxY)
                maxY = loc.getBlockY();
        }

        double newMinX = midLoc.getX() - (centerX - minX);
        double newMinY = midLoc.getY() - (centerY - minY);
        double newMinZ = midLoc.getZ() - (centerZ - minZ);

        double dx = 0, dy = 0, dz = 0;
        double newX = 0, newZ = 0, newY = 0;
        for (Location loc : locations){


            dy = loc.getY() - minY;




            newY = newMinY + dy;




            switch (degrees){
                case (90):
                    newX = newMinX + (loc.getZ() - minZ);
                    newZ = newMinZ + (maxX - loc.getX());
                    break;

                case (180):
                    newX = newMinX + (maxX - loc.getX());
                    newZ = newMinZ + (maxZ - loc.getZ());
                    break;

                case (270):
                    newX = newMinX + (maxZ - loc.getZ());
                    newZ = newMinZ + (loc.getX() - minX);
                    break;

                default:
                    newX = newMinX + (loc.getX() - minX);
                    newZ = newMinZ + (loc.getZ() - minZ);
                    break;




            }





            newLocations.add(new Location(midLoc.getWorld(), newX, newY, newZ));

        }
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Structure details: "
                + ChatColor.WHITE + "width(x): " + (maxX - minX) + ", tall(y): " + (maxY - minY) + ", height(z): " + (maxZ - minZ));
        return newLocations;




    }


}
