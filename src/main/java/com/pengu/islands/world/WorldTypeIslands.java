package com.pengu.islands.world;

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
		return new ChunkGeneratorFlat(world, 0, false, "0:0");
	}
	
	@Override
	public float getCloudHeight()
	{
		return 256;
	}
}