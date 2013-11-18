package maxx.test.blips;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class LoadSavePage extends Activity {
   // File name variable
   protected String saveName;
   
   // Edit Text 
   EditText saveEditText;
   
   // Button
   Button saveButton;
   
   // ListView of Saved Blips Files
   ListView saveListview;
   
   // Array of files
   File[] saveFiles;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.save_page);
      
      saveEditText = (EditText)this.findViewById(R.id.savedBlipName);
      saveButton = (Button)this.findViewById(R.id.savePageButton);
      saveListview = (ListView)findViewById(R.id.savedFilesList);
      
      File dir = getFilesDir();
      saveFiles = dir.listFiles();
      
      saveListview.setAdapter(new FileListAdapter(this, saveFiles));
      //initialize listeners on Button and EditText
      initListeners();
   }
   
   protected void initListeners() {
      this.saveButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view)
         {
            saveName = saveEditText.getText().toString();
            if (!saveName.isEmpty()) {
               saveEditText.setText("");
               save(saveName);
            }
         }
      });
      
      this.saveEditText.setOnKeyListener(new OnKeyListener()
      {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER))
            {
               saveName = saveEditText.getText().toString();
               if (!saveName.isEmpty()) {
                  saveEditText.setText("");
                  save(saveName);
               }
            }
            if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
            {
               InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
               imm.hideSoftInputFromWindow(saveEditText.getWindowToken(), 0);
               return true;
            }
            return false;
         }
      });
   }
   
   public void save(String filename) {
      String encodedSave = "";
      Intent i = getIntent();
      
      encodedSave += i.getIntExtra("ScaleRoot", 0);
      
      for (int x = 0; x < BlipGenerator.minor.length; x++) {
    	  encodedSave += i.getIntExtra("ScaleInterval" + x, BlipGenerator.minor[x]);
      }
      
      for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
         for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
            if (i.getBooleanExtra("ButtonState"+c+r, false)) {
               encodedSave += 1;
            } else {
               encodedSave += 0;
            }
         }
      }
      
      System.out.println("Saving encoded string: " + encodedSave);
      
      try { 
             // catches IOException below

             /* We have to use the openFileOutput()-method
             * the ActivityContext provides, to
             * protect your file from others and
             * This is done for security-reasons.
             * We chose MODE_WORLD_READABLE, because
             *  we have nothing to hide in our file */             
             FileOutputStream fOut = openFileOutput(filename, MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fOut); 

             // Write the string to the file
             osw.write(encodedSave);

             /* ensure that everything is
              * really written out and close */
             osw.flush();
             osw.close();

             //Reading the file back...

             /* We have to use the openFileInput()-method
              * the ActivityContext provides.
              * Again for security reasons with
              * openFileInput(...) */

          } catch (IOException ioe) 
            {ioe.getMessage();}
   }
   
   void load(String filename) {
      try {
          FileInputStream fIn = openFileInput(filename);
          InputStreamReader isr = new InputStreamReader(fIn);

          /* Prepare a char-Array that will
           * hold the chars we read back in. */
           char[] inputBuffer = new char[BlipsMain.GRID_COLS * BlipsMain.GRID_ROWS + BlipGenerator.minor.length + 1];

           // Fill the Buffer with data from the file
           isr.read(inputBuffer);
           System.out.println("Loading encoded string ");
           for (int i = 0; i < inputBuffer.length; i++ ) {
        	   System.out.println(" " + inputBuffer[i]);
           }
           
           /** Create a new intent to send back to parent Activity */
           Intent resI = new Intent();
           
           int i = 0;
           
           resI.putExtra("LoadRoot", Character.getNumericValue(inputBuffer[i++]));
           System.out.println("Loaded root " + inputBuffer[i - 1]);
           
           for (int x = 0; x < BlipGenerator.minor.length; x++) {
         	  resI.putExtra("ScaleInterval" + x, Character.getNumericValue(inputBuffer[i++]));
         	  System.out.println("Loaded interval " + x + " value: " + inputBuffer[i - 1]);
           }

           for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
              for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
                 resI.putExtra("LoadCell"+c+r, inputBuffer[i++]);
                 Log.d("Loading file " + filename, "FILE - Row: " + r + " Col: " + c + " Value: " + inputBuffer[i-1]);
              }
           }
         
           setResult(Activity.RESULT_OK, resI);
           finish();
           
      } catch (IOException e){
         System.out.println(e.getMessage());
      }
   }
   
   void delete(String filename) {
      this.deleteFile(filename);
      ((BaseAdapter) saveListview.getAdapter()).notifyDataSetChanged();
   }
}
