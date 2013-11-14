package maxx.test.blips;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseIntArray;

public class BlipGenerator {
   boolean playing;
   Context mainContext = null;
   int[] scale = null;
   
   // Note name array
   static final String[] notes = {"A5", "Bb5", "B5", "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab6"};
   
   // Scale interval arrays
   static final int[] major = {2, 1, 2, 2, 1, 2, 2};
   static final int[] minor = {2, 2, 1, 2, 2, 2, 1};
    
    static final int S1 = R.raw.a5;
    static final int S2 = R.raw.bb5;
    static final int S3 = R.raw.b5;
    static final int S4 = R.raw.c5;
    static final int S5 = R.raw.db5;
    static final int S6 = R.raw.d5;
    static final int S7 = R.raw.eb5;
    static final int S8 = R.raw.e5;
    static final int S9 = R.raw.f5;
    static final int S10 = R.raw.gb5;
    static final int S11 = R.raw.g5;
    static final int S12 = R.raw.ab6;
	      
    private static SoundPool soundPool;
    private static SparseIntArray soundPoolMap;

    /** Populate the SoundPool*/
    public void initSounds() {
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new SparseIntArray(12);  
        
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener(){

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				System.out.println("Load completed for " + sampleId + " Status: " + status);
			}});

        soundPoolMap.put( S1, soundPool.load(mainContext, S1, 1) );
        soundPoolMap.put( S2, soundPool.load(mainContext, S2, 1) );
        soundPoolMap.put( S3, soundPool.load(mainContext, S3, 1) );
        soundPoolMap.put( S1, soundPool.load(mainContext, S4, 1) );
        soundPoolMap.put( S2, soundPool.load(mainContext, S5, 1) );
        soundPoolMap.put( S3, soundPool.load(mainContext, S6, 1) );
        soundPoolMap.put( S1, soundPool.load(mainContext, S7, 1) );
        soundPoolMap.put( S2, soundPool.load(mainContext, S8, 1) );
        soundPoolMap.put( S3, soundPool.load(mainContext, S9, 1) );
        soundPoolMap.put( S1, soundPool.load(mainContext, S10, 1) );
        soundPoolMap.put( S2, soundPool.load(mainContext, S11, 1) );
        soundPoolMap.put( S3, soundPool.load(mainContext, S12, 1) );
    }
    
    /** Play a given sound in the soundPool */
    public void playSound(int soundID) {
	   if(soundPool == null || soundPoolMap == null || mainContext == null) {
		   System.out.println("Something is null");
	      initSounds();
	   }
	   
       float volume = 0.5f;
       System.out.println("Playing soundpool #" + soundID);

       // play sound with same right and left volume, with a priority of 1, 
       // zero repeats (i.e play once), and a playback rate of 1f
       soundPool.play(soundID, volume, volume, 1, 0, 1f);
       System.out.println("Played sound " + soundID);
    }
	   
	   public BlipGenerator() {
		   playing = false;
	   }
	   
	   public BlipGenerator(Context context) {
		   playing = false;
		   mainContext = context;
		   initSounds();
	   }
	   
	   public void play() {
		   playing = true;
	   }
	   
	   
	   public void stop() {
		   playing = false;		
	   }
}
