package maxx.test.blips;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import maxx.test.blips.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
   
   // Helper text
   TextView saveHelperText;
   
   // Boolean to tell if we've saved a file already
   static boolean savedAlready = false;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.save_page);
      
      saveEditText = (EditText)this.findViewById(R.id.savedBlipName);
      saveButton = (Button)this.findViewById(R.id.savePageButton);
      saveListview = (ListView)findViewById(R.id.savedFilesList);
      saveHelperText = (TextView)findViewById(R.id.fileHelpText);
      
      File dir = getFilesDir();
      saveFiles = dir.listFiles();
      
      if (saveFiles.length > 0) {
    	  saveHelperText.setText(R.string.howToLoad_text);
      }
      
      saveListview.setAdapter(new FileListAdapter(this, saveFiles));
      //initialize listeners on Button and EditText
      initListeners();
   }
   
   protected void initListeners() {
      this.saveButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view)
         {
            saveName = saveEditText.getText().toString();
            
            // See if filename already exists and create pop-up if so
            for (File f : saveFiles) {
            	if (f.getName().equals(saveName)) {
            		AlertDialog.Builder	builder = new AlertDialog.Builder(LoadSavePage.this);
                    builder.setTitle("Overwrite Warning");
                    builder.setMessage("The filename entered already exists. Would you like to overwrite?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        	save(saveName);
                        	savedAlready = true;
                       }
                   });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            savedAlready = true;
                       }
                   });
                   builder.show(); 
                   break;
            	}
            }
            if (!saveName.isEmpty() && !savedAlready) {
               saveEditText.setText("");
               save(saveName);
               savedAlready = false;
            }
            else {
            	saveEditText.setText("");
            	savedAlready = false;
            }
         }
      });
   }
   
   public void save(String filename) {
	   // Save Data state in an encoded string
      String encodedSave = "";
      Intent i = getIntent();
      
      int rootIndex = i.getIntExtra("ScaleRoot", 9);
      
      // Pad single digit rootIndex with leading 0 for uniform length
      if (rootIndex < 10) {
    	  encodedSave += 0; 
      }
      
      
      // Single digit root index stored with leading 0, double digits handled
      encodedSave += rootIndex;
      
      int sliderVal = i.getIntExtra("SliderValue", 250);
      // Pad slider value
      if (sliderVal < 100) {
    	  encodedSave += 0;
      }
      
      if (sliderVal < 10) {
    	  encodedSave += 0;
      }
      
      encodedSave += sliderVal;
      
      encodedSave += i.getIntExtra("ScaleIndex", 1);
      encodedSave += i.getIntExtra("Instrument", 0);
      
      // Save each button's state according to row and column
      for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
         for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
            if (i.getBooleanExtra("ButtonState" + c + r, false)) {
               encodedSave += 1;
            } else {
               encodedSave += 0;
            }
         }
      }
            
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
             File dir = getFilesDir();
             saveFiles = dir.listFiles();
             if(saveFiles.length > 0) {
            	 saveHelperText.setText(R.string.howToLoad_text);
             }
             
             saveListview.setAdapter(new FileListAdapter(this, saveFiles));

          } catch (IOException ioe) 
            {ioe.getMessage();}
   }
   
   void load(String filename) {
      try {
          FileInputStream fIn = openFileInput(filename);
          InputStreamReader isr = new InputStreamReader(fIn);

          /* Prepare a char-Array that will
           * hold the chars we read back in. */
           char[] inputBuffer = new char[BlipsMain.GRID_COLS * BlipsMain.GRID_ROWS + 7];

           // Fill the Buffer with data from the file
           isr.read(inputBuffer);
           System.out.println("Loading encoded string ");
           for (int i = 0; i < inputBuffer.length; i++ ) {
        	   System.out.println(" " + inputBuffer[i]);
           }
           
           /** Create a new intent to send back to parent Activity */
           Intent resI = new Intent();
           
           int i = 0;
          
           // Root index stored as 2 digits, even if single digit number
           resI.putExtra("LoadRoot0", Character.getNumericValue(inputBuffer[i++]));
           resI.putExtra("LoadRoot1", Character.getNumericValue(inputBuffer[i++]));
           
           resI.putExtra("LoadSlider0", Character.getNumericValue(inputBuffer[i++]));
           resI.putExtra("LoadSlider1", Character.getNumericValue(inputBuffer[i++]));          
           resI.putExtra("LoadSlider2", Character.getNumericValue(inputBuffer[i++]));         
           
           resI.putExtra("LoadScaleIndex", Character.getNumericValue(inputBuffer[i++]));
           resI.putExtra("LoadInstrument", Character.getNumericValue(inputBuffer[i++]));

           for (int c = 0; c < BlipsMain.GRID_COLS; c++) {
              for (int r = 0; r < BlipsMain.GRID_ROWS; r++) {
                 resI.putExtra("LoadCell" + c + r, inputBuffer[i++] == '1');
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
      File dir = getFilesDir();
      saveFiles = dir.listFiles();
      if(saveFiles.length == 0) {
     	 saveHelperText.setText(R.string.noFiles_text);
      }
      
      saveListview.setAdapter(new FileListAdapter(this, saveFiles));
   }
}
