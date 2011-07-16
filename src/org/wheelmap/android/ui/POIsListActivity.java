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

import java.net.URI;
import java.net.URISyntaxException;

import wheelmap.org.BoundingBox;
import wheelmap.org.BoundingBox.Wgs84GeoCoordinates;
import wheelmap.org.WheelMapException;
import wheelmap.org.WheelchairState;
import wheelmap.org.domain.node.Node;
import wheelmap.org.domain.node.Nodes;
import wheelmap.org.request.AcceptType;
import wheelmap.org.request.NodesRequestBuilder;
import wheelmap.org.request.Paging;
import wheelmap.org.request.RequestProcessor;
import wheelmap.org.util.XmlSupport;


public class POIsListActivity extends ListActivity {
	
	 private EfficientAdapter adap;
	  	
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
		    private Nodes nodes;
		    
		    private static final String SERVER = "staging.wheelmap.org";
			private static final String API_KEY = "9NryQWfDWgIebZIdqWiK";
			private static RequestProcessor requestProcessor = new RequestProcessor();


		    public EfficientAdapter(Context context) {
		      // Cache the LayoutInflate to avoid asking for a new one each time.
		      mInflater = LayoutInflater.from(context);
		      this.context = context;
		      
		      final NodesRequestBuilder requestBuilder = new NodesRequestBuilder(SERVER,
	    				API_KEY,AcceptType.XML);
	    		
	    		
	    		// 1. maxi nodes
	    		String getRequest = requestBuilder.
	    			paging(new Paging(5)).
	    			boundingBox(new BoundingBox(new Wgs84GeoCoordinates(13.37811,52.43752),new Wgs84GeoCoordinates(13.38278,52.43957))).
	    			wheelchairState(WheelchairState.UNKNOWN).buildRequestUri();
	    		
	    		System.out.println(getRequest);
	    		this.nodes = retrieveNumberOfHIts(getRequest);
	    		 
		    }
		    
		    private static Nodes retrieveNumberOfHIts(String getRequest) {
				String response=null;
				try {
					response = requestProcessor.get(new URI(getRequest),String.class);
				} catch (URISyntaxException e) {
					throw new WheelMapException(e);
				}
				System.out.println(response);

				return unmarshal(response,Nodes.class);
				//System.out.println();
			}

			public static <T> T unmarshal(final String xml, Class<T> clazz) {	
				return XmlSupport.serialize(xml, clazz);
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
		      Node node = this.nodes.getNodes().getNode().get(position);
		      // Bind the data efficiently with the holder.
		      holder.poiIcon.setImageBitmap(mIcon1);
		      
		      holder.poiName.setText(node.getName());
		      //holder.poiName.setText("namec " + String.valueOf(position));
		      holder.poiCategory.setText(node.getStreet() + node.getHousenumber().toString() +"," + node.getCity());
		      holder.poiDistance.setText("13" + String.valueOf(position) + " m");
		      
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
		      return nodes.getMeta().getItemCountTotal().intValue();
		    }

		    @Override
		    public Object getItem(int position) {
		      // TODO Auto-generated method stub
		      return nodes.getNode().get(position);
		    }

		  }

}
