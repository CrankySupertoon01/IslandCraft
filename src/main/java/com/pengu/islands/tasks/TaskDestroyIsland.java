package com.pengu.islands.tasks;

import java.util.ArrayList;
import java.util.List;

import com.pengu.hammercore.api.iProcess;
import com.pengu.hammercore.utils.WorldLocation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TaskDestroyIsland implements iProcess
{
	public World world;
	public List<BlockPos> broken = new ArrayList<>();
	public int lastBroken;
	
	public Runnable onFinish = () ->
	{
	};
	
	public TaskDestroyIsland(WorldLocation start)
	{
		world = start.getWorld();
		broken.add(start.getPos());
		lastBroken = 0;
	}
	
	@Override
	public void update()
	{
		if(lastBroken == -1)
			return;
		
		lastBroken = broken.size();
		
		for(int i = 0; i < broken.size(); ++i)
		{
			BlockPos pos = broken.get(i);
			breakAround(pos);
		}
		
		if(lastBroken == broken.size())
			lastBroken = -1;
	}
	
	public void breakAround(BlockPos pos)
	{
		int r = 12;
		for(int x = -r; x <= r; ++x)
			for(int y = -r; y <= r; ++y)
				for(int z = -r; z <= r; ++z)
				{
					BlockPos t = pos.add(x, y, z);
					
					if(!world.isAirBlock(t) && !broken.contains(t))
					{
						TileEntity te = world.getTileEntity(t);
						if(te != null)
							te.readFromNBT(new NBTTagCompound());
						world.setTileEntity(t, null);
						world.setBlockToAir(t);
						
						broken.add(t);
						
						AxisAlignedBB aabb = new AxisAlignedBB(t).grow(r);
						
						List<net.minecraft.entity.Entity> ents = world.getEntitiesWithinAABBExcludingEntity(null, aabb);
						for(Entity ent : ents)
							if(!(ent instanceof EntityPlayer))
								ent.setDead();
					}
				}
	}
	
	@Override
	public void onKill()
	{
		onFinish.run();
	}
	
	@Override
	public boolean isAlive()
	{
		return lastBroken != -1;
	}
}