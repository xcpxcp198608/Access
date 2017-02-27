package com.wiatec.PX;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jude.rollviewpager.adapter.StaticPagerAdapter;
import com.squareup.picasso.Picasso;
import com.wiatec.update.R;

import java.util.List;

/**
 * Created by PX on 2016/9/19.
 */
public class RollViewAdapter extends StaticPagerAdapter {
    private List<RollViewInfo> list;

    public RollViewAdapter(List<RollViewInfo> list) {
        this.list = list;
    }

    @Override
    public View getView(ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        Picasso.with(container.getContext())
                .load(list.get(position).getUrl())
                .placeholder(R.drawable.btvi3)
                .error(R.drawable.btvi3)
                .into(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
