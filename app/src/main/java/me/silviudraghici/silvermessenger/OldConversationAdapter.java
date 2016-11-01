package me.silviudraghici.silvermessenger;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Silviu on 2015-10-05.
 */

public class OldConversationAdapter extends CursorAdapter {

    public static final String IMAGE_NUMBER_KEY = "image_num_key";
    public static final String MANIFEST_KEY = "manifest_key";

    static private class ViewHolder {
        TextView messageView;
        AsyncImageView pictureView;
        TextView timeView;
        ProgressBar imageProgressBar;
        TextView dayView;
        RelativeLayout dayLayout;
    }

    private View.OnClickListener imageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            displayPicture(v);
        }
    };

    private Context context;

    private final int right;

    private static final int max_width = 150;
    private static final int max_height = 150;
    private final float density;

    private static final DateFormat timeFormat;
    private static final DateFormat dayFormat;
    private static final DateFormat comparison;

    private int stroke_width;
    static {
        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        dayFormat = new SimpleDateFormat("EEE, d MMM, yyyy");
        comparison = new SimpleDateFormat("yyyyMMdd");
    }

    //private final LinkedList<String> manifestList;

    public OldConversationAdapter(Context context, Cursor cursor, int originator) {
        super(context, cursor);
        this.context = context;
        this.right = originator;
        this.density = context.getResources().getDisplayMetrics().density;
        //this.manifestList = PictureDirectory.getInstance();
        stroke_width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private int getItemViewType(Cursor cursor) {
        int type = cursor.getInt(cursor.getColumnIndex(ConversationProvider.KEY_ORIGINATOR));
        if (type == right) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        int type = getItemViewType(position);
//        if (convertView == null) {
//            LayoutInflater inflator =
//                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            if(type == 0) {
//                convertView = inflator.inflate(R.layout.text_bubble_right, parent, false);
//            }
//            else {
//                convertView = inflator.inflate(R.layout.text_bubble_left, parent, false);
//            }
//        }
//
//        MessageBundle bundle = messageList.get(position);
//        TextView message = (TextView) convertView.findViewById(R.id.singleMessage);
//        TextView time = (TextView) convertView.findViewById(R.id.time);
//
//        message.setText(bundle.message);
//        time.setText(bundle.time());
//
//        return convertView;
//    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;

        int columnIndex = cursor.getColumnIndex(ConversationProvider.KEY_ORIGINATOR);

        TextView tv = null;

        if (cursor.getInt(columnIndex) == right) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_bubble_right, parent, false);
            tv = (TextView) view.findViewById(R.id.singleMessage);
            tv.setTextColor( 0xFF000000);
            GradientDrawable bubble = (GradientDrawable) tv.getBackground();
            bubble.mutate();
            bubble.setStroke(stroke_width, 0xFF21BD9C);
            bubble.setColors(new int[]{0xFFFF9F15, 0xFFFFCF45});

        } else if (cursor.getInt(columnIndex) != right) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_bubble_left, parent, false);
            tv = (TextView) view.findViewById(R.id.singleMessage);
            tv.setTextColor( 0xFFFFFFFF);
            GradientDrawable bubble = (GradientDrawable) tv.getBackground();
            bubble.mutate();
            bubble.setStroke(stroke_width, 0xFFFF1414);
            bubble.setColors(new int[]{0xFF1A266F, 0xFF3A66DF});
        }
        ViewHolder holder = new ViewHolder();
        holder.messageView = tv;
        holder.pictureView = (AsyncImageView) view.findViewById(R.id.imageView);
        holder.pictureView.setOnClickListener(imageListener);
        holder.timeView = (TextView) view.findViewById(R.id.time);
        holder.imageProgressBar = (ProgressBar) view.findViewById(R.id.imageProgressBar);
        holder.dayLayout = (RelativeLayout) view.findViewById(R.id.day_layout);
        holder.dayView = (TextView) view.findViewById(R.id.day_text);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String message = cursor.getString(cursor.getColumnIndex(ConversationProvider.KEY_MESSAGE));

        long time = cursor.getLong(cursor.getColumnIndex(ConversationProvider.KEY_DATE));
        Date date = new Date(time);
        String timeString = timeFormat.format(date);
        holder.timeView.setText(timeString);

        if (message.startsWith(ConversationActivity.PICTURE_FLAG)) {
            inflateImage(context, holder, message);
        } else {
            holder.messageView.setText(message);
            holder.messageView.setVisibility(View.VISIBLE);
            holder.pictureView.setVisibility(View.GONE);
            holder.pictureView.setImageDrawable(null);
        }

        displayDay(cursor, holder, date);
    }

    private void inflateImage(Context context, ViewHolder holder, String message) {
        String pathNum = message.replace("manif//", "");
        File pic = new File(context.getFilesDir(), "");//manifestList.get(Integer.parseInt(pathNum)));
        Uri pictureUri = Uri.fromFile(pic);

        holder.pictureView.setImageDrawable(null);
        holder.pictureView.setManifestNum(pathNum);

        final int imageHeight;
        final int imageWidth;
        final double scale;
        int inSampleSize = 1;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pictureUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            imageHeight = options.outHeight;
            imageWidth = options.outWidth;

            if (imageWidth >= imageHeight) {
                scale = imageWidth / (double) max_width;
            } else {
                scale = imageHeight / (double) max_height;
            }
            final int reqWidth = (int) (imageWidth / scale);
            final int reqHeight = (int) (imageHeight / scale);
            inSampleSize = calculateInSampleSize(imageWidth, imageHeight,
                    reqWidth, reqHeight);

            holder.pictureView.getLayoutParams().width = (int) (reqWidth * density);
            holder.pictureView.getLayoutParams().height = (int) (reqHeight * density);
            holder.pictureView.requestLayout();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncImageLoader imageLoader = new AsyncImageLoader(context, inSampleSize,
                holder.pictureView, holder.imageProgressBar);
        holder.pictureView.setImageLoader(imageLoader);
        imageLoader.execute(pictureUri);

        holder.pictureView.setVisibility(View.VISIBLE);
        holder.imageProgressBar.setVisibility(View.VISIBLE);
        holder.messageView.setVisibility(View.GONE);
    }

    private void displayDay(Cursor cursor, ViewHolder holder, Date date) {
        if (cursor.isFirst()) {
            holder.dayView.setText(dayFormat.format(date));
            holder.dayLayout.setVisibility(View.VISIBLE);
        } else {
            cursor.moveToPrevious();
            long time2 = cursor.getLong(cursor.getColumnIndex(ConversationProvider.KEY_DATE));
            Date date2 = new Date(time2);
            if (!comparison.format(date).equals(comparison.format(date2))) {
                holder.dayView.setText(dayFormat.format(date));
                holder.dayLayout.setVisibility(View.VISIBLE);
            } else {
                holder.dayLayout.setVisibility(View.GONE);
            }
        }
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

    private void displayPicture(View v) {
        AsyncImageView picture = (AsyncImageView) v;
        Log.d("here", picture.getManifestNum());
        Intent startImageDisplay = new Intent(context, ImageDisplay.class);
        startImageDisplay.putExtra(IMAGE_NUMBER_KEY, picture.getManifestNum());
        context.startActivity(startImageDisplay);
    }
}
