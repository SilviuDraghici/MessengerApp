package me.silviudraghici.silvermessenger;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class ConversationActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteMessageDialogFragment.NoticeDialogListener {

    private static final int SELECT_PICTURE = 0;
    private static final String DELETE_ROWID_FLAG = "del_flag";
    private static final String DELETE_MESSAGE_FLAG = "del_msg_flag";
    public static final String PICTURE_FLAG = "manif//";

    //OldConversationAdapter adapter;
    private ConversationAdapter adapter;
    private LinearLayoutManager layoutManager;
    private static final int CONVERSATION_LOADER_ID = 0;

    private PictureDirectory pictureManifest;

    private String conversationNumber;

    private boolean insertFlag = false;
    private boolean displayDialog;

    private long  deleteRowid = -1;
    private String delMessage;

    private OnClickListener SendListener = new OnClickListener() {
        public void onClick(View v) {
            EditText message = (EditText) findViewById(R.id.reply_field);
            String text = message.getText().toString();
            if (text.length() > 0 && !text.startsWith(PICTURE_FLAG)) {
                if (text.startsWith(PICTURE_FLAG)) {
                    longToast("Message can not start with \"" + PICTURE_FLAG + "\"");
                } else {
                    send(message.getText().toString());
                    message.setText("");
                }
            }
        }
    };

    protected View.OnLongClickListener deleteListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            setDeleteMessage(v);
            if(displayDialog) {
                DeleteMessageDialogFragment dialog = new DeleteMessageDialogFragment();
                dialog.show(getSupportFragmentManager(), "Delete Dialog");
            }else{
                deleteMessage();
            }
            return true;
        }
    };

    private OnClickListener InsertListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            getPicture();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent creationIntent = getIntent();
        String name = creationIntent.getExtras().getString(MainActivity.PERSON_KEY);
        setTitle(name);
        conversationNumber = String.valueOf(creationIntent.getExtras().getInt(
                MainActivity.CONVERSATION_NUM_KEY));

        pictureManifest = PictureDirectory.getInstance(this, conversationNumber);

        Button sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(SendListener);

        Button insertButton = (Button) findViewById(R.id.insert_button);
        insertButton.setOnClickListener(InsertListener);
        new_conv();
        //or_conv();

        getLoaderManager().initLoader(CONVERSATION_LOADER_ID, null, this);

        if (savedInstanceState != null) {
            deleteRowid = savedInstanceState.getLong(DELETE_ROWID_FLAG);
            delMessage = savedInstanceState.getString(DELETE_MESSAGE_FLAG);
        }

        displayDialog = true;
    }

    private void new_conv(){
        RecyclerView conversation = (RecyclerView) findViewById(R.id.conversation_RecyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        conversation.setLayoutManager(layoutManager);
        adapter = new ConversationAdapter(this, null, deleteListener,conversationNumber, 1);
        conversation.setAdapter(adapter);
    }

//    private void or_conv(){
//        adapter = new OldConversationAdapter(this, null, 1);
//        ListView conversation = (ListView) findViewById(R.id.conversation_listView);
//        conversation.setAdapter(adapter);
//        conversation.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//    }

    @Override
    public void onPause() {
        super.onPause();
        //conversation.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DELETE_ROWID_FLAG, deleteRowid);
        outState.putString(DELETE_MESSAGE_FLAG, delMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
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

    boolean me = false;
    int originator = 1;

    public void send(String message) {

        if (me) {
            originator = 1;
        } else {
            originator = 2;
        }
        me = !me;

        Calendar c = Calendar.getInstance();
        long time = c.getTime().getTime();

        Uri uri = Uri.withAppendedPath(ConversationProvider.CONTENT_URI, conversationNumber);
        ContentValues initialValues = new ContentValues();
        initialValues.put(ConversationProvider.KEY_CONVERSATION_NUM, conversationNumber);
        initialValues.put(ConversationProvider.KEY_ORIGINATOR, originator);
        initialValues.put(ConversationProvider.KEY_DATE, time);
        initialValues.put(ConversationProvider.KEY_MESSAGE, message);
        insertFlag = true;
        getContentResolver().insert(uri, initialValues);
    }

    private void setDeleteMessage(View v){
        ConversationAdapter.MessageViewHolder vh =
                (ConversationAdapter.MessageViewHolder) v.getTag();
        deleteRowid = vh.rowid;
        delMessage = vh.messageView.getText().toString();
    }

    private void deleteMessage() {
        if (deleteRowid != -1) {
            if(delMessage.startsWith(ConversationActivity.PICTURE_FLAG)){
                delMessage = delMessage.replace("manif//", "");
                File pic = new File(getFilesDir(), pictureManifest.getPath(delMessage));
                pic.delete();
                pictureManifest.removePicture(delMessage);
            }
            Uri uri = Uri.withAppendedPath(ConversationProvider.CONTENT_URI, conversationNumber);
            getContentResolver().delete(uri, "rowid=?",
                    new String[]{String.valueOf(deleteRowid)});
            deleteRowid = -1;
            delMessage = null;
        }
    }

    private void getPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        if (id == CONVERSATION_LOADER_ID) {
            Uri uri = Uri.withAppendedPath(ConversationProvider.CONTENT_URI, conversationNumber);
            loader = new CursorLoader(this, uri, null, null, null, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (insertFlag) {
            layoutManager.scrollToPosition(adapter.getItemCount() - 1);
        insertFlag = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                return;
            }
            Uri uri = data.getData();
            String newName = Integer.toHexString((int) (Math.random() * 268435456));
            if (getContentResolver().getType(uri).startsWith("image/")) {
                String path = conversationNumber + "/" + newName;

                copyPicture(uri, path);
                String picKey = pictureManifest.addPicture(newName);
                send(PICTURE_FLAG + picKey);
            } else {
                longToast("Image not selected");
            }
        }
    }

    private void copyPicture(Uri uri, String path) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File outFile = new File(getFilesDir(), path);
            OutputStream out = new FileOutputStream(outFile);

            int orientation = 0, imageWidth, imageHeight, inSampleSize;
            double scale = 1;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = getContentResolver().openInputStream(uri);
            int size = options.outHeight * options.outWidth;
            if (size > 2073600) {
                scale = Math.sqrt(2073600 / (double) size);
                imageWidth = (int) (scale * options.outWidth);
                imageHeight = (int) (scale * options.outHeight);
                inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight,
                        imageWidth, imageHeight);
                options.inSampleSize = inSampleSize;
            }

            orientation = getOrientation(uri);

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
            if(in != null){
                in.close();
            }
            out.close();
            longToast(path);


//
//                    File inf = new File(getFilesDir(), "newFolder/" + uri.getLastPathSegment());
//                    InputStream picst = new FileInputStream(inf);
//                    Bitmap pic = BitmapFactory.decodeStream(picst);
//                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                    imageView.setImageBitmap(pic);
//                    TextView view = (TextView) findViewById(R.id.textView3);
//                    view.setText("newFolder/" + uri.getLastPathSegment());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void longToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private int getOrientation(Uri uri) {
        //Log.d("the path:    ", uri.getPath());
        int orientation = 0;
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
        return orientation;
    }

    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
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

    @Override
    public void onDialogPositiveClick() {
        deleteMessage();
    }

    @Override
    public void onDialogNegativeClick() {

    }

    @Override
    public void checkBoxReturn(boolean retState) {
        displayDialog = !retState;
    }
}
