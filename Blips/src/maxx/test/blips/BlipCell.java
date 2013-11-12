package maxx.test.blips;



import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.widget.ToggleButton;

public class BlipCell extends ToggleButton {
	private int column;
	private int row;
	private boolean active;
	private Context mainActivity = null;
	private Thread t = null;

	int sampleRate = 44100;


	public BlipCell(Context context) {
		super(context);
		
		mainActivity = context;
		column = 0;
		row = 0;
		active = false;
		
		System.out.println("Cell created with default constructor");
	}
	
	public BlipCell(Context context, int c, int r) {
		super(context);
		mainActivity = context;
	
		column = c;
		row = r;
		
		System.out.println("Cell in column:" + c + " row:" + r + " created.");
	}
	
	public int getCol() {
		return column;
	}
	
	public void setCol(int c) {
		column = c;
	}
	
	public int getRow() {
		return row;
	}
	
	public void setRow(int r) {
		row = r;
	}
	
	public boolean isOn() {
		return active;
	}

	public void setChecked(boolean isActive) {
		super.setChecked(isActive);

		// Set the new button state
		active = isActive;
		
		if (active) {
			// Start player thread if button is selected
			play();
		}
		
		System.out.println("Cell in column:" + column + " row:" + row + " set to " + isActive);
	}
	
	public void play() {
		System.out.println("Cell in column:" + column + " row:" + row + " start playing");

	      t = new Thread() {
	         public void run() {
	            // set process priority
	            setPriority(Thread.MAX_PRIORITY);
	            int buffsize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
	                  AudioFormat.ENCODING_PCM_16BIT);
	            // create an audiotrack object
	            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
	                                                   AudioFormat.CHANNEL_OUT_MONO, 
	                                                   AudioFormat.ENCODING_PCM_16BIT, 
	                                                   buffsize, 
	                                                   AudioTrack.MODE_STREAM);
	            short samples[] = new short[buffsize];
	            int amp = 10000;
	            double twopi = 8.*Math.atan(1.);
	            double fr = 55 + 55 * (16 - row);
	            double ph = 0.0;
	        
	            audioTrack.play();
	         
	            // synthesis loop, run constantly while active
	            while(active) {	 
	            	// only play sound if column matches playing index of sequencer
	               if (((BlipsMain)mainActivity).playingIndex == column && !((BlipsMain)mainActivity).isPaused) {
	            	   for (int i=0; i < buffsize; i++){ 
	            		   samples[i] = (short) (amp * Math.sin(ph));
	            		   ph += twopi*fr/sampleRate;
	               	   }

	            	   if (((BlipsMain)mainActivity).playingIndex == column) {
	            		   audioTrack.write(samples, 0, buffsize);
	            	   }
	               }
	            }
	            
	            audioTrack.stop();
	            audioTrack.release();
	         }
	      };
	      
	      t.start();
	}
	
	public void stop() {

      if (t != null) {
	      try {
	         t.join();
	      } catch (InterruptedException e) {
	         e.printStackTrace();
	      }
	      
	      t = null;
      }
      
		System.out.println("Cell in column:" + column + " row:" + row + " stop playing");

		
	}
}
