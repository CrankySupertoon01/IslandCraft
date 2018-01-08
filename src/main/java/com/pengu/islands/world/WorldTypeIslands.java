package com.pengu.islands.world;

import java.util.Arrays;

import com.pengu.islands.config.ConfigsIC;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldTypeIslands extends WorldType
{
	public WorldTypeIslands()
	{
		super("islands");
	}
	
	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
	{
		boolean cnt = false;
		for(int i : ConfigsIC.voidWorlds)
			if(i == world.provider.getDimension())
			{
				cnt = true;
				break;
			}
		if(cnt)
			return new VoidChunkGenerator(world);
		return new ChunkGeneratorFlat(world, 0, false, "0:0");
	}
	
	@Override
	public float getCloudHeight()
	{
		return 256;
	}
}