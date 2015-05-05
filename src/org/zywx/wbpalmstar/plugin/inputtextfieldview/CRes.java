package org.zywx.wbpalmstar.plugin.inputtextfieldview;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;

public class CRes{
	private static boolean init;
	public static int app_name;
	public static int plugin_inputtextfieldview_layout;
	public static int plugin_inputtextfieldview_emojicons_grid;
	public static int plugin_inputtextfieldview_emojicons_item;
	
	public static int plugin_inputtextfieldview_parent_layout;
	public static int plugin_inputtextfieldview_pager_layout;
	public static int plugin_inputtextfieldview_btn_emojicon;
	public static int plugin_inputtextfieldview_edit_input;
	public static int plugin_inputtextfieldview_btn_send;
	public static int plugin_inputtextfieldview_emojicons_layout;
	public static int plugin_inputtextfieldview_emojicons_pager;
	public static int plugin_inputtextfieldview_emojicons_pager_indicator;
	public static int plugin_inputtextfieldview_emojicons_grid_view;
	public static int plugin_inputtextfieldview_emojicon_item;

	public static int plugin_inputtextfieldview_pages_pointer_focus;
	public static int plugin_inputtextfieldview_pages_pointer_normal;
	
	public static int plugin_inputtextfieldview_pager_indicator_width;
	public static int plugin_inputtextfieldview_pager_indicator_left;
	public static int plugin_inputtextfieldview_pager_indicator_top;
	public static boolean init(Context context){
		if(init){
			return init;
		}
		String packg = context.getPackageName();
		Resources res = context.getResources();
		app_name = res.getIdentifier("app_name", "string", packg);
		
		plugin_inputtextfieldview_layout=res.getIdentifier("plugin_inputtextfieldview_layout", "layout", packg);
		plugin_inputtextfieldview_emojicons_grid=res.getIdentifier("plugin_inputtextfieldview_emojicons_grid", "layout", packg);
		plugin_inputtextfieldview_emojicons_item=res.getIdentifier("plugin_inputtextfieldview_emojicons_item", "layout", packg);
		
		plugin_inputtextfieldview_parent_layout = res.getIdentifier("plugin_inputtextfieldview_parent_layout", "id", packg);
		plugin_inputtextfieldview_pager_layout = res.getIdentifier("plugin_inputtextfieldview_pager_layout", "id", packg);
		plugin_inputtextfieldview_btn_emojicon = res.getIdentifier("plugin_inputtextfieldview_btn_emojicon", "id", packg);
		plugin_inputtextfieldview_edit_input = res.getIdentifier("plugin_inputtextfieldview_edit_input", "id", packg);
		plugin_inputtextfieldview_btn_send = res.getIdentifier("plugin_inputtextfieldview_btn_send", "id", packg);
		plugin_inputtextfieldview_emojicons_layout = res.getIdentifier("plugin_inputtextfieldview_emojicons_layout", "id", packg);
		plugin_inputtextfieldview_emojicons_pager = res.getIdentifier("plugin_inputtextfieldview_emojicons_pager", "id", packg);
		plugin_inputtextfieldview_emojicons_pager_indicator = res.getIdentifier("plugin_inputtextfieldview_emojicons_pager_indicator", "id", packg);
		plugin_inputtextfieldview_emojicons_grid_view = res.getIdentifier("plugin_inputtextfieldview_emojicons_grid_view", "id", packg);
		plugin_inputtextfieldview_emojicon_item = res.getIdentifier("plugin_inputtextfieldview_emojicon_item", "id", packg);
		
		plugin_inputtextfieldview_pages_pointer_focus=res.getIdentifier("plugin_inputtextfieldview_pages_pointer_focus", "drawable", packg);
		plugin_inputtextfieldview_pages_pointer_normal=res.getIdentifier("plugin_inputtextfieldview_pages_pointer_normal", "drawable", packg);

		plugin_inputtextfieldview_pager_indicator_width=res.getIdentifier("plugin_inputtextfieldview_pager_indicator_width", "dimen", packg);
		plugin_inputtextfieldview_pager_indicator_left=res.getIdentifier("plugin_inputtextfieldview_pager_indicator_left", "dimen", packg);
		plugin_inputtextfieldview_pager_indicator_top=res.getIdentifier("plugin_inputtextfieldview_pager_indicator_top", "dimen", packg);
		Locale language = Locale.getDefault();
		if(language.equals(Locale.CHINA) 
				|| language.equals(Locale.CHINESE) 
				|| language.equals(Locale.TAIWAN) 
				|| language.equals(Locale.TRADITIONAL_CHINESE)
				|| language.equals(Locale.SIMPLIFIED_CHINESE)
				|| language.equals(Locale.PRC)){
			
		}else{
		}
		init = true;
		return true;
	}
}
