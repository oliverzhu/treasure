/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.home.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.home.R;

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape.
 */
public class FragmentLayout extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_layout);
    }


    /**
     * This is a secondary activity, to show what the user has selected
     * when the screen is not large enough to show it all in one activity.
     */

    public static class DetailsActivity extends FragmentActivity {
    	private static final String TAG = "DetailsActivity";
    	

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        	Log.i(TAG, "onCreate");
            super.onCreate(savedInstanceState);

            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                // If the screen is now in landscape mode, we can show the
                // dialog in-line with the list so we don't need this activity.
                finish();
                return;
            }

            if (savedInstanceState == null) {
                // During initial setup, plug in the details fragment.
                DetailsFragment details = new DetailsFragment();
                details.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
            }
        }
        
        @Override
        protected void onStart() {
        	super.onStart();
        	Log.i(TAG, "onStart");
        }
        
        @Override
        protected void onResume() {
        	super.onResume();
        	Log.i(TAG, "onResume");
        }
        
        @Override
        protected void onPause() {
        	super.onPause();
        	Log.i(TAG, "onPause");
        }
        
        @Override
        protected void onStop() {
        	super.onStop();
        	Log.i(TAG, "onStop");
        }
        
        @Override
        protected void onDestroy() {
        	super.onDestroy();
        	Log.i(TAG, "onDestroy");
        }
    }


    /**
     * This is the "top-level" fragment, showing a list of items that the
     * user can pick.  Upon picking an item, it takes care of displaying the
     * data to the user as appropriate based on the currrent UI layout.
     */

    public static class TitlesFragment extends ListFragment {
        boolean mDualPane;
        int mCurCheckPosition = 0;
        int mShownCheckPosition = -1;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Populate list with our static array of titles.
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, Shakespeare.TITLES));

            // Check to see if we have a frame in which to embed the details
            // fragment directly in the containing UI.
            View detailsFrame = null;
            		//getActivity().findViewById(R.id.details);
            mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
                mShownCheckPosition = savedInstanceState.getInt("shownChoice", -1);
            }

            if (mDualPane) {
                // In dual-pane mode, the list view highlights the selected item.
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                // Make sure our UI is in the correct state.
                showDetails(mCurCheckPosition);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
            outState.putInt("shownChoice", mShownCheckPosition);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            showDetails(position);
        }

        /**
         * Helper function to show the details of a selected item, either by
         * displaying a fragment in-place in the current UI, or starting a
         * whole new activity in which it is displayed.
         */
        void showDetails(int index) {
            mCurCheckPosition = index;

            if (mDualPane) {
                // We can display everything in-place with fragments, so update
                // the list to highlight the selected item and show the data.
                getListView().setItemChecked(index, true);

                if (mShownCheckPosition != mCurCheckPosition) {
                    // If we are not currently showing a fragment for the new
                    // position, we need to create and install a new one.
                    DetailsFragment df = DetailsFragment.newInstance(index);

                    // Execute a transaction, replacing any existing fragment
                    // with this one inside the frame.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.details, df);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                    mShownCheckPosition = index;
                }

            } else {
                // Otherwise we need to launch a new activity to display
                // the dialog fragment with selected text.
                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
            }
        }
    }


    /**
     * This is the secondary fragment, displaying the details of a particular
     * item.
     */

    public static class DetailsFragment extends Fragment {
    	private static final String TAG = "DetailsFragment";
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        public static DetailsFragment newInstance(int index) {
            DetailsFragment f = new DetailsFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("index", index);
            f.setArguments(args);

            return f;
        }
        
        @Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			Log.i(TAG, "onAttach");
		}

        @Override
        public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	Log.i(TAG, "onCreate");
        }

		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
			Log.i(TAG, "onCreateView");
            if (container == null) {
                // We have different layouts, and in one of them this
                // fragment's containing frame doesn't exist.  The fragment
                // may still be created from its saved state, but there is
                // no reason to try to create its view hierarchy because it
                // won't be displayed.  Note this is not needed -- we could
                // just run the code below, where we would create and return
                // the view hierarchy; it would just never be used.
                return null;
            }

            ScrollView scroller = new ScrollView(getActivity());
            TextView text = new TextView(getActivity());
            int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, getActivity().getResources().getDisplayMetrics());
            text.setPadding(padding, padding, padding, padding);
            scroller.addView(text);
            text.setText(Shakespeare.DIALOGUE[getArguments().getInt("index", 0)]);
            return scroller;
        }
		
		 @Override
			public void onActivityCreated(Bundle savedInstanceState) {
				super.onActivityCreated(savedInstanceState);
				Log.i(TAG, "onActivityCreated");
			}
		 

			@Override
			public void onStart() {
				super.onStart();
				Log.i(TAG, "onStart");
			}

			@Override
			public void onResume() {
				super.onResume();
				Log.i(TAG, "onResume");
			}
			
			@Override
			public void onPause() {
				super.onPause();
				Log.i(TAG, "onPause");
			}
			

			@Override
			public void onStop() {
				super.onStop();
				Log.i(TAG, "onStop");
			}

			@Override
			public void onDestroyView() {
				super.onDestroyView();
				Log.i(TAG, "onDestroyView");
			}
			
			@Override
			public void onDestroy() {
				super.onDestroy();
				Log.i(TAG, "onDestroy");
			}

			@Override
			public void onDetach() {
				super.onDetach();
				Log.i(TAG, "onDetach");
			}
    }

}
