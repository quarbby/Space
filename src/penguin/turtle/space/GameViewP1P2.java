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
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameViewP1P2 extends View{

	private static Context myContext;
	private float scale;
	private int screenW;
	private int screenH;
	private int scaledCardW;
	private int scaledCardH;
	private Paint whitePaint;
	private Dialog cardDialog;
	private Boolean onclick; 
	
	private Bitmap rulesIcon;
	private Bitmap background;
	private Bitmap cardBack;
	private Bitmap rotatedCard;		//For computer's hands 
	private Bitmap nextarrow;
	private Bitmap backarrow;
	private Bitmap discardRect;
	private Bitmap cardoverlay;
	private Bitmap decks;
	private static Sound sound = new Sound();
	
	private static Resources res;
	private Toast toast;
	private LayoutInflater mInflater;
	
	public static ArrayList<SpaceLauncher> spaceLaunchers;	//The entire array list
	private ArrayList<SpaceLauncher> discardPile;		//For cards already played out
	private ArrayList<SpaceLauncher> playerHand;		//Players' hand
	private ArrayList<SpaceLauncher> player2Hand;			//Computer deck
	private ArrayList<Deck> deckList; 	//List of decks
	
	private boolean player1Turn = true;
	private boolean firstTime = true;
	private boolean played = false;		//Whether the player has played that round
	public boolean playAgain = false;
	private Bitmap rotatedBitmap;
	private boolean win = false;
	private ChooseDeckAdapter adapter;
	
	//To keep track of category and value
	private String category;
	private float currentValue = -1;
	
	public GameViewP1P2(Context context){
		super(context);
		
		this.myContext = context;
		scale = myContext.getResources().getDisplayMetrics().density;
		res = myContext.getResources();
		mInflater = LayoutInflater.from(myContext);
		toast = new Toast(myContext);
		
		spaceLaunchers = new ArrayList<SpaceLauncher>();
		playerHand = new ArrayList<SpaceLauncher>();
		player2Hand = new ArrayList<SpaceLauncher>();
		discardPile = new ArrayList<SpaceLauncher>();
		deckList = new ArrayList<Deck>();
		
		//White paint is for debug drawing boundaries
		whitePaint = new Paint();
		whitePaint.setAntiAlias(true);
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Paint.Style.STROKE);
		whitePaint.setTextSize(scale*15);
		
		onclick = true;
		
		loadStuff();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		screenW = this.getWidth();
		screenH = this.getHeight();
		scaledCardW = (int) (screenH/20);
        scaledCardH = (int) (scaledCardW*1.28);
		
        rulesIcon = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.book, (int)(scaledCardW*0.8), (int)(scaledCardH*0.8));
		background = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.background2, screenW, screenH);
		cardBack = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.card_background, scaledCardW, scaledCardH);
		cardoverlay = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.cardoverlay, (int)(scaledCardW+(7*scale)), (int)(scaledCardH+(7*scale)));
		nextarrow = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.nextarrow, scaledCardW/2, scaledCardH/2);
		backarrow = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.backarrow, scaledCardW/2, scaledCardH/2);
		decks = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.list, (int)(scaledCardW*0.8), (int)(scaledCardH*0.8));
		
		Matrix matrix = new Matrix();
		matrix.postRotate(180);
		rotatedCard = Bitmap.createBitmap(cardBack, 0, 0, cardBack.getWidth(), cardBack.getHeight(), matrix, true);

		canvas.drawBitmap(background, this.getLeft(), this.getTop(), null);
		canvas.drawBitmap(rulesIcon, this.getRight()-rulesIcon.getWidth()-(130*scale), 
				this.getTop()+(130*scale), null);
		canvas.drawBitmap(decks, (screenW/2)-cardBack.getWidth()-(120*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), null);
		
		//Draw draw piles in the center
		canvas.drawBitmap(cardoverlay, (screenW/2)-cardBack.getWidth()-(50*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), null);
		canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-(40*scale), (screenH/2)-cardBack.getHeight()/2-(5*scale), null);
		//discard pile temporarily use the card back
		Rect discardPileRect = new Rect((int)((screenW/2)+(25*scale)), (int)((screenH/2)-(cardBack.getHeight()/2)-5*scale), (int)((screenW/2)+(23*scale)+cardBack.getWidth()), (int)((screenH/2)-(cardBack.getHeight()/2)+cardBack.getHeight()-(5*scale)));

		int resourceId;
		canvas.drawBitmap(cardoverlay, (screenW/2)+(15*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), null);
		if (!discardPile.isEmpty()) {
			resourceId = res.getIdentifier(discardPile.get(0).getBitmap(),"drawable",myContext.getPackageName());
			discardRect = BitmapLoader.decodeSampledBitmapFromResource(res, resourceId, scaledCardW, scaledCardH);
			canvas.drawBitmap(discardRect, new Rect(0,0,discardRect.getWidth(), discardRect.getHeight()), discardPileRect, null);			
		}
		
		//Draw the category and current value
		String categoryString, valueString;
		if (category == null){
			categoryString = "Category: ";
		}
		else {
			categoryString = "Category: " + category;
		}
		
		if (currentValue == -1){
			valueString = "Current Value: ";
		}
		else {
			valueString = "Current Value: " + Float.toString(currentValue);
		}
		canvas.drawText(categoryString, (screenW/2)-cardBack.getWidth()-(50*scale), (screenH/2)-cardBack.getHeight()/2-(25*scale), whitePaint);
		canvas.drawText(valueString, (screenW/2)-cardBack.getWidth()-(50*scale), (screenH/2)-cardBack.getHeight()/2+cardBack.getHeight()+(35*scale), whitePaint);
		
		//Draw player hands 
		Bitmap tempBitmap, cardBitmap;
		SpaceLauncher sl;
		if (player1Turn&& onclick == true){
			//Draw player 2 hand - closed
			for (int i=0; i<player2Hand.size(); i++){
				canvas.drawBitmap(rotatedCard,(screenH/2-10*player2Hand.size())+i*(scale*25), (20*scale) ,null);
			}
			
			//Draw player 1 hands - 7 cards only, then draw next arrow
			if (playerHand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-nextarrow.getHeight()-scaledCardH-(20*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), screenH-backarrow.getHeight()-scaledCardH-(20*scale) ,null);	
			}
			int scaledCardWidth = (int)(screenW/10);
			int scaledCardHeight = (int)(scaledCardWidth*1.28);
			for (int i=0; i<playerHand.size(); i++){
				sl = playerHand.get(i);
				//temporary use cardback for other cards 
				if (i<7){
					resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
					tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
					cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-(100*scale),null);
				}
		}
		}
		
		else if (!player1Turn && onclick == true){
			//draw player 1 hand closed
			for (int i=0; i<playerHand.size(); i++){
				canvas.drawBitmap(cardBack,(screenH/2-10*player2Hand.size())+i*(scale*25), screenH-(100*scale),null);
			}
			
			//draw player 2 hand
			//Draw player 1 hands - 7 cards only, then draw next arrow
			if (player2Hand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), (20*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), (20*scale) ,null);	
			}
			int scaledCardWidth = (int)(screenW/10);
			int scaledCardHeight = (int)(scaledCardWidth*1.28);
			for (int i=0; i<player2Hand.size(); i++){
				sl = player2Hand.get(i);
				//temporary use cardback for other cards 
				if (i<7){
					resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
					tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
					cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, 30*scale,null);
				}
		}
		}
		invalidate();
	}
	
	public void loadStuff() {
		//spaceLaunchers = new Data().loadLauncherData(myContext, "space1.txt");
		deckList.add(new Deck("space", true));
		deckList.add(new Deck("infinity", false));
		
		for (Deck d : deckList){
			if (d.isChecked() == true){
				spaceLaunchers = new Data().loadLauncherData(myContext, d.getName()+".txt");
			}
		}
		
		dealCards();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();   
        int X = (int)event.getX();
        int Y = (int)event.getY();
        
        switch(eventaction){
        case MotionEvent.ACTION_UP:
        	//Touch rules button
        	if (X > this.getRight()-rulesIcon.getWidth()-(130*scale) &&
        		X < this.getRight()-rulesIcon.getWidth()-(130*scale)+rulesIcon.getWidth() && 
        		Y > this.getTop()+(130*scale) && 
        		Y < this.getTop()+(130*scale)+rulesIcon.getHeight()){
        		Popups.rulesDialog(myContext);
        	}

        	//Player chooses deck
        	if ( X > (screenW/2)-cardBack.getWidth()-(110*scale) && 
        		 X < (screenW/2)-cardBack.getWidth()-(110*scale)+decks.getWidth() && 
        		 Y > (screenH/2)-cardBack.getHeight()/2-(15*scale) && 
        		 Y < (screenH/2)-cardBack.getHeight()/2-(15*scale)+decks.getHeight()){
        		chooseDeckPopup();
        	}
        	
        	//Player touches cards
        	if (player1Turn){
        		SpaceLauncher sl;
        		played = false;
        		for (int i=0; i<7; i++){
            		if (X > (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale && 
            		X < (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale+cardBack.getWidth() && 
            		Y < screenH-(110*scale)+ cardBack.getHeight() && 
            		Y > screenH-(110*scale)){
            			sl = playerHand.get(i);
            			cardDialogPopup(sl);
            		}
        		}
        	}
    
        	//Player touches next arrow
        	if (player1Turn){
        		if (X > screenW-nextarrow.getWidth()-(15*scale) && 
        			X < screenW-(15*scale) && 
        			Y > screenH-nextarrow.getHeight()-scaledCardH-(25*scale) && 
        			Y < screenH-scaledCardH-(25*scale)){
        			Toast.makeText(myContext, "Next arrow!", Toast.LENGTH_SHORT).show();
        			Collections.rotate(playerHand, 1);
        		}
        	}
        	
        	//player touches back arrow
        	if (player1Turn){
        		if (X > 5*scale &&
        			X < 10*scale+backarrow.getWidth() && 
        			Y > screenH-backarrow.getHeight()-scaledCardH-(25*scale) &&
        			Y < screenH-scaledCardH-(25*scale)){
        			Toast.makeText(myContext, "Back arrow!", Toast.LENGTH_SHORT).show();
            		Collections.rotate(playerHand, -1);
        		}
        	}
        	//Player touches draw pile
        	if (player1Turn){
        		if (X > (screenW/2)-cardBack.getWidth()-(50*scale) && 
        		    X < (screenW/2)-cardBack.getWidth()-(50*scale)+cardoverlay.getWidth() && 
        		    Y > (screenH/2)-cardBack.getHeight()/2-(10*scale) && 
        		    Y < (screenH/2)-cardBack.getHeight()/2-(10*scale)+cardoverlay.getHeight()){
        			if(checkForValidDraw(1)){
        				//onclick = false;
        				Toast.makeText(myContext, "Draw cards", Toast.LENGTH_SHORT).show();
            			drawCard(playerHand);
            			drawCard(playerHand);
            			//passTurn();
            			player1Turn = false;
        			}
        			else{
        				Toast.makeText(myContext, "You have a valid card", Toast.LENGTH_SHORT).show();
        			}
        		}
        	}
        	
        	//Touch Actions for player 2 ----------------
        	
        	//Player 2 touches draw pile
        	if (!player1Turn){
        		if (X > (screenW/2)-cardBack.getWidth()-(50*scale) && 
        		    X < (screenW/2)-cardBack.getWidth()-(50*scale)+cardoverlay.getWidth() && 
        		    Y > (screenH/2)-cardBack.getHeight()/2-(10*scale) && 
        		    Y < (screenH/2)-cardBack.getHeight()/2-(10*scale)+cardoverlay.getHeight()){
        			if(checkForValidDraw(0)){
        				Toast.makeText(myContext, "Draw cards", Toast.LENGTH_SHORT).show();
        				//onclick = false;
            			drawCard(player2Hand);
            			drawCard(player2Hand);
            			//player1Turn = true;
            			passTurn();
        			}
        			else{
        				Toast.makeText(myContext, "You have a valid card", Toast.LENGTH_SHORT).show();
        			}
        		}
        	}
        	
        	//Player 2 touches next arrow
        	if (!player1Turn){
        		if (X > screenW-nextarrow.getWidth()-(15*scale) && 
        			X < screenW-(15*scale) && 
        			Y > (15*scale) && 
        			Y < nextarrow.getWidth()+(25*scale)){
        			Toast.makeText(myContext, "Next arrow!", Toast.LENGTH_SHORT).show();
        			Collections.rotate(player2Hand, 1);
        		}
        	}
        	
        	//player 2 touches back arrow
        	if (!player1Turn){
        		if (X > 5*scale &&
        			X < 10*scale+backarrow.getWidth() && 
        			Y > (15*scale) &&
        			Y < backarrow.getWidth()+(25*scale)){
        			Toast.makeText(myContext, "Back arrow!", Toast.LENGTH_SHORT).show();
            		Collections.rotate(player2Hand, -1);
        		}
        	}
        	
        	//Player touches cards
        	if (!player1Turn){
        		SpaceLauncher sl;
        		for (int i=0; i<7; i++){
            		if (X > (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale && 
            		X < (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale+cardBack.getWidth() && 
            		Y > (20*scale) && 
            		Y < (20*scale)+cardBack.getHeight()){
            			//Toast.makeText(myContext, "Player 2!", Toast.LENGTH_SHORT).show();
            			sl = player2Hand.get(i);
            			cardDialogPopup(sl);
            		}
        		}
        	}
        	
        	invalidate();
        	break;
        	
        }
		invalidate();
		return true;
	}
	
	private void dealCards(){
		Collections.shuffle(spaceLaunchers);
		for (int i=0; i<7; i++){
			drawCard(playerHand);
			drawCard(player2Hand);
		}
	}
	
	private void drawCard(ArrayList<SpaceLauncher> hand){
		//Add the top launcher card in
		hand.add(spaceLaunchers.get(0));
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
			player1Turn = true;
		}
		else if (player2Hand.isEmpty()){
			player1Turn = false;
		}
		
		//Put all the cards back into spaceLaunchers arraylist
		spaceLaunchers.addAll(discardPile);
		spaceLaunchers.addAll(playerHand);
		spaceLaunchers.addAll(player2Hand);
		
		playerHand.clear();
		player2Hand.clear();
		
		deckList.clear();
		loadStuff();
		
		//dealCards();
		
		this.category = null;
		this.currentValue = -1;
		this.firstTime=true;
		this.win=false;
		this.firstTime=true;
	}
	
	private void cardDialogPopup(final SpaceLauncher sl){
		cardDialog = new Dialog(myContext);
		cardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		cardDialog.setCanceledOnTouchOutside(false);

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
					cardDialog.dismiss();
					wildCard(sl);
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
					cardDialog.dismiss();
					reverse(sl);
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
					cardDialog.dismiss();
					stop(sl);
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
					drawTwo(sl);
					cardDialog.dismiss();
				}
			});
		}
		else {
			cardDialog.setContentView(R.layout.card);
			image = (ImageView)cardDialog.findViewById(R.id.launcherImage);
			int resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
			image.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res,resourceId,250,120));
			
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
						chooseCategory(sl);
					}
					if (!firstTime){
						//to log card details and pass turn over
						if (checkIfValidPlay(sl)){
							if (player1Turn){
								player1Turn = false;
								removeCard(sl, playerHand);
								checkWin();
							}
							else {
								player1Turn = true;
								removeCard(sl, player2Hand);
								checkWin();
							}
						}
						else {
							Toast.makeText(myContext, "Not a valid play. You must play a card with a " + category 
									+" value higher than " + currentValue + ".", Toast.LENGTH_SHORT).show();
						}
					}
					cardDialog.dismiss();
					firstTime = false;
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
	
	private void setCategory(String category){
		this.category = category;
	}
	
	private void chooseCategory(final SpaceLauncher sl){		
		final Dialog categoryDialog = new Dialog(myContext);
		categoryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		categoryDialog.setContentView(R.layout.choose_category);
		categoryDialog.setCanceledOnTouchOutside(false);
		final View layout = mInflater.inflate(R.layout.custom_toast, null);

		//Will be good if can change image colours when clicked 
		
		//Set scaled down images and on click listeners
		ImageView height = (ImageView)categoryDialog.findViewById(R.id.height);
		height.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.heighted, 100, 80));
		height.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Sound.playSound(R.raw.lock, myContext);
				//Toast.makeText(myContext, "Payload", Toast.LENGTH_SHORT).show();
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Height");
				showToast(layout);
				setCategory("Height");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					if (player1Turn) { removeCard(sl, playerHand); 
						player1Turn = false;}
					else 	{ removeCard(sl, player2Hand); 
						player1Turn = true;}
					passTurn();
					checkWin();
				}
			}
		});
		
		ImageView mass = (ImageView)categoryDialog.findViewById(R.id.mass);
		mass.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.massed, 100, 80));
		mass.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Mass", Toast.LENGTH_SHORT).show();
				setCategory("Mass");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					if (player1Turn) { removeCard(sl, playerHand); 
					player1Turn = false;}
				else 	{ removeCard(sl, player2Hand); 
					player1Turn = true;}
				passTurn();
					checkWin();
				}
			}
		});
		
		ImageView diameter = (ImageView)categoryDialog.findViewById(R.id.diameter);
		diameter.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.diametered, 100, 80));
		diameter.setOnClickListener(new View.OnClickListener(){
		public void onClick(View v){
				Toast.makeText(myContext, "Diameter", Toast.LENGTH_SHORT).show();
				setCategory("Diameter");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					if (player1Turn) { removeCard(sl, playerHand); 
					player1Turn = false;}
				else 	{ removeCard(sl, player2Hand); 
					player1Turn = true;}
				passTurn();
					checkWin();
				}
			}
		});
		
		ImageView payload = (ImageView)categoryDialog.findViewById(R.id.payload);
		payload.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.payloaded, 100, 80));
		payload.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Payload", Toast.LENGTH_SHORT).show();
				setCategory("Payload");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					if (player1Turn) { removeCard(sl, playerHand); 
					player1Turn = false;}
				else 	{ removeCard(sl, player2Hand); 
					player1Turn = true;}
				passTurn();
					checkWin();
				}
			}
		});
		
		categoryDialog.show();
	}
	
	private void logDetails(SpaceLauncher sl){
		onclick = false;
		if (category.equals("Height")){
				currentValue = sl.getHeight();
		}
		if (category.equals("Mass")){
				currentValue = sl.getMass();
		}
		if (category.equals("Diameter")){
				currentValue = sl.getDiameter();
		}
		if (category.equals("Payload")){
				currentValue = sl.getPayload();
		}
		if (player1Turn){
			passTurn();
			removeCard(sl, playerHand);
			player1Turn = false;
		}
		else{
			passTurn();
			removeCard(sl, player2Hand);
			player1Turn = true;
		}
		checkWin();
	}
	
	private void checkWin(){
		//Toast.makeText(myContext, "Check Win", Toast.LENGTH_SHORT).show();
		if (playerHand.isEmpty()){
			winDialog("Player 1");
			if (playAgain){
				initNewGame();
			}
		}
		else if (player2Hand.isEmpty()){
			winDialog("Player 2");
			if (playAgain){
				initNewGame();
			}
		}
	}
	
	private boolean checkIfValidPlay(SpaceLauncher sl){
		//Toast.makeText(myContext, "Check If Valid Play " + category, Toast.LENGTH_SHORT).show();
		boolean isValid = false;
		if (category.equals("Height")){
			if (sl.getHeight() >= currentValue){ 
				currentValue = sl.getHeight();
				isValid = true; }
		}
		if (category.equals("Mass")){
			if (sl.getMass() >= currentValue){ 
				currentValue = sl.getMass();
				isValid = true; }
		}
		if (category.equals("Diameter")){
			if (sl.getDiameter() >= currentValue){ 
				currentValue = sl.getDiameter();
				isValid = true; }
		}
		if (category.equals("Payload")){
			if (sl.getPayload() >= currentValue){ 
				currentValue = sl.getPayload();
				isValid = true; }
		}
		if (isValid){
			played = true;
			onclick = false;
			passTurn();
		}
		return isValid;
	}
	
	private void removeCard(SpaceLauncher sl, ArrayList<SpaceLauncher> handToRemove){
		int index = handToRemove.indexOf(sl);
		discardPile.add(0,sl);
		handToRemove.remove(index);
	}
	
	private void showToast(View layout){
		toast.setGravity(Gravity.BOTTOM,0,20);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	//Special cards actions  -------------------- 
	private void wildCard(SpaceLauncher sl){
		onclick = false;
		currentValue = -1; 	//Reset the value down
		if (firstTime) { firstTime = false; }
		chooseCategory(sl);
		/*
		if (player1Turn){
			player1Turn = false;
		}
		else if (!player1Turn){
			player1Turn = true;
		}
		*/
		//passTurn();
	}
	
	private void drawTwo(SpaceLauncher sl){
		onclick = false;
		if (player1Turn){
			drawCard(player2Hand);
			drawCard(player2Hand);
			player1Turn = true;
			removeCard(sl, playerHand);
			checkWin();
		}
		else if (!player1Turn){
			drawCard(playerHand);
			drawCard(playerHand);
			player1Turn = false;
			removeCard(sl, player2Hand);
			checkWin();
		}
		passTurn();
	}
	
	private void stop(SpaceLauncher sl){
		onclick = false;
		if (player1Turn){
			//Action code for stop
			removeCard(sl, playerHand);
			player1Turn = true;
			checkWin();
		}
		else if (!player1Turn){
			player1Turn = false;
			removeCard(sl, player2Hand);
			checkWin();
		}
		passTurn();
	}
	
	private void reverse(SpaceLauncher sl){
		onclick = false;
		if (player1Turn){
			//Action code for reverse
			player1Turn = true;
			removeCard(sl, playerHand);
			checkWin();
		}
		else if (!player1Turn){
			player1Turn = false;
			removeCard(sl, player2Hand);
			checkWin();
		}
		passTurn();
	}
	
	private boolean checkForValidDraw(int player){
		boolean canDraw = true;
		if (category == null) { return true; }
		else {
			SpaceLauncher sl;
			if (player == 1){
				for (int i=0; i<playerHand.size(); i++){
					sl = playerHand.get(i);
					if (category.equals("Height")){
						if (sl.getHeight() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Mass")){
						if (sl.getMass() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Diameter")){
						if (sl.getDiameter() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Payload")){
						if (sl.getPayload() >= currentValue){
							return false;
						}
					}
				}
			}
			else {
				for (int i=0; i<player2Hand.size(); i++){
					sl = player2Hand.get(i);
					if (category.equals("Height")){
						if (sl.getHeight() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Mass")){
						if (sl.getMass() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Diameter")){
						if (sl.getDiameter() >= currentValue){
							return false;
						}
					}
					else if (category.equals("Payload")){
						if (sl.getPayload() >= currentValue){
							return false;
						}
					}
				}
			}

		}
		return canDraw;
	}
	
	private void winDialog(String winner){
		sound.playSound(R.raw.toptrumpswin, myContext);
		final Dialog winDialog = new Dialog(myContext);
		winDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		winDialog.setContentView(R.layout.custom_dialog);
		winDialog.setCanceledOnTouchOutside(false);
		
		TextView whowins = (TextView)winDialog.findViewById(R.id.winner);
		whowins.setText(winner + " Wins! ");
		
		Button yesBtn = (Button)winDialog.findViewById(R.id.yesBtn);
		yesBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				initNewGame();
				winDialog.dismiss();
			}
		});
		
		Button noBtn = (Button)winDialog.findViewById(R.id.noBtn);
		noBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Toast.makeText(myContext, "Bye bye, thanks for playing!", Toast.LENGTH_SHORT).show();
				winDialog.dismiss();
			}
		});
		winDialog.show();
	}
	
	public void chooseDeckPopup(){
		final Dialog chooseDeckDialog = new Dialog(myContext);
		chooseDeckDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		chooseDeckDialog.setContentView(R.layout.choose_deck_popup);
		chooseDeckDialog.setCanceledOnTouchOutside(false);
		
		ListView list = (ListView) chooseDeckDialog.findViewById(R.id.decksList);
		adapter = new ChooseDeckAdapter(myContext,R.layout.deck_list,deckList);
		list.setAdapter(adapter);
		
		Button okBtn = (Button)chooseDeckDialog.findViewById(R.id.okButton);
		okBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				chooseDeckDialog.dismiss();
				initNewGame();
			}
		});
		chooseDeckDialog.show();
	}
	
	public void passTurn(){
		AlertDialog.Builder builder = new AlertDialog.Builder(myContext, AlertDialog.THEME_HOLO_DARK);
		
		builder.setMessage("Pass the turn to the next player?");
		// Add the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   onclick = true;
		               dialog.dismiss();
		           }
		       });

		AlertDialog dialog = builder.create();
		
		dialog.show();
	}
}

