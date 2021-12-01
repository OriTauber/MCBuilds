package me.ori.main;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.io.Serializable;

public class TileBlocksSerializer implements Serializable {

    private String blockData;

    public String getBlockData() {
        return blockData;
    }

    public TileBlocksSerializer(Block block) {
        this.blockData = getNBTCompoundData(block).toString();
    }



    public NBTTagCompound getNBTCompoundData(Block block) {
        WorldServer ws = ((CraftWorld) block.getWorld()).getHandle();
        NBTTagCompound ntc = new NBTTagCompound();
        TileEntity te = ws.getTileEntity(new BlockPosition(block.getX(),block.getY(),block.getZ()));

        te.save(ntc);

        return ntc;

    }




}
