package maxx.test.blips;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;

public class BlipGenerator {   
	// Static variables
   // Note name array
   static final String[] notes = {"A5", "Bb5", "B5", "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab6", "A6"};
   
   // Scale interval arrays
   static final int[] major = {2, 2, 1, 2, 2, 2, 1};
   static final int[] minor = {2, 1, 2, 2, 1, 2, 2};
       
   static final int S1 = R.raw.pianoa5;
   static final int S2 = R.raw.pianobb5;
   static final int S3 = R.raw.pianob5;
   static final int S4 = R.raw.pianoc5;
   static final int S5 = R.raw.pianodb5;
   static final int S6 = R.raw.pianod5;
   static final int S7 = R.raw.pianoeb5;
   static final int S8 = R.raw.pianoe5;
   static final int S9 = R.raw.pianof5;
   static final int S10 = R.raw.pianogb5;
   static final int S11 = R.raw.pianog5;
   static final int S12 = R.raw.pianoab6;
   static final int S13 = R.raw.pianoa6;
	      
   private static SoundPool soundPool = null;
   
    // Member variables
   boolean playing;
   boolean loading = false;
   int playingIndex = 0;
   int rootIndex = 0;
   float volume = .1f;
   Timer timer = null;

   Context mainContext = null;
   int[] scale = null;
   
   // List of current selections for each column
   ArrayList<ArrayList<Integer>> selections = null;
   
   public BlipGenerator() {
	   playing = false;
	   scale = minor;
   }
   
   public BlipGenerator(Context context) {
	   playing = false;
	   mainContext = context;
	   scale = minor;
	   initSounds();
   }
   
    /** Populate the SoundPool*/
   public void initSounds() {
	   initSelections();

      soundPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 100);

      soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
         @Override
         public void onLoadComplete(SoundPool soundPool, int sampleId,
          int status) {
            System.out.println("Load completed for " + sampleId + " Status: " + status);
            
            if (sampleId == 13) {
            	loading = false;
            }
         }
      });

      loading = true;
      
      soundPool.load(mainContext, S1, 1);
      soundPool.load(mainContext, S2, 1);
      soundPool.load(mainContext, S3, 1);
      soundPool.load(mainContext, S4, 1);
      soundPool.load(mainContext, S5, 1);
      soundPool.load(mainContext, S6, 1);
      soundPool.load(mainContext, S7, 1);
      soundPool.load(mainContext, S8, 1);
      soundPool.load(mainContext, S9, 1);
      soundPool.load(mainContext, S10, 1);
      soundPool.load(mainContext, S11, 1);
      soundPool.load(mainContext, S12, 1);
      soundPool.load(mainContext, S13, 1);
   }
   
   public void initSelections() {
	  if (selections == null) {
		  selections = new ArrayList<ArrayList<Integer>>(0);
	  }

      for (int i = 0; i < BlipsMain.GRID_COLS; i++) {
         selections.add(new ArrayList<Integer>(0));
      }

      System.out.println("Selections initialized. Size: " + selections.size());
   }
    
    /** Play a given sound in the soundPool */
    public void playSound(int soundID) {
	   if(soundPool == null) {
		   System.out.println("Something is null");
	      initSounds();
	   }

       // play sound with same right and left volume, with a priority of 1, 
       // zero repeats (i.e play once), and a playback rate of 1f
       soundPool.play(soundID, volume, volume, 1, 0, 1f);
    }
   
   public void play() {
	   if (soundPool == null) {
		   initSounds();
	   }
	   
	   playing = true;
	   playingIndex = 0;
	  
	   startSequence();			
   }
   
   
   public void stop() {
	   playing = false;	
	   
	   if (timer != null) {
		   timer.cancel();
		   timer = null;
	   }
	   
	   if (soundPool != null) {
		   soundPool.autoPause();
		   soundPool.release();
		   soundPool = null;
	   }
   }
   
   public void startSequence (){
	   final Handler handler = new Handler ();
	   
	   // Timer, calls the inner run() every MILLI_DELAY interval
	   if (timer != null) {
		   timer.cancel();
	   }
	   
	   timer = new Timer();
	   timer.scheduleAtFixedRate (new TimerTask (){
	      public void run () {
	         handler.post (new Runnable () {
	            public void run () {
	            	// Start current index
	            	if (!(((BlipsMain)mainContext).resetting || loading)) {
	            		for (int row : selections.get(playingIndex)) {
	            			// Play sound and check result
	            			if (soundPool.play(row, volume, volume, 1, 0, 1f) == 0) {
	            				System.out.println("Play failed");
	            				
	            				// Error recovery
	            				soundPool.release();
	            				soundPool = null;
	            				initSounds();
	            			}
	            		}
	            		
	 	               // Increment index
	 	               if (++playingIndex >= BlipsMain.GRID_COLS) {
	 	            	   playingIndex = 0;
	 		           }
	            	}
	            }
	         });
	      }
	   }, 0, BlipsMain.MILLI_DELAY);
   }
   
   public void changeScale(int[] newScale, int newRoot) {
	   // Pause playback
	   soundPool.autoPause();
	   loading = true;
	   
	   // Set new scale descriptors
	   rootIndex = newRoot;
	   scale = newScale;
	   
	   // Drop old selected notes
	   selections = null;
	   initSelections();
	   
	   // Set each button's new label and sound index
		for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
			for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
				BlipCell btn = ((BlipsMain)mainContext).cells[c][r];
				btn.resetIndex(); 
	        	
				if (btn.isOn()) {
	        		selections.get(c).add(btn.getIndex());
	        	}
			}
		}
		
		loading = false;
		playingIndex = 0;
   }
}
