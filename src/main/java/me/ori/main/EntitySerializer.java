package me.ori.main;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.World;

import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.UUID;

import java.util.zip.GZIPOutputStream;

public class EntitySerializer implements Serializable {


    private String entity;

    public EntitySerializer(Entity entity) {
        this.entity = this.getNBTCompoundData(entity).toString();
    }

    public NBTTagCompound getNBTCompoundData(Entity entity) {
        net.minecraft.server.v1_15_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle(); //Converting our Entity to NMS
        NBTTagCompound compound = new NBTTagCompound();
        nmsEntity.save(compound); //Taking our entity and calling the obfuscated a_ method which will fill out our NBTCompound Object for us.

        return compound;

    }


    public Entity getEntityFromNBT(String compound, World world) throws CommandSyntaxException {
        NBTTagCompound nbtTagCompound = MojangsonParser.parse(compound); //Using Mojang's API to parse our Vehicle String

        NBTTagCompound clone = nbtTagCompound.clone(); // <-- Unnecessary Clone, you can keep it if you need!

        WorldServer worldServer = ((CraftWorld) world).getHandle(); //Getting our NMS WorldServer from the Bukkit Server

        net.minecraft.server.v1_15_R1.Entity entity = EntityTypes.a(clone, worldServer, (entity1) -> {
            entity1.dead = false;
            UUID uniqueID = entity1.getUniqueID();
            uniqueID = UUID.randomUUID();

            //PreConditions ;
            // dead = false,
            // uniqueID cant be taken,
            // ChunkAccess instanceOf Chunk(entity not in loaded chunk),
            // Event cant be cancelled/World has to allow given entity type

            return !worldServer.addEntitySerialized(entity1) ? null : entity1;
        });
        if (entity != null) {
            if (entity instanceof EntityInsentient) {
                ((EntityInsentient) entity).prepare(worldServer, worldServer.getDamageScaler(entity.getChunkCoordinates()), EnumMobSpawn.COMMAND, null, null);
            }
            return entity.getBukkitEntity();
        }
        return null;
    }

    public boolean saveData(String filePath, String fileName) {
        try {
            File targetDir = new File(filePath);
            File targetFile = new File(filePath + File.separator + fileName + ".gz");
            targetFile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(targetFile, false);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzipOutputStream);

            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return this.entity;
    }

    public Entity getBukkitEntity(World world) throws CommandSyntaxException {
        return this.getEntityFromNBT(this.entity, world);
    }
}