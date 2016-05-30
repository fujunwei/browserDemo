package com.example.browserdemo;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;

public class PopMenu {

	private Context context;
	private PopupWindow popupWindow;
	private GridView gridView;
	Handler myHandler;

	private GridViewAdapter gridViewAdapter;

	public static final int ADD_COLLECT = 1; // 添加收藏
	public static final int COLLECT_FOLDER = 2;// 收藏夹
	public static final int SHARE = 3;// 分享
	public static final int SETTING = 4;// 设置
	public static final int REFRESH = 6;// 刷新
	public static final int TAB_PAGE = 7;// 桌面网页

	public PopMenu(Context context, Handler myHandler) {
		this.context = context;
		this.myHandler = myHandler;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View view = inflater.inflate(R.layout.popmenu, null);
		gridView = (GridView) view.findViewById(R.id.myGridView);
		gridViewAdapter = new GridViewAdapter(context);
		gridView.setAdapter(gridViewAdapter);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setOnItemClickListener(onItemClickistener);
		
		popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, context
				.getResources().getDimensionPixelSize(R.dimen.popmenu_h));
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOutsideTouchable(true);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((keyCode == KeyEvent.KEYCODE_MENU)
						&& (popupWindow.isShowing())) {
					popupWindow.dismiss();// 这里写明模拟menu的PopupWindow退出就行
					return true;
				}
				return false;
			}
		});

	}

	private OnItemClickListener onItemClickistener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0://添加收藏---这边还需判断是否是主页等等
				myHandler.sendEmptyMessage(ADD_COLLECT);
				dismiss();
				break;
				
			case 1://收藏夹
				myHandler.sendEmptyMessage(COLLECT_FOLDER);
				dismiss();
				break;
			case 2://分享
				myHandler.sendEmptyMessage(SHARE);
				dismiss();
				break;
			case 3://设置
				myHandler.sendEmptyMessage(SETTING);
				dismiss();
				break;
			case 4://刷新
				myHandler.sendEmptyMessage(REFRESH);
				dismiss();
				break;
			case 5://桌面网页
				myHandler.sendEmptyMessage(TAB_PAGE);
				dismiss();
				break;
			default:
				break;
			}
			
		}
	};
	private int dip2px(float dpValue) {
		float mDensity =context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * mDensity + 0.5f);
	}

	
	public void show(View parent) { 
		popupWindow.showAsDropDown(parent,0,dip2px(-7.5f));
		popupWindow.update(); 
		
		if(gridViewAdapter!=null){
			gridViewAdapter.notifyDataSetChanged();
		} 
	 
	}
	 
	public void dismiss() { 
		popupWindow.dismiss();
	} 
	public boolean isShow(){
		
		if(popupWindow!=null && popupWindow.isShowing()){
			return true;
		}
		return false;
	}
	
}
