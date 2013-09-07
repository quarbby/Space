package penguin.turtle.space;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameView2 extends SurfaceView implements SurfaceHolder.Callback{

	private GameThread thread;
	private static Context myContext;
	private float scale;
	private int screenW;
	private int screenH;
	private int scaledCardW;
	private int scaledCardH;
	private Paint whitePaint;
	
	private Bitmap rulesIcon;
	private Bitmap background;
	private Bitmap cardBack;
	private Bitmap rotatedCard;		//For computer's hands 
	private Bitmap nextarrow;
	
	private static Resources res;
	
	public static ArrayList<SpaceLauncher> spaceLaunchers;	//The entire array list
	private ArrayList<SpaceLauncher> discardPile;		//For cards already played out
	private ArrayList<SpaceLauncher> playerHand;		//Players' hand
	private ArrayList<SpaceLauncher> comHand;			//Computer deck
	
	private boolean playerTurn = true;
	private boolean firstTime = true;
	private boolean played = false;		//Whether the player has played that round
	public static boolean playAgain = false;
	private int numPlayers;		//Be able to get from previous screen
	private Bitmap rotatedBitmap;
	
	//To keep track of category and value
	private static String category;
	private static float currentValue = -1;
	
	public GameView2(Context context){
		super(context);
		getHolder().addCallback(this);
		
		this.myContext = context;
		scale = myContext.getResources().getDisplayMetrics().density;
		res = myContext.getResources();
		
		spaceLaunchers = new ArrayList<SpaceLauncher>();
		playerHand = new ArrayList<SpaceLauncher>();
		comHand = new ArrayList<SpaceLauncher>();
		discardPile = new ArrayList<SpaceLauncher>();
		numPlayers = 2;		//To be received from previous screen
		
		//White paint is for debug drawing boundaries
		whitePaint = new Paint();
		whitePaint.setAntiAlias(true);
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Paint.Style.STROKE);
		whitePaint.setTextSize(scale*15);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		canvas.drawBitmap(background, this.getLeft(), this.getTop(), null);
		canvas.drawBitmap(rulesIcon, this.getRight()-rulesIcon.getWidth()-(20*scale), 
				this.getTop()+(20*scale), null);
		
		//Draw draw piles in the center
		canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-(10*scale), (screenH/2)-cardBack.getHeight()/2, null);
		//discard pile temporarily use the card back
		Rect discardPileRect = new Rect((int)((screenW/2)+(10*scale)), (screenH/2)-(cardBack.getHeight()/2), (int)((screenW/2)+(10*scale)+cardBack.getWidth()), (screenH/2)-(cardBack.getHeight()/2)+cardBack.getHeight());
		//canvas.drawRect(discardPileRect, whitePaint);
		canvas.drawBitmap(cardBack, new Rect(0,0,cardBack.getWidth(),cardBack.getHeight()), discardPileRect, null);
		//canvas.drawBitmap(cardBack, (screenW/2)+(10*scale), (screenH/2)-(cardBack.getHeight()/2), null);
		
		//Draw computer's cards 
		for (int i=0; i<comHand.size(); i++){
			canvas.drawBitmap(rotatedCard,(screenH/2-5*comHand.size())+i*(scale*25), (20*scale) ,null);
		}
		
		//Draw player's hands - 7 cards only, then rotate
		if (playerHand.size() > 7){
			canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale),
					screenH-nextarrow.getHeight()-scaledCardH-(20*scale) ,null);
		}
		SpaceLauncher sl;
		for (int i=0; i<7; i++){
			sl = playerHand.get(i);
			//temporary use cardback for other cards 
			canvas.drawBitmap(cardBack,(screenH/2-20*comHand.size())+i*(scaledCardW+5)*scale, screenH-(100*scale),null);
		}
		
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
		
		scaledCardW = (int) (screenH/20);
        scaledCardH = (int) (scaledCardW*1.28);
		
        rulesIcon = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.rulesicon, scaledCardW/2, scaledCardH/2);
		background = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.background2, screenW, screenH);
		cardBack = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.card_2background, scaledCardW, scaledCardH);
		nextarrow = BitmapFactory.decodeResource(res, R.drawable.nextarrow);
		
		Matrix matrix = new Matrix();
		matrix.postRotate(180);
		rotatedCard = Bitmap.createBitmap(cardBack, 0, 0, cardBack.getWidth(), cardBack.getHeight(), matrix, true);
		
		new Data().loadLauncherData(myContext, "space1.txt");
		dealCards();

		/*
		if (!playerTurn){
			computerPlay();
		}
		*/
	}
	
	@Override 
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startThread();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try{
			this.thread.setRunning(false);
			this.thread.paused = true;
			this.thread.join();
		} catch (InterruptedException e){
		}
	}
	
	public void startThread(){
		thread = new GameThread(getHolder(), this);
		this.thread.paused = false;
		thread.setRunning(true);
		thread.start();
	}
	
	public void pause(){
		this.thread.paused = true;
	}
	
	public void resume(){
		startThread();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();   
        int X = (int)event.getX();
        int Y = (int)event.getY();
        
        switch(eventaction){
        case MotionEvent.ACTION_UP:
        	//Touch rules button
        	if (X > this.getRight()-rulesIcon.getWidth()- (20*scale) &&
        		X < this.getRight()-(20*scale) && 
        		Y > this.getTop()+(20*scale)){
        		Popups.rulesDialog(myContext);
        	}
        	
        	//Player touches cards
        	if (playerTurn){
        		played = false;
        		for (int i=0; i<playerHand.size(); i++){
            		if (X > (screenH/2-20*comHand.size())+i*(scaledCardW+5)*scale && 
            		X < (screenH/2-20*comHand.size())+i*(scaledCardW+5)*scale+cardBack.getWidth() && 
            		Y < screenH-(100*scale)+ cardBack.getHeight() && 
            		Y > screenH-(100*scale)){
            			SpaceLauncher sl = playerHand.get(i);
            			cardDialog(sl);
            			if (played){
            				discardPile.add(playerHand.remove(i));
            			}
            		}
        		}
        	}
        	
        	//Player touches next arrow
        	if (playerTurn){
        		if (X > screenW-nextarrow.getWidth()-(10*scale) && 
        			X < screenW-(10*scale) && 
        			Y > screenH-nextarrow.getHeight()-scaledCardH-(20*scale)){
        			Collections.rotate(playerHand, 1);
        		}
        	}
        	
        	//Player touches draw pile
        	if (playerTurn){
        		if (X > (screenW/2)-cardBack.getWidth()-(10*scale) && 
        		    X < (screenW/2)-(10*scale) && 
        		    Y > (screenH/2)-cardBack.getHeight()/2){
        			//check if can draw or there's still cards that can be played
        			drawCard(playerHand);
        			drawCard(playerHand);
        		}
        	}
        	invalidate();
        	break;
        	
        case MotionEvent.ACTION_MOVE:
        	break;
        	
        case MotionEvent.ACTION_DOWN:
        	break;
        }
		
		return true;
	}
	
	private void dealCards(){
		Collections.shuffle(spaceLaunchers);
		for (int i=0; i<8; i++){
			drawCard(playerHand);
			drawCard(comHand);
		}
	}
	
	private void drawCard(ArrayList<SpaceLauncher> hand){
		//Add the top launcher card in
		hand.add(0, spaceLaunchers.get(0));
		spaceLaunchers.remove(0);
		
		//If deck is empty, then reshuffle the discard pile
		if (spaceLaunchers.isEmpty()){
			for (int i=discardPile.size()-1; i>0; i--){
				spaceLaunchers.add(discardPile.get(i));
				discardPile.remove(i);
				Collections.shuffle(spaceLaunchers);
			}
		}
	}
	
	private void initNewGame(){
		//Winner in the previous game goes first
		if (playerHand.isEmpty()){
			playerTurn = true;
		}
		else if (comHand.isEmpty()){
			playerTurn = false;
		}
		
		//Put all the cards back into spaceLaunchers arraylist
		spaceLaunchers.addAll(discardPile);
		spaceLaunchers.addAll(playerHand);
		spaceLaunchers.addAll(comHand);
		
		playerHand.clear();
		comHand.clear();
		
		dealCards();
		/*
		 if (!playerTurn){
		 	computerPlay();
		 }
		 */
		this.firstTime=true;
	}
	
	private void cardDialog(final SpaceLauncher sl){
		final Dialog cardDialog = new Dialog(myContext);
		cardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bitmap bitmap;
		ImageView image;
		String name = sl.getName();
		float mass = sl.getMass();
		float height = sl.getHeight(); 
		float diameter = sl.getDiameter();
		float payload = sl.getPayload();
		
		if (name.equals("Wild Card")){
			cardDialog.setContentView(R.layout.specialcard);
			image = (ImageView)cardDialog.findViewById(R.id.cardView);
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.wildcard_, 400, 300));
			
			Button playBtn = (Button)cardDialog.findViewById(R.id.Play);
			playBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					if (firstTime) { 
						firstTime = false;
					}
					cardDialog.dismiss();
					chooseCategory();
					currentValue = -1; 	//Reset the value down
					playerTurn = false;
					played = true;
				}
			});
		}
		else if (name.equals("Reverse")){
			cardDialog.setContentView(R.layout.specialcard);
			image = (ImageView)cardDialog.findViewById(R.id.cardView);
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.reverse_, 400, 300));
		
			Button playBtn = (Button)cardDialog.findViewById(R.id.Play);
			playBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					if (firstTime) { 
						firstTime = false;
					}
					//Action code for reverse
					else{
						playerTurn = true;
						cardDialog.dismiss();
					}
					played = true;
				}
			});
		}
		else if (name.equals("Stop")){
			cardDialog.setContentView(R.layout.specialcard);
			image = (ImageView)cardDialog.findViewById(R.id.cardView);
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.stop_, 400, 300));
		
			Button playBtn = (Button)cardDialog.findViewById(R.id.Play);
			playBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					if (firstTime) { 
						firstTime = false;
					}
					else {
					//Action code for stop
					playerTurn = true;
					cardDialog.dismiss();
					}
					played = true;
				}
			});
		}
		else if (name.equals("Draw Two")){
			cardDialog.setContentView(R.layout.specialcard);
			image = (ImageView)cardDialog.findViewById(R.id.cardView);
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.drawtwo_, 400, 300));
		
			Button playBtn = (Button)cardDialog.findViewById(R.id.Play);
			playBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					if (firstTime) { 
						firstTime = false;
					}
					drawCard(comHand);
					drawCard(comHand);
					playerTurn = true;
					played = true;
					cardDialog.dismiss();
				}
			});
		}
		else {
			cardDialog.setContentView(R.layout.card);
			//Set launcher image - Eventually change to the bitmap of the space launcher
			image = (ImageView)cardDialog.findViewById(R.id.launcherImage);
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.nasa, 300, 160));
			
			//Set data 
			TextView launchername = (TextView)cardDialog.findViewById(R.id.name);
			launchername.setText(name);
			TextView massValue = (TextView)cardDialog.findViewById(R.id.MassValue);
			massValue.setText("" + mass);
			TextView heightValue = (TextView)cardDialog.findViewById(R.id.HeightValue);
			heightValue.setText("" + height);
			TextView payloadValue = (TextView)cardDialog.findViewById(R.id.PayloadValue);
			payloadValue.setText("" + payload);
			TextView diameterValue = (TextView)cardDialog.findViewById(R.id.DiameterValue);
			diameterValue.setText("" + diameter);
			
			Button playBtn = (Button)cardDialog.findViewById(R.id.Play);
			playBtn.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					if (firstTime){
						cardDialog.dismiss();
						chooseCategory();
						checkIfValidPlay(sl);
						firstTime = false;
					}
					else{
					//to log card details and pass turn over
					if (checkIfValidPlay(sl)){
						checkWin();
						//playerTurn = false;
						played = true;
					}
					else {
						Toast.makeText(myContext, "Not a valid play. You must play a card with a " + category 
								+" value higher than " + currentValue + ".", Toast.LENGTH_SHORT).show();
					}
					}
					cardDialog.dismiss();
				}
			});
		}
		
		 Button cancelButton = (Button) cardDialog.findViewById(R.id.Cancel);
	        cancelButton.setOnClickListener(new View.OnClickListener(){
	            public void onClick(View view){
	            	cardDialog.dismiss();
	            }
	        });          
		cardDialog.show();
	}
	
	private static void chooseCategory(){		
		final Dialog categoryDialog = new Dialog(myContext);
		categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		categoryDialog.setContentView(R.layout.choose_category);
		
		//Will be good if can change image colours when clicked 
		
		//Set scaled down images and on click listeners
		ImageView height = (ImageView)categoryDialog.findViewById(R.id.height);
		height.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.heighted, 100, 50));
		height.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Height", Toast.LENGTH_SHORT).show();
				category = "Height";
				categoryDialog.cancel();
			}
		});
		
		ImageView mass = (ImageView)categoryDialog.findViewById(R.id.mass);
		mass.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.massed, 130, 52));
		mass.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Mass", Toast.LENGTH_SHORT).show();
				category = "Mass";
				categoryDialog.cancel();
			}
		});
		
		ImageView diameter = (ImageView)categoryDialog.findViewById(R.id.diameter);
		diameter.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.diametered, 100, 50));
		diameter.setOnClickListener(new View.OnClickListener(){
		public void onClick(View v){
				Toast.makeText(myContext, "Diameter", Toast.LENGTH_SHORT).show();
				category = "Diameter";
				categoryDialog.cancel();
			}
		});
		
		ImageView payload = (ImageView)categoryDialog.findViewById(R.id.payload);
		payload.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.payloaded, 130, 52));
		payload.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Payload", Toast.LENGTH_SHORT).show();
				category = "Payload";
				categoryDialog.cancel();
			}
		});
		categoryDialog.show();
	}
	
	private void checkWin(){
		if (playerHand.isEmpty()){
			winDialog("Player");
			if (playAgain){
				initNewGame();
			}
		}
		else if (comHand.isEmpty()){
			winDialog("Computer");
			if (playAgain){
				initNewGame();
			}
		}
	}
	
	private boolean checkIfValidPlay(SpaceLauncher sl){
		Toast.makeText(myContext, category, Toast.LENGTH_SHORT).show();
		if (category.equals("Height")){
			if (sl.getHeight() >= currentValue){ 
				currentValue = sl.getHeight();
				return true; }
			else	{ return false; }
		}
		if (category.equals("Mass")){
			if (sl.getMass() >= currentValue){ 
				currentValue = sl.getMass();
				return true; }
			else	{ return false; }
		}
		if (category.equals("Diameter")){
			if (sl.getDiameter() >= currentValue){ 
				currentValue = sl.getDiameter();
				return true; }
			else	{ return false; }
		}
		if (category.equals("Payload")){
			if (sl.getPayload() >= currentValue){ 
				currentValue = sl.getPayload();
				return true; }
			else	{ return false; }
		}
		return false;
	}
	
	private static void winDialog(String winner){
		AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
		builder.setTitle(winner + " wins!");
		
		builder.setMessage("Play again?")
			.setCancelable(false);
			
		builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				playAgain = true;
				dialog.cancel();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				//Add like BYEBYE, thanks for playing!
				playAgain = false;
				dialog.cancel();
			}
		});
						
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public class GameThread extends Thread{
		private SurfaceHolder surfaceHolder;
		private GameView2 gameView;
		private boolean run = false;
		private boolean paused = true;
		
		public GameThread(SurfaceHolder surfaceHolder, GameView2 gameView){
			this.surfaceHolder = surfaceHolder;
			this.gameView = gameView;
		}
		
		public void setRunning(boolean run){
			this.run = run;
		}
		
		@Override
		public void run(){
			Canvas c;
			while (run){
				while (paused) {
					try {
						Thread.sleep(50L);
					} catch (InterruptedException ignore) {
					}
				}
				c = null;
				try{
					c = this.surfaceHolder.lockCanvas(null);
					synchronized(this.surfaceHolder){
						//insert methods to modify position of items in onDraw
						if (c!=null){
							onDraw(c);
							postInvalidate();
						}
					}
				} finally{
					if (c!=null){
						this.surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}
	}
}
