package me.silviudraghici.silvermessenger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageDisplay extends AppCompatActivity {

    private static final String NUM_KEY = "numKey";

    /**
     * Detects left and right swipes across a view.
     */
    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context context) {
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        public void onSwipeLeft() {
        }

        public void onSwipeRight() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_DISTANCE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
                    return true;
                }
                return false;
            }
        }
    }

    private PictureDirectory manifestList;
    private ImageView pictureView;
    private int num = -1;
    private int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        if (savedInstanceState != null) {
            num = savedInstanceState.getInt(NUM_KEY, -1);
        }

        Intent startingIntent = getIntent();
        String folder = startingIntent.getExtras().getString(
                ConversationAdapter.CONVERSATION_NUMBER_KEY);
        manifestList = PictureDirectory.getInstance(this, folder);

        if (num < 0) {
            String s = startingIntent.getExtras().getString(
                    ConversationAdapter.IMAGE_NUMBER_KEY);
            num = manifestList.getIndex(s);
        }

        size = manifestList.size();

        pictureView = (ImageView) findViewById(R.id.picture_display);
        pictureView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                num = num + 1;
                if (num >= size) {
                    num = 0;
                }
                loadImage();
            }

            @Override
            public void onSwipeRight() {
                num = num - 1;
                if (num < 0) {
                    num = size - 1;
                }
                loadImage();
            }
        });

        loadImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_display, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(NUM_KEY, num);
        super.onSaveInstanceState(outState);
    }

    private void loadImage() {
        setTitle((num + 1) + "/" + size);

        File picFile = new File(getFilesDir(), manifestList.getPathByIndex(num));
        try {
            InputStream stream = new FileInputStream(picFile);
            Bitmap picture = BitmapFactory.decodeStream(stream);
            pictureView.setImageBitmap(picture);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void longToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
