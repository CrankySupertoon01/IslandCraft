package com.pengu.islands.events;

import java.util.HashMap;

import com.pengu.hammercore.annotations.MCFBus;
import com.pengu.hammercore.event.WorldEventsHC;
import com.pengu.islands.IslandData;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@MCFBus
public class WorldEvents
{
	@SubscribeEvent
	public void save(WorldEventsHC.SaveData e)
	{
		IslandData id = IslandData.getDataFor(e.getWorld());
		e.additionalData.put("islands", id.serialize());
	}
	
	@SubscribeEvent
	public void load(WorldEventsHC.LoadData e)
	{
		HashMap<String, Long> data = (HashMap<String, Long>) e.additionalData.get("islands");
		if(data == null)
			return;
		IslandData id = new IslandData(e.getWorld());
		data.keySet().forEach(key -> id.islands.put(key, BlockPos.fromLong(data.get(key))));
		IslandData.PERWORLD.put(e.getWorld().provider.getDimension(), id);
	}
}