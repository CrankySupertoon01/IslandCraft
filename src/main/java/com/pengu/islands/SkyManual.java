package com.pengu.islands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pengu.hammercore.bookAPI.fancy.ManualCategories;
import com.pengu.hammercore.bookAPI.fancy.ManualEntry;
import com.pengu.hammercore.bookAPI.fancy.ManualEntry.eEntryShape;
import com.pengu.hammercore.bookAPI.fancy.ManualPage;
import com.pengu.hammercore.bookAPI.fancy.ManualPage.PageType;
import com.pengu.hammercore.common.utils.IOUtils;
import com.pengu.hammercore.json.JSONArray;
import com.pengu.hammercore.json.JSONException;
import com.pengu.hammercore.json.JSONObject;
import com.pengu.hammercore.json.JSONTokener;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;

public class SkyManual
{
	static void init()
	{
		ManualCategories.registerCategory(InfoIC.MOD_ID, new ResourceLocation(InfoIC.MOD_ID, "textures/logo_round.png"), new ResourceLocation("hammercore", "textures/gui/manual_back.png"));
		
		File ics = new File("config", InfoIC.MOD_ID + File.separator + "manual.json");
		
		if(!ics.isFile())
		{
			InputStream in = IslandCraft.class.getResourceAsStream("/assets/" + InfoIC.MOD_ID + "/$1.json");
			
			try(OutputStream out = new FileOutputStream(ics))
			{
				IOUtils.pipeData(in, out);
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			JSONArray arr = (JSONArray) new JSONTokener(new String(Files.readAllBytes(ics.toPath()))).nextValue();
			int len = arr.length();
			
			for(int i = 0; i < len; ++i)
				handle(arr.getJSONObject(i));
		} catch(IOException | JSONException | NBTException e)
		{
			e.printStackTrace();
		}
	}
	
	private static ItemStack[] parse(JSONArray obj) throws NBTException, JSONException
	{
		ItemStack[] stacks = new ItemStack[obj.length()];
		for(int i = 0; i < stacks.length; ++i)
			stacks[i] = parse(obj.getJSONObject(i));
		return stacks;
	}
	
	private static ItemStack parse(JSONObject obj) throws NBTException, JSONException
	{
		if(obj == null)
			return null;
		
		if(!obj.has("count"))
			obj.put("count", 1);
		if(!obj.has("damage"))
			obj.put("damage", 0);
		
		obj.put("Count", obj.getInt("count"));
		obj.put("Damage", obj.getInt("damage"));
		
		return new ItemStack(JsonToNBT.getTagFromJson(obj.toString()));
	}
	
	private static void handle(JSONObject e) throws JSONException, NBTException
	{
		String id = InfoIC.MOD_ID + ":" + e.getString("id");
		String label = e.getString("label");
		String shdesc = e.getString("shdesc");
		int x = e.getInt("x");
		int y = e.getInt("y");
		
		List<String> par = new ArrayList<>();
		{
			JSONArray ar = e.optJSONArray("deps");
			if(ar != null)
				for(int i = 0; i < ar.length(); ++i)
					par.add(InfoIC.MOD_ID + ":" + ar.getString(i));
		}
		
		Object icon_stack = e.opt("icon_stack");
		ItemStack[] iconStack = icon_stack == null ? null : icon_stack instanceof JSONObject ? new ItemStack[] { parse(e.optJSONObject("icon_stack")) } : parse(e.optJSONArray("icon_stack"));
		ResourceLocation iconResource = e.has("icon") ? new ResourceLocation(e.optString("icon")) : null;
		String iconOD = e.optString("icon_od", null);
		
		ManualEntry entry = null;
		
		if(iconStack != null)
			entry = new ManualEntry(id, InfoIC.MOD_ID, x, y, iconStack)
			{
				@Override
				public String getText()
				{
					return I18n.format(shdesc);
				}
				
				@Override
				public String getName()
				{
					return I18n.format(label);
				}
			};
		
		if(iconResource != null)
			entry = new ManualEntry(id, InfoIC.MOD_ID, x, y, iconResource)
			{
				@Override
				public String getText()
				{
					return I18n.format(shdesc);
				}
				
				@Override
				public String getName()
				{
					return I18n.format(label);
				}
			};
		
		if(iconOD != null)
			entry = new ManualEntry(id, InfoIC.MOD_ID, x, y, iconOD)
			{
				@Override
				public String getText()
				{
					return I18n.format(shdesc);
				}
				
				@Override
				public String getName()
				{
					return I18n.format(label);
				}
			};
		
		entry.setParents(par.toArray(new String[par.size()]));
		
		if(e.has("color"))
			entry.setColor(e.getInt("color"));
		
		{
			JSONArray mods = e.optJSONArray("modifiers");
			if(mods != null)
				for(int i = 0; i < mods.length(); ++i)
				{
					String mod = mods.getString(i);
					
					if(mod.equalsIgnoreCase("special"))
						entry.setSpecial();
					else if(mod.equalsIgnoreCase("round"))
						entry.setShape(eEntryShape.ROUND);
					else if(mod.equalsIgnoreCase("square"))
						entry.setShape(eEntryShape.SQUARE);
					else if(mod.equalsIgnoreCase("hex"))
						entry.setShape(eEntryShape.HEX);
				}
		}
		
		List<ManualPage> pages = new ArrayList<>();
		JSONArray ar = e.getJSONArray("pages");
		for(int i = 0; i < ar.length(); ++i)
		{
			ManualPage page = loadPage(ar.getJSONObject(i));
			if(page != null)
				pages.add(page);
		}
		entry.setPages(pages.toArray(new ManualPage[pages.size()]));
		
		entry.registerEntry();
	}
	
	private static ManualPage loadPage(JSONObject e) throws JSONException, NBTException
	{
		String type = e.getString("type");
		
		if(type.equalsIgnoreCase("text"))
			return new ManualPage(e.getString("text"));
		
		if(type.equalsIgnoreCase("crafting"))
			return new ManualPage(parse(e.getJSONObject("out")), PageType.NORMAL_CRAFTING);
		
		if(type.equalsIgnoreCase("smelting"))
			return new ManualPage(parse(e.getJSONObject("in")), PageType.SMELTING);
		
		if(type.equalsIgnoreCase("multiblock"))
		{
			List co = new ArrayList();
			
			co.add(e.getInt("x_size"));
			co.add(e.getInt("y_size"));
			co.add(e.getInt("z_size"));
			
			List<Object> items = new ArrayList<>();
			JSONArray ar = e.getJSONArray("items");
			for(int i = 0; i < ar.length(); ++i)
			{
				Object ob = ar.get(i);
				Object is = null;
				
				if(ob instanceof String)
					is = ob.toString();
				else if(ob instanceof JSONObject)
					is = parse((JSONObject) ob);
				else if(ob instanceof JSONArray)
					is = parse((JSONArray) ob);
				
				items.add(is);
			}
			
			co.add(Collections.unmodifiableList(items));
			co = Collections.unmodifiableList(co);
			return new ManualPage(co, PageType.COMPOUND_CRAFTING);
		}
		
		return null;
	}
}