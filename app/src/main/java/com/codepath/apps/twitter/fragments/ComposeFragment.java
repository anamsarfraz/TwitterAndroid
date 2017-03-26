package com.codepath.apps.twitter.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.databinding.FragmentComposeBinding;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.codepath.apps.twitter.util.DateUtil;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.codepath.apps.twitter.R.id.ivProfileImage;
import static com.codepath.apps.twitter.R.string.tweet;


public class ComposeFragment extends DialogFragment implements ConfirmationFragment.UpdateDraftDialogListener {

    final static String DEBUG = "DEBUG";
    private static final int MAX_COUNT = 140;
    private static final int PROFILE_IMG_ROUND = 4;
    private static final String COMPOSE_TEXT = "compose_text";

    private FragmentComposeBinding binding;
    EditText etCompose;
    Button btnTweet;
    TextView tvCharCount;
    boolean disableSelection;


    private OnComposeListener composeListener;
    private SharedPreferences composeSettings;
    InputMethodManager inputMgr;


    public ComposeFragment() {
        // Required empty public constructor
    }

    public static ComposeFragment newInstance() {
        return new ComposeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ComposeDialog);
        composeSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        inputMgr = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_compose, container, false);
        etCompose = binding.etCompose;
        btnTweet = (Button) binding.bnvCompose.findViewById(R.id.btnTweet);
        tvCharCount = (TextView) binding.bnvCompose.findViewById(R.id.tvCharCount);


        composeListener = (OnComposeListener) getActivity();


        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Glide.with(getContext())
                .load(User.getCurrentUser().getProfileImageUrl())
                .bitmapTransform(new RoundedCornersTransformation(getContext(), PROFILE_IMG_ROUND, 0))
                .placeholder(R.drawable.tweet_social)
                .crossFade()
                .into(binding.ivUserProfileImage);


        String composeString = composeSettings.getString(COMPOSE_TEXT, null);
        if (composeString == null) {
            Log.d(DEBUG, "compose text null");
            disableSelection = true;
            etCompose.setText(getString(R.string.compose_hint));
            etCompose.setSelection(0);
            etCompose.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            btnTweet.setEnabled(false);
            btnTweet.setAlpha((float)0.7);
            tvCharCount.setText(String.format("%d", MAX_COUNT));


        } else {
            disableSelection = false;
            etCompose.setText(composeString);
            etCompose.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            btnTweet.setEnabled(false);
            btnTweet.setAlpha((float)1.0);
            tvCharCount.setText(String.format("%d", MAX_COUNT-composeString.length()));

        }

        etCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG, "Disable selection: "+disableSelection);
                if (disableSelection) {
                    etCompose.setSelection(0);
                }

            }
        });

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(DEBUG, "Compose text change: " + s + ", start: "+start + ", before: "+before+", count: "+count+", "+etCompose.getText());

                int currLength = disableSelection ? 1: etCompose.getText().toString().length();
                Log.d(DEBUG, "disable selection "+disableSelection+", current length: "+currLength+", edit text: "+etCompose.getText().toString());

                int remainingCount = MAX_COUNT - currLength;
                tvCharCount.setText(String.format("%d", remainingCount));

                if (remainingCount == MAX_COUNT || remainingCount < 0) {
                    btnTweet.setEnabled(false);
                    btnTweet.setAlpha((float)0.7);

                } else {
                    btnTweet.setEnabled(true);
                    btnTweet.setAlpha((float)1.0);

                }

                if (remainingCount < 0) {
                    tvCharCount.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                } else {
                    tvCharCount.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }



            }

            @Override
            public void afterTextChanged(Editable s) {
                etCompose.removeTextChangedListener(this);

                if (disableSelection) {
                    disableSelection = false;
                    etCompose.setText(s.subSequence(0, 1));
                    etCompose.setSelection(1);
                    etCompose.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                } else if (s.length() == 0) {
                    disableSelection = true;
                    etCompose.setText(getString(R.string.compose_hint));
                    etCompose.setSelection(0);
                    etCompose.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                }

                etCompose.addTextChangedListener(this);

            }

        });

        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweet = new Tweet();
                tweet.setUser(User.getCurrentUser());
                tweet.setBody(etCompose.getText().toString());
                tweet.setCreatedAt(DateUtil.getCurrentTime());
                composeListener.createTweet(tweet);
                dismiss();
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etCompose.getText().length() > 0 && !disableSelection) {
                    FragmentManager fm = getFragmentManager();
                    ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance("Save draft?");
                    // SETS the target fragment for use later when sending results
                    confirmationFragment.setTargetFragment(ComposeFragment.this, 300);
                    confirmationFragment.show(fm, "fragment_confirmation");
                } else {
                    updateComposeDraft(DialogInterface.BUTTON_NEGATIVE);
                }
            }
        });

        // Show soft keyboard automatically and request focus to field
        //showSoftKeyboard(etCompose);

    }

    private void updateComposeDraft(int position) {
        SharedPreferences.Editor editor = composeSettings.edit();
        if (position == DialogInterface.BUTTON_POSITIVE) {
            editor.putString(COMPOSE_TEXT, etCompose.getText().toString());
        } else {
            Log.d(DEBUG, "Remving compose");
            editor.remove(COMPOSE_TEXT);
        }

        editor.apply();
        dismiss();

    }

    @Override
    public void onConfirmUpdateDialog(int position) {
        updateComposeDraft(position);

    }

    public interface OnComposeListener {
        void createTweet(Tweet tweet);

    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        Log.d(DEBUG, "DISMISS CALLED");
        //if (inputMgr.isActive())
        //    inputMgr.toggleSoftInput(InputMethodManager., InputMethodManager.HIDE_NOT_ALWAYS);

        hideSoftKeyboard(etCompose);
        super.onDismiss(dialog);
    }

    public void showSoftKeyboard(View view){
        if(view.requestFocus()){
            InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view){
        InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
