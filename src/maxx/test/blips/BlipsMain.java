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

	
	// Activity Result Code Variable
	static final int LOAD_SAVE_REQ_CODE = 1;
	
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
			
			for (int c = 0; c < GRID_COLS; c++) {
				for (int r = 0; r < GRID_ROWS; r++) {
		        	cells[c][r].setGen(bg); 
					cells[c][r].setChecked(prefs.getBoolean("ButtonState" + c + r, false));
				}
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
      
//      loadsaveButton = (Button)this.findViewById(R.id.loadsave_button);
      clearButton = (Button)this.findViewById(R.id.clear_button);
      playButton = (Button)this.findViewById(R.id.play_button);
      
      isStopped = !prefs.getBoolean("isStopped", true);
      togglePlay();
   }
   

   
   public void initListeners() {
//      this.loadsaveButton.setOnClickListener(new OnClickListener() {
//         public void onClick(View view)
//         {
//            Toast toast = Toast.makeText(BlipsMain.this, "Load or save...", Toast.LENGTH_SHORT);
//            toast.show();
//            Intent i = new Intent(BlipsMain.this, LoadSavePage.class);
//            for(int row = 0; row<GRID_ROWS; row++) {
//               for (int col = 0; col<GRID_COLS; col++) {
//                  i.putExtra("ButtonState" + col + row, cells[col][row].isOn());
//               }
//            }
//            startActivityForResult(i, LOAD_SAVE_REQ_CODE);
//         }
//      });
      
      
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
	   
	   switch (item.getItemId()) {
	   	   case R.id.menu_saveload:
	   		   handleSaveLoad();

	   		   ret = true;
	   		   break;
	   	   case R.id.subscale_major:
	   		   bg.changeScale(BlipGenerator.major, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_major);

	   		   ret = true;
	   		   break;
		   case R.id.subscale_minor:
		       bg.changeScale(BlipGenerator.minor, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_minor);

	           ret = true;
	   		   break;
		   case R.id.subscale_mixolydian:
		       bg.changeScale(BlipGenerator.mixolydian, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_mixolydian);
	
				ret = true;
		   		break;
		   case R.id.subscale_dorian:
		       bg.changeScale(BlipGenerator.dorian, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_dorian);
	
				ret = true;
		   		break;
		   case R.id.subscale_phrygian:
		       bg.changeScale(BlipGenerator.phrygian, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_phrygian);
	
				ret = true;
		   		break;
		   case R.id.subscale_lydian:
		       bg.changeScale(BlipGenerator.lydian, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_lydian);
	
				ret = true;
		   		break;
		   case R.id.subscale_locrian:
		       bg.changeScale(BlipGenerator.locrian, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_locrian);
	
				ret = true;
		   		break;
		   case R.id.subscale_harmonic:
			   bg.changeScale(BlipGenerator.harmonic, bg.rootIndex);
	   		   scaleMenu.setTitle(R.string.scale_harmonic);
			   
			   ret = true;
		   		break;
		   case R.id.subroot_c:
				//TODO add code to goto function
		   	Toast t8 = Toast.makeText(this, "Root changed to C", Toast.LENGTH_SHORT);
			   t8.show();
				ret = true;
		   		break;
		   case R.id.subroot_db:
				//TODO add code to goto function
		   	Toast t9 = Toast.makeText(this, "Root changed to Db", Toast.LENGTH_SHORT);
			   t9.show();
				ret = true;
		   		break;
		   case R.id.subroot_d:
				//TODO add code to goto function
		   	Toast t10 = Toast.makeText(this, "Root changed to D", Toast.LENGTH_SHORT);
			   t10.show();
				ret = true;
		   		break;
		   case R.id.subroot_eb:
				//TODO add code to goto function
		   	Toast t11 = Toast.makeText(this, "Root changed to Eb", Toast.LENGTH_SHORT);
			   t11.show();
				ret = true;
		   		break;
		   case R.id.subroot_e:
				//TODO add code to goto function
		   	Toast t12 = Toast.makeText(this, "Root changed to E", Toast.LENGTH_SHORT);
			   t12.show();
				ret = true;
		   		break;
		   case R.id.subroot_f:
				//TODO add code to goto function
		   	Toast t13 = Toast.makeText(this, "Root changed to F", Toast.LENGTH_SHORT);
			   t13.show();
				ret = true;
		   		break;
		   case R.id.subroot_gb:
				//TODO add code to goto function
		   	Toast t14 = Toast.makeText(this, "Root changed to Gb", Toast.LENGTH_SHORT);
			   t14.show();
				ret = true;
		   		break;
		   case R.id.subroot_g:
				//TODO add code to goto function
		   	Toast t15 = Toast.makeText(this, "Root changed to G", Toast.LENGTH_SHORT);
			   t15.show();
				ret = true;
		   		break;
		   case R.id.subroot_ab:
				//TODO add code to goto function
		   	Toast t16 = Toast.makeText(this, "Root changed to Ab", Toast.LENGTH_SHORT);
			   t16.show();
				ret = true;
		   		break;
		   case R.id.subroot_a:
				//TODO add code to goto function
		   	Toast t17 = Toast.makeText(this, "Root changed to A", Toast.LENGTH_SHORT);
			   t17.show();
				ret = true;
		   		break;
		   case R.id.subroot_bb:
				//TODO add code to goto function
		   	Toast t18 = Toast.makeText(this, "Root changed to Bb", Toast.LENGTH_SHORT);
			   t18.show();
				ret = true;
		   		break;
		   case R.id.subroot_b:
				//TODO add code to goto function
		   	Toast t19 = Toast.makeText(this, "Root changed to B", Toast.LENGTH_SHORT);
			   t19.show();
				ret = true;
		   		break;
			
			
		   default:
				ret = super.onOptionsItemSelected(item);
		   		break;
	   }
	   
	   Editor edit = prefs.edit();
	   edit.putString("savedScale", scaleMenu.getTitle().toString());
	   edit.putString("savedRootNote", rootMenu.getTitle().toString());
	   edit.commit();
	   
	   return ret;
   }
   
   public void handleSaveLoad() {
	   	Toast t = Toast.makeText(this, "Load/Save", Toast.LENGTH_SHORT);
		   t.show();
        Intent i = new Intent(BlipsMain.this, LoadSavePage.class);
        for(int c = 0; c < GRID_COLS; c++) {
           for (int r = 0; r<GRID_ROWS; r++) {
              i.putExtra("ButtonState" + c + r, cells[c][r].isOn());
           }
        }
        startActivityForResult(i, LOAD_SAVE_REQ_CODE);
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  resetting = true;
      super.onActivityResult(requestCode, resultCode, data);
      switch(requestCode) {
	      case (LOAD_SAVE_REQ_CODE):
	         if(resultCode == Activity.RESULT_OK) {
	            for (int c = 0; c < GRID_COLS; c++) {
	               for (int r = 0; r < GRID_ROWS; r++) {
	                  cells[c][r].setChecked(data.getCharExtra("LoadCell" + c + r, '0') == '1');
	                  System.out.println("Setting " + cells[c][r].isOn() + " checked for cell " + c + r);
	               }
	            }
	         }
	      
	      	 break;
      }
      resetting = false;
   }
}
