/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.plugin.inputtextfieldview;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@SuppressLint("NewApi")
public class ACEInputTextFieldView extends LinearLayout implements
        OnPageChangeListener, OnClickListener,ViewTreeObserver.OnGlobalLayoutListener{

    private String TAG = "ACEInputTextFieldView";
    private EUExInputTextFieldView mUexBaseObj;
    private EditText mEditText;
    private ImageButton mBtnEmojicon;
    private Button mBtnSend;
    private LinearLayout mParentLayout;
    private LinearLayout mPagerLayout;
    private LinearLayout mEmojiconsLayout;
    private ViewPager mEmojiconsPager;
    private LinearLayout mEmojiconsIndicator;
    private boolean isKeyBoardVisible;
    private String mEmojiconsDeletePath;
    private ArrayList<String> mEmojiconsPath = new ArrayList<String>();
    private ArrayList<String> mEmojiconsText = new ArrayList<String>();
    private int mEmojiconsPageIndex;
    private InputMethodManager mInputManager;
    private String mEmojiconswgtResXmlPath;
    private static int NUMBER_OF_EMOJICONS;
    private static int NUMBER_OF_EMOJICONS_PER_PAGE = 23;
    
    private View mOutOfTouchView;
    private LayoutTransition mLayoutTransition;
    private boolean isKeyboardChange = false;
    private int keyBoardHeight = 0;
    private int mBrwViewHeight = 0;

    public ACEInputTextFieldView(Context context,JSONObject params,EUExInputTextFieldView uexBaseObj) {
    	super(context);
    	this.setOrientation(VERTICAL);
    	mUexBaseObj = uexBaseObj;
    	CRes.init(getContext().getApplicationContext());
    	mInputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		View inputLayout = LayoutInflater.from(getContext()).inflate(
				CRes.plugin_inputtextfieldview_layout, null, false);
		mOutOfTouchView = new View(getContext());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
		lp.weight = 1;
		LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lp2.gravity = Gravity.BOTTOM;

		this.addView(mOutOfTouchView,lp);
		this.addView(inputLayout,lp2);
		
		initView();
		initKeyboardParams(params);
		initEvent();

        initPagerIndicator();
        initLayoutTransition();
    }
    
    private void initView(){
    	mParentLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_parent_layout);
        mPagerLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_pager_layout);
        mBtnEmojicon = (ImageButton) findViewById(CRes.plugin_inputtextfieldview_btn_emojicon);
        mEditText = (EditText) findViewById(CRes.plugin_inputtextfieldview_edit_input);
        mBtnSend = (Button) findViewById(CRes.plugin_inputtextfieldview_btn_send);
        mEmojiconsLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_emojicons_layout);
        mEmojiconsPager = (ViewPager) findViewById(CRes.plugin_inputtextfieldview_emojicons_pager);
        mEmojiconsIndicator = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_emojicons_pager_indicator);
    }
    
    private void initEvent(){
    	mBtnEmojicon.setOnClickListener(this);
    	mEditText.setOnClickListener(this);
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.requestFocus();
                mEditText.setCursorVisible(true);
                return false;
            }
        });
        mBtnSend.setOnClickListener(this);
        mEmojiconsPager.setOnPageChangeListener(this);
        mOutOfTouchView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isKeyBoardVisible || mPagerLayout.isShown()) {
					outOfViewTouch();
					return true;
				}
				return false;
			}
		});
        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }
    
    private void initKeyboardParams(JSONObject json){
    	try {
    		// EmojiconsXmlPath
			mEmojiconswgtResXmlPath = json
					.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_EMOJICONS);
			initEmojicons();
	        mEmojiconsPager.setAdapter(new EmotjiconsPagerAdapter());
	        // placeHold
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_PLACEHOLD)) {
				String placehold = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_PLACEHOLD);
				mEditText.setHint(placehold);
			}
			// sendBtn text
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT)) {
				String sendBtnText = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT);
				mBtnSend.setText(sendBtnText);
			}
			// sendBtn text size
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT_SIZE)) {
				String sendBtnTextSize = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT_SIZE);
				try {
					mBtnSend.setTextSize(Float.parseFloat(sendBtnTextSize));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// sendBtn text color
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT_COLOR)) {
				String btnTextColor = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_TEXT_COLOR);
				mBtnSend.setTextColor(BUtility.parseColor(btnTextColor));
			}
			// Selector need StateListDrawable
			StateListDrawable myGrad = (StateListDrawable) mBtnSend
					.getBackground();
			// sendBtn color normal
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_COLOR)) {
				String btnColor = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_COLOR);
				if(!TextUtils.isEmpty(btnColor)){
					GradientDrawable drawable = (GradientDrawable) myGrad
							.getCurrent();
					drawable.setColor(BUtility.parseColor(btnColor));
				}
			}
			// sendBtn color pressed
			if (json.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_COLOR_DOWN)) {
				String sendBtnbgColorDown = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_BTN_COLOR_DOWN);
				if(!TextUtils.isEmpty(sendBtnbgColorDown)){
					mBtnSend.setPressed(true);
					GradientDrawable drawable = (GradientDrawable) myGrad
							.getCurrent();
					drawable.setColor(BUtility.parseColor(sendBtnbgColorDown));
					mBtnSend.setPressed(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**
     * 	initLayout Animator
     */
	private void initLayoutTransition(){
		if(mLayoutTransition != null){
			return;
		}
		mLayoutTransition = new LayoutTransition();
		mLayoutTransition.setAnimator(LayoutTransition.CHANGE_APPEARING,  
				mLayoutTransition.getAnimator(LayoutTransition.CHANGE_APPEARING));
		mLayoutTransition.setAnimator(LayoutTransition.APPEARING, null);
		mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
		mLayoutTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,	null);
		// mLayoutTransition.getAnimator(LayoutTransition.CHANGE_DISAPPEARING));
		mLayoutTransition.addTransitionListener(new TransitionListener() {
			
			@Override
			public void startTransition(LayoutTransition transition,
					ViewGroup container, View view, int transitionType) {
			}
			
			@Override
			public void endTransition(LayoutTransition transition, ViewGroup container,
					View view, int transitionType) {
				if (view.getId() == CRes.plugin_inputtextfieldview_parent_layout && transitionType == LayoutTransition.CHANGE_APPEARING ) {
					//Parent view height change ,so input and pager show together.
					goScroll(0);
					jsonKeyBoardShowCallback(isKeyBoardVisible || mPagerLayout.isShown() ? 1 : 0);
				} else if (view.getId() == CRes.plugin_inputtextfieldview_pager_layout && transitionType == LayoutTransition.DISAPPEARING) {
					if(!isKeyBoardVisible)
						backScroll();
					jsonKeyBoardShowCallback(isKeyBoardVisible || mPagerLayout.isShown() ? 1 : 0);
				}
			}
		});
		mParentLayout.setLayoutTransition(mLayoutTransition);
    }

	public void onDestroy() {
		try {
			if (isKeyBoardVisible) {
				mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
			}
			mParentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Reading all emoticons in local cache
     */
    private void initEmojicons() {
        InputStream in = null;
        try {
            String xmlPath = mEmojiconswgtResXmlPath
                    .substring(BUtility.F_Widget_RES_SCHEMA.length());
            String emojiconsFolder = BUtility.F_Widget_RES_path
                    + xmlPath.substring(0, xmlPath.lastIndexOf("/") + 1);
            String resXmlPath = BUtility.F_Widget_RES_path + xmlPath;
            in = getContext().getAssets().open(resXmlPath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, "utf-8");
            int tokenType = 0;
            boolean needContinue = true;
            do {
                tokenType = parser.next();
				switch (tokenType) {
				case XmlPullParser.START_TAG:
					String localName = (parser.getName()).toLowerCase();
					if ("emojicons".equals(localName)) {
						mEmojiconsDeletePath = emojiconsFolder
								+ parser.getAttributeValue(null, "delete");
					} else if ("key".equals(localName)) {
						mEmojiconsText.add(parser.nextText());
					} else if ("string".equals(localName)) {
						mEmojiconsPath.add(emojiconsFolder + parser.nextText());
					}
					break;
				case XmlPullParser.END_DOCUMENT:
					needContinue = false;
					break;
				}
            } while (needContinue);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        NUMBER_OF_EMOJICONS = mEmojiconsPath.size();
    }

    /**
     * For loading smileys from assets
     */
    private Bitmap getBitmap(String path) {
        AssetManager mngr = this.getContext().getAssets();
        InputStream in = null;
        try {
            in = mngr.open(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }

    private class EmotjiconsPagerAdapter extends PagerAdapter {
        public EmotjiconsPagerAdapter() {
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View layout = ((Activity)getContext()).
                    getLayoutInflater().inflate(CRes.plugin_inputtextfieldview_emojicons_grid, null);
            int initialPosition = position * NUMBER_OF_EMOJICONS_PER_PAGE;
            ArrayList<String> emoticonsInAPage = new ArrayList<String>();

            for (int i = initialPosition; i <= initialPosition
                    + NUMBER_OF_EMOJICONS_PER_PAGE
                    && i <= mEmojiconsPath.size(); i++) {
                if (i == initialPosition + NUMBER_OF_EMOJICONS_PER_PAGE
                        || i == mEmojiconsPath.size()) {
                    emoticonsInAPage.add(mEmojiconsDeletePath);
                } else {
                    emoticonsInAPage.add(mEmojiconsPath.get(i));
                }
            }

            GridView grid = (GridView) layout
                    .findViewById(CRes.plugin_inputtextfieldview_emojicons_grid_view);
            EmojiconsGridAdapter adapter = new EmojiconsGridAdapter(
                    emoticonsInAPage);
            grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
            grid.setAdapter(adapter);
			/*if (keyBoardHeight != 0) {
				grid.setVerticalSpacing((int) (keyBoardHeight / 25 * 2));
				layout.setPadding(layout.getPaddingLeft(),
						(int) (keyBoardHeight / 250 * 25),
						layout.getPaddingRight(), layout.getPaddingBottom());
			}*/
            mEmojiconsPageIndex = position;
            container.addView(layout);
            return layout;
        }

        @Override
        public int getCount() {
            return (int) Math.ceil((double) NUMBER_OF_EMOJICONS
                    / (double) NUMBER_OF_EMOJICONS_PER_PAGE);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }
    }

    public class EmojiconsGridAdapter extends BaseAdapter {
        private ArrayList<String> paths;

        public EmojiconsGridAdapter(ArrayList<String> paths) {
            this.paths = paths;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layout = ((Activity)getContext()).
                    getLayoutInflater().inflate(
                    CRes.plugin_inputtextfieldview_emojicons_item, null);
            ImageView image = (ImageView) layout
                    .findViewById(CRes.plugin_inputtextfieldview_emojicon_item);
            final String path = paths.get(position);
            image.setImageBitmap(getBitmap(path));
            if (position == paths.size() - 1) {
                image.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        KeyEvent event = new KeyEvent(0, 0, 0,
                                KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                                KeyEvent.KEYCODE_ENDCALL);
                        mEditText.dispatchKeyEvent(event);
                    }
                });
                image.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mEditText.setText(null);
                        return false;
                    }
                });
            } else {
                final Drawable drawable = image.getDrawable();
                image.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence text = mEmojiconsText.get(mEmojiconsPath
                                .indexOf(path));
                        ImageSpan imageSpan = new ImageSpan(drawable);
                        SpannableString spannable = new SpannableString(text);
                        spannable.setSpan(imageSpan, 0, text.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mEditText.getText().insert(
                                mEditText.getSelectionStart(), spannable);
                    }
                });
            }
            return layout;
        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int position) {
            return paths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private void initPagerIndicator() {
        int emojiconsPagerSize = (int) Math.ceil((double) NUMBER_OF_EMOJICONS
                / (double) NUMBER_OF_EMOJICONS_PER_PAGE);
        if (emojiconsPagerSize > 1) {
            initPagerIndicator(emojiconsPagerSize, mEmojiconsIndicator);
            updateCurrentPage(mEmojiconsPageIndex, mEmojiconsIndicator);
        } else {
            mEmojiconsIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void initPagerIndicator(int pagerSize, LinearLayout layout) {
        int childCount = layout.getChildCount();
        if (pagerSize == childCount) {
            return;
        }
        int width = getResources().getDimensionPixelSize(
                CRes.plugin_inputtextfieldview_pager_indicator_width);
        int left = getResources().getDimensionPixelSize(
                CRes.plugin_inputtextfieldview_pager_indicator_left);
        int top = getResources().getDimensionPixelSize(
                CRes.plugin_inputtextfieldview_pager_indicator_top);
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                width, width);
        viewParams.setMargins(left, top, left, top);
        if (pagerSize > childCount) {// 需要增加
            while (childCount < pagerSize) {
                ImageView imageView = new ImageView(this.getContext());
                layout.addView(imageView, childCount, viewParams);
                childCount++;
            }
        } else {
            while (childCount > pagerSize) {
                layout.removeViewAt(childCount);
                childCount--;
            }
        }
    }

    private void updateCurrentPage(int index, LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View view = layout.getChildAt(i);
            if (i == index) {
                view.setBackgroundResource(CRes.plugin_inputtextfieldview_pages_pointer_focus);
            } else {
                view.setBackgroundResource(CRes.plugin_inputtextfieldview_pages_pointer_normal);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int index, float positionOffset,
                               int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int index) {
        if (mEmojiconsIndicator.getVisibility() == View.VISIBLE) {
            mEmojiconsPageIndex = index;
            updateCurrentPage(index, mEmojiconsIndicator);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == CRes.plugin_inputtextfieldview_btn_emojicon) {
            toggleBtnEmojicon(mEmojiconsLayout.isShown() ? false : true);
        } else if (id == CRes.plugin_inputtextfieldview_btn_send) {
            mEmojiconsLayout.setVisibility(View.GONE);
            mPagerLayout.setVisibility(View.GONE);
            InputMethodManager imm= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            toggleBtnSend();
        } else if (id == CRes.plugin_inputtextfieldview_edit_input) {
            if (mPagerLayout.isShown()) {
                mPagerLayout.setVisibility(View.GONE);
            }
        }
    }

    private void toggleBtnEmojicon(boolean visible) {
        if (visible) {
            if (isKeyBoardVisible) {
            	backScroll();
				mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPagerLayout.setVisibility(View.VISIBLE);
                    mEmojiconsLayout.setVisibility(View.VISIBLE);
                    mEditText.requestFocus();
                }
            }, 200);
        } else {
            if (!isKeyBoardVisible) {
                mInputManager.toggleSoftInputFromWindow(
                        mEditText.getWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
            }
            mEmojiconsLayout.setVisibility(View.GONE);
            mPagerLayout.setVisibility(View.GONE);
        }
    }

    private void toggleBtnSend() {
    	Log.i(TAG, " toggleBtnSend mEditText " + mEditText.getText().toString());
    	jsonSendDataCallback();
    	jsonSendDataJsonCallback();
        mEditText.setText(null);
    }
    
	private void jsonSendDataCallback() {
		// TODO	send callback String
		if (mUexBaseObj != null) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject
						.put(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_EMOJICONS_TEXT,
								mEditText.getText().toString());
				String js = EUExInputTextFieldView.SCRIPT_HEADER
						+ "if("
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_COMMIT
						+ "){"
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_COMMIT
						+ "('" + jsonObject.toString() + "');}";
				mUexBaseObj.onCallback(js);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    private void jsonSendDataJsonCallback() {
		// TODO send callback Json
		if (mUexBaseObj != null) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject
						.put(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_EMOJICONS_TEXT,
								mEditText.getText().toString());
				String js = EUExInputTextFieldView.SCRIPT_HEADER + "if("
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_COMMIT_JSON + "){"
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_COMMIT_JSON + "("
						+ jsonObject.toString() + ");}";
				mUexBaseObj.onCallback(js);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    private void jsonKeyBoardShowCallback(int status){
    	// TODO keyboard show status callback 
		if (mUexBaseObj != null) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject
						.put(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_KEYBOARDSHOW_STATUS,
								status);
				String js = EUExInputTextFieldView.SCRIPT_HEADER
						+ "if("
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_KEYBOARD_SHOW
						+ "){"
						+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_ON_KEYBOARD_SHOW
						+ "('" + jsonObject.toString()
						+ "');}";
				mUexBaseObj.onCallback(js);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    public void outOfViewTouch() {
    	backScroll();
		if (isKeyBoardVisible) {
			mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
			mEditText.clearFocus();
            mEditText.setCursorVisible(false);
		}
		if (mPagerLayout.isShown()) {
			mPagerLayout.setVisibility(View.GONE);
		}
    }

    public void setInputFocused(){
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.clearFocus();
        mEditText.requestFocus();
        mInputManager.showSoftInput(mEditText, 0);
    }
    
    /**
	 * Checking keyboard visibility
	 */
	@Override
	public void onGlobalLayout() {
		// TODO Checking keyboard visibility
		Rect r = new Rect();
		mParentLayout.getWindowVisibleDisplayFrame(r);
		int screenHeight = mParentLayout.getRootView()
				.getHeight();
		int heightDifference = screenHeight - (r.bottom);
		boolean isKeyBoardChange = isKeyBoardVisible;
		if (heightDifference > 100) {
			isKeyBoardVisible = true;
			//弹出键盘的时候,判断下俩者有弹出状态则设置隐藏  2015-08-12
			if(mPagerLayout.isShown()){
				mPagerLayout.setVisibility(View.GONE);
				/*backScroll();
				goScroll(heightDifference);*/
			}
			keyBoardHeight = heightDifference;
			//changeKeyBoardHeight(heightDifference);
		} else {
			isKeyBoardVisible = false;
		}
		if (isKeyBoardVisible && !isKeyBoardChange) {
			goScroll(heightDifference);
		} else if (!mPagerLayout.isShown() && !isKeyBoardVisible ){
			backScroll();
		}
		boolean isChange = (isKeyBoardChange != isKeyBoardVisible) ;
		if (isChange){
			jsonKeyBoardShowCallback(isKeyBoardVisible || mPagerLayout.isShown() ? 1 : 0);
		}
	}
    
    private void goScroll(int heightDifference) {
		if(!isKeyboardChange){
			Log.i(TAG, "↑");
			isKeyboardChange = true;
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mUexBaseObj.mBrwView
					.getLayoutParams();
			int tempHeight = lp.height;
			lp.weight = 0;
			if (tempHeight == LayoutParams.MATCH_PARENT) {
				tempHeight = mUexBaseObj.mBrwView.getHeight();
			}
			if (mBrwViewHeight == 0) {
				mBrwViewHeight = lp.height;
			}
			int keyboardHeight = mPagerLayout.isShown() ? mPagerLayout.getHeight() : 0;
			int inputHeight = isKeyBoardVisible || mPagerLayout.isShown() ? EUExUtil.dipToPixels(50) : 0;
			int screenHeight = mParentLayout.getRootView().getHeight();
			if(mBrwViewHeight > 0 || tempHeight > screenHeight - heightDifference){
				inputHeight = heightDifference + inputHeight;
			}
			Log.i(TAG, "Move! height:" + (tempHeight - keyboardHeight - inputHeight)
					+ " tempHeight:" + tempHeight 
					+ " ParentkeyboardHeight:" + keyboardHeight 
					+ " inputHeight:" + inputHeight);
			lp.height = tempHeight - keyboardHeight - inputHeight;
			((ViewGroup)mUexBaseObj.mBrwView).setLayoutParams(lp);
			((ViewGroup)mUexBaseObj.mBrwView).invalidate();
		}
	}

	private void backScroll() {
		if(isKeyboardChange){
			Log.i(TAG, "↓");
			isKeyboardChange = false;
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mUexBaseObj.mBrwView
					.getLayoutParams();
			lp.height = mBrwViewHeight;
			lp.weight = 1;
			((ViewGroup)mUexBaseObj.mBrwView).setLayoutParams(lp);
			((ViewGroup)mUexBaseObj.mBrwView).invalidate();
		}
	}
	
	private void changeKeyBoardHeight(int keyBoardHeight){
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mPagerLayout.getLayoutParams();
		int pagerHeight = lp.height;
		if(pagerHeight != keyBoardHeight){
			lp.height = keyBoardHeight;
			mPagerLayout.setLayoutParams(lp);
			this.keyBoardHeight = keyBoardHeight;
		}
	}

}