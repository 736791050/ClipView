package com.clipview.git.clipview;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;

/**
 * Created by lk on 16/6/23.
 */
public class Util {

    public static final int x = 15;//截图区域左上角x坐标
    public static final int y = 138;//截图区域左上角y坐标
    public static final int ClipOutColor = 0xb3000000;//截图外围颜色
    public static final int ClipColor = 0xb32d3a60;//截图区域颜色

    public static int getClipX(Context context){
        return dip2px(x, context);
    }

    public static int getClipY(Context context){
        return dip2px(y, context);
    }

    public static int getClipWidth(Context context){
        return getWidthPx(context) - dip2px(x, context) * 2;
    }

    /**
     * 根据手机的分辨率从dp的单位转成为px
     */
    public static int dip2px(float dpValue, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float density = displayMetrics.density;
        return (int) (dpValue * density + 0.5f);
    }


    public static int getWidthPx(Context context){
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        return widthPx;
    }

    public static int getHeightPx(Context context){
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int heightPx = displayMetrics.heightPixels;
        return heightPx;
    }


    /**
     *
     * @param context
     * @param uri 图片地址
     * @param statbar 状态栏高度
     * @return
     */
    public static Bitmap getBitmap(Context context , Uri uri, int statbar, int a){

        int LARGE_ITEM_HEIGHT = getWidthPx(context);//获取屏幕的高度
        int LARGE_ITEM_WIDTH = getHeightPx(context) - statbar; //获取屏幕高度，并减去状态栏高度

        String path = getImageAbsolutePath(context, uri); //获取图片路径
        int degree = readPictureDegree(path); //读取图片翻转角度(为了适配三星机型)

        BitmapFactory.Options ops = new BitmapFactory.Options();//先解析图片边框的大小
        ops.inJustDecodeBounds = true;//true只获取边框，防止溢出
        Bitmap bm = BitmapFactory.decodeFile(path, ops);
        ops.inSampleSize = 1;
        int oHeight = ops.outHeight; //图片高度
        int oWidth = ops.outWidth; //图片宽度

        //控制压缩比
        int contentHeight = 0;
        int contentWidth = 0;
        //等比缩放
        //		if(oWidth>LARGE_ITEM_WIDTH||oHeight>LARGE_ITEM_HEIGHT){
        //			contentHeight = LARGE_ITEM_HEIGHT;
        //			contentWidth = LARGE_ITEM_WIDTH;
        //		}else{
        //			contentHeight = oHeight;
        //			contentWidth = oWidth;
        //		}
        //		if(((float)oHeight/contentHeight) < ((float)oWidth/contentWidth)){
        //			ops.inSampleSize = (int) Math.ceil((float)oWidth/contentWidth);
        //		}else{
        //			ops.inSampleSize = (int) Math.ceil((float)oHeight/contentHeight);
        //		}

        //按照宽缩放
        contentHeight = LARGE_ITEM_HEIGHT;
        contentWidth = LARGE_ITEM_WIDTH;
        float scale = 1;
        if(oWidth >= contentWidth){
            scale = (float)oWidth/contentWidth;
        }else{
            scale = (float)contentWidth/oWidth;
        }
        Log.i("oWidth", "oWidth" + oWidth + " oHeight" + oHeight);

        if(scale > 1){
            bm = BitmapFactory.decodeFile(path);
            bm = Bitmap.createScaledBitmap(bm, contentWidth, (int)((float)contentWidth/oWidth*oHeight), true);
        }else{
            ops.inSampleSize = (int) Math.ceil(scale);
            ops.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, ops);
        }
        if(degree!=0){
            bm = rotaingImageView(degree, bm);
        }

        return bm;
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     * @param
     * @param imageUri
     * @author
     * @date 2015-11-16
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 获取图片信息
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 图片旋转
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

}
