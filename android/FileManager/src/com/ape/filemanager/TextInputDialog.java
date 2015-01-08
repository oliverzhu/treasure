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

package com.ape.filemanager;

import java.io.UnsupportedEncodingException;

import com.ape.filemanager.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextInputDialog extends AlertDialog {
    private static final String TAG = "TextInputDialog";
    private String mInputText;
    private String mTitle;
    private String mMsg;
    private OnFinishListener mListener;
    private Context mContext;
    private View mView;
    private EditText mFolderName;
    
    private static final int INVIND_RES_ID = -1;
    private static final int FILENAME_MAX_LENGTH = 255;
    private static final String INVALID_CHAR = ".*[/\\\\:*?\"<>|\t].*";
    private static final String CLOUD_INVALID_CHAR = ".*[/\\\\:*?\"<>|\t#%&^{}].*";

    private boolean mIsCloudInput = false;

    public interface OnFinishListener {
        // return true to accept and dismiss, false reject
        boolean onFinish(String text);
    }

    public TextInputDialog(Context context, String title, String msg, String text, OnFinishListener listener) {
        super(context);
        mTitle = title;
        mMsg = msg;
        mListener = listener;
        mInputText = text;
        mContext = context;
    }

    public String getInputText() {
        return mInputText;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);

        setTitle(mTitle);
        setMessage(mMsg);

        mFolderName = (EditText) mView.findViewById(R.id.text);
        mFolderName.setText(mInputText);
        setTextChangedCallback(mFolderName, this);

        setView(mView);
        setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            mInputText = mFolderName.getText().toString();
                            if (mListener.onFinish(mInputText)) {
                                dismiss();
                            }
                        }
                    }
                });
        setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
                (DialogInterface.OnClickListener) null);

        super.onCreate(savedInstanceState);
    }
    
    private void setEditTextFilter(final EditText edit, final int maxLength) {
        InputFilter filter = new InputFilter.LengthFilter(maxLength) {
            boolean mHasToasted = false;
            private static final int VIBRATOR_TIME = 100;

            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                String oldText = null;
                String newText = null;
                int oldSize = 0;
                int newSize = 0;
                if (mFolderName != null) {
                    oldText = mFolderName.getText().toString();
                    //oldSize = oldText.length();
                    try {
                        oldSize = oldText.getBytes("UTF-8").length;
                        Log.d(TAG, "filter,oldSize=" + oldSize + ",oldText=" + oldText);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        oldSize = oldText.length();
                    }
                }
                if (source != null) {
                    newText = source.toString();
                   // newSize = newText.length();
                    try {
                        newSize = newText.getBytes("UTF-8").length;
                        Log.d(TAG, "filter,newSize=" + newSize + ",newText =" + newText);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        newSize = newText.length();
                    }
                }
                
                if (source != null && source.length() > 0 && (oldSize + newSize) > maxLength) {
                    Log.d(TAG, "oldSize + newSize) > maxLength,source.length()="
                            + source.length());
                    Vibrator vibrator = (Vibrator) mContext.getSystemService(
                            Context.VIBRATOR_SERVICE);
                    boolean hasVibrator = vibrator.hasVibrator();
                    if (hasVibrator) {
                        vibrator.vibrate(new long[] { VIBRATOR_TIME, VIBRATOR_TIME },
                                INVIND_RES_ID);
                    }
                    Log.w(TAG, "input out of range,hasVibrator:" + hasVibrator);
                    return "";
                }
                if (source != null && source.length() > 0 && !mHasToasted
                        && dstart == 0) {
                    if (source.charAt(0) == '.') {
                        Toast.makeText(mContext, R.string.create_hidden_file, Toast.LENGTH_SHORT).show();
                        mHasToasted = true;
                    }
                }
                return super.filter(source, start, end, dest, dstart, dend);
            }
        };
        edit.setFilters(new InputFilter[] { filter });
    }

    /**
     * This method register callback and set filter to Edit, in order to make sure that user
     * input is legal. The input can't be illegal filename and can't be too long.
     * 
     * @param editText EditText, which user type on
     * @param dialog dialog, which EditText associated with
     */
    protected void setTextChangedCallback(EditText editText,
            final AlertDialog dialog) {
        setEditTextFilter(editText, FILENAME_MAX_LENGTH);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                    int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String invalidChars = mIsCloudInput ? CLOUD_INVALID_CHAR : INVALID_CHAR;

                if (s.toString().length() <= 0
                        || s.toString().matches(invalidChars))
                {
                    // characters not allowed
                    if (s.toString().matches(invalidChars))
                    {
                        Toast.makeText(mContext, R.string.invalid_char_prompt,
                                Toast.LENGTH_SHORT).show();
                    }
                    Button botton = dialog
                            .getButton(DialogInterface.BUTTON_POSITIVE);
                    if (botton != null)
                    {
                        botton.setEnabled(false);
                    }
                } else
                {
                    Button botton = dialog
                            .getButton(DialogInterface.BUTTON_POSITIVE);
                    if (botton != null)
                    {
                        botton.setEnabled(true);
                    }
                }
            }
        });
    }

    public void setCloudInput(boolean isCloud)
    {
        mIsCloudInput = isCloud;
    }
}
