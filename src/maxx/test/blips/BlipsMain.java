package maxx.test.blips;



import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


public class BlipsMain extends Activity {
	// Grid and timer constants
	static final int GRID_ROWS = 8;
	static final int GRID_COLS = 8;
	static final int MILLI_DELAY = 500;
	
	// Reference to every button in grid
	BlipCell[][] cells;
	// Boolean to tell whether we're paused or not
	boolean isStopped = true;
	boolean destroyed;
	
	// Save / Play / Clear Buttons
	Button loadsaveButton;
	Button clearButton;
	Button playButton;
	
	BlipGenerator bg = null;
	boolean resetting = true;

	
	// Activity Result Code Variable
	static final int LOAD_SAVE_REQ_CODE = 1;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
	  bg = new BlipGenerator(this);
	  resetting = true;

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      isStopped = false;

      // create the layout 
      initializeLayout();
      destroyed = true;
      
      // initialize listeners
      initListeners();
   }
   
	public void onPause() {
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		
		for (int c = 0; c < GRID_COLS; c++) {
			for (int r = 0; r < GRID_ROWS; r++) {
				editor.putBoolean("ButtonState" + c + r, cells[c][r].isOn());
			}
		}
		
		editor.putBoolean("isStopped", isStopped);
		editor.commit();
	    bg.stop();
	    bg = null;

		super.onPause();
	}
	
	public void onResume() {	
	    if (bg == null) {
	    	bg = new BlipGenerator(this);
	    }
	    
		resetting = true;

	    
		super.onResume();

		if (!destroyed) {
			isStopped = false;
			
			for (int c = 0; c < GRID_COLS; c++) {
				for (int r = 0; r < GRID_ROWS; r++) {
		        	cells[c][r].setGen(bg); 
					cells[c][r].setChecked(getPreferences(MODE_PRIVATE).getBoolean("ButtonState" + c + r, false));
				}
			}
			
			isStopped = !getPreferences(MODE_PRIVATE).getBoolean("isStopped", true);
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
        	 
        	 btn.setChecked(getPreferences(MODE_PRIVATE).getBoolean("ButtonState" + c + r, false));
         }
         
         container.addView(row);
      }
      
      loadsaveButton = (Button)this.findViewById(R.id.loadsave_button);
      clearButton = (Button)this.findViewById(R.id.clear_button);
      playButton = (Button)this.findViewById(R.id.play_button);
      
      isStopped = !getPreferences(MODE_PRIVATE).getBoolean("isStopped", true);
      togglePlay();
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  resetting = true;
      super.onActivityResult(requestCode, resultCode, data);
      switch(requestCode) {
      case (LOAD_SAVE_REQ_CODE):
         if(resultCode == Activity.RESULT_OK) {
            for (int r = 0; r<GRID_ROWS; r++) {
               for (int c = 0; c<GRID_COLS; c++) {
                  cells[c][r].setChecked(data.getCharExtra("LoadCell"+c+r, '0') == '1');
               }
            }
         }
      break;
      }
      resetting = false;
   }
   
   public void initListeners() {
      this.loadsaveButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view)
         {
            Toast toast = Toast.makeText(BlipsMain.this, "Load or save...", Toast.LENGTH_SHORT);
            toast.show();
            Intent i = new Intent(BlipsMain.this, LoadSavePage.class);
            for(int row = 0; row<GRID_ROWS; row++) {
               for (int col = 0; col<GRID_COLS; col++) {
                  i.putExtra("ButtonState" + col + row, cells[col][row].isOn());
               }
            }
            startActivityForResult(i, LOAD_SAVE_REQ_CODE);
         }
      });
      
      
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
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
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
         bg.stop();
      }
   }
}