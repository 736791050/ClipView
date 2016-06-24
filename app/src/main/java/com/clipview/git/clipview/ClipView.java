package com.clipview.git.clipview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 遮罩
 */
public class ClipView extends View
{
	public ClipView(Context context)
	{
		super(context);
	}

	public ClipView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ClipView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();
		
		Paint paint = new Paint();
		paint.setColor(Util.ClipOutColor);
		
		float x = Util.getClipX(getContext());//截图区域距离左边的距离
		float y = Util.getClipY(getContext());//截图区域距离顶部的距离
		float a = Util.getClipWidth(getContext()); //截图区域的边长
		

		canvas.drawRect(0, 0, width, y, paint);
		canvas.drawRect(0, y, x, height, paint);
		canvas.drawRect(x, a+y, width, height, paint);
		canvas.drawRect(x+a, y, width, a+y, paint);

		paint.setColor(Util.ClipColor);

		int m = 4;
		canvas.drawRect(x-m,y-m,x+m+a,y,paint);
		canvas.drawRect(x-m,y,x,y+m+a,paint);
		canvas.drawRect(x,y+a,x+a,y+a+m,paint);
		canvas.drawRect(x+a,y,x+a+m,y+a+m,paint);
	}
}
