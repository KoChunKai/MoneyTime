package money.time;

import java.util.ArrayList;
import money.time.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
 
public class CustomAdapter extends ArrayAdapter<Custom>{
    private ArrayList<Custom> entries;
    private Activity activity;
    private static int layout;
 
    public CustomAdapter(Activity a, int textViewResourceId, ArrayList<Custom> entries) {
        super(a, textViewResourceId, entries);
        this.entries = entries;
        this.activity = a;
        this.layout = textViewResourceId;
    }
 
    public static class ViewHolder{
        public TextView item1;
        public TextView item2;
        public TextView item3;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            LayoutInflater vi =
                (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layout, null);
            holder = new ViewHolder();
            holder.item1 = (TextView) v.findViewById(R.id.listtext);
            holder.item2 = (TextView) v.findViewById(R.id.listtext2);
            holder.item3 = (TextView) v.findViewById(R.id.datetext);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder)v.getTag();
 
        final Custom custom = entries.get(position);
        if (custom != null) {
            holder.item1.setText(custom.getcustomBig());
            holder.item2.setText(custom.getcustomSmall());
            holder.item3.setText(custom.getdateString());
            switch(layout){
            case R.layout.list_view_setting:
            	holder.item1.setTextColor(Color.RED);
                holder.item2.setTextColor(Color.GREEN);
            	break;
            case R.layout.details_layout:
            	if(holder.item2.getText().toString().contains("¦¬¤J")){
                    holder.item2.setTextColor(Color.GREEN);
            	}
            	else{
                    holder.item2.setTextColor(Color.RED);
            	}
            	break;
            }
        }
        return v;
    }
    
    
 
}
