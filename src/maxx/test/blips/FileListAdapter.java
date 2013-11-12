package maxx.test.blips;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import maxx.test.blips.LoadSavePage;

public class FileListAdapter extends BaseAdapter{
   private File[] files;
   Context con;
   
   FileListAdapter (Context c, File[] files) {
      this.con = c;
      this.files = files;
   }
   
   @Override
   public int getCount() {
      return files.length;
   }

   @Override
   public Object getItem(int position) {
      return files[position];
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(final int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
         LayoutInflater vi = (LayoutInflater)con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         v = vi.inflate(R.layout.file_view, null);
      }
      
      TextView filenameView = (TextView)v.findViewById(R.id.fileTextView);
      
      File f = files[position];
      filenameView.setText(f.getName());
      
      Button load = (Button)v.findViewById(R.id.loadFileButton);
      Button delete = (Button)v.findViewById(R.id.deleteFileButton);
      
      load.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
           Log.d("LOADBUTTON", "Load clicked");
           ((LoadSavePage)con).load(((File)getItem(position)).getName());
         }
      });
      
      delete.setOnClickListener(new OnClickListener() {
         public void onClick(View v){
            Log.d("DELETEBUTTON", "delete clicked");
            ((LoadSavePage)con).delete(((File)getItem(position)).getName());
         }
      });
      
      return v;
   }
   
}
