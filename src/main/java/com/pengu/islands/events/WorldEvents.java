package com.pengu.islands.events;

import java.util.HashMap;

import com.pengu.hammercore.annotations.MCFBus;
import com.pengu.hammercore.event.WorldEventsHC;
import com.pengu.islands.IslandData;
import com.pengu.islands.config.ConfigsIC;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@MCFBus
public class WorldEvents
{
	@SubscribeEvent
	public void save(WorldEventsHC.SaveData e)
	{
		if(e.getWorld().provider.getDimension() == ConfigsIC.islandDim)
		{
			IslandData id = IslandData.getData();
			e.additionalData.put("islands", id.serialize());
		}
	}
	
	@SubscribeEvent
	public void load(WorldEventsHC.LoadData e)
	{
		if(e.getWorld().provider.getDimension() == ConfigsIC.islandDim)
		{
			HashMap<String, Long> data = (HashMap<String, Long>) e.additionalData.get("islands");
			if(data == null)
				return;
			IslandData id = new IslandData();
			data.keySet().forEach(key -> id.islands.put(key, BlockPos.fromLong(data.get(key))));
			IslandData.data = id;
		}
	}
}