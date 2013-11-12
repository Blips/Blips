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
	static final int GRID_SIZE = 4;
	static final int MILLI_DELAY = 500;
	
	// Playing index of sequence
	int playingIndex = 0;
	// Reference to every button in grid
	BlipCell[][] cells;
	// Boolean to tell whether we're paused or not
	boolean isPaused;
	
	// Save / Play / Clear Buttons
	Button loadsaveButton;
	Button clearButton;
	Button playButton;
	
	// Activity Result Code Variable
	static final int LOAD_SAVE_REQ_CODE = 1;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      
      // create the layout 
      initializeLayout();
      isPaused = false;
      // initialize listeners
      initListeners();
      // Start timer
      startSequence();
   }
   
	public void onPause() {
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		
		for (int c = 0; c < GRID_SIZE; c++) {
			for (int r = 0; r < GRID_SIZE; r++) {
				editor.putBoolean("ButtonState" + c + r, cells[c][r].isOn());
			}
		}
		
		editor.commit();
		
		super.onPause();
	}
   
   protected void initializeLayout() {
	   // Grid container
      LinearLayout container = (LinearLayout) findViewById(R.id.llContainer);
      // Grid cells
      cells = new BlipCell[GRID_SIZE][GRID_SIZE];
	 
      // Init row layout params
	  LinearLayout.LayoutParams row_params = new LinearLayout.LayoutParams(
				   LinearLayout.LayoutParams.MATCH_PARENT, 
				   LinearLayout.LayoutParams.MATCH_PARENT);
	  // Init btn layout params
	  LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(
		    	   LinearLayout.LayoutParams.WRAP_CONTENT, 
		    	   LinearLayout.LayoutParams.WRAP_CONTENT);

      for (int r = 0; r < GRID_SIZE; r++) {
    	 // Handle each row in grid
         LinearLayout row = new LinearLayout(this);
 	   	 row.setOrientation(LinearLayout.HORIZONTAL);
 		 row.setLayoutParams(row_params);

         for (int c = 0; c < GRID_SIZE; c++) {
        	 // Handle each column in row
        	 BlipCell btn = new BlipCell(this, c, r);
        	 btn.setCol(c);
        	 btn.setRow(r);
        	 
        	 btn.setTextOff("");
        	 btn.setTextOn("");
        	 btn.setText("");
        	 
        	 btn.setLayoutParams(btn_params);
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
            for (int r = 0; r<GRID_SIZE; r++) {
               for (int c = 0; c<GRID_SIZE; c++) {
                  cells[r][c].setChecked(data.getCharExtra("LoadCell"+r+c, '0') == '1');
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
            for(int row = 0; row<GRID_SIZE; row++) {
               for (int col = 0; col<GRID_SIZE; col++) {
                  i.putExtra("ButtonState" + row + col, cells[row][col].isOn());
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
      super.onDestroy();
      clearAll();
   }
   
   public void clearAll() {
      for(int i = 0; i<GRID_SIZE; i++) {
         for(int j = 0; j<GRID_SIZE; j++) {
            cells[i][j].setChecked(false);
         }
      }
   }
   
   public void togglePlay() {
      if (isPaused) {
         playButton.setText("Pause");
         isPaused = false;
      }
      else {
         isPaused = true;
         playButton.setText("Play");
         
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
	               if (++playingIndex >= GRID_SIZE) {
	                  playingIndex = 0;
	               }	                	
	            }
	         });
	      }
	   }, 0, MILLI_DELAY);
   }
}
