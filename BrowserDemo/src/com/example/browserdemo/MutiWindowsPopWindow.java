package com.example.browserdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.browserdemo.bean.MutiWindowItem;
import com.example.browserdemo.bean.SingleWindowTabInfo;
import com.infinit.wobrowser.utils.Utils;

/**
 * 多标签页窗口
 */
public class MutiWindowsPopWindow {

	private Context context;
	public static final int OPEN_TAB = 1001;
	public static final int CREATE_TAB = 1002;
	public static final int CLOSE_TAB = 1003;
	public static final int CLOSE_TABS = 1004;

	private ListView mutiWindowsListView;
	private TextView mutiWindowsCreateNewTab; // 新建标签页
	private MutiWindowsDialogAdapter mutiAdapter;

	private PopupWindow popupWindow;
	private Handler myHandler;

	public MutiWindowsPopWindow(Context context, Handler myHandler) {
		this.context = context;
		this.myHandler = myHandler;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View view = inflater.inflate(R.layout.activity_dialog, null);

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

		mutiWindowsCreateNewTab = (TextView) view
				.findViewById(R.id.activity_dialog_newTab);
		mutiWindowsListView = (ListView) view
				.findViewById(R.id.activity_dialog_listview);

		mutiAdapter = new MutiWindowsDialogAdapter(context);
		mutiWindowsListView.setAdapter(mutiAdapter);

		setDialogBaseParameters();
		registerListener();
	}

	private void registerListener() {
		// 新建标签页按钮监听事件
		mutiWindowsCreateNewTab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (HomeActivity.mutiWindowsSources.size() == 8) {
					Toast.makeText(context, "标签已满", Toast.LENGTH_LONG).show();
				} else {
					closeMutiWindows(CREATE_TAB,
							HomeActivity.mutiWindowsSources.size(), -1);
				}
			}
		});
	}

	/**
	 * 设置Dialog 基本属性:高以及弹出位置
	 */
	private void setDialogBaseParameters() {
		int size = HomeActivity.mutiWindowsSources.size();
		if (size == 8) {
			size = 7;
		}
		int dialogHeight = Utils.dip2px(context, 70)
				+ Utils.dip2px(context, 64) * size;
		if (dialogHeight > getHeightPixels() - 200)
			dialogHeight = getHeightPixels() - 200;
		popupWindow.setHeight(dialogHeight);
	}

	/**
	 * 关闭窗口
	 * 
	 * @param tabTouchType
	 *            1001:打开标签 1002:新增标签
	 * @param clickPos
	 *            点击标签位置
	 */
	private void closeMutiWindows(int tabTouchType, int displayPos, int delePos) {
		Message msg = myHandler.obtainMessage();
		MutiWindowItem item = new MutiWindowItem();
		item.setClicktype(tabTouchType);
		item.setDisplayPos(displayPos);
		item.setDelePos(delePos);
		msg.obj = item;
		myHandler.sendMessage(msg);
	}

	public void show(View parent) {
		refresh();
		popupWindow.update();
		popupWindow.showAsDropDown(parent, 0, Utils.dip2px(context, -0.5f));
	}

	public void refresh() {
		if (mutiAdapter != null) {
			setDialogBaseParameters();
			mutiAdapter.notifyDataSetChanged();
		}
	}

	public void dismiss() {
		popupWindow.dismiss();
	}

	public boolean isShow() {

		if (popupWindow != null && popupWindow.isShowing()) {
			return true;
		}
		return false;
	}

	/**
	 * 多标签适配器
	 */
	class MutiWindowsDialogAdapter extends BaseAdapter {

		private Context mContext;

		public MutiWindowsDialogAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return HomeActivity.mutiWindowsSources.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return HomeActivity.mutiWindowsSources.size();
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(mContext,
						R.layout.item_mutiwindow_dialog, null);
				holder.tabTitle = (TextView) convertView
						.findViewById(R.id.item_mutiwindow_dialog_tabTitle);
				holder.tabUrl = (TextView) convertView
						.findViewById(R.id.item_mutiwindow_dialog_tabUrl);
				holder.tabRemove = (ImageView) convertView
						.findViewById(R.id.item_mutiwindow_dialog_removeTab);
				holder.tabItem = (RelativeLayout) convertView
						.findViewById(R.id.item_mutiwindow_dialog_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tabTitle.setText(HomeActivity.mutiWindowsSources.get(pos)
					.getTabTitle());
			holder.tabUrl.setText(HomeActivity.mutiWindowsSources.get(pos)
					.getTabUrl());
			updateTabBackground(holder,
					HomeActivity.mutiWindowsSources.get(pos));
			/**
			 * 删除单个标签页
			 */
			holder.tabRemove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (HomeActivity.mutiWindowsSources.size() > 1) {
						// 默认选中标签页为上一页
						if (HomeActivity.mutiWindowsSources.get(pos)
								.isCurTabSelectedStatus()) {
							if (pos != 0) {
								HomeActivity.mutiWindowsSources.get(pos - 1)
										.setCurTabSelectedStatus(true);
							} else {
								HomeActivity.mutiWindowsSources.get(pos + 1)
										.setCurTabSelectedStatus(true);
							}
						}
						HomeActivity.mutiWindowsSources.remove(pos);
						int delePos = pos;
						int disPos = 0;
						if (0 != pos) {
							disPos = (pos - 1);
						}
						dismiss();
						closeMutiWindows(CLOSE_TABS, disPos, delePos);
					} else {
						// 窗口只剩一个标签页时关闭窗口 并且更改URL为默认URL
						closeMutiWindows(CLOSE_TAB, 0, -1);
					}
				}
			});

			/**
			 * 打开某个标签页
			 */
			holder.tabItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					closeMutiWindows(OPEN_TAB, pos, -1);
				}
			});
			return convertView;
		}

		/**
		 * 更改标签页选中的背景颜色
		 * 
		 * @param holder
		 * @param swti
		 */
		private void updateTabBackground(ViewHolder holder,
				SingleWindowTabInfo swti) {
			if (swti.isCurTabSelectedStatus()) {
				holder.tabItem.setBackgroundColor(Color.parseColor("#EEEEEE"));
				holder.tabTitle.setBackgroundColor(Color.parseColor("#EEEEEE"));
				holder.tabUrl.setBackgroundColor(Color.parseColor("#EEEEEE"));
				holder.tabRemove
						.setBackgroundColor(Color.parseColor("#EEEEEE"));
			} else {
				holder.tabItem.setBackgroundColor(Color.parseColor("#FFFFFF"));
				holder.tabTitle.setBackgroundColor(Color.parseColor("#FFFFFF"));
				holder.tabUrl.setBackgroundColor(Color.parseColor("#FFFFFF"));
				holder.tabRemove
						.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}

		}

		class ViewHolder {
			ImageView tabIcon, tabRemove;
			TextView tabTitle, tabUrl;
			RelativeLayout tabItem;
		}
	}

	private int getHeightPixels() {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		return dm.heightPixels;
	}
}
