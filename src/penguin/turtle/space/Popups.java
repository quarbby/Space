package penguin.turtle.space;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Popups {
	
	public static void rulesDialog(Context context){
		final Dialog rulesDialog = new Dialog(context);
		Resources res = context.getResources();
		rulesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		rulesDialog.setContentView(R.layout.rules_popup);
		
		//Set scaled down images
		ImageView image = (ImageView)rulesDialog.findViewById(R.id.wildCard);
		image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.wildcard_, 82, 100));
		image = (ImageView)rulesDialog.findViewById(R.id.drawTwo);
		image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.drawtwo_, 82, 100));
		image = (ImageView)rulesDialog.findViewById(R.id.reverse);
		image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.reverse_, 82, 100));
		image = (ImageView)rulesDialog.findViewById(R.id.stop);
		image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.stop_, 82, 100));

		
		 Button okButton = (Button) rulesDialog.findViewById(R.id.okButton);
         okButton.setOnClickListener(new View.OnClickListener(){
	            public void onClick(View view){
	            	rulesDialog.dismiss();
	            }
	        });          
		rulesDialog.show();
	}
	
	public static void aboutDialog(Context context){
		final Dialog aboutDialog = new Dialog(context);
		Resources res = context.getResources();
		aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		aboutDialog.setContentView(R.layout.about_popup);
		
		 Button okButton = (Button) aboutDialog.findViewById(R.id.okButton);
         okButton.setOnClickListener(new View.OnClickListener(){
	            public void onClick(View view){
	            	aboutDialog.dismiss();
	            }
	        });          
		aboutDialog.show();
	}
	
	public static void chooseCategory(final Context context){		
		final GameView2 gv = new GameView2(context);
		final Dialog categoryDialog = new Dialog(context);
		Resources res = context.getResources();
		categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		categoryDialog.setContentView(R.layout.choose_category);
		
		//Will be good if can change image colours when clicked 
		
		//Set scaled down images and on click listeners
		ImageView height = (ImageView)categoryDialog.findViewById(R.id.height);
		height.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.heighted, 100, 50));
		height.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(context, "Height", Toast.LENGTH_SHORT).show();
				categoryDialog.cancel();
			}
		});
		
		ImageView mass = (ImageView)categoryDialog.findViewById(R.id.mass);
		mass.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.massed, 130, 52));
		mass.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(context, "Mass", Toast.LENGTH_SHORT).show();
				categoryDialog.cancel();
			}
		});
		
		ImageView diameter = (ImageView)categoryDialog.findViewById(R.id.diameter);
		diameter.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.diametered, 100, 50));
		diameter.setOnClickListener(new View.OnClickListener(){
		public void onClick(View v){
				Toast.makeText(context, "Diameter", Toast.LENGTH_SHORT).show();
				categoryDialog.cancel();
			}
		});
		
		ImageView payload = (ImageView)categoryDialog.findViewById(R.id.payload);
		payload.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.payloaded, 130, 52));
		payload.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(context, "Payload", Toast.LENGTH_SHORT).show();
				categoryDialog.cancel();
			}
		});
		categoryDialog.show();
	}
	
	public static void winDialog(String winner, Context context){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(winner + " wins!");
		
		builder.setMessage("Play again?")
			.setCancelable(false);
			
		builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				GameView2.playAgain = true;
				dialog.cancel();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				//Add like BYEBYE, thanks for playing!
				GameView2.playAgain = false;
				dialog.cancel();
			}
		});
						
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
