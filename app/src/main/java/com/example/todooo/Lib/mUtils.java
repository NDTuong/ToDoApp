package com.example.todooo.Lib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class mUtils {
    Context context;
    public mUtils(Context context) {
        this.context = context;
    }

    // HÀM THAY ĐỔI DRAWABLE
    public void changeBackgroundDrawable(TextView tv, int drawable) {
        tv.setBackground((context.getResources().getDrawable(drawable)));
    }
    public void changeBackgroundDrawable(ImageView iv, int drawable) {
        iv.setBackground((context.getResources().getDrawable(drawable)));
    }

    // HÀM THAY ĐỔI MÀU TEXT
    public void changeColorTextView(TextView tv, int color) {
        tv.setTextColor(context.getResources().getColor(color));
    }

    // HÀM THAY ĐỔI MÀU CÁC IMAGE VIEW
    public void changeColorImageView(ImageView iv, int color) {
        Drawable drawable = iv.getBackground();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, context.getResources().getColor(color));
        iv.setBackground(drawable);
    }

    // HÀM THAY ĐỔI MÀU BACKGROUND
    public void changeBackgroundTint(TextView tv, int color) {
        tv.setBackgroundTintList(ContextCompat.getColorStateList(context, color));
    }

    // HÀM SET TEXT VIEW
    public void setTextUseResource(TextView tv, int stringID){
        tv.setText(context.getResources().getString(stringID));
    }

    // HÀM KIỂM TRA GIÁ TRỊ INT < 10 HAY KHÔNG
    // NẾU NHỎ HƠN TRẢ VỀ STRING 0 + INT
    public String checkLessThan10(int i) {
        String s;
        if (i < 10) {
            s = "0" + i;
        } else {
            s = "" + i;
        }
        return s;
    }
}
