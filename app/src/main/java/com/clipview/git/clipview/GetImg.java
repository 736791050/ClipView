package com.clipview.git.clipview;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 获取照片
 */
public final class GetImg {

	// 相册
	public static final int CAMERA_IMG = 11;
	// 拍照
	public static final int PHOTO_IMG = 22;
	// 裁剪
	public static final int CUT_IMG = 33;

	private Activity mActivity = null;

	public AsyncTask<Intent, String, String> saveImgTask = null;

	/* 拍照所得相片路径 */
	public File file_camera = null;
	/* 裁切照片存储路径 */
	public static File file_cut = null;

	public GetImg(Activity activity2) {
		String cameraName = "img.png", cutName = "cutimg.png";

		File cacheDir = null;
		// 判断SD卡是否插入
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = android.os.Environment.getExternalStorageDirectory();
		} else {
			cacheDir = activity2.getCacheDir();
		}

		file_camera = new File(cacheDir, cameraName);
		file_cut = new File(cacheDir, cutName);

		try {
			if (!file_camera.exists()) {
				file_camera.createNewFile();
			}
			if (!file_cut.exists()) {
				file_cut.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		mActivity = activity2;
	}

	/**
	 * 相册
	 * 
	 */
	public void goToGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		mActivity.startActivityForResult(intent, GetImg.PHOTO_IMG);
	}

	/**
	 * 相机
	 * 
	 */
	public void goToCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_camera));
		mActivity.startActivityForResult(intent, GetImg.CAMERA_IMG);
	}

	/**
	 * 裁切图片
	 * 
	 */
	public void gotoCutImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP", null);
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file_cut));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("outputQuality", 10);
		mActivity.startActivityForResult(intent, GetImg.CUT_IMG);
	}

	/**
	 * 获取 从相册选择的照片路径
	 * 
	 */
	@SuppressWarnings("deprecation")
	public String getGalleryPath(Intent data) {
		Uri mImageCaptureUri = data.getData();
		if (mImageCaptureUri != null) {
			String[] proj = { MediaStore.Images.Media.DATA };
			// 好像是android多媒体数据库的封装接口，具体的看Android文档
			Cursor cursor = mActivity.managedQuery(mImageCaptureUri, proj,
					null, null, null);
			// 按我个人理解 这个是获得用户选择的图片的索引值
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			// 将光标移至开头 ，这个很重要，不小心很容易引起越界
			cursor.moveToFirst();
			// 最后根据索引值获取图片路径www.2cto.com
			return cursor.getString(column_index);
		} else {
			return "";
		}
	}

	/**
	 * 保存裁切后的图片
	 */
	public void saveCutImg(final Intent data) {
		saveImgTask = new AsyncTask<Intent, String, String>() {
			@Override
			protected String doInBackground(Intent... params) {
				// if (params.length > 0) {
				try {
					Bitmap photo = BitmapFactory.decodeFile(file_cut
							.getAbsolutePath());
					FileOutputStream out = new FileOutputStream(file_cut);
					photo.compress(Bitmap.CompressFormat.JPEG, 35, out);
					return file_cut.getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// }
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
			}
			
			
		};
		saveImgTask.execute(data);
	}

}