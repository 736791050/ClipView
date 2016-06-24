package com.clipview.git.clipview;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.TextView;


/**
 * Created by lk on 16/5/6.
 */
public class PWTakePhoto extends BasePW implements View.OnClickListener{

    private  TextView button1, button0;
    public static final int R_ID_BTN_0 = R.id.button0;
    public static final int R_ID_BTN_1 = R.id.button1;

    public PWTakePhoto(Context context) {
        super(context, R.layout.unit_dialog_common);

        button0 = (TextView) view.findViewById(R.id.button0);
        button1 = (TextView) view.findViewById(R.id.button1);
//        view.findViewById(R.id.divide0).setVisibility(View.VISIBLE);
        button0.setVisibility(View.VISIBLE);
        button1.setVisibility(View.VISIBLE);

        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        view.findViewById(R.id.includeBottom).setOnClickListener(this);

        button0.setText("拍照");
        button1.setText("从相册中选取");


        setBackgroundDrawable(new BitmapDrawable());
        // 窗口宽度
        int screenWidth = Util.getWidthPx(context);
        setWidth(screenWidth);
        setAnimationStyle(R.style.AdShare);
    }

    private View.OnClickListener onClickListener;
    public PWTakePhoto setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        if(onClickListener != null) onClickListener.onClick(v);
        dismiss();
    }
}
