package com.robooot.myapplication.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.robooot.myapplication.R;

public class CustomDialog extends Dialog {
    private TextView textView0;
    private TextView textView1;
    private TextView tvCancel;
    private Context mConetxt;
    public CustomDialog(Context context) {
        this(context,R.style.Theme_CustomDialog);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);

        init(context);
    }

    protected CustomDialog(Context context, boolean cancelable,DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

        init(context);
    }

    private void init(Context context) {

        this.mConetxt = context;
        setContentView(R.layout.custom_dialog_layout);

        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.CustomDialogAanimationStyle);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        textView0 = findViewById(R.id.textView0);
        textView1 = findViewById(R.id.textView1);

        tvCancel = findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
    }

    public CustomDialog setTitles(String[] titles){
        if (textView0 != null)
            textView0.setText(titles[0]);

        if (textView0 != null){
            textView1.setText(titles[1]);
        }
        return this;
    }

    public CustomDialog setColors(String[] colors){
        if (colors == null)
            return this;

        if (colors.length == 1)
            textView0.setTextColor(Color.parseColor(colors[0]));

        if (colors.length == 2){
            textView0.setTextColor(Color.parseColor(colors[0]));
            textView1.setTextColor(Color.parseColor(colors[1]));
        }

        if (colors.length == 3){
            textView0.setTextColor(Color.parseColor(colors[0]));
            textView1.setTextColor(Color.parseColor(colors[1]));
            tvCancel.setTextColor(Color.parseColor(colors[2]));
        }
        return this;
    }

    public CustomDialog setColors(int[] colors){
        if (colors == null||colors.length == 0)
            return this;

        if (colors.length == 1)
            textView0.setTextColor(ContextCompat.getColor(mConetxt,colors[0]));

        if (colors.length == 2){
            textView0.setTextColor(ContextCompat.getColor(mConetxt,colors[0]));
            textView1.setTextColor(ContextCompat.getColor(mConetxt,colors[1]));
        }

        if (colors.length == 3){
            textView0.setTextColor(ContextCompat.getColor(mConetxt,colors[0]));
            textView1.setTextColor(ContextCompat.getColor(mConetxt,colors[1]));
            tvCancel.setTextColor(ContextCompat.getColor(mConetxt,colors[2]));
        }

        return this;
    }
    public CustomDialog setOnItemClickListener(final OnItemClickListener listener){
        if (listener != null){
            if (textView0 != null)
                textView0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        listener.onItemClick(TAKE_PHOTO);
                    }
                });

            if (textView1!=null)
                textView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        listener.onItemClick(PICK_PHOTO);
                    }
                });
        }

        return this;
    }

    public static final int TAKE_PHOTO = 0X00;
    public static final int PICK_PHOTO = 0X01;

    public interface OnItemClickListener {
        void onItemClick(int id);
    }
}
