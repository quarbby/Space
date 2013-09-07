package penguin.turtle.space;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	Resources res;
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		res = this.getResources();
		context = this;
	}

	//Change menu or disable inflation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onPlayButtonClick (View Button){
    	Intent intent = new Intent();
    	intent.setClass(this, Levels.class);
    	startActivity(intent);
    	finish();		
    }
	
	public void onRulesButtonClick (View Button){
		Popups.rulesDialog(context);
    }
	
	public void onAboutButtonClick (View Button){
		Popups.aboutDialog(context);
    }
}
