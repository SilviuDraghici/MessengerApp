package me.silviudraghici.silvermessenger;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    public static final String PERSON_KEY = "name_key";
    public static final String CONVERSATION_NUM_KEY = "con_num_key";

    private Uri pictureUri;

    private OnClickListener StartConversation = new OnClickListener() {
        public void onClick(View v) {
            startConversation();
        }
    };

    private OnClickListener StartConversationList = new OnClickListener() {
        public void onClick(View v) {
            startConversationList();
        }
    };

    private void startConversationList() {
        Intent conversationListIntent = new Intent(this, ConversationListActivity.class);
        startActivity(conversationListIntent);
    }

    private OnClickListener DoNotPushButton = new OnClickListener() {
        public void onClick(View v) {
            PictureDirectory manif = PictureDirectory.getInstance(MainActivity.this , "testfldr");
            File dir = new File(getFilesDir(), "newFolder");
            dir.mkdir();
            try {
                InputStream in = getContentResolver().openInputStream(pictureUri);
                File outFile = new File(dir, pictureUri.getLastPathSegment());
                OutputStream out = new FileOutputStream(outFile);

                int orientation = 0, imageWidth, imageHeight, inSampleSize;
                double scale = 1;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                in.close();
                in = getContentResolver().openInputStream(pictureUri);
                int size = options.outHeight * options.outWidth;
                if (size > 2073600) {
                    scale = Math.sqrt(2073600 / (double) size);
                    imageWidth = (int) (scale * options.outWidth);
                    imageHeight = (int) (scale * options.outHeight);
                    inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight,
                            imageWidth, imageHeight);
                    options.inSampleSize = inSampleSize;
                }
                Cursor cursor =
                        getContentResolver().query(pictureUri,
                                new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                                null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        orientation = cursor.getInt(0);
                        if (orientation < 0) {
                            orientation = 360 + orientation;
                        }
                    }
                    cursor.close();
                }

                if (orientation > 0 || scale != 1) {
                    Log.d("here", "orientation or scale");
                    Bitmap pictureMap;
                    if (scale != 1) {
                        Log.d("here", "scale");
                        options.inJustDecodeBounds = false;
                        pictureMap = BitmapFactory.decodeStream(in, null, options);
                    } else {
                        Log.d("here", "not scale");
                        pictureMap = BitmapFactory.decodeStream(in);
                    }
                    if (orientation > 0) {
                        Log.d("here", "orientation");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(orientation);
                        pictureMap = Bitmap.createBitmap(pictureMap, 0, 0, pictureMap.getWidth(),
                                pictureMap.getHeight(), matrix, true);
                    }
                    pictureMap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                } else {
                    Log.d("here", "not orientation or scale");
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                in.close();
                out.close();

                longToast(outFile.getAbsolutePath());

                manif.addPicture(pictureUri.getLastPathSegment());
                Log.d("here", manif.toString());

                File inf = new File(getFilesDir(), "newFolder/" + pictureUri.getLastPathSegment());
                InputStream picst = new FileInputStream(inf);
                Bitmap pic = BitmapFactory.decodeStream(picst);
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(pic);
                TextView view = (TextView) findViewById(R.id.textView3);
                view.setText("newFolder/" + pictureUri.getLastPathSegment());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //current project
            File manifestFile = new File(dir, "manifest");
            BufferedReader manifestIn = null;
            LinkedList<String> manifestList = new LinkedList<>();
            try {
                manifestIn = new BufferedReader(new FileReader(manifestFile));
                String line;
                while ((line = manifestIn.readLine()) != null) {
                    manifestList.add(line);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (manifestIn != null) {
                    try {
                        manifestIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            manifestList.add("newFolder/" + pictureUri.getLastPathSegment());

            FileWriter manifestOut = null;
            try {
                manifestOut = new FileWriter(manifestFile);
                for (String line : manifestList) {
                    manifestOut.write(line + "\n");
                }
                manifestOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (manifestOut != null) {
                    try {
                        manifestOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.conversationButton);
        button.setOnClickListener(StartConversation);

        Button buttn = (Button) findViewById(R.id.button3);
        buttn.setOnClickListener(StartConversationList);

        Button butn = (Button) findViewById(R.id.button);
        butn.setOnClickListener(Insert);

        Button btn = (Button) findViewById(R.id.button2);
        btn.setOnClickListener(DoNotPushButton);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startConversation() {
        Intent conversationIntent = new Intent(this, ConversationActivity.class);
        conversationIntent.putExtra(PERSON_KEY, "Arthur");
        conversationIntent.putExtra(CONVERSATION_NUM_KEY, 2);
        startActivity(conversationIntent);
    }

    private static final int SELECT_PICTURE = 0;
    private OnClickListener Insert = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(intent, SELECT_PICTURE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                return;
            }
            Uri uri = data.getData();
            pictureUri = uri;
            int orientation = -1;
            try {
                String or = null;
                InputStream is = getContentResolver().openInputStream(uri);
                BufferedInputStream bis = new BufferedInputStream(is);
                Metadata metadata = ImageMetadataReader.readMetadata(bis);

                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        if(tag.getTagType() == 0x112){
                            or = tag.getDescription();
                        }
                    }
                }

                if(or != null) {
                    if (or.contains("90")) {
                        orientation = 90;
                    } else if (or.contains("180")) {
                        orientation = 180;
                    } else if (or.contains("270")) {
                        orientation = 270;
                    }
                }
            }
            catch (ImageProcessingException e){}
            catch (IOException e) {}

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                int imageHeight;
                int imageWidth;
                if (orientation == 90 || orientation == 270) {
                    imageHeight = options.outWidth;
                    imageWidth = options.outHeight;
                } else {
                    imageHeight = options.outHeight;
                    imageWidth = options.outWidth;
                }
                String imageType = options.outMimeType;

                TextView text = (TextView) findViewById(R.id.textView);
                text.setText("or: " + orientation + " width: " + imageWidth + " height: " + imageHeight + " type: " +
                        imageType);


                int max_width = 300;
                int max_height = 300;
                double scale;
                if (imageWidth >= imageHeight) {
                    scale = imageWidth / (double) max_width;
                } else {
                    scale = imageHeight / (double) max_height;
                }
                int reqWidth = (int) (imageWidth / scale);
                int reqHeight = (int) (imageHeight / scale);
                TextView text2 = (TextView) findViewById(R.id.textView2);
                text2.setText("Scale: " + scale + " width: " + reqWidth
                        + " height: " + reqHeight);

                inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = decodeSampledBitmapFromResource(inputStream, imageWidth,
                        imageHeight, reqWidth, reqHeight);
                if (orientation > 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);
                }
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.getLayoutParams().width = reqWidth;
                imageView.getLayoutParams().height = reqHeight;
                imageView.requestLayout();
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(InputStream inputStream, int width, int height,
                                                         int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(width, height, reqWidth, reqHeight);
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    private void longToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}