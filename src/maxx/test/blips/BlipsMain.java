package maxx.test.blips;



import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
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
import android.widget.Toast;


public class BlipsMain extends SherlockFragmentActivity {
	// Grid and timer constants
	static final int GRID_ROWS = 8;
	static final int GRID_COLS = 8;
	static final int MILLI_DELAY = 500;
	static int widthPixels = 0;
	static int heightPixels = 0;
	static int rotation = 0;
	static Display display;
	
	// Reference to every button in grid
	BlipCell[][] cells;
	// Boolean to tell whether we're paused or not
	boolean isStopped = true;
	boolean destroyed;
	
	// Save / Play / Clear Buttons
//	Button loadsaveButton;   - now inside the actionbar
	Button clearButton;
	Button playButton;
	
	BlipGenerator bg = null;
	boolean resetting = true;

	
	// Activity Result Code Variable and tracker
	static final int LOAD_SAVE_REQ_CODE = 1;
	protected static boolean DATA_LOADED = false;
	
	// Menu for Changing Root note, Scale, and saving/loading
	protected static Menu mainMenu;
	protected static MenuItem scaleMenu;
	protected static MenuItem rootMenu;
	
	SharedPreferences prefs = null;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      if (prefs == null) {
    	  prefs = getPreferences(MODE_PRIVATE);
      }
      
      resetting = true;
	  bg = new BlipGenerator(this);
	  resetting = true;
      isStopped = false;
      
      // Reset dimensions if unset or screen rotated
      if (heightPixels == 0 || widthPixels == 0 || rotation != display.getRotation()) {
    	  setScreenDimensions();
      }

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
		Editor editor = prefs.edit();
		
		for (int c = 0; c < GRID_COLS; c++) {
			for (int r = 0; r < GRID_ROWS; r++) {
				editor.putBoolean("ButtonState" + c + r, cells[c][r].isOn());
			}
		}
		
		for (int i = 0; i < bg.scale.length; i++) {
			editor.putInt("ScaleInterval" + i, bg.scale[i]);
		}
		
		editor.putInt("ScaleRoot", bg.rootIndex);
		editor.putBoolean("isStopped", isStopped);
		
		editor.commit();
	  
		bg.stop();
	    bg = null;

		super.onPause();
	}
	
	public void onResume() {
		if (prefs == null) {
			prefs = getPreferences(MODE_PRIVATE);
		}
		resetting = true;

		if (bg == null) {
	    	bg = new BlipGenerator(this);
	    }
    
		super.onResume();

		if (!destroyed) {
			isStopped = false;
			if(!DATA_LOADED) {
				for (int c = 0; c < GRID_COLS; c++) {
					for (int r = 0; r < GRID_ROWS; r++) {
						cells[c][r].setGen(bg); 
						cells[c][r].setChecked(prefs.getBoolean("ButtonState" + c + r, false));
					}
				}
			}
			else {
				for (int c = 0; c < GRID_COLS; c++) {
					for (int r = 0; r < GRID_ROWS; r++) {
						cells[c][r].setGen(bg); 
					}
				}
				DATA_LOADED = false;
			}
			
			
			isStopped = !prefs.getBoolean("isStopped", true);
			togglePlay();
		}
		
		resetting = false;
		destroyed = false;
   }
   
   protected void initializeLayout() {
	   // Grid container
      LinearLayout container = (LinearLayout) findViewById(R.id.llContainer);
      // Grid cells
      cells = new BlipCell[GRID_COLS][GRID_ROWS];
    
      if (bg == null) {
    	  bg = new BlipGenerator(this);
      }
	 
      // Init row layout params
	  LinearLayout.LayoutParams row_params = new LinearLayout.LayoutParams(
				   LinearLayout.LayoutParams.MATCH_PARENT, 
				   LinearLayout.LayoutParams.MATCH_PARENT);

	  isStopped = false;

      for (int r = 0; r < GRID_ROWS; r++) {
    	 // Handle each row in grid
         LinearLayout row = new LinearLayout(this);
 	   	 row.setOrientation(LinearLayout.HORIZONTAL);
 		 row.setLayoutParams(row_params);

         for (int c = 0; c < GRID_COLS; c++) {
        	 // Handle each column in row
        	 BlipCell btn = new BlipCell(this, bg, c, r);
        	 btn.setCol(c);
        	 btn.setRow(r);
        	 
        	 btn.setTextOff("");
        	 btn.setTextOn("");
        	 btn.setText("");
        	 
        	 row.addView(btn);
        	 cells[c][r] = btn;
        	 
        	 btn.setChecked(prefs.getBoolean("ButtonState" + c + r, false));
         }
         
         container.addView(row);
      }
      
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
   }
   
   public void togglePlay() {      
	  if (isStopped) {
         playButton.setText("Pause");
         isStopped = false;
    	 bg.play();
      }
      else {
         isStopped = true;
         playButton.setText("Play");
         // Test change scale (it works)
         //bg.changeScale(bg.scale == BlipGenerator.major ? BlipGenerator.minor : BlipGenerator.major, 0);
         bg.stop();
      }
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
	   MenuInflater inflater = this.getSupportMenuInflater();
	   inflater.inflate(R.menu.main, menu);
	   
	   mainMenu = menu;
	   rootMenu = menu.findItem(R.id.menu_rootnote);
	   scaleMenu = menu.findItem(R.id.menu_scale);
	   
	   scaleMenu.setTitle(prefs.getString("savedScale", "Minor"));
	   rootMenu.setTitle(prefs.getString("savedRootNote", "A"));
	   System.out.println("resumedScale: " + scaleMenu.getTitle().toString() + " resumedRootNote: " + rootMenu.getTitle().toString());
	   
	   return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
	   boolean ret = false;
	   int[] scale = null;
	   int root = -1;
	   
	   switch (item.getItemId()) {
	   	   case R.id.menu_saveload:
	   		   handleSaveLoad();

	   		   ret = true;
	   		   break;
	   	   case R.id.subscale_major:
	   		   scale = BlipGenerator.major;
	   		   scaleMenu.setTitle(R.string.scale_major);

	   		   ret = true;
	   		   break;
		   case R.id.subscale_minor:
	   		   scale = BlipGenerator.minor;
	   		   scaleMenu.setTitle(R.string.scale_minor);

	           ret = true;
	   		   break;
		   case R.id.subscale_mixolydian:
	   		   scale = BlipGenerator.mixolydian;
	   		   scaleMenu.setTitle(R.string.scale_mixolydian);
	
				ret = true;
		   		break;
		   case R.id.subscale_dorian:
	   		   scale = BlipGenerator.dorian;
	   		   scaleMenu.setTitle(R.string.scale_dorian);
	
				ret = true;
		   		break;
		   case R.id.subscale_phrygian:
	   		   scale = BlipGenerator.phrygian;
	   		   scaleMenu.setTitle(R.string.scale_phrygian);
	
				ret = true;
		   		break;
		   case R.id.subscale_lydian:
	   		   scale = BlipGenerator.lydian;
	   		   scaleMenu.setTitle(R.string.scale_lydian);
	
				ret = true;
		   		break;
		   case R.id.subscale_locrian:
	   		   scale = BlipGenerator.locrian;
	   		   scaleMenu.setTitle(R.string.scale_locrian);
	
				ret = true;
		   		break;
		   case R.id.subscale_harmonic:
	   		   scale = BlipGenerator.harmonic;
	   		   scaleMenu.setTitle(R.string.scale_harmonic);
			   
			   ret = true;
		   		break;
		   case R.id.subroot_a:
	   		    root = 0;
		   		rootMenu.setTitle(R.string.root_a);

				ret = true;
		   		break;
		   case R.id.subroot_bb:
				root = 1;
				rootMenu.setTitle(R.string.root_bb);

				ret = true;
		   		break;
		   case R.id.subroot_b:
				root = 2;
				rootMenu.setTitle(R.string.root_b);

				ret = true;
		   		break;
		   case R.id.subroot_c:
				root = 3;
				rootMenu.setTitle(R.string.root_c);

				ret = true;
		   		break;
		   case R.id.subroot_db:
				root = 4;
				rootMenu.setTitle(R.string.root_db);

				ret = true;
		   		break;
		   case R.id.subroot_d:
				root = 5;
				rootMenu.setTitle(R.string.root_d);

				ret = true;
		   		break;
		   case R.id.subroot_eb:
				root = 6;
				rootMenu.setTitle(R.string.root_eb);

				ret = true;
		   		break;
		   case R.id.subroot_e:
				root = 7;
				rootMenu.setTitle(R.string.root_e);

				ret = true;
		   		break;
		   case R.id.subroot_f:
				root = 8;
				rootMenu.setTitle(R.string.root_f);

				ret = true;
		   		break;
		   case R.id.subroot_gb:
				root = 9;
				rootMenu.setTitle(R.string.root_gb);

				ret = true;
		   		break;
		   case R.id.subroot_g:
				root = 10;
				rootMenu.setTitle(R.string.root_g);

				ret = true;
		   		break;
		   case R.id.subroot_ab:
				root = 11;
				rootMenu.setTitle(R.string.root_ab);

				ret = true;
		   		break;
			
		   default:
				ret = super.onOptionsItemSelected(item);
		   		break;
	   }
	   
	   if (bg.changeScale(scale, root)) {
		   // Only save preferences if something changed
		   Editor edit = prefs.edit();
		   edit.putString("savedScale", scaleMenu.getTitle().toString());
		   edit.putString("savedRootNote", rootMenu.getTitle().toString());
		   edit.commit();
	   }
	   
	   return ret;
   }
   
   public void handleSaveLoad() {
	   	Toast t = Toast.makeText(this, "Load/Save", Toast.LENGTH_SHORT);
		   t.show();
        Intent i = new Intent(BlipsMain.this, LoadSavePage.class);
        for(int c = 0; c < GRID_COLS; c++) {
           for (int r = 0; r < GRID_ROWS; r++) {
              i.putExtra("ButtonState" + c + r, cells[c][r].isOn());
           }
        }
        
		for (int x = 0; x < bg.scale.length; x++) {
			i.putExtra("ScaleInterval" + x, bg.scale[x]);
		}
		
		i.putExtra("ScaleRoot", bg.rootIndex);
		
        startActivityForResult(i, LOAD_SAVE_REQ_CODE);
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  resetting = true;
	  
      super.onActivityResult(requestCode, resultCode, data);
      switch(requestCode) {
	      case (LOAD_SAVE_REQ_CODE):
	         if(resultCode == Activity.RESULT_OK) {
	        	Editor edit = prefs.edit();
	         	DATA_LOADED = true;
	         	int[] scale = new int[BlipGenerator.minor.length];
		      	
	         	if (bg == null) {
			    	bg = new BlipGenerator(this);
	         	}

	            for (int c = 0; c < GRID_COLS; c++) {
	               for (int r = 0; r < GRID_ROWS; r++) {
	                  cells[c][r].setChecked(data.getCharExtra("LoadCell" + c + r, '0') == '1');
	  				  edit.putBoolean("ButtonState" + c + r, cells[c][r].isOn());
	                  System.out.println("Setting " + cells[c][r].isOn() + " checked for cell " + c + r);
	               }
	            }
	            
	    		for (int x = 0; x < scale.length; x++) {
	    			scale[x] = data.getIntExtra("ScaleInterval" + x, BlipGenerator.minor[x]);
	    			edit.putInt("ScaleInterval" + x, bg.scale[x]);
	    		}
	    			    		
	    		if (bg.changeScale(scale, data.getIntExtra("LoadRoot", 0))) {
		    		for (int x = 0; x < scale.length; x++) {
		    			scale[x] = data.getIntExtra("ScaleInterval" + x, BlipGenerator.minor[x]);
		    			edit.putInt("ScaleInterval" + x, bg.scale[x]);
		    		}
		    		
		    		edit.putInt("ScaleRoot", bg.rootIndex);
	    		}
	            
	            
	            bg.play();
	            
	            
 	            edit.commit();
	         }
	      
	      	 break;
      }
     
      resetting = false;
   }
}
