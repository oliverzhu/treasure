package com.ape.onelogin.myos.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.onelogin.R;

@SuppressLint("NewApi")
public class MyOSEditText extends RelativeLayout implements OnClickListener{

    private static final String TAG = "MyOSEditText";
    
    public static final int MYOS_EDITTEXT_STYLE_TOP         = 0;
    public static final int MYOS_EDITTEXT_STYLE_MIDDLE      = 1;
    public static final int MYOS_EDITTEXT_STYLE_BOTTOM      = 2;
    public static final int MYOS_EDITTEXT_STYLE_INDEPENDENT = 3;
    public static final int MYOS_EDITTEXT_STYLE_ALL         = 4;
    
    private static final int CLEARBUTTON_MARGIN_RIGHT       = 36; // dip
    
    private static final int INPUTTEXT_PADDING_LEFT         = 10; // dip
    private static final int INPUTTEXT_PADDING_TOP          = 2;  // dip
    private static final int INPUTTEXT_PADDING_RIGHT        = CLEARBUTTON_MARGIN_RIGHT + 5; // dip
    private static final int INPUTTEXT_PADDING_BOTTOM       = 2;  // dip
    
    private static final int POPUP_MAX_WIDTH                = 300; //dip
    
    private static final int DEFAULT_MAX_LENGTH             = 1024;
    
    private MyOSEditText mMyOSEditText;
    
    private EditTextDrawable[] mStyleDrawableArray;
    private EditTextDrawable mEditTextDrawable;
    
    private LinearLayout mContainerLayout;
    private TextView mTitleTextView;
    private ImageView mIconImageView;
    private EditText mInputEditText;
    private ImageButton mClearButton;
    
    private RelativeLayout mInfoPadLayout;
    private ImageButton mNoticeButton;
    private ImageButton mNecessaryButton;
    
    private int mStyle;
    private int mIconID;
    private int mMaxLength;
    private boolean mShowNotice;
    private boolean mShowNecessary;
    private String mTitle;
    private String mHint;
    private String mNecessaryHint;
    private String mNoticeHint;
    
    private OnInputLegallyListener mInputLegallyListener;
    private OnTextChangeListener mTextChangeListener;
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                if (mInputEditText.hasFocus()) {
                    mClearButton.setVisibility(View.VISIBLE);
                }
                mNecessaryButton.setVisibility(View.GONE);
            } else {
                mClearButton.setVisibility(View.GONE);
                mNoticeButton.setVisibility(View.GONE);
                if (mShowNecessary) {
                    mNecessaryButton.setVisibility(View.VISIBLE);
                } else {
                    mNecessaryButton.setVisibility(View.GONE);
                }
            }
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // do nothing;
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            if (mTextChangeListener != null) {
                mTextChangeListener.afterTextChanged(mMyOSEditText, s.toString());
            }
        }
    };
    
    public MyOSEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, 
                R.styleable.MyOSEditText, defStyleAttr, 0);
        mStyle = typedArray.getInt(R.styleable.MyOSEditText_style, MYOS_EDITTEXT_STYLE_INDEPENDENT);
        if (mStyle == -1) {
            throw new IllegalArgumentException("The style attribute is required.");
        }
        
        mShowNotice = typedArray.getBoolean(R.styleable.MyOSEditText_showNotice, true);
        mShowNecessary = typedArray.getBoolean(R.styleable.MyOSEditText_showNecessary, true);
        mTitle = typedArray.getString(R.styleable.MyOSEditText_title);
        mHint = typedArray.getString(R.styleable.MyOSEditText_hint);
        mNecessaryHint = typedArray.getString(R.styleable.MyOSEditText_necessaryHint);
        mIconID = typedArray.getResourceId(R.styleable.MyOSEditText_icon, 0);
        mMaxLength = typedArray.getInt(R.styleable.MyOSEditText_maxLength, DEFAULT_MAX_LENGTH);
        typedArray.recycle();
        initResources();
        mMyOSEditText = this;
    }
    
    public MyOSEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyOSEditText(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onFinishInflate() {
        // container
        LinearLayout.LayoutParams containerLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mContainerLayout = new LinearLayout(getContext());
        mContainerLayout.setLayoutParams(containerLayoutParams);
        mContainerLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        // title
        LinearLayout.LayoutParams titleLinearParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1.6f);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setBackgroundResource(mEditTextDrawable.leftDrawable);
        linearLayout.setLayoutParams(titleLinearParams);
        linearLayout.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (mIconID != 0) {
            mIconImageView = new ImageView(getContext());
            mIconImageView.setVisibility(View.VISIBLE);
            mIconImageView.setImageResource(mIconID);
            mIconImageView.setLayoutParams(titleParams);
            linearLayout.addView(mIconImageView);
        } else {
            mTitleTextView = new TextView(getContext());
            if (mTitle != null && !mTitle.equals("")) {
                mTitleTextView.setVisibility(View.VISIBLE);
                mTitleTextView.setText(mTitle);
            } else {
                mTitleTextView.setVisibility(View.GONE);
            }
            mTitleTextView.setGravity(Gravity.CENTER);
            mTitleTextView.setLayoutParams(titleParams);
            linearLayout.addView(mTitleTextView);
        }
        
        // input
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 5.0f);
        mInputEditText = new EditText(getContext());
        if (mTitle != null && !mTitle.equals("")) {
            mInputEditText.setBackgroundResource(mEditTextDrawable.rightDrawable);
        } else {
            mInputEditText.setBackgroundResource(mEditTextDrawable.leftDrawable);
        }
        if (mShowNecessary || mShowNotice) {
            mInputEditText.setPadding(
                    parserDipToPx(INPUTTEXT_PADDING_LEFT),
                    parserDipToPx(INPUTTEXT_PADDING_TOP), 
                    parserDipToPx(INPUTTEXT_PADDING_RIGHT),
                    parserDipToPx(INPUTTEXT_PADDING_BOTTOM));
        } else {
            mInputEditText.setPadding(
                    parserDipToPx(INPUTTEXT_PADDING_LEFT),
                    parserDipToPx(INPUTTEXT_PADDING_TOP), 
                    parserDipToPx(INPUTTEXT_PADDING_RIGHT + 6),
                    parserDipToPx(INPUTTEXT_PADDING_BOTTOM));
        }
        mInputEditText.setHint(mHint);
        mInputEditText.setSingleLine();
        mInputEditText.setTextColor(getResources().getColor(R.color.myos_label_text));
        mInputEditText.setHintTextColor(getResources().getColor(R.color.myos_edittext_hint));
        mInputEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.myos_edittext));
        mInputEditText.setLayoutParams(editParams);
        mInputEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxLength) });
        mInputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TextView textView = (TextView) v;
                if (!hasFocus) {
                    inputLegallyCheck();
                }
                
                if (textView.getText().length() > 0 && hasFocus) {
                    mClearButton.setVisibility(View.VISIBLE);
                } else {
                    mClearButton.setVisibility(View.GONE);
                }
            }
        });
        mInputEditText.addTextChangedListener(mTextWatcher);
        
        // information pad
        LinearLayout.LayoutParams infoPadLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
        mInfoPadLayout = new RelativeLayout(getContext());
        mInfoPadLayout.setLayoutParams(infoPadLayoutParams);
        
        // notice
        RelativeLayout.LayoutParams noticeParams =  new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        noticeParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        mNoticeButton = new ImageButton(getContext());
        mNoticeButton.setOnClickListener(this);
        mNoticeButton.setVisibility(View.GONE);
        mNoticeButton.setBackground(null);
        mNoticeButton.setImageResource(R.drawable.notice_selector);
        mNoticeButton.setLayoutParams(noticeParams);
        
        // necessary
        RelativeLayout.LayoutParams necessaryParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        necessaryParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        mNecessaryButton = new ImageButton(getContext());
        if (mShowNecessary) {
            mNecessaryButton.setVisibility(View.VISIBLE);
        } else {
            mNecessaryButton.setVisibility(View.GONE);
        }
        mNecessaryButton.setOnClickListener(this);
        mNecessaryButton.setBackground(null);
        mNecessaryButton.setImageResource(R.drawable.ic_necessary);
        mNecessaryButton.setLayoutParams(necessaryParams);
        
        mInfoPadLayout.addView(mNoticeButton);
        mInfoPadLayout.addView(mNecessaryButton);
        
        mContainerLayout.addView(linearLayout);
        mContainerLayout.addView(mInputEditText);
        if (mShowNecessary || mShowNotice) {
            mContainerLayout.addView(mInfoPadLayout);
        }
        
        // clear
        RelativeLayout.LayoutParams clearParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        clearParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
        if (mShowNecessary || mShowNotice) {
            clearParams.rightMargin = parserDipToPx(CLEARBUTTON_MARGIN_RIGHT);
        }
        mClearButton = new ImageButton(getContext());
        mClearButton.setBackground(null);
        mClearButton.setImageResource(R.drawable.delete_selector);
        mClearButton.setVisibility(View.GONE);
        mClearButton.setOnClickListener(this);
        mClearButton.setLayoutParams(clearParams);
        
        addView(mContainerLayout);
        addView(mClearButton);
    }
    
    @Override
    public void onClick(View v) {
        if (v == mClearButton) {
            mInputEditText.setText("");
            mNoticeButton.setVisibility(View.GONE);
            if (mShowNecessary) {
                mNecessaryButton.setVisibility(View.VISIBLE);
            } else {
                mNecessaryButton.setVisibility(View.GONE);
            }
        } else if (v == mNoticeButton) {
            showNoticeView(mNoticeButton, mNoticeHint);
        } else if (v == mNecessaryButton) {
            showNoticeView(mNecessaryButton, mNecessaryHint);
        }
    }
    
    private void initResources() {
        mStyleDrawableArray = new EditTextDrawable[]{
                new EditTextDrawable(R.drawable.textfield_special_top_left, 
                        R.drawable.textfield_special_top_right),
                new EditTextDrawable(R.drawable.textfield_special_middle_left, 
                        R.drawable.textfield_special_middle_right),
                new EditTextDrawable(R.drawable.textfield_special_bottom_left, 
                        R.drawable.textfield_special_bottom_right),
                new EditTextDrawable(R.drawable.textfield_independent_left, 
                        R.drawable.textfield_independent_right)
        };
        
        mEditTextDrawable = mStyleDrawableArray[mStyle];
    }
    
    private void showNoticeView(View parent, String message) {
        if (message == null) {
            return;
        }
        if (message.equals("")) {
            return;
        }
        LinearLayout noticeLayout;
        PopupWindow noticeDialog;
        
        noticeLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams optionMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noticeLayout.setLayoutParams(optionMenuParams);
        
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView noticeView = new TextView(getContext());
        noticeView.setBackground(getResources().getDrawable(R.drawable.ic_hint_right));
        noticeView.setText(message);
        noticeView.setLayoutParams(textParams);
        noticeLayout.addView(noticeView);
        
        noticeLayout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        int popupWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (noticeLayout.getMeasuredWidth() > parserDipToPx(POPUP_MAX_WIDTH)) {
            popupWidth = parserDipToPx(POPUP_MAX_WIDTH);
        }
        noticeDialog = new PopupWindow(noticeLayout,
                popupWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            
        noticeDialog.setFocusable(true);
        noticeDialog.setOutsideTouchable(true);
        noticeDialog.setBackgroundDrawable(new ColorDrawable(0));

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        
        int[] location = new int[2];
        parent.getLocationInWindow(location);
        if (noticeLayout.getMeasuredWidth() > parserDipToPx(POPUP_MAX_WIDTH)) {
            popupWidth = parserDipToPx(POPUP_MAX_WIDTH);
        } else {
            popupWidth = noticeLayout.getMeasuredWidth();
        }
        
        int xPos = location[0] - popupWidth + parent.getWidth() / 4;
        int yPox = location[1] - noticeLayout.getMeasuredHeight() + parent.getHeight() / 4 * 3;
        noticeDialog.showAtLocation(parent, Gravity.NO_GRAVITY, 
                xPos, yPox);
    }
    
    public boolean inputLegallyCheck() {
        if (mShowNotice) {
            if (mInputEditText.getText().length() > 0) {
                if (mInputLegallyListener != null) {
                    mNoticeButton.setVisibility(View.VISIBLE);
                    mNoticeHint = mInputLegallyListener.onInputLegallyCheck(mMyOSEditText, mInputEditText.getText());
                    if (mNoticeHint != null && !mNoticeHint.equals("")) {
                        mNoticeButton.setImageResource(R.drawable.notice_selector);
                        mNoticeButton.setEnabled(true);
                        return false;
                    } else {
                        mNoticeButton.setImageResource(R.drawable.ic_correct);
                        mNoticeButton.setEnabled(false);
                    }
                } else {
                    mNoticeButton.setVisibility(View.GONE);
                }   
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    public void setEditEnable(boolean enabled) {
        mInputEditText.setFocusable(enabled);
        mInputEditText.setFocusableInTouchMode(enabled);
        mInputEditText.setEnabled(enabled);
    }
    
    public void setInputType(int type) {
        mInputEditText.setInputType(type);
        if ((type & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            mInputEditText.setTypeface(Typeface.DEFAULT);
            if (type != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                mInputEditText.setTransformationMethod(new PasswordTransformationMethod());
            } else {
                mInputEditText.setTransformationMethod(new HideReturnsTransformationMethod());
            }
        }
    }
    
    public void setText(CharSequence text) {
        mInputEditText.setText(text);
        mInputEditText.setSelection(text.length());
    }
    
    public Editable getText() {
        return mInputEditText.getText();
    }
    
    public boolean isNecessary() {
        return mShowNecessary;
    }
    
//    public EditText getInputEditText() {
//        return mInputEditText;
//    }
    
    public void setNoticeMessage(String message) {
        if (message != null && !message.equals("")) {
            mNoticeHint = message;
            mNoticeButton.setImageResource(R.drawable.notice_selector);
            mNoticeButton.setEnabled(true);
        }
    }
    
    public void setNecessaryMessage(String message) {
        if (message != null && !message.equals("")) {
            mNecessaryHint = message;
            // TODO
        }
    }
    
    public void setClearVisibility(int visibility) {
        mClearButton.setVisibility(visibility);
    }
    
    public void setOnInputLegallyListener(OnInputLegallyListener listener) {
        mInputLegallyListener = listener;
    }
    
    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }
    
    public void setMaxLength(int length) {
        mMaxLength = length;
        mInputEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxLength) });
    }
    
    private int parserDipToPx(int dip) {
        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics()); 
        return px;
    }
    
    class EditTextDrawable {
        int leftDrawable;
        int rightDrawable;
        
        public EditTextDrawable(int left, int right) {
            this.leftDrawable = left;
            this.rightDrawable = right;
        }
    }
    
    public interface OnInputLegallyListener {
        public String onInputLegallyCheck(View view, CharSequence text);
    }
    
    public interface OnTextChangeListener {
        public void afterTextChanged(View view, String text);
    }
}
