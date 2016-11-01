package me.silviudraghici.silvermessenger;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

/**
 * Created by silvi on 2016-10-14.
 */

public class DeleteMessageDialogFragment extends DialogFragment {
    interface NoticeDialogListener {
        void onDialogPositiveClick();
        void onDialogNegativeClick();
        void checkBoxReturn(boolean retState);
    }

    private NoticeDialogListener listener;
    private CheckBox checkBox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final View checkBoxView = View.inflate(getContext(), R.layout.dialog_checkbox, null);
        checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
        checkBox.setText(R.string.dont_repeat_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog)
                .setView(checkBoxView)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(listener != null){
                            listener.onDialogPositiveClick();
                            listener.checkBoxReturn(checkBox.isChecked());
                        }else{
                            Log.d("here", "it was null");
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(listener != null){
                            listener.onDialogNegativeClick();
                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    public void setListener(Object o){
        try {
            listener = (NoticeDialogListener) o;
        } catch (ClassCastException e) {
            throw new ClassCastException(o.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void resetListener(){
        listener = null;
    }
}