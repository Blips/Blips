package maxx.test.blips;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseIntArray;

public class BlipGenerator {
   boolean playing;
   Thread toneThread;
   Context mainContext = null;
    
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
	  
    int sampleRate = 44100;
    
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
		   toneThread = null;
	   }
	   
	   public BlipGenerator(Context context) {
		   playing = false;
		   toneThread = null;
		   mainContext = context;
		   initSounds();
	   }
	   
	   public void play() {
	      toneThread = new Thread() {
	         public void run() {
	        	 try {
	        		 mixSound(mainContext);
	        	 } catch (IOException e) {
	        		 System.out.println(e.getMessage());
	        	 }
	         }
	      };
	      
	      toneThread.start();
	   }
	   
	   private void mixSound(Context context) throws IOException {
		    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, 44100, AudioTrack.MODE_STREAM);
	
		    InputStream in1=context.getResources().openRawResource(R.raw.a5);      
		    InputStream in2=context.getResources().openRawResource(R.raw.c5);
	
		    byte[] music1 = null;
		    music1= new byte[in1.available()]; 
		    music1=convertStreamToByteArray(in1);
		    in1.close();
	
	
		    byte[] music2 = null;
		    music2= new byte[in2.available()]; 
		    music2=convertStreamToByteArray(in2);
		    in2.close();
	
		    byte[] output = new byte[music1.length];
	
		    audioTrack.play();
	
		    for(int i=0; i < output.length; i++) {
			    float samplef1 = music1[i] / 128.0f;      //     2^7=128
			//    float samplef2 = music2[i] / 128.0f;
		
		
			    float mixed = samplef1;// + samplef2;
		
			    // reduce the volume a bit:
			    mixed *= 0.6;
			    // hard clipping
			    if (mixed > 1.0f) mixed = 1.0f;
		
		        if (mixed < -1.0f) mixed = -1.0f;
		    
		        byte outputSample = (byte)(mixed * 128.0f);
		        output[i] = outputSample;
		    } 
		    audioTrack.write(output, 0, 20);
	   }
	   
	   public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
	
	
	
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    byte[] buff = new byte[10240];
		    int i = Integer.MAX_VALUE;
		    while ((i = is.read(buff, 0, buff.length)) > 0) {
		        baos.write(buff, 0, i);
		    }
	
		    return baos.toByteArray(); // be sure to close InputStream in calling function
	
		}
	   
	   public void stop() {
		   playing = false;
		  
		   if (toneThread != null) {
			   try {
				   toneThread.join();
			   } catch (InterruptedException e) {
				   System.out.println(e.getMessage());
			   }
		   }
		   
		   toneThread = null;
	   }
}
