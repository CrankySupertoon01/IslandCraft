package com.pengu.islands.config;

import java.io.File;

import com.pengu.hammercore.cfg.HCModConfigurations;
import com.pengu.hammercore.cfg.iConfigReloadListener;
import com.pengu.hammercore.cfg.fields.ModConfigPropertyInt;
import com.pengu.islands.InfoIC;

@HCModConfigurations(modid = InfoIC.MOD_ID)
public class ConfigsIC implements iConfigReloadListener
{
	@ModConfigPropertyInt(name = "Y", category = "Island Properties", comment = "On what Y level should lowest point of island spawn?", defaultValue = 127, min = 0, max = 250)
	public static int islandY;
	
	@ModConfigPropertyInt(name = "Distance", category = "Island Properties", comment = "What distance should islands have inbetween?", defaultValue = 500, min = 64, max = 100000)
	public static int islandDistance;
	
	@ModConfigPropertyInt(name = "Dimension", category = "Island Properties", comment = "In what dimension should islands spawn?", defaultValue = 0, min = Integer.MIN_VALUE, max = Integer.MAX_VALUE)
	public static int islandDim;
	
	@Override
	public File getSuggestedConfigurationFile()
	{
		File cfg = new File("config", getModid());
		if(!cfg.isDirectory())
			cfg.mkdirs();
		return new File(cfg, "config.txt");
	}
}