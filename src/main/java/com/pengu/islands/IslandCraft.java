package com.pengu.islands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import com.pengu.hammercore.HammerCore;
import com.pengu.hammercore.HammerCore.HCAuthor;
import com.pengu.hammercore.common.utils.IOUtils;
import com.pengu.islands.commands.CommandIC;
import com.pengu.islands.proxy.CommonProxy;
import com.pengu.islands.world.WorldTypeIslands;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = InfoIC.MOD_ID, name = InfoIC.MOD_NAME, version = InfoIC.MOD_VERSION, dependencies = "required-after:hammercore")
public class IslandCraft
{
	public static WorldTypeIslands islandWorldType;
	
	@Instance
	public static IslandCraft instance;
	
	@SidedProxy(serverSide = InfoIC.PROXY_SERVER, clientSide = InfoIC.PROXY_CLIENT)
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		islandWorldType = new WorldTypeIslands();
		
		ModMetadata meta = e.getModMetadata();
		meta.autogenerated = false;
		meta.authorList = new ArrayList<>();
		for(HCAuthor au : HammerCore.getHCAuthors())
			meta.authorList.add(au.getUsername());
		meta.authorList = Collections.unmodifiableList(meta.authorList);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e)
	{
		File ics = new File("config", InfoIC.MOD_ID + File.separator + "island.ics");
		
		if(!ics.isFile())
		{
			try(InputStream in = IslandCraft.class.getResourceAsStream("/island.ics");FileOutputStream fos = new FileOutputStream(ics))
			{
				IOUtils.pipeData(in, fos);
			} catch(Throwable err)
			{
				err.printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		e.registerServerCommand(new CommandIC());
	}
	
	@EventHandler
	public void serverStopping(FMLServerStoppingEvent e)
	{
		CommandIC.pending.clear();
	}
	
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent e)
	{
		IslandData.PERWORLD.clear();
	}
}