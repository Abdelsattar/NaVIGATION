package com.androidtech.around.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ahmed Donkl on 11/20/2016.
 */

public class UploadPhotoToFirebaseFragment extends Fragment {
    private static final String TAG = "UploadPhotoToFirebaseFragment";

    private static final float MAX_HEIGHT_RESIZED_IMAGE = 612.0f;
    private static final float MAX_WIDTH_RESIZED_IMAGE = 412.0f;

    private static final float MAX_HEIGHT_FULL_IMAGE = 1016.0f;
    private static final float MAX_WIDTH_FULL_IMAGE = 712.0f;

    public interface TaskCallbacks {
        void onImageResized(String resizedImage, boolean isFull);
        void onPhotoUploaded(String fullUrl, String thumbnailUrl);
        void onPhotoProfileUploaded(String imageUrl);
    }
    private Context mApplicationContext;
    private UploadPhotoToFirebaseFragment.TaskCallbacks mCallbacks;

    public UploadPhotoToFirebaseFragment() {
        // Required empty public constructor
    }

    public static UploadPhotoToFirebaseFragment newInstance() {
        return new UploadPhotoToFirebaseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across config changes.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UploadPhotoToFirebaseFragment.TaskCallbacks) {
            mCallbacks = (UploadPhotoToFirebaseFragment.TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void resizeImage(String imageUri,boolean isFull) {
        UploadPhotoToFirebaseFragment.LoadResizedImageTask task = new UploadPhotoToFirebaseFragment.LoadResizedImageTask(isFull);
        task.execute(imageUri);
    }

    public void uploadPhoto(String imageUri, String resizedImageUri) {
        UploadPhotoToFirebaseFragment.UploadPhotoTask uploadTask =
                new UploadPhotoToFirebaseFragment.UploadPhotoTask(imageUri, resizedImageUri);
        uploadTask.execute();
    }

    class UploadPhotoTask extends AsyncTask<Void, Void, Void> {
        private String imageUri;
        private String resizedImageUri;

        public UploadPhotoTask(String imageUri, String resizedImageUri) {
            this.imageUri = imageUri;
            this.resizedImageUri = resizedImageUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (imageUri == null || resizedImageUri == null) {
                return null;
            }

            final File resizedFile = new File(resizedImageUri);
            final File fullFile = new File(imageUri);

            final Uri FullImageFile = Uri.fromFile(fullFile);
            final Uri resizedImageFile = Uri.fromFile(resizedFile);
            String fileName = FullImageFile.getLastPathSegment();

            FirebaseStorage storageRef = FirebaseStorage.getInstance();
            StorageReference photoRef = storageRef.getReferenceFromUrl("gs://" + getString(R.string.google_storage_bucket));

            Long timestamp = System.currentTimeMillis();
            final StorageReference fullSizeRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("full").child(timestamp.toString()).child(fileName);
            final StorageReference thumbnailRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("thumb").child(timestamp.toString()).child(fileName);


            UploadTask fullImageUploadTask = fullSizeRef.putFile(FullImageFile);
            fullImageUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload pic to storage.");
                    FirebaseCrash.report(exception);
                    //callback results
                    mCallbacks.onPhotoUploaded(mApplicationContext.getString(
                            R.string.error_upload_pic),null);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    final Uri fullUrl = taskSnapshot.getDownloadUrl();
                 //   Log.d(TAG, fullUrl.toString());

                    //start upload thumbnail
                    UploadTask resizedImageUploadTask = thumbnailRef.putFile(resizedImageFile);
                    resizedImageUploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload pic to storage.");
                            FirebaseCrash.report(exception);
                            //delete images
                            if(resizedFile!=null)
                                resizedFile.deleteOnExit();
                            if(fullFile!=null)
                                fullFile.deleteOnExit();

                            //callback results
                            mCallbacks.onPhotoUploaded(mApplicationContext.getString(
                                    R.string.error_upload_pic),null);

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri thumbnailUrl = taskSnapshot.getDownloadUrl();
                          //  Log.d(TAG, thumbnailUrl.toString());
                            //delete images
                            if(resizedFile!=null)
                                resizedFile.deleteOnExit();
                            if(fullFile!=null)
                                fullFile.deleteOnExit();
                            //callBack results
                            mCallbacks.onPhotoUploaded(fullUrl.toString(),thumbnailUrl.toString());
                        }
                    });

                }
            });

            return null;
        }
    }

    public void uploadPhotoProfile(Uri imageUri) {
        UploadPhotoToFirebaseFragment.UploadPhotoTaskProfile uploadTask =
                new UploadPhotoToFirebaseFragment.UploadPhotoTaskProfile(imageUri);
        uploadTask.execute();
    }

    class UploadPhotoTaskProfile extends AsyncTask<Void, Void, Void> {
        private Uri imageUri;

        public UploadPhotoTaskProfile(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (imageUri == null) {
                return null;
            }


            String fileName = imageUri.getLastPathSegment();

            FirebaseStorage storageRef = FirebaseStorage.getInstance();
            StorageReference photoRef = storageRef.getReferenceFromUrl("gs://" + getString(R.string.google_storage_bucket));

            Long timestamp = System.currentTimeMillis();
            final StorageReference fullSizeRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("full").child(timestamp.toString()).child(fileName);

            UploadTask fullImageUploadTask = fullSizeRef.putFile(imageUri);
            fullImageUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload pic to storage.");
                    FirebaseCrash.report(exception);
                    //callback results
                    mCallbacks.onPhotoProfileUploaded(null);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    final Uri fullUrl = taskSnapshot.getDownloadUrl();
                    //   Log.d(TAG, fullUrl.toString());

                    //callback results
                    mCallbacks.onPhotoProfileUploaded(fullUrl.toString());
                }
            });

            return null;
        }
    }

    class LoadResizedImageTask extends AsyncTask<String, Void, String> {

        boolean isFull;
        public LoadResizedImageTask(boolean isFull) {
            this.isFull=isFull;
        }

        // Decode image in background.
        @Override
        protected String doInBackground(String... params) {
            String uri = params[0];
            if (uri != null) {
                String resizedImageUri = null;
                try {
                    resizedImageUri = compressImage(uri,isFull);
                }catch (Exception e){
                  //  Log.e(TAG, "Can't find file to resize: " + e.getMessage());
                   // FirebaseCrash.report(e);
                }
                return resizedImageUri;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resizedImageUri) {
            mCallbacks.onImageResized(resizedImageUri,isFull);
        }
    }


    public String compressImage(String imageUri,boolean isFull) {

        String filePath = imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight ;
        float maxWidth ;
        if(isFull){
             maxHeight = MAX_HEIGHT_FULL_IMAGE;
             maxWidth = MAX_WIDTH_FULL_IMAGE;
        }else{
             maxHeight = MAX_HEIGHT_RESIZED_IMAGE;
             maxWidth = MAX_WIDTH_RESIZED_IMAGE;
        }

        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "InstaR");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}

