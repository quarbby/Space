package penguin.turtle.space;

import java.io.BufferedInputStream;
import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

public class BitmapLoader {

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		//Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;	//Means no scaling 
		
		if (height > reqHeight || width > reqWidth){
			//calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float)height/(float)reqHeight);
			final int widthRatio = Math.round((float)width/(float)reqWidth);
			
			//choose smallest ratio as inSampleSize value, this will make final image with both dimensions >= requested
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		
		return inSampleSize;
		
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight){
		//measure bitmap dimensions without allocating memory
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		
		//calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		//decode bitmap
		options.inJustDecodeBounds = false;
		
		Bitmap b = BitmapFactory.decodeResource(res, resId, options);
		
		return b;
	}
	
}
