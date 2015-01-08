/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.cloudfile.widget;

import java.text.NumberFormat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ape.filemanager.R;

public class ApeProgressDialog extends AlertDialog
{

    /**
     * Creates a ProgressDialog with a circular, spinning progress bar. This is
     * the default.
     */
    public static final int STYLE_SPINNER = 0;

    /**
     * Creates a ProgressDialog with a horizontal progress bar.
     */
    public static final int STYLE_HORIZONTAL = 1;

    private ProgressBar mProgress;

    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;

    private int mMax;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    
    private TextView mMessageView;
    private CharSequence mMessage;
    private boolean mIndeterminate;

    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    
    private long mTotalNumber;
    private int mCurrentNumber;

    public ApeProgressDialog(Context context)
    {
        super(context);
        initFormats();
    }

    public ApeProgressDialog(Context context, int theme)
    {
        super(context, theme);
        initFormats();
    }

    private void initFormats()
    {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static ProgressDialog show(Context context, CharSequence title,
            CharSequence message)
    {
        return show(context, title, message, false);
    }

    public static ProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate)
    {
        return show(context, title, message, indeterminate, false, null);
    }

    public static ProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable)
    {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static ProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable,
            OnCancelListener cancelListener)
    {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /*
         * Use a separate handler to update the text views as they must be
         * updated on the same thread that created them.
         */
        mViewUpdateHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                /* Update the number and percent */
                int progress = mProgress.getProgress();
                int max = mProgress.getMax();
                if (mProgressNumberFormat != null)
                {
                    String format = mProgressNumberFormat;
                    mProgressNumber.setText(String.format(format, mCurrentNumber,
                            mTotalNumber));
                } else
                {
                    mProgressNumber.setText("");
                }
                if (mProgressPercentFormat != null)
                {
                    double percent = (double) progress / (double) max;
                    SpannableString tmp = new SpannableString(
                            mProgressPercentFormat.format(percent));
                    tmp.setSpan(new StyleSpan(
                            android.graphics.Typeface.BOLD), 0, tmp
                            .length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mProgressPercent.setText(tmp);
                } else
                {
                    mProgressPercent.setText("");
                }
                
                if (mMessage != null)
                {
                    mMessageView.setText(mMessage);
                }
            }
        };
        View view = getLayoutInflater().inflate(
                R.layout.alert_dialog_progress, null);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mProgressNumber = (TextView) view
                .findViewById(R.id.progress_number);
        mProgressPercent = (TextView) view
                .findViewById(R.id.progress_percent);
        mMessageView = (TextView) view
                .findViewById(R.id.message);
        setView(view);

        if (mMax > 0)
        {
            setMax(mMax);
        }
        if (mProgressVal > 0)
        {
            setProgress(mProgressVal);
        }
        if (mSecondaryProgressVal > 0)
        {
            setSecondaryProgress(mSecondaryProgressVal);
        }
        if (mIncrementBy > 0)
        {
            incrementProgressBy(mIncrementBy);
        }
        if (mIncrementSecondaryBy > 0)
        {
            incrementSecondaryProgressBy(mIncrementSecondaryBy);
        }
        if (mProgressDrawable != null)
        {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null)
        {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null)
        {
            setMessage(mMessage);
        }
        setIndeterminate(mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mHasStarted = false;
    }

    public void setProgress(int value)
    {
        if (mHasStarted)
        {
            mProgress.setProgress(value);
            onProgressChanged();
        } else
        {
            mProgressVal = value;
        }
    }

    public void setSecondaryProgress(int secondaryProgress)
    {
        if (mProgress != null)
        {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else
        {
            mSecondaryProgressVal = secondaryProgress;
        }
    }

    public int getProgress()
    {
        if (mProgress != null)
        {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public int getSecondaryProgress()
    {
        if (mProgress != null)
        {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }

    public int getMax()
    {
        if (mProgress != null)
        {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max)
    {
        if (mProgress != null)
        {
            mProgress.setMax(max);
            onProgressChanged();
        } else
        {
            mMax = max;
        }
    }

    public void incrementProgressBy(int diff)
    {
        if (mProgress != null)
        {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else
        {
            mIncrementBy += diff;
        }
    }

    public void incrementSecondaryProgressBy(int diff)
    {
        if (mProgress != null)
        {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else
        {
            mIncrementSecondaryBy += diff;
        }
    }

    public void setProgressDrawable(Drawable d)
    {
        if (mProgress != null)
        {
            mProgress.setProgressDrawable(d);
        } else
        {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d)
    {
        if (mProgress != null)
        {
            mProgress.setIndeterminateDrawable(d);
        } else
        {
            mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate)
    {
        if (mProgress != null)
        {
            mProgress.setIndeterminate(indeterminate);
        } else
        {
            mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate()
    {
        if (mProgress != null)
        {
            return mProgress.isIndeterminate();
        }
        return mIndeterminate;
    }

    @Override
    public void setMessage(CharSequence message)
    {
        mMessage = message;
        if (mProgress != null)
        {
            onProgressChanged();
            
        }
    }
    
    public long getTotalNumber()
    {
        return mTotalNumber;
    }

    public void setTotalNumber(long totalNumber)
    {
        mTotalNumber = totalNumber;
        if (mHasStarted)
        {
            onProgressChanged();
        }
    }

    public void setCurrentNumber(int value)
    {
        mCurrentNumber = value;
        if (mHasStarted)
        {
            onProgressChanged();
        }
    }

    public int getCurrentNumber()
    {
        return mCurrentNumber;
    }

    /**
     * Change the format of the small text showing current and maximum units of
     * progress. The default is "%1d/%2d". Should not be called during the
     * number is progressing.
     * 
     * @param format
     *            A string passed to {@link String#format String.format()}; use
     *            "%1d" for the current number and "%2d" for the maximum. If
     *            null, nothing will be shown.
     */
    public void setProgressNumberFormat(String format)
    {
        mProgressNumberFormat = format;
        onProgressChanged();
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is {@link NumberFormat#getPercentInstance()
     * NumberFormat.getPercentageInstnace().} Should not be called during the
     * number is progressing.
     * 
     * @param format
     *            An instance of a {@link NumberFormat} to generate the
     *            percentage text. If null, nothing will be shown.
     */
    public void setProgressPercentFormat(NumberFormat format)
    {
        mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged()
    {
        if (mViewUpdateHandler != null
                && !mViewUpdateHandler.hasMessages(0))
        {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
