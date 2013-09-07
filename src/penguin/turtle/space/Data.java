package penguin.turtle.space;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

public class Data {
	
	public ArrayList<SpaceLauncher> loadLauncherData(Context context, String nameOfFile){
		ArrayList<SpaceLauncher> spaceLaunchers = new ArrayList<SpaceLauncher>();
		
		Resources res = context.getResources();
		int resourceId =  res.getIdentifier(nameOfFile, "raw", context.getPackageName());
		InputStream inputStream = res.openRawResource(R.raw.data);
		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
				
		String line = "";
		String name;
		float height, diameter, mass, payload;
		String[] rawData;
		//int resourceId;
		Bitmap bitmap;
		
		//Data is stored as a comma delimited file
		try{
			while((line = buffreader.readLine()) != null){
				rawData = line.trim().split(",");
				name = rawData[0];
				name = rawData[0];
				height = Float.parseFloat(rawData[1]);
				diameter = Float.parseFloat(rawData[2]);
				mass = Float.parseFloat(rawData[3]);
				payload = Float.parseFloat(rawData[4]);
				SpaceLauncher sl = new SpaceLauncher(name, height, diameter, mass, payload);
				
				//Set bitmap
				String nameString = name.toLowerCase();
				nameString = nameString.replaceAll(" ", "");
				//resourceId = res.getIdentifier(nameString,"drawable",context.getPackageName());
				//bitmap = BitmapLoader.decodeSampledBitmapFromResource(res,resourceId,300,160);
				sl.setBitmapName(nameString);

				spaceLaunchers.add(sl);
			}
		}catch (Exception e){
			e.printStackTrace();
		} finally{
			if (buffreader != null){
				try { buffreader.close(); }
				catch (IOException e){e.printStackTrace(); }
			}
		}
		
		//Add special cards - 3 each per deck
		SpaceLauncher special;
		for (int i=0; i<4; i++){
			special = new SpaceLauncher("Wild Card", 0, 0, 0, 0);
			bitmap = BitmapLoader.decodeSampledBitmapFromResource(res,R.drawable.wildcard_,300,160);
			special.setBitmapName("wildcard_");
			spaceLaunchers.add(special);
		}
		
		for (int i=0; i<4; i++){
			special = new SpaceLauncher("Reverse", 0, 0, 0, 0);
			bitmap = BitmapLoader.decodeSampledBitmapFromResource(res,R.drawable.reverse_,300,160);
			special.setBitmapName("reverse_");
			spaceLaunchers.add(special);
		}
		
		for (int i=0; i<4; i++){
			special = new SpaceLauncher("Draw Two", 0, 0, 0, 0);
			bitmap = BitmapLoader.decodeSampledBitmapFromResource(res,R.drawable.drawtwo_,300,160);
			special.setBitmapName("drawtwo_");
			spaceLaunchers.add(special);
		}
		
		for (int i=0; i<3; i++){
			special = new SpaceLauncher("Stop", 0, 0, 0, 0);
			bitmap = BitmapLoader.decodeSampledBitmapFromResource(res,R.drawable.stop_,300,160);
			special.setBitmapName("stop_");
			spaceLaunchers.add(special);
		}
		
		return spaceLaunchers;
	}
}
