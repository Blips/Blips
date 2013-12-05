package maxx.test.blips;



import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BlipCell extends ToggleButton {
	// Layout and state vars
	private int column = -1;
	private int row = -1;
	private boolean active;
	private BlipsMain mainContext;
	
	// User feedback vars
	private int soundIndex;
	private String name = null;

	public BlipCell(Context context) {
		super(context);
	}
	
	public BlipCell(Context context, int c, int r) {
		super(context);
		
		mainContext = (BlipsMain)context;
		column = c;
		row = r;
		
		resetIndex();

		int heightOffset = BlipsMain.display.getRotation() == 0 ? 8 : 5;

		
		  // Init btn layout params
		LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(
													BlipsMain.widthPixels / (BlipsMain.GRID_COLS + 1), 
													BlipsMain.heightPixels / (BlipsMain.GRID_ROWS + heightOffset));
	 	setLayoutParams(btn_params);
	 	setIncludeFontPadding(false);
	 	setMaxLines(1);
	 	setPadding(2, 2, 2, 2);
	 	
	 	setTextSize(16.0f);	 	
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
		if (mainContext == null) {
			mainContext = (BlipsMain)this.getContext();
		}
		if (mainContext.bg == null) {
			System.out.println("Whoops! Generator is NULL!!!");
			return;
		}
		
		if (isActive) {		
		 	if (!(mainContext.bg.playing || mainContext.resetting)) {
				// Play demo sound if not sequencing already
				mainContext.bg.playSound(soundIndex);
			}
		 	
			setText(name);
			mainContext.bg.selections.get(column).add(soundIndex);
	   		setBackgroundResource(R.drawable.ic_cell_on);
		} else {
			ArrayList<Integer> col = mainContext.bg.selections.get(column);
			
	   		setBackgroundResource(R.drawable.ic_cell_off);

			if (col.contains(soundIndex)) {
				for (int i = 0; i < col.size(); i++) {
					if (col.get(i) == soundIndex) {
						col.remove(i);
						break;
					}
				}
			}
		}
		
		if (!mainContext.resetting) {
			Editor edit = mainContext.prefs.edit();
			edit.putBoolean("ButtonState" + column + row, active);
			edit.commit();
			System.out.println("Column " + column + " row " + row + " active " + active);		
		}		
	}
	
	public void resetIndex() {
		// SoundPool indexes at 1 (wtf???)
		soundIndex = mainContext.bg.rootIndex + 1;
		
		// Sound index (0-12) is the sum of all scale intervals preceding note
		for (int i = 0; i < BlipsMain.GRID_ROWS - row - 1; i++) {
			soundIndex += mainContext.bg.scale[i];
		}
		
		// Name is root note + sound index (modulo for wrap around) -1 for zero index
		name = BlipGenerator.noteNames[(soundIndex - 1) % 12];
		
		//System.out.println("Reset: name: " + name + " soundIndex: " + soundIndex);
		
		if (active) {
			setText(name);
		}
	}
	
	public int getIndex() {
		return soundIndex;
	}
}
