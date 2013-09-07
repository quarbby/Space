package penguin.turtle.space;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class NumPlayers extends Activity {

	String opp;
	int numPlayers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_num_players);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			opp = extras.getString("opp");
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.num_players, menu);
		return true;
	}
	
	public void twoPlayer (View Button){
    	numPlayers = 2;
    	startIntent();
    }
	
	public void threePlayer (View Button){
		numPlayers = 3;
    	startIntent();		
    }
	
	public void fourPlayer (View Button){
		numPlayers = 4;
    	startIntent();	
    }
	
	public void startIntent(){
		Intent intent = new Intent();
		if (opp.equals("com")){
			if (numPlayers == 2){
		    	intent.setClass(this, Game.class);
			}
			else {
		    	intent.setClass(this, Game4P.class);
		    	intent.putExtra("numPlayers", numPlayers);
			}
		}
		else {
			if (numPlayers == 2){
				intent.setClass(this, GameP1P2.class);
			}
			else {
				intent.setClass(this, GameMultP.class);
				intent.putExtra("numPlayers", numPlayers);
			}
		}
		
    	startActivity(intent);
    	finish();	
	}

}
