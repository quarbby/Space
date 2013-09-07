package penguin.turtle.space;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Levels extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_levels);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.levels, menu);
		return true;
	}
	
	public void onfriendsClick (View Button){
    	Intent intent = new Intent();
    	intent.setClass(this, NumPlayers.class);
    	intent.putExtra("opp", "friends");
    	startActivity(intent);
    	finish();		
    }
	
	public void oncomClick (View Button){
    	Intent intent = new Intent();
    	intent.setClass(this, NumPlayers.class);
    	intent.putExtra("opp", "com");
    	startActivity(intent);
    	finish();		
    }

}
