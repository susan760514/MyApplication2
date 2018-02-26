package com.example.user123.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 *
 */

public class ImageScaleSample extends Activity {
    private String tag = "ImageScaleSample";
    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imglayout);
        mImageView = (ImageView) findViewById(R.id.imgview);

        addImageView(this);
    }

    public void addImageView(Context context){
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context).load(R.drawable.test).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if(resource!=null){
                    Bitmap bitmap = cutBitmap(resource);
                    if(bitmap!=null)
                        mImageView.setImageBitmap(bitmap);
                }
            }
        });
    }

    public Bitmap cutBitmap(Bitmap bm){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        Log.d(tag, width + ", " + height + ", " + bm.getWidth() + ", " + bm.getHeight());

        Bitmap bitmap = null;
        if(bm!=null){
            //y = bm.gethight 扣掉 要顯示的的高
            int y = bm.getHeight() - height;
            Log.d(tag, y +"");
            bitmap = Bitmap.createBitmap(bm,0,y, width, height);
        }

        Log.d(tag,  bitmap.getWidth() + ", " + bitmap.getHeight());

        return bitmap;
    }
}