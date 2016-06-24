package com.clipview.git.clipview;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;


/**
 * PopupWindow父类
 * 
 * @author lk
 */
public class BasePW extends PopupWindow {

	protected View view;

	protected Context context;

	public BasePW(Context context, int layoutId) {
		this.context = context;

		view = View.inflate(context, layoutId, null);

		// 窗口宽度
		int screenWidth = Util.getWidthPx(context);
		setWidth(screenWidth * 3 / 4);
		setHeight(LayoutParams.WRAP_CONTENT);

		setFocusable(true);
		setContentView(view);

	}
	


	/**
	 * 显示PopupWindow
	 * 
	 * @param parent
	 *            父控件
	 */
	public void show(View parent) {
		setAlpha(0.6f);
		showAtLocation(parent, Gravity.CENTER, 0, 0);
	}

	public void showBottom(View parent) {
		setAlpha(0.6f);
		showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}

	/**
	 * PopupWindow消失
	 */
	@Override
	public void dismiss() {
		setAlpha(1f);
		super.dismiss();
	}

	/**
	 * 设置PopupWindow背景亮度
	 * 
	 * @param f
	 */
	public void setAlpha(float f) {
		WindowManager.LayoutParams params = ((Activity) context).getWindow().getAttributes();
		params.alpha = f;
		((Activity) context).getWindow().setAttributes(params);
	}

}
