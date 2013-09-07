package penguin.turtle.space;

import android.content.Context;
import android.media.MediaPlayer;

public class Sound {

	private static MediaPlayer mp;
	
	public static void playSound(int soundFile, final Context context) {
		final int mFile = soundFile;
		Thread thread = new Thread(new Runnable() {
			public void run(){
				mp = MediaPlayer.create(context,mFile);
				mp.start();
			}
		});
		thread.start();
		
	}
	
}
