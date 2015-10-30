package org.wheelmap.android.adapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.wheelmap.android.activity.PictureActivity;
import org.wheelmap.android.online.R;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by SMF on 17/03/14.
 */
public class HorizontalImageAdapter extends BaseAdapter implements
        android.widget.AdapterView.OnItemClickListener{

    private Activity context;

    private static ImageView imageView;

    private List plotsImages;

    private static ViewHolder holder;
    private LayoutInflater l_Inflater;

    public HorizontalImageAdapter(Activity context, List plotsImages) {

        this.context = context;
        this.plotsImages = plotsImages;
        l_Inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return plotsImages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void clear(){
       plotsImages.clear();
       notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = l_Inflater.inflate(R.layout.listview_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.iconHList);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader.getInstance().displayImage((String)plotsImages.get(position),holder.imageView);


        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }

    @Override
    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position,
            long id) {
        String url =    (String)plotsImages.get(position);

        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_URL,url);
        context.startActivity(intent);
    }
}