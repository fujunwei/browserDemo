package com.example.browserdemo;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {

	private int imgRecouse[];
	private String title[];
	private LayoutInflater inflater;
	private Context context;

	public GridViewAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);

		setData();
	}

	private void setData() {
		imgRecouse = new int[] {
				R.drawable.v60_new_webview_option_add2bookmark,
				R.drawable.v60_new_webview_option_go2bookmark_root_folder,
				R.drawable.v60_new_webview_option_share,
				R.drawable.v60_new_webview_option_option,
				R.drawable.v60_new_webview_option_refresh_page };

		title = new String[] { "添加收藏", "收藏夹", "分享", "设置", "刷新" };
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imgRecouse.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imgRecouse[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View currentView, ViewGroup arg2) {
		currentView = inflater.inflate(R.layout.imagebutton, null);
		ImageView imageView = (ImageView) currentView
				.findViewById(R.id.imgbtn_img);
		TextView textView = (TextView) currentView
				.findViewById(R.id.imgbtn_text);

		imageView.setBackgroundResource(imgRecouse[position]);
		textView.setText(title[position]);
		textView.setTextColor(context.getResources().getColor(
				R.color.favorites_textcolor));

		return currentView;
	}

}
