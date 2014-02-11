package maxx.test.blips;

import java.text.DecimalFormat;
import java.util.ArrayList;

import maxx.test.blips.R;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class BlipsMain extends SherlockFragmentActivity {
	// Grid and timer constants
	static final int GRID_ROWS = 8;
	static final int GRID_COLS = 8;
	static final int MILLI_DELAY = 650;
	static final DecimalFormat formatter = new DecimalFormat("#.#");
	static int widthPixels = 0;
	static int heightPixels = 0;
	static int rotation = 0;
	static Display display;
	
	// Constants for instrument offsets
	static final int PIANO = 0;
	static final int GUITAR = 1;
	static final int CLARINET = 2;
	static final int TRUMPET = 3;
	static final int TROMBONE = 4;
	static final int SAXOPHONE = 5;
	protected static int currentInstrument = PIANO;
	
	// Reference to every button in grid
	BlipCell[][] cells = null;
	// Boolean to tell whether we're paused or not
	boolean isStopped = true;
	boolean destroyed;
	
	// Play / Clear Buttons
	Button clearButton;
	Button playButton;
	
	// Tempo Slider and slider value
	TextView tempoLabel;
	SeekBar tempoSlider;
	protected static int tsliderValue;
	
	// Swing Slider and slider value
	TextView swingLabel;
	SeekBar swingSlider;
	protected static int ssliderValue;
	
	// Store generator and other state variables
	ArrayList<BlipGenerator> bg = new ArrayList<BlipGenerator>();
	boolean resetting = true;

	
	// Activity Result Code Variable and tracker
	static final int LOAD_SAVE_REQ_CODE = 1;
	protected static boolean DATA_LOADED = false;
	
	// Menu for Changing Root note, Scale, and saving/loading
	protected static Menu mainMenu;
	protected static MenuItem scaleMenu;
	protected static MenuItem rootMenu;
	protected static MenuItem instMenu;
	
	SharedPreferences prefs = null;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_main);
      
      if (prefs == null) {
    	  prefs = getPreferences(MODE_PRIVATE);
      }
      
      resetting = true;
      isStopped = false;
      
      // Reset dimensions if unset or screen rotated
      if (heightPixels == 0 || widthPixels == 0 || rotation != display.getRotation()) {
    	  setScreenDimensions();
      }
      
      if (bg.isEmpty()) {
    	  bg.add(new BlipGenerator(this));
      }
      
      // Remove the App title from Action bar
      ActionBar ab = getSupportActionBar();
      ab.setDisplayShowTitleEnabled(false);
      ab.setDisplayShowHomeEnabled(false);

      // create the layout 
      initializeLayout();
      destroyed = true;
      
      // initialize listeners
      initListeners();
   }
   
   public void setScreenDimensions() {
	   WindowManager w = this.getWindowManager();
	   display = w.getDefaultDisplay();
	   DisplayMetrics metrics = new DisplayMetrics();
	   display.getMetrics(metrics);
	   
	   // since SDK_INT = 1;
	   widthPixels = metrics.widthPixels;
	   heightPixels = metrics.heightPixels;
	   rotation = display.getRotation();
	   
	   // includes window decorations (statusbar bar/menu bar)
	   if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
		   try {
		       widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
		       heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
		   } catch (Exception ignored) {
	   }
	   
	   // includes window decorations (statusbar bar/menu bar)
	   if (Build.VERSION.SDK_INT >= 17)
		   try {
		       Point realSize = new Point();
		       Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
		       widthPixels = realSize.x;
		       heightPixels = realSize.y;
		   } catch (Exception ignored) {
	   }
   }
   
	public void onPause() {
		bg.get(0).stop();
		super.onPause();
	}
	
	public void onResume() {
		System.out.println("On resume called");
		if (prefs == null) {
			prefs = getPreferences(MODE_PRIVATE);
		}
		resetting = true;

		if (bg.isEmpty()) {
	    	bg.add(new BlipGenerator(this));
	    }
    
		super.onResume();
		System.out.println("super on resume called");

		if (!destroyed) {
			isStopped = false;
			
			if(!DATA_LOADED) {
				for (int c = 0; c < GRID_COLS; c++) {
					for (int r = 0; r < GRID_ROWS; r++) {
						cells[c][r].setChecked(prefs.getBoolean("ButtonState" + c + r, false));
					}
				}
			}
			else {
				DATA_LOADED = false;
			}
			
			
			isStopped = !prefs.getBoolean("isStopped", true);
			togglePlay();
		}
		
		resetting = false;
		destroyed = false;
   }
   
   protected void initializeLayout() {
	   System.out.println("Init layout called");
	   // Grid container
      LinearLayout container = (LinearLayout) findViewById(R.id.llContainer);
      
      tsliderValue = prefs.getInt("SliderValue", 250);
      ssliderValue = prefs.getInt("SwingValue", 0);
      tempoLabel = (TextView) findViewById(R.id.tempoLabel);
	  tempoLabel.setText(formatter.format(60000.0 / (MILLI_DELAY - tsliderValue)) + " BPM");

	  cells = new BlipCell[GRID_COLS][GRID_ROWS];
          
      if (bg.isEmpty()) {
    	  bg.add(new BlipGenerator(this));
      }
	 
      // Init row layout params
	  LinearLayout.LayoutParams row_params = new LinearLayout.LayoutParams(
				   LinearLayout.LayoutParams.MATCH_PARENT, 
				   LinearLayout.LayoutParams.MATCH_PARENT);

      for (int r = 0; r < GRID_ROWS; r++) {
    	 // Handle each row in grid
         LinearLayout row = new LinearLayout(this);
 	   	 row.setOrientation(LinearLayout.HORIZONTAL);
 		 row.setLayoutParams(row_params);

         for (int c = 0; c < GRID_COLS; c++) {
        	 // Handle each column in row
        	 BlipCell btn = new BlipCell(this, c, r);
        	 btn.setCol(c);
        	 btn.setRow(r);
        	 btn.setBackgroundResource(R.drawable.ic_cell_off);
        	 btn.setTextOff("");
        	 btn.setTextOn("");
        	 btn.setText("");
        	 
        	 row.addView(btn);
        	 btn.setChecked(prefs.getBoolean("ButtonState" + c + r, false));
        	 
        	 cells[c][r] = btn;
         }
         
         container.addView(row);
      }
      
      // Initialize tempo slider and buttons
      tempoSlider = (SeekBar)this.findViewById(R.id.tempobar);
      tempoSlider.setMax(500);
      tempoSlider.setProgress(tsliderValue);
      swingSlider = (SeekBar)this.findViewById(R.id.swingbar);
      swingSlider.setMax(100);
      swingSlider.setProgress(ssliderValue);
      clearButton = (Button)this.findViewById(R.id.clear_button);
      playButton = (Button)this.findViewById(R.id.play_button);
      

      isStopped = !prefs.getBoolean("isStopped", true);
      togglePlay();
   }
   
   public void initListeners() {      
      
      this.clearButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view)
         {
            Toast toast = Toast.makeText(BlipsMain.this, "Clearing...", Toast.LENGTH_SHORT);
            toast.show();
            clearAll();
         }
      });
      
      this.playButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view)
         {
            togglePlay();
         }
      });
      
      this.tempoSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	 @Override
    	 public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    	 	 tsliderValue = progress;
    	 	 tempoLabel.setText(formatter.format(60000.0 / (MILLI_DELAY - tsliderValue)) + " BPM");
    	 	 swingSlider.setMax(550 - tsliderValue);
    	 }

		 @Override
		 public void onStartTrackingTouch(SeekBar seekBar) {
			 // Stop the sequencer. This allows the Timer to update its delay
			 if (!isStopped && bg.get(0).playing) {
				 bg.get(0).pause();
			 }
		 }

		 @Override
		 public void onStopTrackingTouch(SeekBar seekBar) {
			 if (!isStopped) {
				 bg.get(0).startSequence();
			 }
			 
			   Editor edit = prefs.edit();
			   edit.putInt("SliderValue", tsliderValue);
			   edit.commit();
		 }
      });
      
      this.swingSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              ssliderValue = progress;
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
              if (!isStopped && bg.get(0).playing) {
                  bg.get(0).pause();
              }
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
              if (!isStopped) {
                  bg.get(0).startSequence();
              }
              
                Editor edit = prefs.edit();
                edit.putInt("SwingValue", ssliderValue);
                edit.commit();
          }
       });
   }
   
   @Override
   public void onDestroy() {  
	  destroyed = true;
      super.onDestroy();
   } 
   
   public void clearAll() {
      for(int c = 0; c < GRID_COLS; c++) {
         for(int r = 0; r < GRID_ROWS; r++) {
            cells[c][r].setChecked(false);
         }
      } 
      
      bg.get(0).selections = null;
      bg.get(0).initSelections();
      bg.get(0).loading = false;
   }
   
   public void togglePlay() {      
	  if (isStopped) {
         playButton.setBackgroundResource(R.drawable.ic_pause);
         isStopped = false;
         bg.get(0).play();
      } else {
         isStopped = true;
         playButton.setBackgroundResource(R.drawable.ic_play);
         bg.get(0).pause();
      }
	  
		Editor editor = prefs.edit();
		editor.putBoolean("isStopped", isStopped);
		editor.commit();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
	   MenuInflater inflater = this.getSupportMenuInflater();
	   inflater.inflate(R.menu.main, menu);
	   
	   mainMenu = menu;
	   rootMenu = menu.findItem(R.id.menu_rootnote);
	   scaleMenu = menu.findItem(R.id.menu_scale);
	   instMenu = menu.findItem(R.id.menu_instrument);
	   
	   scaleMenu.setTitle(prefs.getString("savedScale", getString(R.string.scale_aeolian)));
	   rootMenu.setTitle(prefs.getString("savedRootNote", getString(R.string.root_a)));
	   currentInstrument = prefs.getInt("Instrument", PIANO);
	   if(currentInstrument == PIANO) {
		   instMenu.setIcon(R.drawable.ic_action_piano);
	   }
	   else if (currentInstrument == GUITAR) {
		   instMenu.setIcon(R.drawable.ic_action_guitar);
	   }
	   else if (currentInstrument == CLARINET) {
		   instMenu.setIcon(R.drawable.ic_action_clarinet);
	   }
	   else if (currentInstrument == TRUMPET) {
		   instMenu.setIcon(R.drawable.ic_trumpet);
	   }
	   else if (currentInstrument == TROMBONE) {
		   instMenu.setIcon(R.drawable.ic_action_trombone);
	   }
	   else if (currentInstrument == SAXOPHONE) {
		   instMenu.setIcon(R.drawable.ic_action_sax);
	   }
	   System.out.println("resumedScale: " + scaleMenu.getTitle().toString() + " resumedRootNote: " + rootMenu.getTitle().toString());
	   
	   return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
	   boolean ret = false;
	   int scaleIndex = -1;
	   int root = -1;
	   int instrument = -1;
	   
	   switch (item.getItemId()) {
	   	   case R.id.menu_saveload:
	   		   handleSaveLoad();

	   		   ret = true;
	   		   break;
	   	   case R.id.subscale_ionian:
	   		   scaleIndex = 0;
	   		   scaleMenu.setTitle(R.string.scale_ionian);

	   		   ret = true;
	   		   break;
		   case R.id.subscale_dorian:
	   		   scaleIndex = 1;
	   		   scaleMenu.setTitle(R.string.scale_dorian);
	
				ret = true;
		   		break;
		   case R.id.subscale_phrygian:
	   		   scaleIndex = 2;
	   		   scaleMenu.setTitle(R.string.scale_phrygian);
	
				ret = true;
		   		break;
		   case R.id.subscale_lydian:
                scaleIndex = 3;
                scaleMenu.setTitle(R.string.scale_lydian);
    
                ret = true;
                break;
		   case R.id.subscale_mixolydian:
	   		   scaleIndex = 4;
	   		   scaleMenu.setTitle(R.string.scale_mixolydian);
	
				ret = true;
		   		break;
	       case R.id.subscale_aeolian:
	            scaleIndex = 5;
	            scaleMenu.setTitle(R.string.scale_aeolian);

	            ret = true;
	            break;
           case R.id.subscale_locrian:
                scaleIndex = 6;
                scaleMenu.setTitle(R.string.scale_locrian);
    
                ret = true;
                break;
		   case R.id.subscale_harmonic:
	   		   scaleIndex = 7;
	   		   scaleMenu.setTitle(R.string.scale_harmonic);
			   
			   ret = true;
		   		break;
		   case R.id.subroot_c:
	   		    root = 0;
		   		rootMenu.setTitle(R.string.root_c);

				ret = true;
		   		break;
		   case R.id.subroot_db:
				root = 1;
				rootMenu.setTitle(R.string.root_db);

				ret = true;
		   		break;
		   case R.id.subroot_d:
				root = 2;
				rootMenu.setTitle(R.string.root_d);

				ret = true;
		   		break;
		   case R.id.subroot_eb:
				root = 3;
				rootMenu.setTitle(R.string.root_eb);

				ret = true;
		   		break;
		   case R.id.subroot_e:
				root = 4;
				rootMenu.setTitle(R.string.root_e);

				ret = true;
		   		break;
		   case R.id.subroot_f:
				root = 5;
				rootMenu.setTitle(R.string.root_f);

				ret = true;
		   		break;
		   case R.id.subroot_gb:
				root = 6;
				rootMenu.setTitle(R.string.root_gb);

				ret = true;
		   		break;
		   case R.id.subroot_g:
				root = 7;
				rootMenu.setTitle(R.string.root_g);

				ret = true;
		   		break;
		   case R.id.subroot_ab:
				root = 8;
				rootMenu.setTitle(R.string.root_ab);

				ret = true;
		   		break;
		   case R.id.subroot_a:
				root = 9;
				rootMenu.setTitle(R.string.root_a);

				ret = true;
		   		break;
		   case R.id.subroot_bb:
				root = 10;
				rootMenu.setTitle(R.string.root_bb);

				ret = true;
		   		break;
		   case R.id.subroot_b:
				root = 11;
				rootMenu.setTitle(R.string.root_b);

				ret = true;
		   		break;
		   case R.id.instrument_piano:
			   Toast.makeText(this, "Loading Piano", Toast.LENGTH_SHORT).show();
			   instrument = PIANO;
			   instMenu.setIcon(R.drawable.ic_action_piano);
			   break;
		   case R.id.instrument_guitar:
			   Toast.makeText(this, "Loading Guitar", Toast.LENGTH_SHORT).show();
			   instrument = GUITAR;
			   instMenu.setIcon(R.drawable.ic_action_guitar);
			   break;
		   case R.id.instrument_trumpet:
			   Toast.makeText(this, "Loading Trumpet", Toast.LENGTH_SHORT).show();
			   instrument = TRUMPET;
			   instMenu.setIcon(R.drawable.ic_trumpet);
			   break;
		   case R.id.instrument_trombone:
			   Toast.makeText(this, "Loading Trombone", Toast.LENGTH_SHORT).show();
			   instrument = TROMBONE;
			   instMenu.setIcon(R.drawable.ic_action_trombone);
			   break;
		   case R.id.instrument_clarinet:
			   Toast.makeText(this, "Loading Clarinet", Toast.LENGTH_SHORT).show();
			   instrument = CLARINET;
			   instMenu.setIcon(R.drawable.ic_action_clarinet);
			   break;
		   case R.id.instrument_sax:
			   Toast.makeText(this,  "Loading Sax", Toast.LENGTH_SHORT).show();
			   instrument = SAXOPHONE;
			   instMenu.setIcon(R.drawable.ic_action_sax);
			   break;
			
		   default:
				ret = super.onOptionsItemSelected(item);
		   		break;
	   }
	   
	   if (bg.get(0).changeScale(scaleIndex, root, instrument)) {
		   // Only save preferences if something changed
		   Editor edit = prefs.edit();
		   edit.putInt("ScaleRoot", bg.get(0).rootIndex);
		   edit.putInt("ScaleIndex", bg.get(0).scaleIndex);
		   edit.putInt("Instrument", bg.get(0).instrumentOffset);
		   edit.putString("savedScale", bg.get(0).scaleName);
		   edit.putString("savedRootNote", BlipGenerator.noteNamesFlat[bg.get(0).rootIndex]);
		   edit.commit();
	   }
	   
	   return ret;
   }
   
   public void handleSaveLoad() {
        Intent i = new Intent(BlipsMain.this, LoadSavePage.class);
        for(int c = 0; c < GRID_COLS; c++) {
           for (int r = 0; r < GRID_ROWS; r++) {
              i.putExtra("ButtonState" + c + r, cells[c][r].isOn());
           }
        }
        
		i.putExtra("ScaleIndex", bg.get(0).scaleIndex);
		i.putExtra("ScaleRoot", bg.get(0).rootIndex);
		i.putExtra("Instrument", bg.get(0).instrumentOffset);
		i.putExtra("SliderValue", tsliderValue);
		i.putExtra("SwingValue", ssliderValue);
		
        startActivityForResult(i, LOAD_SAVE_REQ_CODE);
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  resetting = true;
	  
      super.onActivityResult(requestCode, resultCode, data);
      switch(requestCode) {
	      case (LOAD_SAVE_REQ_CODE):
	         if(resultCode == SherlockFragmentActivity.RESULT_OK) {
	        	Editor edit = prefs.edit();
	         	DATA_LOADED = true;
		      	
	         	if (bg.isEmpty()) {
	         		bg.add(new BlipGenerator(this));
	         	}
	           
	         	boolean active = false;

	    		for (int c = 0; c < GRID_COLS; c++) {
	                for (int r = 0; r < GRID_ROWS; r++) {
	                   active = data.getBooleanExtra("LoadCell" + c + r, prefs.getBoolean("ButtonState" + c + r, false));
	                   cells[c][r].setChecked(active);
	                   edit.putBoolean("ButtonState" + c + r, active);
	                }
	            }	  
	    		    		
	    		// Grab second digit of root first
	    		int root = data.getIntExtra("LoadRoot1", 9);
	    		
	    		// If first digit is not 0, we need to handle the offset
	    		if (data.getIntExtra("LoadRoot0", 0) == 1) {
	    			root += 10;
	    		}
	    		// Grab the instrument and set the Menu Icon to it
	    		int instrument = data.getIntExtra("LoadInstrument", 0);
	    		if(instrument == PIANO) {
	    			instMenu.setIcon(R.drawable.ic_action_piano);
	    		}
	    		else if(instrument == GUITAR) {
	    			instMenu.setIcon(R.drawable.ic_action_guitar);
	    		}
	    		else if(instrument == TRUMPET) {
	    			instMenu.setIcon(R.drawable.ic_trumpet);
	    		}
	    		else if(instrument == CLARINET) {
	    			instMenu.setIcon(R.drawable.ic_action_clarinet);
	    		}
	    		else if(instrument == TROMBONE) {
	    			instMenu.setIcon(R.drawable.ic_action_piano);
	    		}
	    		
	    		
	    		// Handle each digit of slider value individually
	    		int slider0 = data.getIntExtra("LoadSlider0", 2);
	    		int slider1 = data.getIntExtra("LoadSlider1", 5);
	    		tsliderValue = data.getIntExtra("LoadSlider2", 0);
	    		currentInstrument = data.getIntExtra("LoadInstrument", 0);
	    		if(currentInstrument == PIANO) {
	    			instMenu.setIcon(R.drawable.ic_action_piano);
	    		}
	    		else if (currentInstrument == GUITAR) {
	    			instMenu.setIcon(R.drawable.ic_action_guitar);
	    		}
	    		else if (currentInstrument == CLARINET) {
	    		   instMenu.setIcon(R.drawable.ic_action_clarinet);
	    		}
	    		else if (currentInstrument == TRUMPET) {
	    		   instMenu.setIcon(R.drawable.ic_trumpet);
	    		}
	    		else if (currentInstrument == TROMBONE) {
	    		   instMenu.setIcon(R.drawable.ic_action_trombone);
	    		}
	    		else if (currentInstrument == SAXOPHONE) {
	    			instMenu.setIcon(R.drawable.ic_action_sax);
	    		}
	    		
	    		tsliderValue += 100 * slider0 + 10 * slider1;
	    		edit.putInt("SliderValue", tsliderValue);
	    		tempoSlider.setProgress(tsliderValue);    		
	    		bg.get(0).changeScale(data.getIntExtra("LoadScaleIndex", 1), 
	    				       root, 
	    				       data.getIntExtra("LoadInstrument", 0));

	    		edit.putString("savedScale", bg.get(0).scaleName);
	    		scaleMenu.setTitle(bg.get(0).scaleName);
	    		
	    		edit.putString("savedRootNote", BlipGenerator.noteNamesFlat[bg.get(0).rootIndex]);
	    		rootMenu.setTitle(BlipGenerator.noteNamesFlat[bg.get(0).rootIndex]);
	    		
				edit.putInt("ScaleRoot", bg.get(0).rootIndex);
				edit.putInt("ScaleIndex", bg.get(0).scaleIndex);
				edit.putInt("Instrument", bg.get(0).instrumentOffset);

	    		edit.commit();
	    		
	    		bg.get(0).play();    
	         }
	      
	      	 break;
      }
     
      resetting = false;
   }
}
