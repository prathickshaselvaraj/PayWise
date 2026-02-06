package com.example.paywise.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ImageUtils - Image manipulation and storage utilities
 *
 * Features:
 * - Load images from URI
 * - Resize and compress images
 * - Save to internal storage
 * - Load from internal storage
 */
public class ImageUtils {

    /**
     * Compress and save image to internal storage
     *
     * @param context Application context
     * @param imageUri URI of the selected image
     * @param fileName Name to save the file as
     * @return Path of saved image or null if failed
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileName) {
        try {
            // Load bitmap from URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            // Resize bitmap
            bitmap = resizeBitmap(bitmap, Constants.MAX_IMAGE_SIZE);

            // Compress bitmap
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_QUALITY, bytes);

            // Save to internal storage
            File directory = context.getFilesDir();
            File imageFile = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bytes.toByteArray());
            fos.close();

            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize bitmap to specified max dimension while maintaining aspect ratio
     *
     * @param bitmap Original bitmap
     * @param maxSize Maximum width or height
     * @return Resized bitmap
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = (float) width / height;

        if (width > height) {
            // Landscape
            width = maxSize;
            height = Math.round(maxSize / ratio);
        } else {
            // Portrait
            height = maxSize;
            width = Math.round(maxSize * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Load bitmap from internal storage
     *
     * @param imagePath Path to the image file
     * @return Bitmap or null if failed
     */
    public static Bitmap loadBitmapFromPath(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete image from internal storage
     *
     * @param imagePath Path to the image file
     * @return true if deleted successfully
     */
    public static boolean deleteImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return imageFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get file size in KB
     *
     * @param imagePath Path to the image file
     * @return File size in KB
     */
    public static long getImageSizeInKB(String imagePath) {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            return imageFile.length() / 1024;
        }
        return 0;
    }
}