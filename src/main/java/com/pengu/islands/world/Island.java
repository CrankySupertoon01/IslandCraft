package com.pengu.islands.world;

import java.util.List;

import com.pengu.hammercore.utils.IndexedMap;
import com.pengu.hammercore.utils.WorldLocation;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class Island
{
	public IndexedMap<Long, NBTTagCompound> data = new IndexedMap<>();
	
	public Island(WorldLocation center, int x, int y, int z)
	{
		World world = center.getWorld();
		
		int cx = center.getPos().getX();
		int cy = center.getPos().getY();
		int cz = center.getPos().getZ();
		
		for(int ix = cx - x; ix <= cx + x; ++ix)
			for(int iy = cy - y; iy <= cy + y; ++iy)
				for(int iz = cz - z; iz <= cz + z; ++iz)
				{
					BlockPos pos = new BlockPos(ix, iy, iz);
					NBTTagCompound tag = new NBTTagCompound();
					IBlockState state = world.getBlockState(pos);
					tag.setString("id", state.getBlock().getRegistryName().toString());
					tag.setInteger("data", state.getBlock().getMetaFromState(state));
					TileEntity tile = world.getTileEntity(pos);
					if(tile != null)
						tag.setTag("tile", tile.serializeNBT());
					long p = pos.subtract(center.getPos()).toLong();
					tag.setLong("pos", p);
					data.put(p, tag);
				}
	}
	
	public Island(NBTTagList list)
	{
		for(int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound tag = list.getCompoundTagAt(i);
			data.put(tag.getLong("pos"), tag);
		}
	}
	
	public NBTTagList serialize()
	{
		NBTTagList list = new NBTTagList();
		List<NBTTagCompound> vals = data.getValues();
		for(int i = 0; i < vals.size(); ++i)
			list.appendTag(vals.get(i));
		return list;
	}
	
	public void build(WorldLocation center)
	{
		BlockPos cpos = center.getPos();
		World world = center.getWorld();
		IForgeRegistry<Block> reg = GameRegistry.findRegistry(Block.class);
		
		List<Long> longs = data.getKeys();
		
		for(int i = 0; i < longs.size(); ++i)
		{
			BlockPos pos = BlockPos.fromLong(longs.get(i)).add(cpos);
			NBTTagCompound nbt = data.get(longs.get(i));
			
			IBlockState state = reg.getValue(new ResourceLocation(nbt.getString("id"))).getStateFromMeta(nbt.getInteger("data"));
			TileEntity tile = null;
			
			if(nbt.hasKey("tile", NBT.TAG_COMPOUND))
			{
				NBTTagCompound tag = nbt.getCompoundTag("tile").copy();
				tag.setInteger("x", pos.getX());
				tag.setInteger("y", pos.getY());
				tag.setInteger("z", pos.getZ());
				tile = TileEntity.create(world, tag);
			}
			
			world.setBlockState(pos, state);
			world.setTileEntity(pos, tile);
		}
	}
}