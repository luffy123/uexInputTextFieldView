package org.zywx.wbpalmstar.plugin.inputtextfieldview;

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class EUExInputTextFieldView extends EUExBase implements Parcelable {

	public static final String INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY = "inputtextfieldviewFunParamsKey";
	public static final String INPUTTEXTFIELDVIEW_ACTIVITY_ID = "inputtextfieldviewActivityID";

	public static final int INPUTTEXTFIELDVIEW_MSG_OPEN = 0;
	public static final int INPUTTEXTFIELDVIEW_MSG_CLOSE = 1;
	private static LocalActivityManager mgr;

	public EUExInputTextFieldView(Context context, EBrowserView view) {
		super(context, view);
		mgr = ((ActivityGroup) mContext).getLocalActivityManager();
	}

	private void sendMessageWithType(int msgType, String[] params) {
		if (mHandler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = msgType;
		msg.obj = this;
		Bundle b = new Bundle();
		b.putStringArray(INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY, params);
		msg.setData(b);
		mHandler.sendMessage(msg);
	}

	@Override
	public void onHandleMessage(Message msg) {
		if (msg.what == INPUTTEXTFIELDVIEW_MSG_OPEN) {
			handleOpen(msg);
		} else {
			handleMessageInputTextFieldView(msg);
		}
	}

	private void handleMessageInputTextFieldView(Message msg) {
		String activityId = INPUTTEXTFIELDVIEW_ACTIVITY_ID
				+ EUExInputTextFieldView.this.hashCode();
		Activity activity = mgr.getActivity(activityId);

		if (activity != null
				&& activity instanceof ACEInputTextFieldViewActivity) {
			String[] params = msg.getData().getStringArray(
					INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY);
			ACEInputTextFieldViewActivity iActivity = ((ACEInputTextFieldViewActivity) activity);

			switch (msg.what) {
			case INPUTTEXTFIELDVIEW_MSG_CLOSE:
				handleClose(iActivity, mgr);
				break;
			}
		}
	}

	private void handleClose(ACEInputTextFieldViewActivity activity,
			LocalActivityManager mgr) {
		View decorView = activity.getWindow().getDecorView();
		mBrwView.removeViewFromCurrentWindow(decorView);
		String activityId = INPUTTEXTFIELDVIEW_ACTIVITY_ID
				+ EUExInputTextFieldView.this.hashCode();
		mgr.destroyActivity(activityId, true);
	}

	private void handleOpen(Message msg) {
		String[] params = msg.getData().getStringArray(
				INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY);
		try {
			JSONObject json = new JSONObject(params[0]);
			String emojicons = json
					.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_EMOJICONS);

			String activityId = INPUTTEXTFIELDVIEW_ACTIVITY_ID
					+ EUExInputTextFieldView.this.hashCode();
			ACEInputTextFieldViewActivity activity = (ACEInputTextFieldViewActivity) mgr
					.getActivity(activityId);
			if (activity != null) {
				return;
			}
			Intent intent = new Intent(mContext,
					ACEInputTextFieldViewActivity.class);
			intent.putExtra(
					EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_UEXBASE_OBJ,
					this);
			intent.putExtra(
					EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_XML_PATH,
					emojicons);
			boolean hasPlacehold = json
					.has(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_PLACEHOLD);
			if (hasPlacehold) {
				String placehold = json
						.getString(EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_PARAMS_JSON_KEY_PLACEHOLD);
				intent.putExtra(
						EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_EXTRA_EMOJICONS_PLACEHOLD,
						placehold);
			}
			Window window = mgr.startActivity(activityId, intent);
			View decorView = window.getDecorView();
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					dm.widthPixels, RelativeLayout.LayoutParams.WRAP_CONTENT);
			addView2CurrentWindow(activityId, decorView, lp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addView2CurrentWindow(final String activityId, final View child,
			RelativeLayout.LayoutParams parms) {
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.BOTTOM;
		lp.leftMargin = l;
		lp.topMargin = t;
		adptLayoutParams(parms, lp);
		mBrwView.addViewToCurrentWindow(child, lp);
		
		mBrwView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
					float h = child.getHeight();
					float y = event.getY();
					if (dm.heightPixels - Math.abs(y) > h) {
						Activity activity = mgr.getActivity(activityId);
						if (activity != null
								&& activity instanceof ACEInputTextFieldViewActivity) {
							((ACEInputTextFieldViewActivity) activity).outOfViewTouch();
						}
					}
				}
				return false;
			}
		});
	}

	public void open(String[] params) {
		sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_OPEN, params);
	}

	public void close(String[] params) {
		sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_CLOSE, params);
	}

	@Override
	protected boolean clean() {
		close(null);
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}
