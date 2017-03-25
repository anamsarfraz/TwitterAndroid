package com.codepath.apps.twitter.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import com.codepath.apps.twitter.R;

public class ConfirmationFragment extends DialogFragment {

    private static final String SAVE = "SAVE";
    private static final String DELETE = "DELETE";


    public interface UpdateDraftDialogListener {
        void onConfirmUpdateDialog(int position);
    }

    public ConfirmationFragment() {
        // Empty constructor required for DialogFragment
    }

    public static ConfirmationFragment newInstance(String message) {
        ConfirmationFragment frag = new ConfirmationFragment();
        Bundle args = new Bundle();

        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.ConfirmationDialogCustom);
        alertDialogBuilder.setMessage(getArguments().getString("message"));
        alertDialogBuilder.setPositiveButton(SAVE,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                UpdateDraftDialogListener listener = (UpdateDraftDialogListener) getTargetFragment();
                listener.onConfirmUpdateDialog(DialogInterface.BUTTON_POSITIVE);

                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(DELETE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UpdateDraftDialogListener listener = (UpdateDraftDialogListener) getTargetFragment();
                listener.onConfirmUpdateDialog(DialogInterface.BUTTON_NEGATIVE);

                dialog.dismiss();
            }
        });
        return alertDialogBuilder.create();
    }
}