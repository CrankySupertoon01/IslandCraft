package com.pengu.islands.events;

import java.io.File;
import java.io.FileInputStream;

import com.pengu.hammercore.annotations.MCFBus;
import com.pengu.hammercore.utils.WorldLocation;
import com.pengu.islands.InfoIC;
import com.pengu.islands.IslandCraft;
import com.pengu.islands.IslandData;
import com.pengu.islands.config.ConfigsIC;
import com.pengu.islands.world.Island;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

@MCFBus
public class PlayerEvents
{
	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent e)
	{
		EntityPlayer p = e.player;
		
		if(p.world.getWorldType() == IslandCraft.islandWorldType && p.getServer() != null)
		{
			IslandData id = IslandData.getDataFor(p.getServer().getWorld(ConfigsIC.islandDim));
			String name = p.getGameProfile().getName();
			boolean first = !id.hasIsland(name);
			BlockPos island = id.getIsland(name);
			
			if(first && p instanceof EntityPlayerMP)
			{
				File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
				
				try(FileInputStream fis = new FileInputStream(ics))
				{
					NBTTagList list = CompressedStreamTools.readCompressed(fis).getTagList("data", NBT.TAG_COMPOUND);
					
					Island isl = new Island(list);
					isl.build(new WorldLocation(p.getEntityWorld(), island));
				} catch(Throwable err)
				{
					err.printStackTrace();
				}
				
				double x, y, z;
				((EntityPlayerMP) p).connection.setPlayerLocation(x = island.getX() + .5, y = p.world.getHeight(island).getY() + 2, z = island.getZ() + .5, 0, 0);
				p.setPositionAndUpdate(x, y, z);
				p.setSpawnPoint(p.world.getHeight(island), true);
				p.fallDistance = 0;
			}
		}
	}
	
	@SubscribeEvent
	public void playerRespawn(PlayerRespawnEvent e)
	{
		EntityPlayer p = e.player;
		
		if(p.world.getWorldType() == IslandCraft.islandWorldType && p.getServer() != null)
		{
			IslandData id = IslandData.getDataFor(p.getServer().getWorld(ConfigsIC.islandDim));
			String name = p.getGameProfile().getName();
			boolean first = !id.hasIsland(name);
			BlockPos island = id.getIsland(name);
			
			if(first && p instanceof EntityPlayerMP)
			{
				File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
				
				try(FileInputStream fis = new FileInputStream(ics))
				{
					NBTTagList list = CompressedStreamTools.readCompressed(fis).getTagList("data", NBT.TAG_COMPOUND);
					
					Island isl = new Island(list);
					isl.build(new WorldLocation(p.getEntityWorld(), p.getPosition()));
				} catch(Throwable err)
				{
					err.printStackTrace();
				}
			}
			
			if(p instanceof EntityPlayerMP)
			{
				double x, y, z;
				((EntityPlayerMP) p).connection.setPlayerLocation(x = island.getX() + .5, y = p.world.getHeight(island).getY() + 2, z = island.getZ() + .5, 0, 0);
				p.setPositionAndUpdate(x, y, z);
				p.fallDistance = 0;
			}
		}
	}
}