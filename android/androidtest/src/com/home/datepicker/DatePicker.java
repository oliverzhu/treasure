package com.home.datepicker;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.home.R;
import com.home.datepicker.NumberPicker.OnValueChangeListener;

public class DatePicker extends LinearLayout {
    
    private LinearLayout mPickers;
    private FrameLayout mYearLayout;
    private FrameLayout mMonthLayout;
    private FrameLayout mDayLayout;
    private NumberPicker mYearPicker;
    private NumberPicker mMonthPicker;
    private NumberPicker mDayPicker;
    
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    
    private int mNumberOfMonths;
    private String[] mShortMonths;
    
    private Locale mCurrentLocale;
    
    private Calendar mTempDate;
    private Calendar mMinDate;
    private Calendar mMaxDate;
    private Calendar mCurrentDate;
    
    private OnDateChangedListener mOnDateChangedListener;
    
    
    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context) {
        this(context, null, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCurrentLocale(Locale.getDefault());
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.date_picker, this, true);
        
        OnValueChangeListener onValueChangeListener = new OnValueChangeListener() {
            
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                if (picker == mYearPicker) {
                    mTempDate.set(Calendar.YEAR, newVal);
                } else if (picker == mMonthPicker) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal);
                    }
                } else if (picker == mDayPicker) {
                    int maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
                
                setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH));
                updatePicker();
                mDayPicker.forceLayout();
                notifyDateChanged();
            }
        };
        
        mPickers = (LinearLayout) findViewById(R.id.pickers);
        mYearLayout = (FrameLayout) findViewById(R.id.year_layout);
        mMonthLayout = (FrameLayout) findViewById(R.id.month_layout);
        mDayLayout = (FrameLayout) findViewById(R.id.day_layout);
        
        mYearPicker = (NumberPicker) findViewById(R.id.year);
        mYearPicker.setOnValueChangedListener(onValueChangeListener);
        
        mMonthPicker = (NumberPicker) findViewById(R.id.month);
        mMonthPicker.setMinValue(0);
        mMonthPicker.setMaxValue(mNumberOfMonths - 1);
        mMonthPicker.setDisplayedValues(mShortMonths);
        mMonthPicker.setOnValueChangedListener(onValueChangeListener);
        
        mDayPicker = (NumberPicker) findViewById(R.id.day);
        mDayPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        mDayPicker.setOnValueChangedListener(onValueChangeListener);
        
        mTempDate.clear();
        mTempDate.set(DEFAULT_START_YEAR, 0, 1);
        setMinDate(mTempDate.getTimeInMillis());
        
        mTempDate.clear();
        mTempDate.set(DEFAULT_END_YEAR, 11, 31);
        setMaxDate(mTempDate.getTimeInMillis());
        
        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH), mCurrentDate
                .get(Calendar.DAY_OF_MONTH), null);
        
        reorderSpinners();
    }
    
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        
        mCurrentLocale = locale;
        
        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);
        
        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
//        mShortMonths = new DateFormatSymbols().getShortMonths();
        
//        if (usingNumericMonths()) {
            mShortMonths = new String[mNumberOfMonths];
            for (int i = 0; i < mNumberOfMonths; ++i) {
                mShortMonths[i] = String.format("%d", i + 1);
//            }
        }
    }
    
    private Calendar getCalendarForLocale(Calendar oldcCalendar, Locale locale) {
        if (oldcCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldcCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }
    
    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }
    
    private void updatePicker() {
        if (mCurrentDate.equals(mMinDate)) {
            mDayPicker.setMinValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDayPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDayPicker.setWrapSelectorWheel(false);
            mMonthPicker.setDisplayedValues(null);
            mMonthPicker.setMinValue(mCurrentDate.get(Calendar.MONTH));
            mMonthPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.MONTH));
            mMonthPicker.setWrapSelectorWheel(false);
        } else if (mCurrentDate.equals(mMaxDate)) {
            mDayPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            mDayPicker.setMaxValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mDayPicker.setWrapSelectorWheel(false);
            mMonthPicker.setDisplayedValues(null);
            mMonthPicker.setMinValue(mCurrentDate.getActualMinimum(Calendar.MONTH));
            mMonthPicker.setMaxValue(mCurrentDate.get(Calendar.MONTH));
            mMonthPicker.setWrapSelectorWheel(false);
        } else {
            mDayPicker.setMinValue(1);
            mDayPicker.setMaxValue(mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mDayPicker.setWrapSelectorWheel(true);
            mMonthPicker.setDisplayedValues(null);
            mMonthPicker.setMinValue(0);
            mMonthPicker.setMaxValue(11);
            mMonthPicker.setWrapSelectorWheel(true);
        }
        
        String[] displayedValues = Arrays.copyOfRange(mShortMonths,
                mMonthPicker.getMinValue(), mMonthPicker.getMaxValue() + 1);
        mMonthPicker.setDisplayedValues(displayedValues);
        
        mYearPicker.setMinValue(mMinDate.get(Calendar.YEAR));
        mYearPicker.setMaxValue(mMaxDate.get(Calendar.YEAR));
        mYearPicker.setWrapSelectorWheel(false);
        
        mYearPicker.setValue(mCurrentDate.get(Calendar.YEAR));
        mMonthPicker.setValue(mCurrentDate.get(Calendar.MONTH));
        mDayPicker.setValue(mCurrentDate.get(Calendar.DAY_OF_MONTH));
        
    }
    
    private boolean usingNumericMonths() {
        return Character.isDigit(mShortMonths[Calendar.JANUARY].charAt(0));
    }
    
    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }
    
    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth
                || mCurrentDate.get(Calendar.DAY_OF_MONTH) != month);
    }
    
    private void reorderSpinners() {
        mPickers.removeAllViews();
        
        String dateFormat = Settings.System.getString(getContext().getContentResolver(),
                Settings.System.DATE_FORMAT);
        
        if (dateFormat == null || dateFormat.equals("")) {
            char[] order = DateFormat.getDateFormatOrder(getContext());
            for (int i = 0; i < order.length; i ++) {
                switch (order[i]) {
                case 'd':
                    mPickers.addView(mDayLayout);
                    break;
                case 'M':
                    mPickers.addView(mMonthLayout);
                    break;
                case 'y':
                    mPickers.addView(mYearLayout);
                    break;
                }
            }
            return;
        }
        
        if(dateFormat.equals("MM-dd-yyyy") || dateFormat.equals("EE-MMM-d-yyyy")){
            mPickers.addView(mMonthLayout);
            mPickers.addView(mDayLayout);
            mPickers.addView(mYearLayout);
        } else if(dateFormat.equals("dd-MM-yyyy") || dateFormat.equals("EE-d-MMM-yyyy")){
            mPickers.addView(mDayLayout);
            mPickers.addView(mMonthLayout);
            mPickers.addView(mYearLayout);
        } else {
            mPickers.addView(mYearLayout);
            mPickers.addView(mMonthLayout);
            mPickers.addView(mDayLayout);
        }
    }
    
    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
        updatePicker();
    }
    
    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);
    }
    
    public void init(int year, int monthOfYear, int dayOfMonth,
            OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updatePicker();
        mOnDateChangedListener = onDateChangedListener;
    }
    
    public int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    public int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    public int getDayOfMonth() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }
    
    public void updateDate(int year, int month, int dayOfMonth) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return;
        }
        setDate(year, month, dayOfMonth);
        updatePicker();
        notifyDateChanged();
    }
    
    public interface OnDateChangedListener {
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
}
