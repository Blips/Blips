package maxx.test.blips;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
//import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;

public class BlipGenerator {   
	// Static variables
   // Note name array
   // TODO: Why is this here?
   static final String[] notes = {"A5", "Bb5", "B5", "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab6", "A6"};
   
   // Scale interval arrays
   static final int[] major      = {2, 2, 1, 2, 2, 2, 1};
   static final int[] minor      = {2, 1, 2, 2, 1, 2, 2};
   static final int[] dorian     = {2, 1, 2, 2, 2, 1, 2};
   static final int[] lydian     = {2, 2, 2, 1, 2, 2, 1};
   static final int[] locrian    = {1, 2, 2, 1, 2, 2, 2};
   static final int[] phrygian   = {1, 2, 2, 2, 1, 2, 2};
   static final int[] harmonic   = {2, 1, 2, 2, 1, 3, 1};
   static final int[] mixolydian = {2, 2, 1, 2, 2, 1, 2};
   
   // TODO: Read below
   // I parsed all the files from the site as they were,
   // which was from C to C. While we don't need to change
   // our default from being A, you should still take note
   // that the lowest note we support is C5, and that the
   // notes have now moved in terms of SX positioning.
   static final int S1 = R.raw.pianoc5;
   static final int S2 = R.raw.pianodb5;
   static final int S3 = R.raw.pianod5;
   static final int S4 = R.raw.pianoeb5;
   static final int S5 = R.raw.pianoe5;
   static final int S6 = R.raw.pianof5;
   static final int S7 = R.raw.pianogb5;
   static final int S8 = R.raw.pianog5;
   static final int S9 = R.raw.pianoab5;
   static final int S10 = R.raw.pianoa5; // here's A!
   static final int S11 = R.raw.pianobb5;
   static final int S12 = R.raw.pianob5;
   static final int S13 = R.raw.pianoc6;
   static final int S14 = R.raw.pianodb6;
   static final int S15 = R.raw.pianod6;
   static final int S16 = R.raw.pianoeb6;
   static final int S17 = R.raw.pianoe6;
   static final int S18 = R.raw.pianof6;
   static final int S19 = R.raw.pianogb6;
   static final int S20 = R.raw.pianog6;
   static final int S21 = R.raw.pianoab6;
   static final int S22 = R.raw.pianoa6;
   static final int S23 = R.raw.pianobb6;
   static final int S24 = R.raw.pianob6;
   static final int S25 = R.raw.pianoc7;
	      
   private static SoundPool soundPool = null;
   
    // Member variables
   boolean playing;
   boolean loading = true;
   int playingIndex = 0;
   int rootIndex = 0;
   float volume = .5f;
   Timer timer = null;

   BlipsMain mainContext = null;
   int[] scale = null;
   
   // List of current selections for each column
   ArrayList<ArrayList<Integer>> selections = null;
   
   public BlipGenerator(Context context) {
	   loading = true;
	   playing = false;
	   mainContext = (BlipsMain)context;
	   scale = new int[7];
		
	   for (int i = 0; i < scale.length; i++) {
			scale[i] = mainContext.prefs.getInt("ScaleInterval" + i, BlipGenerator.minor[i]);
	   }
	   
	   rootIndex = mainContext.prefs.getInt("ScaleRoot", 0);
	   
	   initSounds();
   }
   
    /** Populate the SoundPool*/
   public void initSounds() {
	   initSelections();

	   if (soundPool == null) {
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
	      soundPool.load(mainContext, S14, 1);
	      soundPool.load(mainContext, S15, 1);
	      soundPool.load(mainContext, S16, 1);
	      soundPool.load(mainContext, S17, 1);
	      soundPool.load(mainContext, S18, 1);
	      soundPool.load(mainContext, S19, 1);
	      soundPool.load(mainContext, S20, 1);
	      soundPool.load(mainContext, S21, 1);
	      soundPool.load(mainContext, S22, 1);
	      soundPool.load(mainContext, S23, 1);
	      soundPool.load(mainContext, S24, 1);
	      soundPool.load(mainContext, S25, 1);
	   }
   }
   
   public void initSelections() {
	  loading = true;
	  
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
	   
	   // Timer, calls the inner run() every MILLI_DELAY+sliderValue interval
	   if (timer != null) {
		   timer.cancel();
	   }
	   
	   timer = new Timer();
	   timer.scheduleAtFixedRate (new TimerTask (){
	      public void run () {
	         handler.post (new Runnable () {
	            public void run () {
	            	// Start current index
	            	if (!(mainContext.resetting || loading || soundPool == null)) {
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
	            	   
	            	   //TODO: Implement playing/selected/off graphics
	            	   /*
	            	   
	            	   int lastIndex = (playingIndex == 0 ? BlipsMain.GRID_COLS : playingIndex) - 1;

	            	   for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
	            		   if (mainContext.cells[playingIndex][r].isOn()) {
	            			   mainContext.cells[playingIndex][r].setBackgroundColor(Color.GREEN);
	            		   }
	            		   
	            		   if (mainContext.cells[lastIndex][r].isOn()) {
	            			   mainContext.cells[lastIndex][r].setBackgroundColor(Color.GRAY);
	            		   }
	            	   }
	            	   */
	            		
	            		
	 	               // Increment index for next loop
	 	               if (++playingIndex >= BlipsMain.GRID_COLS) {
	 	            	   playingIndex = 0;
	 		           }
	            	}
	            }
	         });
	      }
	   }, 0, BlipsMain.MILLI_DELAY-BlipsMain.sliderValue);
   }
   
   public boolean changeScale(int[] newScale, int newRoot) {	   
	   // Don't do anything if nothing changed
	   if (newScale == scale && newRoot == rootIndex) {
		   return false;
	   }
	   
	   System.out.println("Changing scale");

	   
	   // Pause playback
      if (playing) {
	      soundPool.autoPause();
      }

	   loading = true;
	   
	   // Pass negative root to maintain current value
	   if (newRoot >= 0) {
		   System.out.println("Changed root note from " + rootIndex + " to " + newRoot);

		   rootIndex = newRoot;
	   }
	   
	   // Pass null scale to maintain current value
	   if (newScale != null) {
		   scale = newScale;
	   }
	  
	   // Drop old selected notes
	   selections = null;
	   initSelections();
	   
	   // Set each button's new label and sound index
		for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
			for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
				BlipCell btn = mainContext.cells[c][r];
				btn.resetIndex(); 
	        	
				if (btn.isOn()) {
	        		selections.get(c).add(btn.getIndex());
	        	}
			}
		}
		
		loading = false;
		playingIndex = 0;
		
		return true;
   }
}
