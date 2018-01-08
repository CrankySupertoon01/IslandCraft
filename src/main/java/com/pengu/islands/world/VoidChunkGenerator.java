package com.pengu.islands.world;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class VoidChunkGenerator implements IChunkGenerator
{
	public final World world;
	
	public VoidChunkGenerator(World worldIn)
	{
		world = worldIn;
	}
	
	@Override
	public Chunk generateChunk(int x, int z)
	{
		return new Chunk(world, x, z);
	}
	
	@Override
	public void populate(int x, int z)
	{
	}
	
	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z)
	{
		return true;
	}
	
	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
	{
		return world.getBiome(pos).getSpawnableList(creatureType);
	}
	
	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
	{
		return null;
	}
	
	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z)
	{
	}
	
	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
	{
		return false;
	}
}