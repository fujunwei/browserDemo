package com.example.browserdemo;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.browserdemo.bean.MutiWindowItem;
import com.example.browserdemo.bean.SingleWindowTabInfo;
import com.example.browserdemo.logic.NewWebViewLogic;
import com.example.browserdemo.logic.NewWebViewLogic.UpdateBackAndForwardInterface;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;

/**
 * 6.0版本home页面--webview
 * 
 * @author shendongdong
 * 
 */
@SuppressLint("HandlerLeak")
public class HomeActivity extends Activity implements OnClickListener,
		UpdateBackAndForwardInterface, AudioCapabilitiesReceiver.Listener {

	public static HomeActivity INSTANCE = null;

	/**
	 * 多窗口集合
	 */
	public static ArrayList<SingleWindowTabInfo> mutiWindowsSources = new ArrayList<SingleWindowTabInfo>();

	protected LayoutInflater mInflater = null;
	/**
	 * 底部menu
	 */
	private PopMenu popMenu;
	/**
	 * 多窗口window
	 */
	private MutiWindowsPopWindow mutiWinPop;

	// 底部五个按钮
	private RelativeLayout mToolMenuBtn;
	private RelativeLayout mPreviousButton;
	private RelativeLayout mNextButton;
	private RelativeLayout mNewTabButton;
	private RelativeLayout returnHomeTabButton;
	private ImageView previousIv;
	private ImageView nextIv;
	private ImageView homeIv;

	private Button userBtn;
	// private ImageButton newTabBtn;
	private Button addBtn;
	private ImageView clearIv;
	private Button cancelBtn;
	private ImageView searchIv;

	/** 顶部Bar控件声明---start */
	private AutoCompleteTextView searchEditText;

	/** 顶部Bar控件声明---end */

	/** 页面查找控件声明---start */
	private TextView number;

	private NewWebViewLogic logic;

	private boolean isExit;

	public static final String HOME_URL = "http://122.96.25.242:22080/woindex/index.html";
	public static final String HOME_TITLE = "沃浏览器导航";
	public static final String SEARCH_ENGINE = "http://www.baidu.com/s?wd=";

	private View mDecorView;
	private boolean mOriginalFullscreen;
	private boolean mOriginalForceNotFullscreen;
	private boolean mIsFullscreen = false;
	private int mSystemUiFlag;

	private AudioCapabilitiesReceiver audioCapabilitiesReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		INSTANCE = HomeActivity.this;
		isExit = false;
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = View.inflate(this, R.layout.activity_home, null);
		setContentView(root);

		mToolMenuBtn = (RelativeLayout) findViewById(R.id.ToolMenuBtn);
		mToolMenuBtn.setOnClickListener(this);
		mPreviousButton = (RelativeLayout) findViewById(R.id.PreviousBtn);
		mPreviousButton.setOnClickListener(this);
		mNextButton = (RelativeLayout) findViewById(R.id.NextBtn);
		mNextButton.setOnClickListener(this);
		mNewTabButton = (RelativeLayout) findViewById(R.id.NewTabBtn);
		mNewTabButton.setOnClickListener(this);
		returnHomeTabButton = (RelativeLayout) findViewById(R.id.returnHomeTabBtn);
		returnHomeTabButton.setOnClickListener(this);
		nextIv = (ImageView) findViewById(R.id.next_iv);
		previousIv = (ImageView) findViewById(R.id.previous_iv);
		homeIv = (ImageView) findViewById(R.id.home_iv);
		number = (TextView) findViewById(R.id.number);

		popMenu = new PopMenu(INSTANCE, handler);
		mutiWinPop = new MutiWindowsPopWindow(INSTANCE, myHandler);

		initOrCreatedTab(0, 0);

		userBtn = (Button) findViewById(R.id.userBtn);
		userBtn.setOnClickListener(this);

		addBtn = (Button) findViewById(R.id.addBtn);
		addBtn.setOnClickListener(this);

		cancelBtn = (Button) findViewById(R.id.btn_cancel);
		cancelBtn.setOnClickListener(this);

		searchIv = (ImageView) findViewById(R.id.iv_search);
		searchIv.setOnClickListener(this);

		clearIv = (ImageView) findViewById(R.id.btn_clear);
		clearIv.setOnClickListener(this);

		logic = new NewWebViewLogic(this, root);
		logic.setUpdateBackAndForwardInterface(this);

		logic.addNewWebView(0);
		logic.setWebViewUrl(HOME_URL);
		logic.loadUrl();
		logic.setViewSetting();

		initAboutXwalkView();

	}

	/**
	 * 初始化关于内核的参数
	 */
	@SuppressLint("InlinedApi")
	private void initAboutXwalkView() {
		mDecorView = this.getWindow().getDecorView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mSystemUiFlag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		}
		audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
		audioCapabilitiesReceiver.register();
	}

	@Override
	protected void onResume() {
		logic.onResume();
		super.onResume();
	}

	@Override
	public void onStop() {
		logic.exitBrowser();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		logic.onDestory();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 最底部右边第二个按钮添加标签后执行方法
	 */
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {

			MutiWindowItem item = (MutiWindowItem) msg.obj;
			switch (item.getClicktype()) {
			case MutiWindowsPopWindow.CREATE_TAB:
				initOrCreatedTab(item.getClicktype(), item.getDisplayPos());
			case MutiWindowsPopWindow.OPEN_TAB:
				updateSingleWindowInfoSelectionStatus(item.getDisplayPos(),
						item.getClicktype());
				mutiWinPop.dismiss();
				break;
			case MutiWindowsPopWindow.CLOSE_TAB:
				mutiWindowsSources.get(item.getDisplayPos())
						.setTabUrl(HOME_URL);
				mutiWindowsSources.get(item.getDisplayPos()).setTabTitle(
						HOME_TITLE);
				updateSingleWindowInfoSelectionStatus(item.getDisplayPos(),
						item.getClicktype());
				mutiWinPop.dismiss();
				break;
			case MutiWindowsPopWindow.CLOSE_TABS:
				logic.removeWebView(item.getDelePos());
				updateSingleWindowInfoSelectionStatus(item.getDisplayPos(),
						item.getClicktype());
				mutiWinPop.show(mNewTabButton);
				break;
			}
		}
	};

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case PopMenu.ADD_COLLECT:// 添加收藏
				// addBookMark();
				break;
			case PopMenu.COLLECT_FOLDER:// 收藏夹
				break;
			case PopMenu.REFRESH:// 刷新
				logic.reload();
				break;
			case PopMenu.SETTING:// 设置
				break;
			case PopMenu.SHARE:// 分享
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, logic.getCurrentUrl());
				startActivity(Intent.createChooser(shareIntent, "分享使用"));
				break;
			case PopMenu.TAB_PAGE:// 桌面网页
				break;

			default:
				break;
			}

		}

	};

	/**
	 * TODO拦截系统MENU
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		return super.onMenuOpened(featureId, menu);
	}

	/**
	 * 新增窗口标签
	 * 
	 * @param type
	 *            0:代表初始化默认第一个标签
	 * @param pos
	 */
	private void initOrCreatedTab(int type, int pos) {
		if (type != MutiWindowsPopWindow.CREATE_TAB)
			mutiWindowsSources.clear();
		SingleWindowTabInfo swti = new SingleWindowTabInfo();
		swti.setTabTitle(HOME_TITLE);
		swti.setTabUrl(HOME_URL);
		swti.setCurTabSelectedStatus(true);
		mutiWindowsSources.add(pos, swti);
	}

	private void findSelectedTab() {
		for (int i = 0; i < mutiWindowsSources.size(); i++) {
			if (mutiWindowsSources.get(i).isCurTabSelectedStatus()) {
				mutiWindowsSources.get(i).setTabUrl(logic.getUrl());
				mutiWindowsSources.get(i).setTabTitle(logic.getTitle());
				break;
			}
		}
	}

	/**
	 * 改变标签选中状态
	 * 
	 * @param curPos
	 */
	private void updateSingleWindowInfoSelectionStatus(int curPos, int type) {
		number.setText(mutiWindowsSources.size() + "");
		String url = "";
		for (int i = 0; i < mutiWindowsSources.size(); i++) {
			if (i == curPos) {
				mutiWindowsSources.get(i).setCurTabSelectedStatus(true);
				url = mutiWindowsSources.get(i).getTabUrl();
			} else {
				mutiWindowsSources.get(i).setCurTabSelectedStatus(false);
			}
		}

		switch (type) {
		case MutiWindowsPopWindow.CREATE_TAB:
			logic.addNewWebView(curPos);
			logic.setWebViewUrl(url);
			logic.loadUrl();
			break;
		case MutiWindowsPopWindow.CLOSE_TAB:
			logic.removeWebView(-1);
			logic.setWebViewUrl(url);
			logic.loadUrl();
			break;
		case MutiWindowsPopWindow.OPEN_TAB:
		case MutiWindowsPopWindow.CLOSE_TABS:
			logic.showCurrentWebView(curPos);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (logic.canGoBack()) {
				logic.goBack();
				return false;
			}

			doubleClickQuit();
			return false;

		}

		return super.onKeyDown(keyCode, event);
	}

	private void doubleClickQuit() {
		if (!isExit) {
			Toast.makeText(HomeActivity.this,
					getResources().getString(R.string.exit_message),
					Toast.LENGTH_LONG).show();
			isExit = true;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					isExit = true;
				}
			}, 1000);
		} else {
			// quitClient();
			finish();
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.userBtn: // 测试使用 打开代理
			logic.openProxy();
			break;

		case R.id.addBtn:// 地址按钮
			logic.closeProxy();
			break;
		case R.id.ToolMenuBtn:// 功能菜单
			popMenu.show(mToolMenuBtn);
			break;
		case R.id.PreviousBtn:// 前面一页
			logic.goBack();
			break;
		case R.id.NextBtn:// 后面一页
			logic.goForward();
			break;
		case R.id.NewTabBtn:// 多窗口管理
			findSelectedTab();
			mutiWinPop.show(mNewTabButton);
			break;
		case R.id.returnHomeTabBtn:// 主页
			logic.setWebViewUrl(HOME_URL);
			logic.loadUrl();
			break;
		case R.id.deleteFindBtn:
			closeFindStatus();
			break;

		case R.id.find_next:
			logic.findNext();
			break;

		case R.id.find_last:
			logic.findLast();
			break;

		case R.id.UrlText:// 流量页面顶部title中间点击事件(点我开通流量包等字样 )
			// 已订购
			break;

		default:
			break;
		}

	}

	/**
	 * 关闭页面查询
	 */
	@Override
	public void closeFindStatus() {
	}

	@Override
	public void handleWebViewGoBackOrPreviousUI() {
		if (logic.isWebViewLoading()) {
			mNextButton.setEnabled(true);
			nextIv.setImageResource(R.drawable.v60_new_webview_bookmark_close);
		} else {
			if (null != logic.getCurrentWebView()
					&& logic.getCurrentWebView().canGoBack()) {
				previousIv.setEnabled(true);
			} else {
				previousIv.setEnabled(false);
			}
			nextIv.setImageResource(R.drawable.v60_bottom_page_forward);
			if (null != logic.getCurrentWebView()
					&& logic.getCurrentWebView().canGoForward()) {
				nextIv.setEnabled(true);
			} else {
				nextIv.setEnabled(false);
			}
		}
		// 当前页是主页的话,下方导航栏主页按钮置灰,否则可用
		if (HOME_URL.equals(logic.getCurrentWebView().getUrl())) {
			returnHomeTabButton.setEnabled(false);
			homeIv.setEnabled(false);
		} else {
			returnHomeTabButton.setEnabled(true);
			homeIv.setEnabled(true);
		}
	}

	@Override
	public void updateCurrentUrl(String url) {
	}

	@Override
	public void packageOrderSuccess() {
		posHandler.sendEmptyMessage(0);
	}

	private Handler posHandler = new Handler() {
		public void handleMessage(Message msg) {
			logic.setWebViewUrl(HOME_URL);
			logic.loadUrl();
		};
	};

	@SuppressLint("InlinedApi")
	public void onFullscreenToggled(boolean enterFullscreen) {
		Activity activity = this;
		if (enterFullscreen) {
			if ((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN) != 0) {
				mOriginalForceNotFullscreen = true;
				activity.getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			} else {
				mOriginalForceNotFullscreen = false;
			}
			if (!mIsFullscreen) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					mSystemUiFlag = mDecorView.getSystemUiVisibility();
					mDecorView
							.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
									| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_FULLSCREEN
									| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
				} else {
					if ((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
						mOriginalFullscreen = true;
					} else {
						mOriginalFullscreen = false;
						activity.getWindow().addFlags(
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
					}
				}
				mIsFullscreen = true;
				logic.getCurrentWebView().setVisibility(View.INVISIBLE);

			}
		} else {
			if (mOriginalForceNotFullscreen) {
				activity.getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mDecorView.setSystemUiVisibility(mSystemUiFlag);
			} else {
				if (!mOriginalFullscreen) {
					activity.getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			}
			mIsFullscreen = false;
			logic.getCurrentWebView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAudioCapabilitiesChanged(AudioCapabilities arg0) {
		// TODO Auto-generated method stub
		if (logic.getmXWalkExoMediaPlayer() != null) {
			logic.getmXWalkExoMediaPlayer().releasePlayer();
			logic.getmXWalkExoMediaPlayer().preparePlayer(true);
		}
	}

}
