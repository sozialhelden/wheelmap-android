package org.wheelmap.android.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class POIsListActivity extends ListActivity {
	
	 private EfficientAdapter adap;
	  private static String[] data = new String[] { "0", "1", "2", "3", "4" };

	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        adap = new EfficientAdapter(this);
        //ListView lv = (ListView) findViewById(R.id.pois_list_view);
        setListAdapter(adap);
    }
    
	public void onHomeClick(View v) {
		 final Intent intent = new Intent(this, WheelmapHomeActivity.class);
	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        this.startActivity(intent);
   }
	
	public void onRefreshClick(View v) {
		Toast.makeText(this, "Refreshing..", Toast.LENGTH_SHORT).show();
  }
	
	public void onSearchClick(View v) {
		Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
  }
	
	
	
	
	 public static class EfficientAdapter extends BaseAdapter implements Filterable {
		    private LayoutInflater mInflater;
		    private Bitmap mIcon1;
		    private Context context;

		    public EfficientAdapter(Context context) {
		      // Cache the LayoutInflate to avoid asking for a new one each time.
		      mInflater = LayoutInflater.from(context);
		      this.context = context;
		    }

		    /**
		     * Make a view to hold each row.
		     * 
		     * @see android.widget.ListAdapter#getView(int, android.view.View,
		     *      android.view.ViewGroup)
		     */
		    public View getView(final int position, View convertView, ViewGroup parent) {
		      // A ViewHolder keeps references to children views to avoid
		      // unneccessary calls
		      // to findViewById() on each row.
		      ViewHolder holder;

		      // When convertView is not null, we can reuse it directly, there is
		      // no need
		      // to reinflate it. We only inflate a new View when the convertView
		      // supplied
		      // by ListView is null.
		      if (convertView == null) {
		        convertView = mInflater.inflate(R.layout.adaptor_content, null);

		        // Creates a ViewHolder and store references to the two children
		        // views
		        // we want to bind data to.
		        holder = new ViewHolder();
		        holder.poiName = (TextView) convertView.findViewById(R.id.list_item_place_name);
		        holder.poiCategory = (TextView) convertView.findViewById(R.id.list_item_category);
		        holder.poiIcon = (ImageView) convertView.findViewById(R.id.place_type_icon);
		        holder.poiDistance = (TextView) convertView.findViewById(R.id.list_item_distance);
		        
		        
		        
		        convertView.setOnClickListener(new OnClickListener() {

		          @Override
		          public void onClick(View v) {
		        	  
		        	// Launch overall conference schedule
		        	  context.startActivity(new Intent(context, POIDetailActivity.class));
		          }
		        });		        		        
		        

		        convertView.setTag(holder);
		      } else {
		        // Get the ViewHolder back to get fast access to the TextView
		        // and the ImageView.
		        holder = (ViewHolder) convertView.getTag();
		      }

		      // Get flag name and id
		      String filename = "wheelmap_" + String.valueOf(position % 3);
		      int id = context.getResources().getIdentifier(filename, "drawable", "org.wheelmap.android.ui");

		      // Icons bound to the rows.
		      if (id != 0x0) {
		        mIcon1 = BitmapFactory.decodeResource(context.getResources(), id);
		      }

		      // Bind the data efficiently with the holder.
		      holder.poiIcon.setImageBitmap(mIcon1);
		      holder.poiName.setText("name " + String.valueOf(position));
		      holder.poiCategory.setText("Cafe " + String.valueOf(position));
		      holder.poiDistance.setText("12" + String.valueOf(position) + " m");
		      
		      return convertView;
		    }

		    static class ViewHolder {
		      TextView poiCategory;
		      TextView poiName;
		      TextView poiDistance;
		      ImageView poiIcon;
		    }

		    @Override
		    public Filter getFilter() {
		      // TODO Auto-generated method stub
		      return null;
		    }

		    @Override
		    public long getItemId(int position) {
		      // TODO Auto-generated method stub
		      return 0;
		    }

		    @Override
		    public int getCount() {
		      // TODO Auto-generated method stub
		      return data.length;
		    }

		    @Override
		    public Object getItem(int position) {
		      // TODO Auto-generated method stub
		      return data[position];
		    }

		  }

}
