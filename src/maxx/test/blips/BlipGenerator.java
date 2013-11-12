package maxx.test.blips;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class BlipGenerator {
	   int sampleRate = 44100;
	   boolean playing;
	   Thread toneThread;
	   
	   public BlipGenerator() {
		   playing = false;
		   toneThread = null;
	   }
	   
	   public void play() {
	      toneThread = new Thread() {
	         public void run() {
	            // set process priority
	            setPriority(Thread.MAX_PRIORITY);
	            int buffsize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
	                  AudioFormat.ENCODING_PCM_16BIT);
	            // create an audio track object
	            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
	                                                   AudioFormat.CHANNEL_OUT_MONO, 
	                                                   AudioFormat.ENCODING_PCM_16BIT, 
	                                                   buffsize, 
	                                                   AudioTrack.MODE_STREAM);
	            short samples[] = new short[buffsize];
	            int amp = 10000;
	            double twopi = 8.*Math.atan(1.);
	            double fr = 440.f;
	            double ph = 0.0;
	            audioTrack.play();
	       
	            // synthesis loop
	            while (playing){
	               fr = 440;
	               
	               for (int i=0; i < buffsize; i++) { 
	                 samples[i] = (short) (amp*Math.sin(ph));
	                 ph += twopi*fr/sampleRate;
	               }
	               audioTrack.write(samples, 0, buffsize);
	            }
	            audioTrack.stop();
	            audioTrack.release();
	         }
	      };
	      
	      toneThread.start();
	   }
	   
	   public void stop() {
		   playing = false;
		   try {
			   toneThread.join();
		   } catch (InterruptedException e) {
			   System.out.println(e.getMessage());
		   }
	   }
}
