package me.silviudraghici.silvermessenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Silviu on 2015-12-09.
 */
public class AsyncImageLoader extends AsyncTask<Uri, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<ProgressBar> imageProgressBarReference;
    private final Context context;
    private final int inSampleSize;

    public AsyncImageLoader(Context context,int inSampleSize,
                            ImageView imageView, ProgressBar progressBar) {
        this.inSampleSize = inSampleSize;
        this.imageViewReference = new WeakReference<>(imageView);
        this.imageProgressBarReference = new WeakReference<>(progressBar);
        this.context = context;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        final Uri uri = params[0];
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap;
        bitmap = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
        final ProgressBar progressBar = imageProgressBarReference.get();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        final ProgressBar progressBar = imageProgressBarReference.get();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
