package org.zywx.wbpalmstar.plugin.inputtextfieldview;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.zywx.wbpalmstar.base.BUtility;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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

public class ACEInputTextFieldViewActivity extends FragmentActivity implements
		OnPageChangeListener, TextWatcher, OnClickListener {

	private String TAG = "ACEInputTextFieldViewActivity";
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, " onCreate ");
		CRes.init(getApplication());
		Intent intent = getIntent();
		if (intent
				.hasExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_UEXBASE_OBJ)) {
			mUexBaseObj = (EUExInputTextFieldView) intent
					.getParcelableExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_UEXBASE_OBJ);
		}
		if (intent
				.hasExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_XML_PATH)) {
			mEmojiconswgtResXmlPath = intent
					.getStringExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_XML_PATH);
		}

		setContentView(CRes.plugin_inputtextfieldview_layout);
		mParentLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_parent_layout);
		mPagerLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_pager_layout);

		mBtnEmojicon = (ImageButton) findViewById(CRes.plugin_inputtextfieldview_btn_emojicon);
		mBtnEmojicon.setOnClickListener(this);
		mEditText = (EditText) findViewById(CRes.plugin_inputtextfieldview_edit_input);
		mEditText.addTextChangedListener(this);
		mEditText.setOnClickListener(this);
		if (intent
				.hasExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_PLACEHOLD)) {
			String hint = intent
					.getStringExtra(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_PLACEHOLD);
			mEditText.setHint(hint);
		}

		mBtnSend = (Button) findViewById(CRes.plugin_inputtextfieldview_btn_send);
		mBtnSend.setOnClickListener(this);

		mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		initEmojicons();

		mEmojiconsLayout = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_emojicons_layout);
		mEmojiconsPager = (ViewPager) findViewById(CRes.plugin_inputtextfieldview_emojicons_pager);
		mEmojiconsPager.setAdapter(new EmotjiconsPagerAdapter());
		mEmojiconsPager.setOnPageChangeListener(this);
		mEmojiconsIndicator = (LinearLayout) findViewById(CRes.plugin_inputtextfieldview_emojicons_pager_indicator);

		initPagerIndicator();
		checkKeyboardHeight(mParentLayout);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (isKeyBoardVisible) {
			mInputManager.toggleSoftInputFromWindow(
					mEditText.getWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
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
			in = getAssets().open(resXmlPath);
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
		AssetManager mngr = getAssets();
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
			View layout = getLayoutInflater().inflate(
					CRes.plugin_inputtextfieldview_emojicons_grid, null);
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
			View layout = getLayoutInflater().inflate(
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
				ImageView imageView = new ImageView(this);
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
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mBtnSend.setVisibility(mEditText.getText().length() != 0 ? View.VISIBLE
				: View.GONE);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	/**
	 * Checking keyboard visibility
	 */
	private void checkKeyboardHeight(final View parentLayout) {
		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);
						int screenHeight = parentLayout.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);
						if (heightDifference > 100) {
							isKeyBoardVisible = true;
							if (!mEditText.isFocused()) {
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										mEditText.requestFocus();
									}
								}, 100);
							}
						} else {
							isKeyBoardVisible = false;
						}
					}
				});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == CRes.plugin_inputtextfieldview_btn_emojicon) {
			toggleBtnEmojicon(mEmojiconsLayout.isShown() ? false : true);
		} else if (id == CRes.plugin_inputtextfieldview_btn_send) {
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
				mInputManager.toggleSoftInputFromWindow(
						mEditText.getWindowToken(),
						InputMethodManager.SHOW_FORCED, 0);
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
		mEditText.setText(null);
	}

	public void outOfViewTouch() {
		if (isKeyBoardVisible) {
			mInputManager.toggleSoftInputFromWindow(mEditText.getWindowToken(),
					InputMethodManager.SHOW_FORCED, 0);
		}
		if (mPagerLayout.isShown()) {
			mPagerLayout.setVisibility(View.GONE);
		}
	}
}