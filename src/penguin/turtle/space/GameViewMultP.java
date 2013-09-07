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

public class GameViewMultP extends View{

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
	private boolean onclick;
	private int turn = 0; 	//Keep track of the array index of the player to play
							//0: player, 1: left com, 2: opp com, 3: right com
	
	private Bitmap rulesIcon;
	private Bitmap background;
	private Bitmap cardBack;
	private Bitmap oppCom;		//For computer's hands 
	private Bitmap leftCom;
	private Bitmap rightCom;
	private Bitmap decks;
	private LayoutInflater mInflater;
	private Toast toast;
	private ArrayList<Deck> deckList; 	//List of decks
	private ChooseDeckAdapter adapter;
	
	//For player hand
	private Bitmap nextarrow;
	private Bitmap backarrow;
	private Bitmap discardRect;
	private Bitmap cardoverlay;
	
	private static Resources res;
	
	public static ArrayList<SpaceLauncher> spaceLaunchers;	//The entire array list
	private ArrayList<SpaceLauncher> discardPile;		//For cards already played out
	private ArrayList<ArrayList<SpaceLauncher>> comHand;			
	
	private boolean playerTurn = true;
	private boolean firstTime = true;
	public boolean playAgain = false;
	private boolean win = false;
	private boolean reverse = false; 	//track if there is a need for reverse
	private int numPlayers;
	
	//To keep track of category and value
	private String category;
	private float currentValue = -1;
	
	public GameViewMultP(Context context){
		super(context);
		
		this.myContext = context;
		scale = myContext.getResources().getDisplayMetrics().density;
		res = myContext.getResources();
		mInflater = LayoutInflater.from(myContext);
		toast = new Toast(myContext);
		
		spaceLaunchers = new ArrayList<SpaceLauncher>();
		discardPile = new ArrayList<SpaceLauncher>();
		deckList = new ArrayList<Deck>();
		
		numPlayers = GameMultP.getNumPlayers();
		//numPlayers = 4;
		comHand = new ArrayList<ArrayList<SpaceLauncher>>();
		for (int i=0; i<numPlayers; i++){
			ArrayList<SpaceLauncher> arrayList = new ArrayList<SpaceLauncher>();
			comHand.add(arrayList);
		}
		
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
		canvas.drawBitmap(cardoverlay, (screenW/2)-cardBack.getWidth()-(50*scale), (screenH/2)-cardBack.getHeight()/2-(25*scale), null);
		canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-(40*scale), (screenH/2)-cardBack.getHeight()/2-(15*scale), null);
		//discard pile temporarily use the card back
		Rect discardPileRect = new Rect((int)((screenW/2)+(25*scale)), (int)((screenH/2)-(cardBack.getHeight()/2)-15*scale), (int)((screenW/2)+(23*scale)+cardBack.getWidth()), (int)((screenH/2)-(cardBack.getHeight()/2)+cardBack.getHeight()-(5*scale)));

		int resourceId;
		canvas.drawBitmap(cardoverlay, (screenW/2)+(15*scale), (screenH/2)-cardBack.getHeight()/2-(25*scale), null);
		if (!discardPile.isEmpty()) {
			resourceId = res.getIdentifier(discardPile.get(0).getBitmap(),"drawable",myContext.getPackageName());
			discardRect = BitmapLoader.decodeSampledBitmapFromResource(res, resourceId, scaledCardW, scaledCardH);
			canvas.drawBitmap(discardRect, new Rect(0,0,discardRect.getWidth(), discardRect.getHeight()), discardPileRect, null);			
		}
		
		//Draw the category and current value
		String categoryString, valueString;
		if (category == null){  categoryString = "Category: ";  }
		else { 	categoryString = "Category: " + category; }
		
		if (currentValue == -1){ valueString = "Current Value: "; }
		else { valueString = "Current Value: " + Float.toString(currentValue); }
		canvas.drawText(categoryString, (screenW/2)-cardBack.getWidth()-(100*scale), (screenH/2)-cardBack.getHeight()/2-(35*scale), whitePaint);
		canvas.drawText(valueString, (screenW/2)-cardBack.getWidth()+(80*scale), (screenH/2)-cardBack.getHeight()/2-(35*scale), whitePaint);

		ArrayList<SpaceLauncher> playerHand;
		SpaceLauncher sl;
		Bitmap cardBitmap, tempBitmap;
		if (onclick){
		//Player 1 turn
		if (turn == 0){
			//Player 2
			for (int i=0; i<comHand.get(1).size();i++){
				canvas.drawBitmap(leftCom, (20*scale), (screenH/2-40*comHand.get(1).size())+i*(scale*25), null);
			}
			//Player 3
			for (int i=0; i<comHand.get(2).size(); i++){
				canvas.drawBitmap(oppCom,(screenH/2-10*comHand.get(2).size())+i*(scale*25), (20*scale) ,null);
			}
			if (numPlayers == 4){
				//Player 4
				for (int i=0; i<comHand.get(3).size(); i++){
					canvas.drawBitmap(rightCom, this.getRight()-cardBack.getHeight()-(20*scale), (screenH/2-40*comHand.get(3).size())+i*(scale*25), null);
				}
			}
			//Player 1
			playerHand = comHand.get(0);
			if (playerHand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-(90*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), screenH-(90*scale) ,null);	
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
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-120*scale,null);
				}
		}
		}
		//Player 2 turn
		else if (turn == 1){
			//Player 1
			for (int i=0; i<comHand.get(0).size(); i++){
				canvas.drawBitmap(cardBack,(screenH/2-10*comHand.get(0).size())+i*(scale*25), screenH-(100*scale),null);
			}
			//Player 3
			for (int i=0; i<comHand.get(2).size(); i++){
				canvas.drawBitmap(oppCom,(screenH/2-10*comHand.get(2).size())+i*(scale*25), (20*scale) ,null);
			}
			if (numPlayers == 4){
				//Player 4
				for (int i=0; i<comHand.get(3).size(); i++){
					canvas.drawBitmap(rightCom, this.getRight()-cardBack.getHeight()-(20*scale), (screenH/2-40*comHand.get(3).size())+i*(scale*25), null);
				}
			}
			//Player 2
			playerHand = comHand.get(1);
			if (playerHand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-(90*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), screenH-(90*scale) ,null);	
			}
			int scaledCardWidth = (int)(screenW/10);
			int scaledCardHeight = (int)(scaledCardWidth*1.28);
			for (int i=0; i<playerHand.size(); i++){
				sl = playerHand.get(i);
				if (i<7){
					resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
					tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
					cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-120*scale,null);
				}
		}
		}
		//Player 3 turn
		else if (turn == 2){
			//Player 1
			for (int i=0; i<comHand.get(0).size(); i++){
				canvas.drawBitmap(cardBack,(screenH/2-10*comHand.get(0).size())+i*(scale*25), screenH-(100*scale),null);
			}
			//Player 2
			for (int i=0; i<comHand.get(1).size();i++){
				canvas.drawBitmap(leftCom, (20*scale), (screenH/2-40*comHand.get(1).size())+i*(scale*25), null);
			}
			if (numPlayers == 4){
				//Player 4
				for (int i=0; i<comHand.get(3).size(); i++){
					canvas.drawBitmap(rightCom, this.getRight()-cardBack.getHeight()-(20*scale), (screenH/2-40*comHand.get(3).size())+i*(scale*25), null);
				}
			}
			//Player 3
			playerHand = comHand.get(2);
			if (playerHand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-(90*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), screenH-(90*scale) ,null);	
			}
			int scaledCardWidth = (int)(screenW/10);
			int scaledCardHeight = (int)(scaledCardWidth*1.28);
			for (int i=0; i<playerHand.size(); i++){
				sl = playerHand.get(i);
				if (i<7){
					resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
					tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
					cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-120*scale,null);
				}
		}
		}
		//Player 4 turn
		else if (numPlayers == 4){
			if (turn == 3){
				//Player 1
				for (int i=0; i<comHand.get(0).size(); i++){
					canvas.drawBitmap(cardBack,(screenH/2-10*comHand.get(0).size())+i*(scale*25), screenH-(100*scale),null);
				}
			}
			//Player 2
			for (int i=0; i<comHand.get(1).size();i++){
				canvas.drawBitmap(leftCom, (20*scale), (screenH/2-40*comHand.get(1).size())+i*(scale*25), null);
			}
			//Player 3
			for (int i=0; i<comHand.get(2).size(); i++){
				canvas.drawBitmap(oppCom,(screenH/2-10*comHand.get(2).size())+i*(scale*25), (20*scale) ,null);
			}
			//Player 4
			playerHand = comHand.get(3);
			if (playerHand.size() > 7){
				canvas.drawBitmap(nextarrow, screenW-nextarrow.getWidth()-(10*scale), screenH-(90*scale) ,null);
				canvas.drawBitmap(backarrow, (10*scale), screenH-(90*scale) ,null);	
			}
			int scaledCardWidth = (int)(screenW/10);
			int scaledCardHeight = (int)(scaledCardWidth*1.28);
			for (int i=0; i<playerHand.size(); i++){
				sl = playerHand.get(i);
				if (i<7){
					resourceId = res.getIdentifier(sl.getBitmap(),"drawable",myContext.getPackageName());
					tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);
					cardBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardWidth, scaledCardHeight, false); 
					canvas.drawBitmap(cardBitmap,(screenH/2-15*scale*7)+i*(scaledCardW+10)*scale, screenH-120*scale,null);
				}
		}
		}
		}
		invalidate();
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

        	ArrayList<SpaceLauncher> currentHand = comHand.get(turn);
        	//Player touches cards
    		SpaceLauncher sl;
    		for (int i=0; i<7; i++){
        		if (X > (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale && 
        		X < (screenH/2-15*scale*7)+i*(scaledCardW+10)*scale+cardBack.getWidth() && 
        		Y < screenH-(130*scale)+ cardBack.getHeight() && 
        		Y > screenH-(130*scale)){
        			sl = currentHand.get(i);
        			cardDialogPopup(sl);
        		}
    		}
    
        	//Player touches next arrow
    		if (X > screenW-nextarrow.getWidth()-(15*scale) && 
    			X < screenW-(15*scale) && 
    			Y > screenH-(95*scale) && 
    			Y < screenH-(95*scale)+nextarrow.getHeight()){
    			Toast.makeText(myContext, "Next arrow!", Toast.LENGTH_SHORT).show();
    			Collections.rotate(currentHand, 1);
    		}
        	
        	//player touches back arrow
    		if (X > 5*scale &&
    			X < 10*scale+backarrow.getWidth() && 
    			Y > screenH-(95*scale) &&
    			Y < screenH-(95*scale)+nextarrow.getHeight()){
    			Toast.makeText(myContext, "Back arrow!", Toast.LENGTH_SHORT).show();
        		Collections.rotate(currentHand, -1);
    		}
        	
        	
        	//Player touches draw pile
    		if (X > (screenW/2)-cardBack.getWidth()-(50*scale) && 
    		    X < (screenW/2)-cardBack.getWidth()-(50*scale)+cardoverlay.getWidth() && 
    		    Y > (screenH/2)-cardBack.getHeight()/2-(10*scale) && 
    		    Y < (screenH/2)-cardBack.getHeight()/2-(10*scale)+cardoverlay.getHeight()){
    			if(checkForValidDraw()){
    				Toast.makeText(myContext, "Draw cards", Toast.LENGTH_SHORT).show();
        			drawCard(currentHand);
        			drawCard(currentHand);
        			if (reverse) { 
        				turn--;
        				if (turn == -1) {turn = numPlayers-1; }
        			}
        			else if (!reverse) { turn++;
        				if(turn == numPlayers) { turn = 0; }
        			}
    			}
    			else{ Toast.makeText(myContext, "You have a valid card", Toast.LENGTH_SHORT).show();  }
    		}
        	    
        	checkWin();
        	invalidate();
        	break;
        }
		invalidate();
		return true;
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
	
	private void checkWin(){
		//Toast.makeText(myContext, "Check Win", Toast.LENGTH_SHORT).show();
		if (this.win == false){
			for (int i=0; i<numPlayers; i++){
				if (comHand.get(i).isEmpty()){
					this.win = true;
					winDialog("Player " + (i-1));
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
	
	private void initNewGame(){
		//Winner in the previous game goes first
		for (int i=0; i<comHand.size(); i++){
			if (comHand.get(i).isEmpty()){
				turn = i;
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
		deckList.clear();
		loadStuff();
		
		//dealCards();
		 
		this.category = null;
		this.currentValue = -1;
		this.firstTime=true;
		this.win=false;
	}
	
	private boolean checkForValidDraw(){
		boolean canDraw = true;
		if (category == null) { return false; }
		else {
			SpaceLauncher sl;
			ArrayList<SpaceLauncher> list = comHand.get(turn);
			for (int i=0; i<list.size(); i++){
				sl = list.get(i);
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
							removeCard(sl, comHand.get(turn));
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
				Sound.playSound(R.raw.lock, myContext);
				//Toast.makeText(myContext, "Height", Toast.LENGTH_SHORT).show();
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Height");
				showToast(layout);
				setCategory("Height");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					removeCard(sl, comHand.get(turn));
					passTurn();
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					checkWin();
				}
			}
		});
		
		ImageView mass = (ImageView)categoryDialog.findViewById(R.id.mass);
		mass.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.massed, 100, 80));
		mass.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Sound.playSound(R.raw.lock, myContext);
				//Toast.makeText(myContext, "Mass", Toast.LENGTH_SHORT).show();
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Mass");
				showToast(layout);
				setCategory("Mass");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					passTurn();
					removeCard(sl, comHand.get(turn));
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					checkWin();
				}
			}
		});
		
		ImageView diameter = (ImageView)categoryDialog.findViewById(R.id.diameter);
		diameter.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.diametered, 100, 90));
		diameter.setOnClickListener(new View.OnClickListener(){
		public void onClick(View v){
				Sound.playSound(R.raw.lock, myContext);	
				//Toast.makeText(myContext, "Diameter", Toast.LENGTH_SHORT).show();
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Diameter");
				showToast(layout);
				setCategory("Diameter");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					passTurn();
					removeCard(sl, comHand.get(turn));
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					checkWin();
				}
			}
		});
		
		ImageView payload = (ImageView)categoryDialog.findViewById(R.id.payload);
		payload.setImageBitmap(BitmapLoader.decodeSampledBitmapFromResource(res, R.drawable.payloaded, 100, 80));
		payload.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Sound.playSound(R.raw.lock, myContext);
				//Toast.makeText(myContext, "Payload", Toast.LENGTH_SHORT).show();
				TextView text = (TextView) layout.findViewById(R.id.msg);
				text.setText("New Category: Payload");
				showToast(layout);
				setCategory("Payload");
				categoryDialog.dismiss();
				if (!sl.getName().equals("Wild Card")){ logDetails(sl); }
				else {
					passTurn();
					removeCard(sl, comHand.get(turn));
					if (!reverse) {turn++;}
					else {turn = numPlayers-1; }
					checkWin();
				}
			}
		});
		categoryDialog.show();
	}
	
	private void showToast(View layout){
		toast.setGravity(Gravity.BOTTOM,0,20);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
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
			onclick = false;
			passTurn();
		}
		return isValid;
	}
	
	private void logDetails(SpaceLauncher sl){
		onclick = false;
		if (category.equals("Height")){ currentValue = sl.getHeight(); }
		if (category.equals("Mass")){ currentValue = sl.getMass(); }
		if (category.equals("Diameter")){ currentValue = sl.getDiameter(); }
		if (category.equals("Payload")){ currentValue = sl.getPayload(); }
		passTurn();
		removeCard(sl, comHand.get(turn));
		if (reverse){
			turn--;
			if (turn == -1) { turn = numPlayers-1; }
		}
		else if (!reverse){
			turn++;
			if (turn == numPlayers) { turn = 0; }
		}
		checkWin();
	}
	
	//Special cards actions -----
	private void wildCard(SpaceLauncher sl){
		onclick = false;
		currentValue = -1; 	//Reset the value down
		if (firstTime) {firstTime = false; }
		chooseCategory(sl);
		checkWin();			
	}
	
	private void drawTwo(SpaceLauncher sl){
		onclick = false;
		removeCard(sl, comHand.get(turn));
		if (!reverse) {
			turn++;
			if (turn == numPlayers){
				turn = 0;
			}
			drawCard(comHand.get(turn));
			drawCard(comHand.get(turn));
			turn++;
			if (turn == numPlayers){
				turn = 0;
			}
			checkWin();
		}
		else {
			turn--;
			if (turn == -1) {turn = numPlayers-1;}
			drawCard(comHand.get(turn));
			drawCard(comHand.get(turn));
			turn--;
			if (turn == -1) {turn = numPlayers-1;}
			playerTurn = false;
			checkWin();
		}
		passTurn();
		//Toast.makeText(myContext, turn + " " + playerTurn, Toast.LENGTH_SHORT).show();
	}
	
	private void stop(SpaceLauncher sl){
		onclick = false;
		removeCard(sl, comHand.get(turn));
		if (!reverse){
			turn++;
			if (turn == numPlayers){  turn = 0;  }
			Toast.makeText(myContext, "Player skips a turn!", Toast.LENGTH_SHORT).show();
			turn++;
			if (turn == numPlayers){ turn = 0; }
		}
		else {
			turn--;
			Toast.makeText(myContext, "Player skips a turn!", Toast.LENGTH_SHORT).show();
			if (turn == -1) { turn = numPlayers-1; }
			turn--;
			if (turn == -1) { turn = numPlayers-1;  	}
		}
		checkWin();
		passTurn();
	}
	
	private void reverse(SpaceLauncher sl){
		onclick = false;
		removeCard(sl, comHand.get(turn));
		reverse = !reverse;
		if (reverse){
			turn--;
			if (turn == -1) { turn = numPlayers-1;  }
		}
		else {
			turn++;
			if (turn == numPlayers){ turn = 0; }
		}
		checkWin();
		passTurn();
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
