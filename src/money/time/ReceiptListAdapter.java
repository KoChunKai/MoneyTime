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
 
public class ReceiptListAdapter extends ArrayAdapter<Custom>{
    private ArrayList<Custom> entries;
    private Context activity;
    private static int layout;
 
    public ReceiptListAdapter(Context a, int textViewResourceId, ArrayList<Custom> entries) {
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
            holder.item1 = (TextView) v.findViewById(R.id.shoppingdatetextView);
            holder.item2 = (TextView) v.findViewById(R.id.receiptnumtextView);
            holder.item3 = (TextView) v.findViewById(R.id.costtextView);
            v.setTag(holder);
        }
        else
            holder=(ViewHolder)v.getTag();
 
        final Custom custom = entries.get(position);
        if (custom != null) {
            holder.item1.setText(custom.getcustomBig());
            holder.item2.setText(custom.getcustomSmall());
            holder.item3.setText(custom.getdateString());
        }
        return v;
    }
    
    
 
}
