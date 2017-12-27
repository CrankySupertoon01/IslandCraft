package com.pengu.islands.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.pengu.hammercore.common.utils.XPUtil;
import com.pengu.hammercore.utils.IndexedMap;
import com.pengu.hammercore.utils.WorldLocation;
import com.pengu.islands.InfoIC;
import com.pengu.islands.IslandCraft;
import com.pengu.islands.IslandData;
import com.pengu.islands.config.ConfigsIC;
import com.pengu.islands.events.PlayerEvents;
import com.pengu.islands.tasks.TaskDestroyIsland;
import com.pengu.islands.world.Island;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants.NBT;

public class CommandIC extends CommandBase
{
	public static IndexedMap<String, String> pending = new IndexedMap<>();
	
	@Override
	public String getName()
	{
		return "islandcraft";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/ic <move/get/reset/kick>";
	}
	
	@Override
	public List<String> getAliases()
	{
		return Arrays.asList("ic", "islands");
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof EntityPlayer;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length == 0)
			throw new CommandException("No arguments!");
		
		args[0] = args[0].toLowerCase();
		
		if(args[0].equals("export") && sender.canUseCommand(3, "ic"))
		{
			int x = parseInt(args[1], 0, 32);
			int y = parseInt(args[2], 0, 32);
			int z = parseInt(args[3], 0, 32);
			
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Capturing..."));
			long start = System.currentTimeMillis();
			Island is = new Island(new WorldLocation(sender.getEntityWorld(), sender.getPosition()), x, y, z);
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Captured (" + (System.currentTimeMillis() - start) + " ms). Saving..."));
			
			start = System.currentTimeMillis();
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setTag("data", is.serialize());
			
			File exports = new File("island-exports");
			if(!exports.isDirectory())
				exports.mkdirs();
			File file = new File(exports, "island_" + new SimpleDateFormat("yy_MM_dd.hh.mm.ss").format(Date.from(Instant.now())) + ".ics");
			
			try(FileOutputStream fos = new FileOutputStream(file))
			{
				CompressedStreamTools.writeCompressed(nbt, fos);
			} catch(Throwable err)
			{
				throw new CommandException("Failed to save!", err);
			}
			
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Saved (" + (System.currentTimeMillis() - start) + " ms)."));
		} else if(args[0].equals("build") && sender.canUseCommand(3, "ic"))
		{
			File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
			
			try(FileInputStream fis = new FileInputStream(ics))
			{
				NBTTagList list = CompressedStreamTools.readCompressed(fis).getTagList("data", NBT.TAG_COMPOUND);
				
				Island isl = new Island(list);
				isl.build(new WorldLocation(sender.getEntityWorld(), sender.getPosition()));
			} catch(Throwable err)
			{
				err.printStackTrace();
			}
		} else if(args[0].equals("move"))
		{
			if(args[1].equals("confirm+"))
			{
				if(pending.containsKey(sender.getName()))
				{
					String key = pending.getKey(sender.getName());
					pending.remove(key);
					EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(key);
					
					sender.sendMessage(new TextComponentTranslation("islands.requestcomfirmed"));
					player.sendMessage(new TextComponentTranslation("islands.pconfirm", sender.getName()));
					
					IslandData id = IslandData.getData();
					
					BlockPos target = id.getIsland(sender.getName());
					BlockPos destroy = id.getIsland(player.getName());
					
					id.islands.put(player.getName(), id.islands.get(sender.getName()));
					
					TaskDestroyIsland task = new TaskDestroyIsland(new WorldLocation(server.getWorld(ConfigsIC.islandDim), destroy));
					while(task.isAlive())
						task.update();
					
					if(!id.hasIsland(sender.getName()))
					{
						File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
						
						try(FileInputStream fis = new FileInputStream(ics))
						{
							NBTTagList list = CompressedStreamTools.readCompressed(fis).getTagList("data", NBT.TAG_COMPOUND);
							
							Island isl = new Island(list);
							isl.build(new WorldLocation(server.getWorld(ConfigsIC.islandDim), target));
						} catch(Throwable err)
						{
							err.printStackTrace();
						}
					}
					
					if(player instanceof EntityPlayerMP)
					{
						EntityPlayerMP p = player;
						double x, y, z;
						p.connection.setPlayerLocation(x = target.getX() + .5, y = p.world.getHeight(target).getY() + 2, z = target.getZ() + .5, 0, 0);
						p.setPositionAndUpdate(x, y, z);
						p.setSpawnPoint(p.world.getHeight(target), true);
						p.fallDistance = 0;
					}
				} else
					throw new CommandException("You don't have pending requests.");
			} else if(args[1].equals("deny-"))
			{
				if(pending.containsKey(sender.getName()))
				{
					String key = pending.getKey(sender.getName());
					pending.remove(key);
					
					sender.sendMessage(new TextComponentTranslation("islands.requestcanceled"));
					EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(key);
					player.sendMessage(new TextComponentTranslation("islands.pdeny", sender.getName()));
				} else
					throw new CommandException("You don't have pending requests.");
			} else if(Arrays.asList(server.getPlayerList().getOnlinePlayerNames()).contains(args[1]))
			{
				if(args[1].equals(sender.getName()))
					throw new CommandException("You can't send move request to yourself!");
				if(!pending.containsKey(sender.getName()))
				{
					EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[1]);
					sender.sendMessage(new TextComponentTranslation("islands.move_to", player.getName()));
					player.sendMessage(new TextComponentTranslation("islands.move_senttoyou", sender.getName()));
					
					pending.put(sender.getName(), player.getName());
				} else
					throw new CommandException("You have pending incoming request.");
			} else
				throw new CommandException("Player not found!");
		} else if(args[0].equals("kick"))
		{
			if(Arrays.asList(server.getPlayerList().getOnlinePlayerNames()).contains(args[1]))
			{
				EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[1]);
				
				IslandData id = IslandData.getData();
				BlockPos pos = id.getIsland(player.getGameProfile().getName());
				
				String cp = null;
				double dist = Double.POSITIVE_INFINITY;
				
				List<String> users = id.islands.getKeys();
				for(int i = 0; i < users.size(); ++i)
				{
					BlockPos ppos = id.getIsland(users.get(i));
					
					double d = sender.getPosition().distanceSq(ppos);
					
					if(d < dist)
					{
						cp = users.get(i);
						dist = d;
					}
				}
				
				if(cp.equals(player.getGameProfile().getName()))
					throw new CommandException("You cannot kick the owner of the island.");
				
				id.islands.remove(player.getGameProfile().getName());
				
				PlayerEvents.homePlayer(player, true);
			} else
				throw new CommandException("Player not found!");
		} else if(args[0].equals("get"))
		{
			IslandData id = IslandData.getData();
			
			String cp = null;
			double dist = Double.POSITIVE_INFINITY;
			
			List<String> users = id.islands.getKeys();
			for(int i = 0; i < users.size(); ++i)
			{
				BlockPos pos = id.getIsland(users.get(i));
				
				double d = sender.getPosition().distanceSq(pos);
				
				if(d < dist)
				{
					cp = users.get(i);
					dist = d;
				}
			}
			
			if(cp != null)
				sender.sendMessage(new TextComponentTranslation("islands.island", cp, dist));
		} else if(args[0].equals("reset"))
		{
			IslandData id = IslandData.getData();
			
			if(!id.hasIsland(sender.getName()))
				throw new CommandException("You don't own an island!");
			
			BlockPos local = id.getLocalIsland(sender.getName());
			
			List<String> users = id.islands.getKeys();
			users.removeIf(u -> !Objects.equals(id.islands.get(u), local));
			
			if(users.size() > 1)
				throw new CommandException("You have allies on your island; you can not reset the island.");
			
			BlockPos target = id.getIsland(sender.getName());
			
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "Resetting..."));
			TaskDestroyIsland task = new TaskDestroyIsland(new WorldLocation(server.getWorld(ConfigsIC.islandDim), target));
			
			task.onFinish = () ->
			{
				sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Building..."));
				
				File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
				
				try(FileInputStream fis = new FileInputStream(ics))
				{
					NBTTagList list = CompressedStreamTools.readCompressed(fis).getTagList("data", NBT.TAG_COMPOUND);
					
					Island isl = new Island(list);
					isl.build(new WorldLocation(server.getWorld(ConfigsIC.islandDim), target));
				} catch(Throwable err)
				{
					err.printStackTrace();
				}
				
				if(sender instanceof EntityPlayerMP)
				{
					sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Teleporting Player (& reseting player info)..."));
					
					EntityPlayerMP p = (EntityPlayerMP) sender;
					PlayerEvents.homePlayer(p, true);
					p.fallDistance = 0;
					p.inventory.clear();
					p.setHealth(20F);
					p.getFoodStats().setFoodLevel(20);
					p.getFoodStats().setFoodSaturationLevel(2);
					XPUtil.setPlayersExpTo(p, 0);
				}
				
				sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Done! Enjoy your new island!"));
			};
			
			new Thread(() ->
			{
				long ticks = 0;
				while(task.isAlive())
				{
					task.update();
					++ticks;
				}
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Your old island has been broken in " + ticks + " ticks."));
				task.onKill();
			}).start();
		} else if(args[0].equals("h") || args[0].equals("home"))
		{
			if(sender instanceof EntityPlayerMP)
				PlayerEvents.homePlayer((EntityPlayerMP) sender, false);
			else
				throw new CommandException("You are not a player!");
		} else if(args[0].equals("tp") && sender.canUseCommand(3, "ic"))
		{
			IslandData id = IslandData.getData();
			
			if(id.hasIsland(args[1]))
			{
				BlockPos pos = id.getIsland(args[1]);
				IslandCraft.teleportPlayer((EntityPlayerMP) sender, pos.getX() + .5, server.getWorld(ConfigsIC.islandDim).getHeight(pos).getY() + 2, pos.getZ() + .5, ConfigsIC.islandDim);
			} else
				throw new CommandException("Player not found!");
		} else if(args[0].equals("forcereset") && sender.canUseCommand(3, "ic"))
		{
			IslandData id = IslandData.getData();
			
			if(id.hasIsland(args[1]))
			{
				BlockPos pos = id.getIsland(args[1]);
				
				id.islands.remove(args[1]);
				
				new TaskDestroyIsland(new WorldLocation(server.getWorld(ConfigsIC.islandDim), pos)).start();
			} else
				throw new CommandException("Player not found!");
		}
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
	{
		if(args.length == 1)
		{
			List<String> vars = new ArrayList<>();
			
			vars.add("move");
			vars.add("kick");
			vars.add("get");
			vars.add("reset");
			vars.add("h");
			vars.add("home");
			
			if(sender.canUseCommand(3, "ic"))
			{
				vars.add("export");
				vars.add("build");
				vars.add("tp");
				vars.add("forcereset");
			}
			
			return complete(vars, args[0]);
		}
		
		if(args.length == 2)
		{
			if(args[0].equals("move"))
			{
				if(pending.containsKey(sender.getName()))
					return complete(Arrays.asList("confirm+", "deny-"), args[1]);
				return complete(Arrays.asList(server.getPlayerList().getOnlinePlayerNames()), args[1]);
			} else if(args[0].equals("kick"))
				return complete(Arrays.asList(server.getPlayerList().getOnlinePlayerNames()), args[1]);
			else if(args[0].equals("tp") && sender.canUseCommand(3, "ic"))
				return complete(IslandData.getData().islands.getKeys(), args[1]);
			else if(args[0].equals("forcereset") && sender.canUseCommand(3, "ic"))
				return complete(IslandData.getData().islands.getKeys(), args[1]);
		}
		
		return Collections.emptyList();
	}
	
	private static List<String> complete(List<String> src, String cur)
	{
		src = new ArrayList<>(src);
		src.removeIf(str -> !str.toLowerCase().startsWith(cur.toLowerCase()));
		return src;
	}
}