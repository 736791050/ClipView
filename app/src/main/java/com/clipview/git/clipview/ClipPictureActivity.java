package com.clipview.git.clipview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;


public class ClipPictureActivity extends AppCompatActivity implements OnTouchListener,
		OnClickListener {

	private RelativeLayout rl_back,rl_reverse;
	private RelativeLayout rl_ok;

	private ImageView iv_photo;


	private int x;//截图区域距离左边的距离
	private int y;//截图区域距离顶部的距离
	private int a; //截图区域的边长
	/**
	 * 初始化状态常量
	 */
	public static final int STATUS_INIT = 0;

	/**
	 * 图片缩放状态常量
	 */
	public static final int STATUS_ZOOM = 1;

	/**
	 * 图片拖动状态常量
	 */
	public static final int STATUS_MOVE = 2;

	/**
	 * 当前状态
	 */
	private int currentStatus;

	/**
	 * 起始按下X坐标
	 */
	private float startx;

	/**
	 * 起始按下Y坐标
	 */
	private float starty;

	private int l;

	private int r;

	private int t;

	private int b;

	/**
	 * 起始两指距离
	 */
	private double disStart;

	/**
	 * 起始高度
	 */
	private double heightStart = -1;

	/**
	 * 起始宽度
	 */
	private double widthStart = -1;

	/**
	 * 结束宽度
	 */
	private double width;

	/**
	 * 结束高度
	 */
	private double height;

	/**
	 * 结束两指距离
	 */
	private double disMove;
	private Bitmap bm;
	private int angle;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clip_picture);


		x = Util.getClipX(this);//截图区域距离左边的距离
		y = Util.getClipY(this);//截图区域距离顶部的距离
		a = Util.getClipWidth(this); //截图区域的边长

		Uri uri = getIntent().getData();
		setViews();
		setLinsteners();

		if(uri!=null) {
			bm = Util.getBitmap(this, uri, returnBarHeight(), a);
			drawAndMovePic(angle, bm);
		}else {
			finish();
		}


	}

	/**
	 * 初始化view
	 */
	private void setViews() {
		iv_photo = (ImageView)findViewById(R.id.iv_photo);
		rl_back = (RelativeLayout)findViewById(R.id.rl_back);
		rl_reverse = (RelativeLayout)findViewById(R.id.rl_reverse);
		rl_ok = (RelativeLayout)findViewById(R.id.rl_ok);
	}

	/**
	 * 监听事件
	 */
	private void setLinsteners() {
		rl_back.setOnClickListener(this);
		rl_reverse.setOnClickListener(this);
		rl_ok.setOnClickListener(this);
		iv_photo.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_back:
				finish();
				break;
			case R.id.rl_reverse:
				angle=(angle + 90) % 360;
				drawAndMovePic(angle, bm);
				break;
			case R.id.rl_ok:
				Bitmap fianBitmap = getBitmap();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				fianBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
				byte[] bitmapByte = baos.toByteArray();

				Intent intent = new Intent();
				intent.putExtra("bitmap", bitmapByte);
				intent.setAction("com.clipview.git.clipview.activity.acpreview.picture");
				// 发广播
				this.sendBroadcast(intent);
				finish();
				break;

			default:
				break;
		}
	}
	/**
	 * 调整图片初始位置
	 * @param
	 */
	private void drawAndMovePic(int angle,Bitmap bm) {
		if (bm != null) {
			bm = Util.rotaingImageView(angle, bm);
			iv_photo.setImageBitmap(bm);
			// 图片像素宽和高度
			int h = bm.getHeight();
			int w = bm.getWidth();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Util.getWidthPx(this), Util.getWidthPx(this) * h / w);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			iv_photo.setLayoutParams(params);
		}
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()){
			case R.id.iv_photo:
				switch (event.getActionMasked()){
					case MotionEvent.ACTION_DOWN:
						currentStatus = STATUS_MOVE;
						// 得到imageView最开始的各顶点的坐标
						l = iv_photo.getLeft();
						r = iv_photo.getRight();
						t = iv_photo.getTop();
						b = iv_photo.getBottom();

						width = r - l;
						height = b - t;
						Log.i("w_h","l:"+l+" t:"+t);
						if(widthStart == -1 && heightStart == -1){
							widthStart = width;
							heightStart = height;
							Log.i("w_h", "widthStart:"+widthStart+"  heightStart:"+heightStart);
							Log.i("w_h", "a:"+a+"  x:"+x);
						}

						startx = event.getRawX();
						starty = event.getRawY();
						Log.i("move", "startx:"+startx+"   starty:"+starty);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						if(event.getPointerCount() == 1){
							currentStatus = STATUS_MOVE;
						}else if (event.getPointerCount() == 2) {
							currentStatus = STATUS_ZOOM;
							disStart = distanceBetweenFingers(event);
						}
						break;
					case MotionEvent.ACTION_MOVE:
						Log.i("state", "" + currentStatus);
						if (currentStatus == STATUS_MOVE && event.getPointerCount() == 1) {
							actionMove(event);
						} else if (currentStatus == STATUS_ZOOM && event.getPointerCount() == 2) {
							actionZoom(event);
						}
						break;
					case MotionEvent.ACTION_UP:
						if(currentStatus == STATUS_ZOOM) {
							// 得到imageView最开始的各顶点的坐标
							l = iv_photo.getLeft();
							r = iv_photo.getRight();
							t = iv_photo.getTop();
							b = iv_photo.getBottom();

							width = r - l;
							height = b - t;
							if (width <= a || height <= a) {
								l = (int)(x+(a-width)/2);
								t = (int)(y+(a-height)/2);
								r = (int)(x+(a+width)/2);
								b = (int)(y+(a+height)/2);
								iv_photo.layout(l,t,r,b);

							}
						}
						currentStatus = STATUS_INIT;
						break;
					default:
						break;
				}
				break;
		}
		return true;
	}

	/**
	 * 缩放
	 * @param event
	 */
	private void actionZoom(MotionEvent event) {
		disMove = distanceBetweenFingers(event);

		double scale = disMove/disStart;

		double scaleTemp = (width * scale)/widthStart;

		double minScale = 0.5;
		if(a < heightStart) {
			minScale = a / heightStart;
		}else{
			minScale = 1;
		}

		Log.i("zoom", "scaleTemp:"+scaleTemp);
		if(scaleTemp > 2){
			scale = 2*widthStart/(width);
		}else if(scaleTemp < minScale){
			scale = minScale*widthStart/(width);
		}

		Log.i("scale", ""+scale);
		double dw = width*(scale -1);
		double dh = height*(scale -1);


		int lm = (int)(l - dw/2);
		int rm = (int)(r + dw/2);
		int tm = (int)(t - dh/2);
		int bm = (int)(b + dh/2);

		if(heightStart > a && bm - tm <a){
			bm +=1;
			tm -=1;
		}
		iv_photo.layout(lm, tm, rm, bm);
	}

	/**
	 * 移动
	 * @param event
	 */
	private void actionMove(MotionEvent event) {
		int x1 = (int) event.getRawX();
		int y1 = (int) event.getRawY();
		Log.i("move", "x1:"+x1+"    y1:"+y1);

		// 获取手指移动的距离
		int dx = (int) (x1 - startx);
		int dy = (int) (y1 - starty);

		if(width > a){
			if(l+dx >= x){
				dx = x - l;
			}

			if(r+dx <= x +a){
				dx = x + a - r;
			}
		}else{
			if(l+dx <= x){
				dx = x - l;
			}

			if(r+dx >= x +a){
				dx = x + a - r;
			}
		}

		if(height >a){
			if(t+dy >= y){
				dy = y - t;
			}

			if(b+dy <= y + a){
				dy = y + a - b;
			}
		}else{
			if(t+dy <= y){
				dy = y - t;
			}

			if(b+dy >= y + a){
				dy = y + a - b;
			}
		}
		iv_photo.layout(l+dx, t+dy, r+dx, b+dy);
	}

	/**
	 * 计算两个手指之间的距离。
	 *
	 * @param event
	 * @return 两个手指之间的距离
	 */
	private double distanceBetweenFingers(MotionEvent event) {
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}


	/* 获取矩形区域内的截图 */
	private Bitmap getBitmap() {
		getBarHeight();
		Bitmap screenShoot = null;
		screenShoot = takeScreenShot();
		Bitmap finalBitmap = Bitmap.createBitmap(
				screenShoot,
				x  + 1 , //x轴方向起点
				y  + 1 + titleBarHeight + statusBarHeight,//y轴方向起点
				a  - 1, //截取的宽度
				a  - 1  //截取的高度
		);
		return imageZoom(finalBitmap, 200);
	}

	private Bitmap imageZoom(Bitmap bitMap, double size) {
		//图片允许最大空间   单位：KB
		//将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		//将字节换成KB
		double mid = b.length/1024;
		//判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
		if (mid > size) {
			//获取bitmap大小 是允许最大大小的多少倍
			double i = mid / size;
			//开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
//			bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
//					bitMap.getHeight() / Math.sqrt(i));
			bitMap = zoomImage(bitMap, 800, 800);
		}
		return bitMap;
	}

	/***
	 * 图片的缩放方法
	 *
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
								   double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Log.i("wh", "w"+w+" h"+h);

		return bitmap;
	}

	int statusBarHeight = 0;
	int titleBarHeight = 0;

	private void getBarHeight() {
		// 获取状态栏高度
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;

		int contenttop = this.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// statusBarHeight是上面所求的状态栏的高度
		titleBarHeight = contenttop - statusBarHeight;
		if(titleBarHeight < 0) titleBarHeight = 0;

		Log.v("bar", "statusBarHeight = " + statusBarHeight + ", titleBarHeight = " + titleBarHeight);
	}

	private int returnBarHeight() {
		// 获取状态栏高度
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return frame.top;
	}

	// 获取Activity的截屏
	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}
}
