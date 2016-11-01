package me.silviudraghici.silvermessenger;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * Created by silvi on 2016-10-24.
 */

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Cursor cursor;
    private int rowIDCol = -1;

    RecyclerViewCursorAdapter(Cursor cursor){
        this.cursor = cursor;

        setHasStableIds(true);
    }

    public Cursor swapCursor(Cursor cursor) {
        if (cursor == this.cursor) {
            return null;
        }
        Cursor oldCursor = this.cursor;
        this.cursor = cursor;
        rowIDCol = (cursor == null) ? -1 : cursor.getColumnIndexOrThrow("_id");
        notifyDataSetChanged();
        return oldCursor;
    }

    @Override
    public long getItemId(int position) {
        if ((cursor != null) && cursor.moveToPosition(position)) {
            return getItemId(cursor);
        }
        return RecyclerView.NO_ID;
    }

    public long getItemId(Cursor cursor){
        return cursor.getLong(rowIDCol);
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (cursor == null) {
            throw new IllegalStateException("Null Cursor");
        }

        boolean posChangeSuccessful = cursor.moveToPosition(position);
        if (!posChangeSuccessful) {
            throw new IllegalStateException("Couldn't move to position: " + position);
        }
        return getItemViewType(cursor);
    }

    public int getItemViewType(Cursor cursor){
        return 1;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (cursor == null) {
            throw new IllegalStateException("Null Cursor");
        }

        boolean posChangeSuccessful = cursor.moveToPosition(position);
        if (!posChangeSuccessful) {
            throw new IllegalStateException("Couldn't move to position: " + position);
        }
        onBindViewHolder(holder, cursor);
    }

    public abstract void onBindViewHolder(VH holder, Cursor cursor);
}
