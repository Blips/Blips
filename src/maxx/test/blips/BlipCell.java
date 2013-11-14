package maxx.test.blips;



import java.util.ArrayList;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class BlipCell extends ToggleButton {
	private int column;
	private int row;
	private boolean active;
	private BlipGenerator generator = null;
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
		name = BlipGenerator.notes[BlipsMain.GRID_ROWS - row - 1];
		// Strip octave number off name for now
		name = name.substring(0, name.length() - 1);

		
		  // Init btn layout params
		LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(125, 120);
	 	setLayoutParams(btn_params);
		
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
		super.setChecked(isActive);

		// Set the new button state
		active = isActive;
		
		if (isActive && generator!= null) {
		 	setText(name);
		 	generator.selections.get(column).add(BlipsMain.GRID_ROWS - row);
		
		 	if (!generator.playing) {
				// Play demo sound if not sequencing already
				generator.playSound(BlipsMain.GRID_ROWS - row);
			}
		} else if (generator != null) {
			System.out.println("Remove row " + row + " from col " + column);

			ArrayList<Integer> col = generator.selections.get(column);
			
			if (col.contains(BlipsMain.GRID_ROWS - row)) {
				for (int i = 0; i < col.size(); i++) {
					if (col.get(i) == BlipsMain.GRID_ROWS - row) {
						col.remove(i);
					}
				}
			}
		} else {
			System.out.println("Generator is NULL!");
		}

		System.out.println("Cell in column:" + column + " row:" + row + " set to " + isActive);
	}
	
	public void setGen(BlipGenerator bg) {
		this.generator = bg;
	}
}
