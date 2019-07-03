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

public class GameAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ImageFile> images;

    public GameAdapter(Context context, ArrayList<ImageFile> images) {
        this.context = context;
        this.images = images;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_image, viewGroup, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            imageView.setTag(getItem(i));
        }
        return view;
    }
}
