package com.nus.memorygame.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nus.memorygame.R;
import com.nus.memorygame.model.ImageFile;

import java.util.ArrayList;

public class ImageListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ImageFile> images;

    public ImageListAdapter(Context context, ArrayList<ImageFile> images) {
        this.images = images;
        this.context = context;
    }

    @Override
    public int getCount() {
        return (images == null) ? 0 : images.size();
    }

    @Override
    public Object getItem(int i) {
        return (images == null) ? null : images.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item_image, null);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.image_view);
            final ImageView selected = (ImageView) convertView.findViewById(R.id.select);

            ImageFile image = (ImageFile) getItem(position);

            if(selected != null) {
                selected.setVisibility(image.isChecked() ? View.VISIBLE : View.GONE);
            }

            if(imageView != null) {
                imageView.setImageBitmap(image.getBitmap());
            }
        }
        return convertView;
    }

    public void updateItems(ArrayList<ImageFile> images) {
        this.images.clear();
        this.images.addAll(images);
        notifyDataSetChanged();
    }

}
