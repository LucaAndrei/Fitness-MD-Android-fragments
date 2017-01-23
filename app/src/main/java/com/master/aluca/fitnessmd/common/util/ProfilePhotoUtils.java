package com.master.aluca.fitnessmd.common.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Android internals have been modified to store images in the media folder with
 * the correct date meta data
 * @author samuelkirton
 */
public class ProfilePhotoUtils {

    private static final String LOG_TAG = "Fitness_PhotoUtils";

    /**
     * A copy of the Android internals  insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
     */
    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, title);
        values.put(Images.Media.DESCRIPTION, description);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(Media.EXTERNAL_CONTENT_URI, values);
            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F,Images.Thumbnails.MICRO_KIND);

                Log.d(LOG_TAG,"insert Image");
                Log.d(LOG_TAG,"convert image to base 64");
                Log.d(LOG_TAG,"source : " );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                source.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byteFormat = stream.toByteArray();
                // get the base 64 string
                String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
                Log.d(LOG_TAG,imgString);

            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(Images.Thumbnails.KIND,kind);
        values.put(Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public static Bitmap getProfilePicFromGallery(ContentResolver contentResolver, Uri mImageUri) {
        Log.d(LOG_TAG, "mImageUri : " + mImageUri);
        try {
            Bitmap rotateImage = null;
            Bitmap image = Media.getBitmap(contentResolver, mImageUri);

            int orientation = getOrientation(contentResolver, mImageUri);
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);

                Bitmap newImage = getResizedBitmap(image, 200, 150);

                if (rotateImage != null)
                    rotateImage.recycle();
                rotateImage = Bitmap.createBitmap(newImage, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

                return rotateImage;
            } else {
                Bitmap newImage = getResizedBitmap(image, 200, 150);
                Log.d(LOG_TAG,"get profile pic from gallery");
                Log.d(LOG_TAG,"convert image to base 64");
                Log.d(LOG_TAG,"source : " );
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                newImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byteFormat = stream.toByteArray();
                // get the base 64 string
                String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
                Log.d(LOG_TAG,imgString);
                return newImage;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }

    private static int getOrientation(ContentResolver contentResolver, Uri photoUri) {
        Cursor cursor = contentResolver.query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap rotatePhoto(ContentResolver contentResolver, Uri mImageUri) {
        Bitmap rotateImage = null;
        Bitmap image = null;
        try {
            image = Media.getBitmap(contentResolver, mImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = getOrientation(contentResolver, mImageUri);
        Log.d(LOG_TAG,"orientation : " + orientation);
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            Bitmap newImage = getResizedBitmap(image, 200, 150);

            if (rotateImage != null)
                rotateImage.recycle();
            rotateImage = Bitmap.createBitmap(newImage, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

            return rotateImage;
        } else {
            Bitmap newImage = getResizedBitmap(image, 200, 150);
            return newImage;
        }
    }
}