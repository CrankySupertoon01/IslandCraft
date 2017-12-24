package com.pengu.islands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pengu.hammercore.utils.IndexedMap;
import com.pengu.islands.config.ConfigsIC;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class IslandData
{
	public static Map<Integer, IslandData> PERWORLD = new HashMap<>();
	
	public IndexedMap<String, BlockPos> islands = new IndexedMap<>();
	public final World world;
	
	public IslandData(World world)
	{
		this.world = world;
	}
	
	public static IslandData getDataFor(World world)
	{
		IslandData data = PERWORLD.get(ConfigsIC.islandDim);
		if(data == null)
			PERWORLD.put(ConfigsIC.islandDim, data = new IslandData(world));
		return data;
	}
	
	public boolean hasIsland(String player)
	{
		return islands.containsKey(player);
	}
	
	public BlockPos getIsland(String player)
	{
		BlockPos pos = islands.get(player);
		if(pos == null)
			islands.put(player, pos = getNextIsland());
		return new BlockPos(pos.getX() * ConfigsIC.islandDistance, ConfigsIC.islandY, pos.getZ() * ConfigsIC.islandDistance);
	}
	
	public BlockPos getNextIsland()
	{
		if(this.islands.isEmpty())
			return BlockPos.ORIGIN;
		
		List<BlockPos> islands = (ArrayList<BlockPos>) this.islands.values();
		List<BlockPos> candidates = new ArrayList<>();
		
		for(int i = 0; i < islands.size(); ++i)
		{
			BlockPos pos = islands.get(i);
			
			if(!islands.contains(pos.east()))
				candidates.add(pos.east());
			
			if(!islands.contains(pos.west()))
				candidates.add(pos.west());
			
			if(!islands.contains(pos.south()))
				candidates.add(pos.south());
			
			if(!islands.contains(pos.north()))
				candidates.add(pos.north());
		}
		
		double closest = Double.POSITIVE_INFINITY;
		BlockPos cand = null;
		
		for(int i = 0; i < candidates.size(); ++i)
		{
			BlockPos pos = candidates.get(i);
			double dist = pos.getDistance(0, 0, 0);
			
			if(dist <= closest)
			{
				closest = dist;
				cand = pos;
			}
		}
		
		return cand;
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		islands.clear();
		
		NBTTagList list = nbt.getTagList("islands", NBT.TAG_COMPOUND);
		for(int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound tag = list.getCompoundTagAt(i);
			islands.put(tag.getString("key"), BlockPos.fromLong(tag.getLong("val")));
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for(String key : islands.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("key", key);
			tag.setLong("val", islands.get(key).toLong());
		}
		nbt.setTag("islands", list);
		return nbt;
	}
	
	public Serializable serialize()
	{
		HashMap<String, Long> ser = new HashMap<>();
		for(String s : islands.getKeys())
			ser.put(s, islands.get(s).toLong());
		return ser;
	}
}