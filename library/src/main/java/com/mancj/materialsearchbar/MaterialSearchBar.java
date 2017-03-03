package com.mancj.materialsearchbar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mancj on 19.07.2016.
 */
public class MaterialSearchBar
        extends RelativeLayout
        implements View.OnClickListener,
        Animation.AnimationListener,
        View.OnFocusChangeListener,
        TextView.OnEditorActionListener,
        TextWatcher {

    public static final int BUTTON_SPEECH = 1;

    private View startContainer;
    private ImageView startingIcon;
    private TextView tvStartHint;

    private LinearLayout inputContainer;
    private ImageView activeIcon;
    private EditText searchEdit;
    private View clearIcon;

    private OnSearchActionListener onSearchActionListener;
    private boolean searchEnabled;

    public static final int VIEW_VISIBLE = 1;
    public static final int VIEW_INVISIBLE = 0;

    private float destiny;

    private int searchIconResId, activeIconResId;

    private CharSequence activeHint, startingHint;
    private boolean speechMode;

    private int textColor;

    private int hintColor;
    private boolean cardMode;
    private boolean lockedMode;

    public MaterialSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialSearchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSearchBar);
        searchIconResId = array.getResourceId(R.styleable.MaterialSearchBar_searchIconDrawable, -1);
        activeIconResId = array.getResourceId(R.styleable.MaterialSearchBar_activeIconDrawable, -1);
        startingHint = array.getString(R.styleable.MaterialSearchBar_startingHint);
        activeHint = array.getString(R.styleable.MaterialSearchBar_activeHint);
        speechMode = array.getBoolean(R.styleable.MaterialSearchBar_speechMode, false);
        cardMode = array.getBoolean(R.styleable.MaterialSearchBar_cardMode, true);
        lockedMode = array.getBoolean(R.styleable.MaterialSearchBar_lockedMode, false);
        hintColor = array.getColor(R.styleable.MaterialSearchBar_hintColor, -1);
        textColor = array.getColor(R.styleable.MaterialSearchBar_textColor, -1);

        array.recycle();

        if (cardMode) {
            inflate(getContext(), R.layout.msb_searchbar_card, this);
        } else {
            inflate(getContext(), R.layout.msb_searchbar_non_card, this);
        }

        destiny = getResources().getDisplayMetrics().density;

        startContainer = findViewById(R.id.start_container);
        tvStartHint = (TextView) findViewById(R.id.tv_start_hint);
        startingIcon = (ImageView) findViewById(R.id.mt_starting_icon);
        activeIcon = (ImageView) findViewById(R.id.mt_activated_icon);

        searchEdit = (EditText) findViewById(R.id.mt_editText);
        inputContainer = (LinearLayout) findViewById(R.id.inputContainer);
        clearIcon = findViewById(R.id.mt_clear);

        setOnClickListener(this);
        activeIcon.setOnClickListener(this);
        startingIcon.setOnClickListener(this);
        searchEdit.setOnFocusChangeListener(this);
        searchEdit.setOnEditorActionListener(this);
        searchEdit.addTextChangedListener(this);
        clearIcon.setOnClickListener(this);

        postSetup();
    }


    void postSetup() {

        if (searchIconResId < 0) {
            searchIconResId = R.drawable.ic_magnify_black_48dp;
        }

        setStartingIcon(searchIconResId);

        if (activeIconResId < 0) {
            activeIconResId = R.drawable.ic_back_green;
        }

        setActiveIcon(activeIconResId);

        setSpeechMode(speechMode);

        if (activeHint != null)
            searchEdit.setHint(activeHint);

        if (tvStartHint != null) {
            tvStartHint.setText(startingHint);
        }

        setupTextColors();

        if (lockedMode) {
            enableSearch(false);
        }

    }

    void setupTextColors() {
        if (hintColor != -1)
            searchEdit.setHintTextColor(ContextCompat.getColor(getContext(), hintColor));
        if (textColor != -1)
            searchEdit.setTextColor(ContextCompat.getColor(getContext(), textColor));
    }

    /**
     * Register listener for search bar callbacks.
     *
     * @param onSearchActionListener the callback listener
     */
    public void setOnSearchActionListener(OnSearchActionListener onSearchActionListener) {
        this.onSearchActionListener = onSearchActionListener;
    }

    /**
     * Hides search input and close arrow
     */
    public void disableSearch() {
        searchEnabled = false;
        Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_right);
        out.setAnimationListener(this);
        startContainer.setVisibility(VISIBLE);
        inputContainer.startAnimation(out);
        startContainer.startAnimation(in);

        if (listenerExists())
            onSearchActionListener.onSearchStateChanged(false);

    }

    /**
     * Shows search input and close arrow
     */
    public void enableSearch(boolean animate) {

        searchEnabled = true;
        inputContainer.setVisibility(VISIBLE);

        if (animate) {
            Animation left_in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_left);
            Animation left_out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_left);
            left_in.setAnimationListener(this);
            inputContainer.startAnimation(left_in);
            startContainer.startAnimation(left_out);
        } else {
            startContainer.setVisibility(GONE);
        }

        if (listenerExists()) {
            onSearchActionListener.onSearchStateChanged(true);
        }

        clearIcon.setVisibility(GONE);
    }

    /**
     * Set search icon drawable resource
     *
     * @param searchIconResId icon resource id
     */
    public void setStartingIcon(int searchIconResId) {
        this.searchIconResId = searchIconResId;
        this.startingIcon.setImageResource(searchIconResId);
    }

    /**
     * Set search icon drawable resource
     *
     * @param activeIconResId icon resource id
     */
    public void setActiveIcon(int activeIconResId) {
        this.activeIconResId = activeIconResId;
        this.activeIcon.setImageResource(this.activeIconResId);
    }

    /**
     * Sets search bar activeHint
     *
     * @param activeHint
     */
    public void setActiveHint(CharSequence activeHint) {
        this.activeHint = activeHint;
        searchEdit.setHint(activeHint);
    }

    /**
     * Sets search bar startingHint
     *
     * @param startingHint
     */
    public void setStartingHint(CharSequence startingHint) {
        this.startingHint = startingHint;
        tvStartHint.setText(startingHint);
    }

    public CharSequence getActiveHint() {
        return activeHint;
    }

    public CharSequence getStartingHint() {
        return startingHint;
    }

    /**
     * sets the speechMode for the search bar.
     * If set to true, microphone icon will display instead of the search icon.
     * Also clicking on this icon will trigger the callback method onButtonClicked()
     *
     * @param speechMode
     * @see #BUTTON_SPEECH
     * @see OnSearchActionListener#onButtonClicked(int)
     */
    public void setSpeechMode(boolean speechMode) {
        this.speechMode = speechMode;
        if (speechMode) {
            startingIcon.setImageResource(R.drawable.ic_microphone_black_48dp);
            startingIcon.setClickable(true);
        } else {
            startingIcon.setImageResource(searchIconResId);
            startingIcon.setClickable(false);
        }
    }

    /**
     * True if MaterialSearchBar is in speech mode
     *
     * @return speech mode
     */
    public boolean isSpeechModeEnabled() {
        return speechMode;
    }

    /**
     * Check if search bar is in edit mode
     *
     * @return true if search bar is in edit mode
     */
    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    /**
     * Set search input text color
     *
     * @param textColor text color
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        setupTextColors();
    }

    /**
     * Set text input activeHint color
     *
     * @param hintColor text activeHint color
     */
    public void setTextHintColor(int hintColor) {
        this.hintColor = hintColor;
        setupTextColors();
    }

    /**
     * Set search text
     *
     * @param text text
     */
    public void setText(String text) {
        searchEdit.setText(text);
    }

    /**
     * Get search text
     *
     * @return text
     */
    public String getText() {
        return searchEdit.getText().toString();
    }

    private boolean listenerExists() {
        return onSearchActionListener != null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId()) {
            if (!searchEnabled) {
                enableSearch(true);
            }
        } else if (id == R.id.mt_activated_icon) {
            if (lockedMode) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).onBackPressed();
                }
            } else {
                disableSearch();
            }
        } else if (id == R.id.mt_starting_icon) {
            if (listenerExists())
                onSearchActionListener.onButtonClicked(BUTTON_SPEECH);
        } else if (id == R.id.mt_clear) {
            searchEdit.setText("");
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (!searchEnabled) {
            inputContainer.setVisibility(GONE);
            searchEdit.setText("");
        } else {
            startContainer.setVisibility(GONE);
            searchEdit.requestFocus();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (listenerExists())
            onSearchActionListener.onSearchConfirmed(searchEdit.getText());

        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (searchEnabled) {
            if (listenerExists()) {
                onSearchActionListener.onSearchTextChanged(s);
            }

            if (s.length() > 0) {
                clearIcon.setVisibility(VISIBLE);
            } else {
                clearIcon.setVisibility(GONE);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * Interface definition for MaterialSearchBar callbacks.
     */
    public interface OnSearchActionListener {
        /**
         * Invoked when SearchBar opened or closed
         *
         * @param enabled
         */
        void onSearchStateChanged(boolean enabled);

        /**
         * Invoked when search confirmed and "search" button is clicked on the soft keyboard
         *
         * @param text search input
         */
        void onSearchConfirmed(CharSequence text);

        /**
         * Invoked when "speech" or "navigation" buttons clicked.
         *
         * @param buttonCode {@link #BUTTON_SPEECH} will be passed
         */
        void onButtonClicked(int buttonCode);

        void onSearchTextChanged(CharSequence text);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.isSearchBarVisible = searchEnabled ? VIEW_VISIBLE : VIEW_INVISIBLE;
        savedState.speechMode = speechMode ? VIEW_VISIBLE : VIEW_INVISIBLE;
        savedState.searchIconResId = searchIconResId;
        savedState.activeIconResId = activeIconResId;
        if (activeHint != null) savedState.hint = activeHint.toString();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        searchEnabled = savedState.isSearchBarVisible == VIEW_VISIBLE;

        if (searchEnabled) {
            inputContainer.setVisibility(VISIBLE);
            startContainer.setVisibility(GONE);
        }
//        speechMode = savedState.speechMode == VIEW_VISIBLE;
//        navIconResId = savedState.navIconResId;
//        searchIconResId = savedState.searchIconResId;
//        activeHint = savedState.activeHint;
//        maxSuggestionCount = savedState.maxSuggestions > 0 ? maxSuggestionCount  = savedState.maxSuggestions : maxSuggestionCount;
//        postSetup();
    }

    private static class SavedState extends BaseSavedState {
        private int isSearchBarVisible;
        private int speechMode;
        private int searchIconResId;
        private int activeIconResId;

        private String hint;


        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isSearchBarVisible);
            out.writeInt(speechMode);
            out.writeInt(searchIconResId);
            out.writeInt(activeIconResId);
            out.writeString(hint);
        }

        public SavedState(Parcel source) {
            super(source);
            isSearchBarVisible = source.readInt();
            speechMode = source.readInt();
            searchIconResId = source.readInt();
            activeIconResId = source.readInt();
            hint = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && searchEnabled && !lockedMode) {
            disableSearch();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public EditText getSearchEdit() {
        return searchEdit;
    }
}
