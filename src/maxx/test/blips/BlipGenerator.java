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
import android.widget.Toast;

public class BlipGenerator {   
	// Static variables

   // Unicode character for musical flat symbol
   static final String FLAT = "\u266D";
   
   // Note name array
   static String[] noteNames = {"C", 
	   							"D" + FLAT, "D", 
	   							"E" + FLAT, "E", 
	   							"F", 
	   							"G" + FLAT, "G", 
	   							"A" + FLAT, "A",
	   							"B" + FLAT, "B"};
   
   static final LinkedHashMap<String, int[]> scales = 
		    new LinkedHashMap<String, int[]>() {
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
   
   static final int[] RAW_SOUNDS = {R.raw.pianoc4, R.raw.pianodb4, R.raw.pianod4, 
	   								R.raw.pianoeb4, R.raw.pianoe4, R.raw.pianof4,
	   								R.raw.pianogb4, R.raw.pianog4, R.raw.pianoab4,
	   								R.raw.pianoa4, R.raw.pianobb4, R.raw.pianob4,
	   								R.raw.pianoc5, R.raw.pianodb5, R.raw.pianod5,
	   								R.raw.pianoeb5, R.raw.pianoe5, R.raw.pianof5,
	   								R.raw.pianogb5, R.raw.pianog5, R.raw.pianoab5,
	   								R.raw.pianoa5, R.raw.pianobb5, R.raw.pianob5,
	   								R.raw.guitarc3, R.raw.guitardb3, R.raw.guitard3,
	   								R.raw.guitareb3, R.raw.guitare3, R.raw.guitarf3,
	   								R.raw.guitargb3, R.raw.guitarg3, R.raw.guitarab3,
	   								R.raw.guitara3, R.raw.guitarbb3, R.raw.guitarb3,
	   								R.raw.guitarc4, R.raw.guitardb4, R.raw.guitard4,
	   								R.raw.guitareb4, R.raw.guitare4, R.raw.guitarf4,
	   								R.raw.guitargb4, R.raw.guitarg4, R.raw.guitarab4,
	   								R.raw.guitara4, R.raw.guitarbb4, R.raw.guitarb4,
	   								R.raw.clarinetc4, R.raw.clarinetdb4,R.raw.clarinetd4,
	   								R.raw.clarineteb4, R.raw.clarinete4,R.raw.clarinetf4, 
	   								R.raw.clarinetgb4, R.raw.clarinetg4,R.raw.clarinetab4,
	   								R.raw.clarineta4, R.raw.clarinetbb4,R.raw.clarinetb4, 
	   								R.raw.clarinetc5, R.raw.clarinetdb5,R.raw.clarinetd5, 
	   								R.raw.clarineteb5, R.raw.clarinete5,R.raw.clarinetf5,
	   								R.raw.clarinetgb5, R.raw.clarinetg5,R.raw.clarinetab5, 
	   								R.raw.clarineta5, R.raw.clarinetbb5,R.raw.clarinetb5, 
	   								R.raw.trumpetc4, R.raw.trumpetdb4,R.raw.trumpetd4, 
	   								R.raw.trumpeteb4,R.raw.trumpete4, R.raw.trumpetf4, 
	   								R.raw.trumpetgb4, R.raw.trumpetg4, R.raw.trumpetab4,
	   								R.raw.trumpeta4, R.raw.trumpetbb4, R.raw.trumpetb4, 
	   								R.raw.trumpetc5, R.raw.trumpetdb5, R.raw.trumpetd5,
	   								R.raw.trumpeteb5,R.raw.trumpete5,R.raw.trumpetf5,
	   								R.raw.trumpetgb5,R.raw.trumpetg5, R.raw.trumpetab5, 
	   								R.raw.trumpeta5, R.raw.trumpetbb5, R.raw.trumpetb5, 
	   								R.raw.trombonec3,R.raw.trombonedb3, R.raw.tromboned3, 
	   								R.raw.tromboneeb3,R.raw.trombonee3, R.raw.trombonef3,
	   								R.raw.trombonegb3,R.raw.tromboneg3, R.raw.tromboneab3, 
	   								R.raw.trombonea3,R.raw.trombonebb3,R.raw.tromboneb3, 
	   								R.raw.trombonec4, R.raw.trombonedb4, R.raw.tromboned4, 
	   								R.raw.tromboneeb4, R.raw.trombonee4, R.raw.trombonef4, 
	   								R.raw.trombonegb4, R.raw.tromboneg4,R.raw.tromboneab4, 
	   								R.raw.trombonea4, R.raw.trombonebb4, R.raw.tromboneb4};
   
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
   int instrumentOffset = 0;
   
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
	   instrumentOffset = mainContext.prefs.getInt("Instrument", 0);
	   
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
	            
	            // Turn off loading flag when last sample is loaded
	            if (sampleId == 24) {
	            	loading = false;
	 			    Toast.makeText(mainContext, "Sound Library Ready", Toast.LENGTH_SHORT).show();
	            }
	         }
	      });
	      
	      for (int i = 0; i < 24; i++) {
	    	  soundPool.load(mainContext, RAW_SOUNDS[instrumentOffset * 24 + i], 1);
	      }
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
	   
	   int lastIndex = (playingIndex == 0 ? BlipsMain.GRID_COLS : playingIndex) - 1;

	   for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
		   // Set all previous index cell images to "selected"
		   if (mainContext.cells[lastIndex][r].isOn()) {
	   		   mainContext.cells[lastIndex][r].setBackgroundResource(R.drawable.ic_cell_on);
		   }
	   }
	   
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
	            	   
	            	   // Update playing index in UI (Scroll column highlighting with playing index)
	            	   int lastIndex = (playingIndex == 0 ? BlipsMain.GRID_COLS : playingIndex) - 1;

	            	   for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
	            		   // Set all current index cell images to "play"
	            		   if (mainContext.cells[playingIndex][r].isOn()) {
	            			   mainContext.cells[playingIndex][r].setBackgroundResource(R.drawable.ic_cell_play);
	            		   }
	            		   
	            		   // Set all previous index cell images to "selected"
	            		   if (mainContext.cells[lastIndex][r].isOn()) {
        			   		   mainContext.cells[lastIndex][r].setBackgroundResource(R.drawable.ic_cell_on);
	            		   }
	            	   }
	            		
	            		
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
   
   public boolean changeScale(int newScale, int newRoot, int newInst) {	   
	   // Don't do anything if nothing changed
	   if ((newScale == scaleIndex || newScale < 0) && 
		   (newRoot == rootIndex || newRoot < 0) &&
		   (newInst == instrumentOffset || newInst < 0)) {
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
	   
	   // Pass negative scale to maintain current value
	   if (newScale >= 0) {
		   System.out.println("Changing scale from " + scale + " to " + newScale);

		   scaleIndex = newScale;
		   scaleName = (String)scales.keySet().toArray()[scaleIndex];
		   scale = (int[])scales.values().toArray()[scaleIndex];
	   }

	   BlipCell btn;

	   if (newScale >= 0 || newRoot >= 0) {
		   // Drop old selected notes
		   selections = null;
		   initSelections();
		   
		   // Set each button's new label and sound index
			for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
				for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
					btn = mainContext.cells[c][r];
					btn.resetIndex(); 
		        	
					if (btn.isOn()) {
		        		selections.get(c).add(btn.getIndex());
		    	   		btn.setBackgroundResource(R.drawable.ic_cell_on);
		        	}
				}
			}
	    } else {
	    	int lastIndex = (playingIndex == 0 ? BlipsMain.GRID_COLS : playingIndex) - 1;
	    	
	    	for (int i = 0; i < BlipsMain.GRID_ROWS; i++) {
	    		btn = mainContext.cells[lastIndex][i];
	    		
	    		if (btn.isOn()) {
	    			btn.setBackgroundResource(R.drawable.ic_cell_on);
	    		}
	    	}
	    }

		playingIndex = 0;

	    if (newInst >= 0) {
		   System.out.println("Changing instrument from offset " + instrumentOffset + " to " + newInst);
		   instrumentOffset = newInst;
		   // Set new instrument image here

		   release();
		   initSounds();
	    } else {
	    	loading = false;
	    }
			   		
		return true;
   }
}
