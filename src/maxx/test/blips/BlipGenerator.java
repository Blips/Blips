package maxx.test.blips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.Resources;
//import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;

public class BlipGenerator {   
	// Static variables
   // Note name array
   // TODO: Why is this here?BlipsMain
   static String[] noteNames = {"C", 
	   							"D\u266D", "D", 
	   							"E\u266D", "E", 
	   							"F", 
	   							"G\u266D", "G", 
	   							"A\u266D", "A",
	   							"B\u266D", "B"};
   
   static final LinkedHashMap<String, int[]> scales = 
		    new LinkedHashMap<String, int[]>() {{
		        put("Major",          new int[] {2, 2, 1, 2, 2, 2, 1});
		        put("Minor",      	  new int[] {2, 1, 2, 2, 1, 2, 2});
		        put("Dorian",     	  new int[] {2, 1, 2, 2, 2, 1, 2});
		        put("Lydian",     	  new int[] {2, 2, 2, 1, 2, 2, 1});
		        put("Locrian",    	  new int[] {1, 2, 2, 1, 2, 2, 2});
		        put("Phrygian",   	  new int[] {1, 2, 2, 2, 1, 2, 2});
		        put("Mixolydian",     new int[] {2, 2, 1, 2, 2, 1, 2});
		        put("Harmonic Minor", new int[] {2, 1, 2, 2, 1, 3, 1});
		    }};
   
   
   // Scale interval arrays
   static final int[] major      = {2, 2, 1, 2, 2, 2, 1};
   static final int[] minor      = {2, 1, 2, 2, 1, 2, 2};
   static final int[] dorian     = {2, 1, 2, 2, 2, 1, 2};
   static final int[] lydian     = {2, 2, 2, 1, 2, 2, 1};
   static final int[] locrian    = {1, 2, 2, 1, 2, 2, 2};
   static final int[] phrygian   = {1, 2, 2, 2, 1, 2, 2};
   static final int[] harmonic   = {2, 1, 2, 2, 1, 3, 1};
   static final int[] mixolydian = {2, 2, 1, 2, 2, 1, 2};
   
   // Piano C5 - B6 inclusive
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
   
   /*
   // Guitar C5 - B6 inclusive
   static final int S25 = R.raw.guitarc5;
   static final int S26 = R.raw.guitardb5;
   */
	      
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
   String scaleName = null;
   int scaleIndex;
   
   // List of current selections for each column
   ArrayList<ArrayList<Integer>> selections = null;
   
   public BlipGenerator(Context context) {
	   loading = true;
	   playing = false;
	   mainContext = (BlipsMain)context;
	   
	   // Load root index (default A)
	   rootIndex = mainContext.prefs.getInt("ScaleRoot", 9);
	   
	   // Load scale info (default minor)
	   scaleIndex = mainContext.prefs.getInt("ScaleIndex", 1);
	   scaleName = (String)scales.keySet().toArray()[scaleIndex];
	   scale = (int[])scales.values().toArray()[scaleIndex];
	   
	   // Load sounds
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
   
   // Don't release sounds when pausing, just stop timer
   public void pause() {
	   if (timer != null) {
		   timer.cancel();
		   timer = null;
	   }
   }
   
   public void stop() {
	   pause();
	   release();
   }
   
   public void release() {
	   if (soundPool != null) {
		   soundPool.autoPause();
		   soundPool.release();
		   soundPool = null;
	   }
   }
   
   // Just restart timer
   public void startSequence (){
	   final Handler handler = new Handler ();
	   
	   // Timer, calls the inner run() every MILLI_DELAY-sliderValue interval
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
   
   public boolean changeScale(int newScale, int newRoot) {	   
	   // Don't do anything if nothing changed
	   if ((newScale == scaleIndex && newRoot == rootIndex) || (newScale < 0 && newRoot < 0)) {
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
	   if (newScale >= 0) {
		   scaleIndex = newScale;
		   scaleName = (String)scales.keySet().toArray()[scaleIndex];
		   scale = (int[])scales.values().toArray()[scaleIndex];
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
