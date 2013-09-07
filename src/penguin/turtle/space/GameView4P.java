package penguin.turtle.space;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.Inflater;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class GameView4P extends View{

	private static Context myContext;
	private float scale;
	private int screenW;
	private int screenH;
	private int scaledCardW;
	private int scaledCardH;
	private Paint whitePaint;
	private Computer computer = new Computer();
	private Sound sound = new Sound();
	private Dialog cardDialog;
	private int turn = 0; 	//Keep track of the array index of the player to play
							//0: player, 1: left com, 2: opp com, 3: right com
	
	private Bitmap rulesIcon;
	private Bitmap background;
	private Bitmap cardBack;
	private Bitmap oppCom;		//For computer's hands 
	private Bitmap leftCom;
	private Bitmap rightCom;
	private LayoutInflater mInflater;
	private Toast toast;
	private ArrayList<Deck> deckList; 	//List of decks
	private ChooseDeckAdapter adapter;
	
	//For player hand
	private Bitmap nextarrow;
	private Bitmap backarrow;
	private Bitmap discardRect;
	private Bitmap cardoverlay;
	private Bitmap decks;
	
	private static Resources res;
	
	public static ArrayList<SpaceLauncher> spaceLaunchers;	//The entire array list
	private ArrayList<SpaceLauncher> discardPile;		//For cards already played out
	private ArrayList<SpaceLauncher> playerHand;		//Players' hand
	private ArrayList<ArrayList<SpaceLauncher>> comHand;			//Computers deck
	
	private boolean playerTurn = true;
	private boolean firstTime = true;
	public boolean playAgain = false;
	private boolean win = false;
	private boolean reverse = false; 	//track if there is a need for reverse
	private int numPlayers;
	
	//To keep track of category and value
	private String category;
	private float currentValue = -1;
	
	public GameView4P(Context context){
		super(context);
		
		this.myContext = context;
		scale = myContext.getResources().getDisplayMetrics().density;
		res = myContext.getResources();
		mInflater = LayoutInflater.from(myContext);
		toast = new Toast(myContext);
		
		spaceLaunchers = new ArrayList<SpaceLauncher>();
		playerHand = new ArrayList<SpaceLauncher>();
		discardPile = new ArrayList<SpaceLauncher>();
		deckList = new ArrayList<Deck>();
		
		numPlayers = Game4P.getNumPlayers();
		//numPlayers = 4;
		comHand = new ArrayList<ArrayList<SpaceLauncher>>();
		for (int i=0; i<numPlayers; i++){
			ArrayList<SpaceLauncher> arrayList = new ArrayList<SpaceLauncher>();
			comHand.add(arrayList);
		}
		playerHand = comHand.get(0);
		
		//White paint is for debug drawing boundaries
		whitePaint = new Paint();
		whitePaint.setAntiAlias(true);
		whitePaint.setColor(Color.WHITE);
		whitePaint.setStyle(Paint.Style.STROKE);
		whitePaint.setTextSize(scale*15);
		
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
		cardBack = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.card_2background, scaledCardW, scaledCardH);
		cardoverlay = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.cardoverlay, (int)(scaledCardW+(7*scale)), (int)(scaledCardH+(7*scale)));
		nextarrow = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.nextarrow, scaledCardW/2, scaledCardH/2);
		backarrow = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.backarrow, scaledCardW/2, scaledCardH/2);
		decks = BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.list, (int)(scaledCardW*0.8), (int)(scaledCardH*0.8));

		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		leftCom = Bitmap.createBitmap(cardBack, 0, 0, cardBack.getWidth(), cardBack.getHeight(), matrix, true);
		matrix.postRotate(90);
		oppCom = Bitmap.createBitmap(cardBack, 0, 0, cardBack.getWidth(), cardBack.getHeight(), matrix, true);
		matrix.postRotate(90);
		rightCom = Bitmap.createBitmap(cardBack, 0, 0, cardBack.getWidth(), cardBack.getHeight(), matrix, true);
		
		canvas.drawBitmap(background, this.getLeft(), this.getTop(), null);
		canvas.drawBitmap(rulesIcon, this.getRight()-rulesIcon.getWidth()-(130*scale), 
				this.getTop()+(130*scale), null);
		canvas.drawBitmap(decks, (screenW/2)-cardBack.getWidth()-(120*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), null);
		
		//Draw draw piles in the center
		canvas.drawBitmap(cardoverlay, (screenW/2)-cardBack.getWidth()-(50*scale), (screenH/2)-cardBack.getHeight()/2-(10*scale), null);
		canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-(40*scale), (screenH/2)-cardBack.getHeight()/2, null);
		Rect discardPileRect = new Rect((int)((screenW/2)+(25*scale)), (int)(screenH/2)-(cardBack.getHeight()/2), (int)((screenW/2)+(23*scale)+cardBack.getWidth()), (screenH/2)-(cardBack.getHeight()/2)+cardBack.getHeight());

		int resourceId;
		canvas.drawBitmap(cardoverlay, (screenW/2)+(15*scale), (screenH/2)-cardBack.getHeight()/2-(10*scale), null);
		if (!discardPile.isEmpty()) {
			resourceId = res.getIdentifier(discardPile.get(0).getBitmap(),"drawable",myContext.getPackageName());
			discardRect = BitmapLoader.decodeSampledBitmapFromResource(res, resourceId, scaledCardW, scaledCardH);
			canvas.drawBitmap(discardRect, new Rect(0,0,discardRect.getWidth(), discardRect.getHeight()), discardPileRect, null);			
		}
		
		//Draw the category and current value
		String categoryString, valueString;
		if (category == null){  categoryString = "Category: ";   }
		else {  categoryString = "Category: " + category;  }
		
		if (currentValue == -1){ valueString = "Current Value: "; }
		else { valueString = "Current Value: " + Float.toString(currentValue); }
		canvas.drawText(categoryString, (screenW/2)-cardBack.getWidth()-(10*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), whitePaint);
		canvas.drawText(valueString, (screenW/2)-cardBack.getWidth()-(10*scale), (screenH/2)-cardBack.getHeight()/2+cardBack.getHeight()+(35*scale), whitePaint);
		
		//Draw computer's cards 
		//Left com
		for (int i=0; i<comHand.get(1).size();i++){
			canvas.drawBitmap(leftCom, (20*scale), (screenH/2-40*comHand.get(1).size())+i*(scale*25), null);
		}
		//Opposite com
		for (int i=0; i<comHand.get(2).size(); i++){
			canvas.drawBitmap(oppCom,(screenH/2-10*comHand.get(2).size())+i*(scale*25), (20*scale) ,null);
		}
		if (numPlayers == 4){
			//Right com
			for (int i=0; i<comHand.get(3).size(); i++){
				canvas.drawBitmap(rightCom, this.getRight()-cardBack.getHeight()-(20*scale), (screenH/2-40*comHand.get(3).size())+i*(scale*25), null);
			}
		}
		
		//Draw player's hands - 7 cards only, then draw next arrow
		SpaceLauncher sl;
		if (turn == 0){ playerTurn = true; }
		if (playerHand.size() > 7){
			canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-nextarrow.getHeight()-scaledCardH ,null);
			canvas.drawBitmap(backarrow, (10*scale), screenH-backarrow.getHeight()-scaledCardH ,null);	
		}
		Bitmap cardBitmap;
		int scaledCardWidth = (int)(screenW/10);
		int scaledCardHeight = (int)(scaledCardWidth*1.28);
		for (int i=0; i<playerHand.size(); i++){
			sl = playerHand.get(i);
			//temporary use cardback for other cards 
			if (i<7){
				resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
				Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
				cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
				canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-(80*scale),null);
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
        	if (playerTurn){
        		SpaceLauncher sl;
        		for (int i=0; i<7; i++){
            		if (X > (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale && 
            		X < (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale+cardBack.getWidth() && 
            		Y < screenH-(80*scale)+ cardBack.getHeight() && 
            		Y > screenH-(80*scale)){
            			sl = playerHand.get(i);
            			cardDialogPopup(sl);
            		}
        		}
        	}
    
        	//Player touches next arrow
        	if (playerTurn){
        		if (X > screenW-nextarrow.getWidth()-(15*scale) && 
        			X < screenW-(15*scale) && 
        			Y > screenH-nextarrow.getHeight()-scaledCardH && 
        			Y < screenH-scaledCardH){
        			Toast.makeText(myContext, "Next arrow!", Toast.LENGTH_SHORT).show();
        			Collections.rotate(playerHand, 1);
        		}
        	}
        	
        	//player touches back arrow
        	if (playerTurn){
        		if (X > 5*scale &&
        			X < 10*scale+backarrow.getWidth() && 
        			Y > screenH-backarrow.getHeight()-scaledCardH &&
        			Y < screenH-scaledCardH){
        			Toast.makeText(myContext, "Back arrow!", Toast.LENGTH_SHORT).show();
            		Collections.rotate(playerHand, -1);
        		}
        	}
        	
        	//Player touches draw pile
        	if (playerTurn){
        		if (X > (screenW/2)-cardBack.getWidth()-(50*scale) && 
        		    X < (screenW/2)-cardBack.getWidth()-(50*scale)+cardoverlay.getWidth() && 
        		    Y > (screenH/2)-cardBack.getHeight()/2-(10*scale) && 
        		    Y < (screenH/2)-cardBack.getHeight()/2-(10*scale)+cardoverlay.getHeight()){
        			if(checkForValidDraw()){
        				Toast.makeText(myContext, "Draw cards", Toast.LENGTH_SHORT).show();
            			drawCard(playerHand);
            			drawCard(playerHand);
            			playerTurn = false;
            			if (reverse) { turn = numPlayers-1; }
            			else if (!reverse) { turn++; }
                		//computerPlay();
        			}
        			else{
        				Toast.makeText(myContext, "You have a valid card", Toast.LENGTH_SHORT).show();
        			}
        		}
        	}      
        	checkWin();
        	invalidate();
        	break;
        	
        case MotionEvent.ACTION_MOVE:
        	break;
        	
        case MotionEvent.ACTION_DOWN:
        	break;
        }
		invalidate();
		return true;
	}
	
	private void dealCards(){
		Collections.shuffle(spaceLaunchers);
		ArrayList<SpaceLauncher> player;
		for (int i=0; i<numPlayers; i++){
			player = comHand.get(i);
			for (int j=0; j<7; j++){
				drawCard(player);
			}
		}
	}
	
	private void initNewGame(){
		//Winner in the previous game goes first
		for (int i=0; i<comHand.size(); i++){
			if (comHand.get(i).isEmpty()){
				if (i==0) { 
					turn = 0;
					playerTurn = true; }
				else { 
					turn = i;
					playerTurn = false; }
			}
		}
		
		//Put all the cards back into spaceLaunchers arraylist
		spaceLaunchers.addAll(discardPile);
		ArrayList<SpaceLauncher> list;
		for (int i=0; i<numPlayers; i++){
			list = comHand.get(i);
			spaceLaunchers.addAll(list);
			list.clear();
		}
		//spaceLaunchers.addAll(comHand);
		//playerHand.clear();
		//comHand.clear();
		
		deckList.clear();
		loadStuff();
		
		//dealCards();
		
		 if (!playerTurn){
		 	computerPlay();
		 }
		 
		this.category = null;
		this.currentValue = -1;
		this.firstTime=true;
		this.win=false;
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
	
	private boolean checkForValidDraw(){
		boolean canDraw = true;
		if (category == null) { return false; }
		else {
			SpaceLauncher sl;
			for (int i=0; i<playerHand.size(); i++){
				sl = playerHand.get(i);
				if (category.equals("Height")){
					if (sl.getHeight() >= currentValue){ return false; }
				}
				else if (category.equals("Mass")){
					if (sl.getMass() >= currentValue){ return false; }
				}
				else if (category.equals("Diameter")){
					if (sl.getDiameter() >= currentValue){ return false; }
				}
				else if (category.equals("Payload")){
					if (sl.getPayload() >= currentValue){ return false; }
				}
			}
		}
		return canDraw;
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
					Sound.playSound(R.raw.lock, myContext);
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
					Sound.playSound(R.raw.lock, myContext);
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
					Sound.playSound(R.raw.lock, myContext);
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
					Sound.playSound(R.raw.lock, myContext);
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
					Sound.playSound(R.raw.lock, myContext);
					if (firstTime){
						cardDialog.dismiss();
						chooseCategory(sl);
					}
					
					else if (!firstTime){
						//to log card details and pass turn over
						if (checkIfValidPlay(sl)){
							playerTurn = false;
							removeCard(sl, comHand.get(turn));
							computerPlay();
							checkWin();
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
	
	private void removeCard(SpaceLauncher sl, ArrayList<SpaceLauncher> handToRemove){
		int index = handToRemove.indexOf(sl);
		discardPile.add(0,sl);
		handToRemove.remove(index);
		invalidate();
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
		height.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.heighted, 100, 90));
		height.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Toast.makeText(myContext, "Height", Toast.LENGTH_SHORT).show();
				Sound.playSound(R.raw.lock, myContext);
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Height");
				showToast(layout);
				setCategory("Height");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					removeCard(sl, playerHand);
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					computerPlay();
					checkWin();
				}
			}
		});
		
		ImageView mass = (ImageView)categoryDialog.findViewById(R.id.mass);
		mass.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.massed, 100, 80));
		mass.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Toast.makeText(myContext, "Mass", Toast.LENGTH_SHORT).show();
				Sound.playSound(R.raw.lock, myContext);
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Mass");
				showToast(layout);
				setCategory("Mass");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					removeCard(sl, playerHand);
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					computerPlay();
					checkWin();
				}
			}
		});
		
		ImageView diameter = (ImageView)categoryDialog.findViewById(R.id.diameter);
		diameter.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.diametered, 100, 90));
		diameter.setOnClickListener(new View.OnClickListener(){
		public void onClick(View v){
				//Toast.makeText(myContext, "Diameter", Toast.LENGTH_SHORT).show();
				Sound.playSound(R.raw.lock, myContext);
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Diameter");
				showToast(layout);
				setCategory("Diameter");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					removeCard(sl, playerHand);
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					computerPlay();
					checkWin();
				}
			}
		});
		
		ImageView payload = (ImageView)categoryDialog.findViewById(R.id.payload);
		payload.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.payloaded, 100, 80));
		payload.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Toast.makeText(myContext, "Payload", Toast.LENGTH_SHORT).show();
				Sound.playSound(R.raw.lock, myContext);
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Payload");
				showToast(layout);
				setCategory("Payload");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					removeCard(sl, playerHand);
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					computerPlay();
					checkWin();
				}
			}
		});
		
		categoryDialog.show();
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
		return isValid;
	}
	
	private void logDetails(SpaceLauncher sl){
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
		removeCard(sl, comHand.get(turn));
		if (!reverse){
			turn++;
			if (turn == numPlayers){
				turn = 0;
				playerTurn = true;
			}
			else { playerTurn = false;} 
		}
		else if (reverse){
			turn--;
			if (turn == -1){
				turn = numPlayers-1;
				playerTurn = false;
			}
			else if (turn == 0) { playerTurn = true; }
			else { playerTurn = false; } 
		}
		checkWin();
		if (this.win == false && playerTurn == false) { computerPlay(); }
	}
	
	private void checkWin(){
		//Toast.makeText(myContext, "Check Win", Toast.LENGTH_SHORT).show();
		if (this.win == false){
			for (int i=0; i<numPlayers; i++){
				if (comHand.get(i).isEmpty()){
					this.win = true;
					if (i==0) { winDialog("Player"); }
					else {winDialog ("Computer " + (i-1)); }
				}
			}
		}
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
	
	private void showToast(View layout){
		toast.setGravity(Gravity.BOTTOM,0,20);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	//Special cards actions -----------------
		private void wildCard(SpaceLauncher sl){
			currentValue = -1; 	//Reset the value down
			if (firstTime) {firstTime = false; }
			if (!reverse){
				if (playerTurn){
					//playerTurn = false;
					//turn++;
					chooseCategory(sl);
				}
				else if (!playerTurn){
					removeCard(sl, comHand.get(turn));
					turn++;
					if (turn == numPlayers){
						turn = 0;
						playerTurn = true;
					}
					else { playerTurn = false; }
					checkWin();
					if (this.win == false && playerTurn == false) { computerPlay(); }
				}
			}
			else {
				if (playerTurn){
					//playerTurn = false;
					//turn = numPlayers-1;
					chooseCategory(sl);
				}
				else if (!playerTurn){
					removeCard(sl, comHand.get(turn));
					turn--;
					if (turn == -1){
						turn = 0;
						playerTurn = true;
					}
					else { playerTurn = false; }
					checkWin();
					if (this.win == false && playerTurn == false) { computerPlay(); }
				}
			}
			//Toast.makeText(myContext, turn + " " + playerTurn, Toast.LENGTH_SHORT).show();
		}
		
		private void drawTwo(SpaceLauncher sl){
			removeCard(sl, comHand.get(turn));
			if (!reverse) {
				turn++;
				if (turn == numPlayers){
					turn = 0;
					playerTurn = true;
				}
				else { playerTurn = false; }
				drawCard(comHand.get(turn));
				drawCard(comHand.get(turn));
				turn++;
				if (turn == numPlayers){
					turn = 0;
					playerTurn = true;
				}
				else { playerTurn = false; }
				checkWin();
				if (this.win == false && playerTurn == false) { computerPlay(); }
			}
			else {
				turn--;
				if (turn == 0) { playerTurn = true; }
				else {
					if (turn == -1) {turn = numPlayers-1;}
					playerTurn = false;
				}
				drawCard(comHand.get(turn));
				drawCard(comHand.get(turn));
				turn--;
				if (turn == 0) { playerTurn = true; }
				else {
					if (turn == -1) {turn = numPlayers-1;}
					playerTurn = false;
				}
				checkWin();
				if (this.win == false && playerTurn == false) { computerPlay(); }
			}
			//Toast.makeText(myContext, turn + " " + playerTurn, Toast.LENGTH_SHORT).show();
		}
		
		private void stop(SpaceLauncher sl){
			removeCard(sl, comHand.get(turn));
			if (!reverse){
				turn++;
				if (turn == numPlayers){
					turn = 0;
					Toast.makeText(myContext, "Player skips a turn!", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(myContext, "Computer " + turn + " skips a turn!", Toast.LENGTH_SHORT).show();
				}
				turn++;
				if (turn == numPlayers){
					turn = 0;
					playerTurn = true;
				}
				else { playerTurn = false; }
			}
			else {
				turn--;
				if (turn == 0){
					playerTurn = false;
					Toast.makeText(myContext, "Player skips a turn!", Toast.LENGTH_SHORT).show();
				}
				else{
					if (turn == -1) { turn = numPlayers-1; }
					Toast.makeText(myContext, "Computer " + turn + " skips a turn!", Toast.LENGTH_SHORT).show();
				}
				turn--;
				if (turn == 0) { playerTurn = true; }
				else if (turn == -1) { 
					turn = numPlayers-1;
					playerTurn = false;
				}
				else { playerTurn = false; }
			}
			checkWin();
			if (this.win == false && playerTurn == false) { computerPlay(); }
		}
		
		private void reverse(SpaceLauncher sl){
			removeCard(sl, comHand.get(turn));
			reverse = !reverse;
			if (reverse){
				turn--;
				if (turn == 0) { playerTurn = true; }
				else if (turn == -1) { 
					turn = numPlayers-1;
					playerTurn = false;
				}
				else { playerTurn = false; }
			}
			else {
				turn++;
				if (turn == numPlayers){
					turn = 0;
					playerTurn = true;
				}
				else { playerTurn = false; }
			}
			checkWin();
			if (this.win == false && playerTurn == false) { computerPlay(); }
		}
		
		private void computerPlay(){
			Toast.makeText(myContext, "Computer " + turn + " 's Turn", Toast.LENGTH_SHORT).show();
			if (category == null && currentValue == -1){
				category = Computer.chooseCategoryToPlay(comHand.get(turn));
				SpaceLauncher sl = Computer.chooseRandomCard(comHand.get(turn));
				logDetails(sl);
			}
			else {
				SpaceLauncher sl = Computer.play(comHand.get(turn), category, currentValue);
				//Toast.makeText(myContext, sl.getName(), Toast.LENGTH_SHORT).show();
				TextView text;
				View layout = mInflater.inflate(R.layout.custom_toast, null);
				if(sl == null){
					//No valid play, draw two from pile 
					Toast.makeText(myContext, "Computer draws two from pile.", Toast.LENGTH_SHORT).show();
					text = (TextView) layout.findViewById(R.id.msg);
					text.setText("Computer " + turn + " draws two cards from pile.");
					//showToast(layout);
					drawCard(comHand.get(turn));
					drawCard(comHand.get(turn));
					
					if(!reverse){
						turn++;
						if (turn == numPlayers){
							turn = 0;
							playerTurn = true;
						}
						else { playerTurn = false; }
					}
					else if (reverse){
						turn--;
						if (turn == -1){
							turn = numPlayers-1;
							playerTurn = false;
						}
						else if (turn == 0){ playerTurn = true;	}
						else { playerTurn = false; }
					}
					checkWin();
					if (!playerTurn) {  computerPlay(); }
				}
				else {
					String name = sl.getName();
					if (name.equals("Reverse"))	{ 
						Toast.makeText(myContext, "Computer " + turn + " plays reverse!", Toast.LENGTH_SHORT).show();
						text = (TextView) layout.findViewById(R.id.msg);
						text.setText("Computer" + turn + " plays Reverse!");
						//showToast(layout);
						reverse(sl);
					}
					else if (name.equals("Stop")){
						Toast.makeText(myContext, "Computer " + turn + " plays stop!", Toast.LENGTH_SHORT).show();
						text = (TextView) layout.findViewById(R.id.msg);
						text.setText("Computer" + turn + " plays Stop!");
						//showToast(layout);
						stop(sl);
					}
					else if (name.equals("Draw Two")){
						Toast.makeText(myContext, "Computer " + turn + " plays Draw Two!", Toast.LENGTH_SHORT).show();
						text = (TextView) layout.findViewById(R.id.msg);
						text.setText("Computer " + turn + " plays Draw Two!");
						//showToast(layout);
						drawTwo(sl);
					}
					else if (name.equals("Wild Card")){
						String category = Computer.chooseCategoryToPlay(comHand.get(turn));
						Toast.makeText(myContext, "Computer " + turn + " chooses " + category, Toast.LENGTH_SHORT).show();
						text = (TextView) layout.findViewById(R.id.msg);
						text.setText("Computer " + turn + " changes category to " + category);
						//showToast(layout);
						removeCard(sl, comHand.get(turn));
					}
					else {
						Toast.makeText(myContext, "Computer " + turn + " plays " + sl.getName(), Toast.LENGTH_SHORT).show();
						text = (TextView) layout.findViewById(R.id.msg);
						text.setText("Computer " + turn + " plays " + sl.getName());
						//showToast(layout);
						logDetails(sl);
					}
				}
			}
			invalidate();
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
}
