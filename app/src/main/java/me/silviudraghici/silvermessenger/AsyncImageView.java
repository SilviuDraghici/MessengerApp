package me.silviudraghici.silvermessenger;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Silviu on 2015-12-13.
 */
public class AsyncImageView extends ImageView {
    WeakReference<AsyncImageLoader> imageLoaderWeakReference;

    private String manifestNum;

    public AsyncImageView(Context context) {
        super(context);
    }


    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setImageLoader(AsyncImageLoader loader) {
        imageLoaderWeakReference = new WeakReference<>(loader);
    }

    /**
     * Sets a drawable as the content of this ImageView.
     *
     * @param drawable The drawable to set
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null && imageLoaderWeakReference != null) {
            AsyncImageLoader loader = imageLoaderWeakReference.get();
            if (loader != null) {
                imageLoaderWeakReference.get().cancel(true);
            }
        }
    }

    public void setManifestNum(String path){
        this.manifestNum = path;
    }

    public String getManifestNum(){
        return this.manifestNum;
    }
}
