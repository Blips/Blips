package maxx.test.blips;


import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
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
	static final int GRID_ROWS = 12;
	static final int GRID_COLS = 8;
	static final int MILLI_DELAY = 500;
	
	// Playing index of sequence
	int playingIndex = 0;
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

	
	// Activity Result Code Variable
	static final int LOAD_SAVE_REQ_CODE = 1;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
	  bg = new BlipGenerator(this);  

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      // create the layout 
      initializeLayout();
      isStopped = true;
      destroyed = true;
      
      // initialize listeners
      initListeners();
      // Start timer
      startSequence();
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
		clearAll();
	    bg.stop();
	    bg = null;

		super.onPause();
	}
	
	public void onResume() {	
	    if (bg == null) {
	    	bg = new BlipGenerator(this);
	    }
	    
		super.onResume();

		isStopped = getPreferences(MODE_PRIVATE).getBoolean("isStopped", true);

		if (!destroyed) {
			for (int c = 0; c < GRID_COLS; c++) {
				for (int r = 0; r < GRID_ROWS; r++) {
		        	cells[c][r].setGen(bg); 
					cells[c][r].setChecked(getPreferences(MODE_PRIVATE).getBoolean("ButtonState" + c + r, false));
				}
			}
		}
		
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
      playButton.setText("Pause");
   }
   
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
      for(int c = 0; c<GRID_COLS; c++) {
         for(int r = 0; r<GRID_ROWS; r++) {
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
   
   public void startSequence (){
	   final Handler handler = new Handler ();
	   
	   // Timer, calls the inner run() every MILLI_DELAY interval
	   Timer timer = new Timer();
	   timer.scheduleAtFixedRate (new TimerTask (){
	      public void run () {
	         handler.post (new Runnable () {
	            public void run () {
	               if (++playingIndex >= GRID_COLS) {
	                  playingIndex = 0;
	               }	                	
	            }
	         });
	      }
	   }, 0, MILLI_DELAY);
   }
}
