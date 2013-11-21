package maxx.test.blips;



import java.util.ArrayList;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BlipCell extends ToggleButton {
	// Layout and state vars
	private int column;
	private int row;
	private boolean active;
	private BlipGenerator generator = null;
	
	// User feedback vars
	private int soundIndex;
	private String name = null;

	public BlipCell(Context context) {
		super(context);
		
		column = 0;
		row = 0;
		active = false;
		
		System.out.println("Cell created with default constructor");
	}
	
	public BlipCell(Context context, BlipGenerator bg, int c, int r) {
		super(context);
	
		column = c;
		row = r;
		generator = bg;
		
		resetIndex();

		int heightOffset = BlipsMain.display.getRotation() == 0 ? 8 : 5;

		
		  // Init btn layout params
		LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(
													BlipsMain.widthPixels / (BlipsMain.GRID_COLS + 1), 
													BlipsMain.heightPixels / (BlipsMain.GRID_ROWS + heightOffset));
	 	setLayoutParams(btn_params);
	 	setIncludeFontPadding(false);
	 	setPadding(2, 2, 2, 2);
	 	
	 	setTextSize(12.0f);
	 	
	 	
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
		if (generator == null) {
			System.out.println("Getting new generator");
			generator = ((BlipsMain)getContext()).bg;
		}
		
		super.setChecked(isActive);

		// Set the new button state
		active = isActive;
		if (generator == null) {
			System.out.println("Whoops! Generator is NULL!!!");
			return;
		}
		
		if (isActive) {		
			System.out.println("Setting button name: " + name);
			setText(name);
			generator.selections.get(column).add(soundIndex);
	   		setBackgroundResource(R.drawable.ic_cell_on);

		
		 	if (!(generator.playing || ((BlipsMain)getContext()).resetting)) {
				// Play demo sound if not sequencing already
				generator.playSound(soundIndex);
			}
		} else {
			ArrayList<Integer> col = generator.selections.get(column);
			
			if (col.contains(soundIndex)) {
				System.out.println("Remove row " + row + " from col " + column);
		   		setBackgroundResource(R.drawable.ic_cell_off);

				for (int i = 0; i < col.size(); i++) {
					if (col.get(i) == soundIndex) {
						col.remove(i);
						break;
					}
				}
			}
		}

		System.out.println("Cell in column:" + column + " row:" + row + " set to " + isActive);		
	}
	
	public void resetIndex() {
		// SoundPool indexes at 1 (wtf???)
		soundIndex = generator.rootIndex + 1;
		
		// Sound index (0-12) is the sum of all scale intervals preceding note
		for (int i = 0; i < BlipsMain.GRID_ROWS - row - 1; i++) {
			soundIndex += generator.scale[i];
		}
		
		// Name is root note + sound index (modulo for wrap around) -1 for zero index
		name = BlipGenerator.noteNames[(soundIndex - 1) % 12];
		
		System.out.println("Reset: name: " + name + " soundIndex: " + soundIndex);
		
		if (active) {
			setText(name);
		}
	}
	
	public int getIndex() {
		return soundIndex;
	}
	
	public void setGen(BlipGenerator bg) {
		this.generator = bg;
	}
}
