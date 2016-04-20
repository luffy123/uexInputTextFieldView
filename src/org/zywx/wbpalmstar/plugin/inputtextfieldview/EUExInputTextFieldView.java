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

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class EUExInputTextFieldView extends EUExBase{

	private static final String TAG = "uexInputTextFieldView";
	private static final String INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY = "inputTextFieldViewFunParamsKey";

	private static final int INPUTTEXTFIELDVIEW_MSG_OPEN = 0;
	private static final int INPUTTEXTFIELDVIEW_MSG_CLOSE = 1;
	private static final int INPUTTEXTFIELDVIEW_MSG_SET_INPUT_FOCUSED = 2;
	private static final int INPUTTEXTFIELDVIEW_MSG_HIDE_KEYBOARD = 3;
	private static final int INPUTTEXTFIELDVIEW_MSG_GET_INPUTBAR_HEIGHT = 4;
    
    private ACEInputTextFieldView inputTextFieldView;

    public EUExInputTextFieldView(Context context, EBrowserView view) {
        super(context, view);
    }

    @Override
	public void onHandleMessage(Message message) {
		if (message == null) {
			return;
		}
		switch (message.what) {
		case INPUTTEXTFIELDVIEW_MSG_OPEN:
			handleOpen(message);
			break;
		case INPUTTEXTFIELDVIEW_MSG_CLOSE:
			handleClose();
			break;
		case INPUTTEXTFIELDVIEW_MSG_SET_INPUT_FOCUSED:
			handleSetInputFocusedMsg();
			break;
		case INPUTTEXTFIELDVIEW_MSG_HIDE_KEYBOARD:
			handleHideKeyboard();
			break;
		case INPUTTEXTFIELDVIEW_MSG_GET_INPUTBAR_HEIGHT:
			handleGetInputBarHeight();
			break;
		default:
			;
		}
	}

    public void open(String[] params) {
        sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_OPEN, params);
    }

    public void close(String[] params) {
        sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_CLOSE, params);
    }
    
    public void setInputFocused(String[] params) {
    	sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_SET_INPUT_FOCUSED,params);
    }
    
    public void hideKeyboard(String[] params) {
    	sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_HIDE_KEYBOARD,params);
    }
    
    public void getInputBarHeight(String[] params) {
    	sendMessageWithType(INPUTTEXTFIELDVIEW_MSG_GET_INPUTBAR_HEIGHT,params);
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
    
    private void handleOpen(Message msg) {
        String[] params = msg.getData().getStringArray(
                INPUTTEXTFIELDVIEW_FUN_PARAMS_KEY);
        if (params == null || params.length < 1) return;
        try {
            if (inputTextFieldView != null) return;
            JSONObject json = new JSONObject(params[0]);
            inputTextFieldView = new ACEInputTextFieldView(mContext,json,this);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            addView2CurrentWindow(inputTextFieldView, lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	private void handleClose() {
		if (inputTextFieldView == null) {
			return;
		}
		removeViewFromCurrentWindow(inputTextFieldView);
		inputTextFieldView.onDestroy();
		inputTextFieldView = null;
	}

    private void handleSetInputFocusedMsg() {
        if (inputTextFieldView != null) {
            inputTextFieldView.setInputFocused();
        }
    }
    
    /**
     * 隐藏键盘的接口
     * @param params
     */
	private void handleHideKeyboard() {
		if (inputTextFieldView != null) {
			inputTextFieldView.outOfViewTouch();
		}
	}
	
	private void handleGetInputBarHeight() {
		// 当前输入框的高度是固定的，50dp
		int height = EUExUtil.dipToPixels(50);
		String result = "{\"height\":" + "\"" + height + "\"}";
		String jsCallBack = SCRIPT_HEADER
				+ "if("
				+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_CB_GET_INPUTBAR_HEIGHT
				+ "){"
				+ EInputTextFieldViewUtils.INPUTTEXTFIELDVIEW_FUN_CB_GET_INPUTBAR_HEIGHT
				+ "('" + result + "');}";
		onCallback(jsCallBack);
	}
    
    private void addView2CurrentWindow(View child,
			RelativeLayout.LayoutParams parms) {
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.BOTTOM;
		lp.leftMargin = l;
		lp.bottomMargin = parms.bottomMargin;
		lp.topMargin = t;
		adptLayoutParams(parms, lp);
		mBrwView.addViewToCurrentWindow(child, lp);
	}

    @Override
    protected boolean clean() {
    	Log.i(TAG, "clean");
        close(null);
        return false;
    }
}
