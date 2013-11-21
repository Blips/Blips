package maxx.test.blips;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
   static String[] noteNames = {"C", 
	   							"D\u266D", "D", 
	   							"E\u266D", "E", 
	   							"F", 
	   							"G\u266D", "G", 
	   							"A\u266D", "A",
	   							"B\u266D", "B"};
   
   static final LinkedHashMap<String, int[]> scales = 
		    new LinkedHashMap<String, int[]>() {/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			{
		        put("Major",          new int[] {2, 2, 1, 2, 2, 2, 1});
		        put("Minor",      	  new int[] {2, 1, 2, 2, 1, 2, 2});
		        put("Dorian",     	  new int[] {2, 1, 2, 2, 2, 1, 2});
		        put("Lydian",     	  new int[] {2, 2, 2, 1, 2, 2, 1});
		        put("Locrian",    	  new int[] {1, 2, 2, 1, 2, 2, 2});
		        put("Phrygian",   	  new int[] {1, 2, 2, 2, 1, 2, 2});
		        put("Mixolydian",     new int[] {2, 2, 1, 2, 2, 1, 2});
		        put("Harmonic Minor", new int[] {2, 1, 2, 2, 1, 3, 1});
		    }};
   
   // Piano C4 - B5 inclusive
   static final int S1 = R.raw.pianoc4;
   static final int S2 = R.raw.pianodb4;
   static final int S3 = R.raw.pianod4;
   static final int S4 = R.raw.pianoeb4;
   static final int S5 = R.raw.pianoe4;
   static final int S6 = R.raw.pianof4;
   static final int S7 = R.raw.pianogb4;
   static final int S8 = R.raw.pianog4;
   static final int S9 = R.raw.pianoab4;
   static final int S10 = R.raw.pianoa4;
   static final int S11 = R.raw.pianobb4;
   static final int S12 = R.raw.pianob4;
   static final int S13 = R.raw.pianoc5;
   static final int S14 = R.raw.pianodb5;
   static final int S15 = R.raw.pianod5;
   static final int S16 = R.raw.pianoeb5;
   static final int S17 = R.raw.pianoe5;
   static final int S18 = R.raw.pianof5;
   static final int S19 = R.raw.pianogb5;
   static final int S20 = R.raw.pianog5;
   static final int S21 = R.raw.pianoab5;
   static final int S22 = R.raw.pianoa5;
   static final int S23 = R.raw.pianobb5;
   static final int S24 = R.raw.pianob5;
   
   
   // Guitar C3 - B4 inclusive
   static final int S25 = R.raw.guitarc3;
   static final int S26 = R.raw.guitardb3;
   static final int S27 = R.raw.guitard3;
   static final int S28 = R.raw.guitareb3;
   static final int S29 = R.raw.guitare3;
   static final int S30 = R.raw.guitarf3;
   static final int S31 = R.raw.guitargb3;
   static final int S32 = R.raw.guitarg3;
   static final int S33 = R.raw.guitarab3;
   static final int S34 = R.raw.guitara3;
   static final int S35 = R.raw.guitarbb3;
   static final int S36 = R.raw.guitarb3;
   static final int S37 = R.raw.guitarc4;
   static final int S38 = R.raw.guitardb4;
   static final int S39 = R.raw.guitard4;
   static final int S40 = R.raw.guitareb4;
   static final int S41 = R.raw.guitare4;
   static final int S42 = R.raw.guitarf4;
   static final int S43 = R.raw.guitargb4;
   static final int S44 = R.raw.guitarg4;
   static final int S45 = R.raw.guitarab4;
   static final int S46 = R.raw.guitara4;
   static final int S47 = R.raw.guitarbb4;
   static final int S48 = R.raw.guitarb4;
   
   
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
	            
	            if (sampleId == 24) {
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
	   playing = false;
	   
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
	   
	   playing = true;
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
	            	   
	            	   /** This is pretty broken... Not sure how to fix it */
//	            	   int lastIndex = (playingIndex == 0 ? BlipsMain.GRID_COLS : playingIndex) - 1;
//
//	            	   for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
//	            		   if (mainContext.cells[playingIndex][r].isOn()) {
//	            			   mainContext.cells[playingIndex][r].setBackgroundResource(R.drawable.ic_matrix_play);
//	            		   }
//	            		   
//	            		   if (mainContext.cells[lastIndex][r].isOn()) {
//	            			   mainContext.cells[lastIndex][r].setBackgroundColor(R.drawable.cell);
//	            		   }
//	            	   }
	            		
	            		
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
	   
	   // Pause playback
      if (playing) {
	      soundPool.autoPause();
      }

	   loading = true;
	   
	   // Pass negative root to maintain current value
	   if (newRoot >= 0) {
		   System.out.println("Changed root note from " + noteNames[rootIndex] + " to " + noteNames[newRoot]);

		   rootIndex = newRoot;
	   }
	   
	   // Pass null scale to maintain current value
	   if (newScale >= 0) {
		   System.out.println("Changing scale from " + scale + " to " + newScale);

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
				btn.setGen(this);
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
