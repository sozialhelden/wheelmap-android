package org.wheelmap.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.wheelmap.android.activity.PictureActivity;
import org.wheelmap.android.online.R;

import java.util.List;

/**
 * Created by SMF on 17/03/14.
 */
public class HorizontalImageAdapter extends BaseAdapter implements
        android.widget.AdapterView.OnItemClickListener {

    private Activity context;

    private List plotsImages;

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

    public void clear() {
        plotsImages.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.listview_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.iconHList);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressbar_photo);
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        holder.progressBar.setVisibility(View.VISIBLE);
        ImageLoader.getInstance().displayImage((String) plotsImages.get(position), holder.imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.progressBar.setVisibility(View.GONE);
            }
        });


        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }

    @Override
    public void onItemClick(android.widget.AdapterView<?> parent, View view, int position,
                            long id) {
        String url = (String) plotsImages.get(position);

        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_URL, url);
        context.startActivity(intent);
    }
}