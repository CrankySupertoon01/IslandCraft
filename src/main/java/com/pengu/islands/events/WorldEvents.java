package com.pengu.islands.events;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.pengu.hammercore.json.JSONArray;
import com.pengu.hammercore.json.JSONException;
import com.pengu.hammercore.json.JSONObject;
import com.pengu.hammercore.json.JSONTokener;
import com.pengu.hammercore.json.io.Jsonable;
import com.pengu.islands.IslandData;

import net.minecraft.util.math.BlockPos;

public class WorldEvents
{
	public static void save(File json)
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[\n\t");
		
		IslandData id = IslandData.getData();
		
		for(String u : id.islands.getKeys())
		{
			BlockPos pos = id.islands.get(u);
			
			str.append("{");
			str.append("\n\t\t\"user\": \"" + Jsonable.formatInsideString(u) + "\",");
			str.append("\n\t\t\"x\": " + pos.getX() + ",");
			str.append("\n\t\t\"z\": " + pos.getZ());
			str.append("\n\t},\n\t");
		}
		
		if(str.indexOf(",\n\t", str.length() - 3) != -1)
			str = str.delete(str.length() - 3, str.length());
		
		str.append("\n]");
		
		byte[] data = str.toString().replaceAll("\n", System.lineSeparator()).getBytes();
		
		try
		{
			FileOutputStream fos = new FileOutputStream(json);
			fos.write(data);
			fos.close();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void load(File json)
	{
		if(!json.isFile())
		{
			System.out.println("Island data file does not exist!");
			return;
		}
		
		try
		{
			JSONArray arr = (JSONArray) new JSONTokener(new String(Files.readAllBytes(json.toPath()))).nextValue();
			
			IslandData id = IslandData.data == null ? new IslandData() : IslandData.data;
			
			for(int i = 0; i < arr.length(); ++i)
			{
				JSONObject obj = arr.getJSONObject(i);
				
				String u = obj.getString("user");
				BlockPos p = new BlockPos(obj.getInt("x"), 0, obj.getInt("z"));
				
				System.out.println("I see " + obj);
				
				id.islands.put(u, p);
			}
			
			IslandData.data = id;
		} catch(JSONException | IOException e)
		{
			e.printStackTrace();
		}
	}
}