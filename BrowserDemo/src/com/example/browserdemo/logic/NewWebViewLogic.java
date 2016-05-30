package com.example.browserdemo.logic;

import java.util.ArrayList;
import java.util.List;

import org.xwalk.core.XWalkJavascriptResult;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import xwalk.core.proxy.XWalkExoMediaPlayer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebIconDatabase;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.browserdemo.R;
import com.example.browserdemo.bean.XWalkStateBean;

public class NewWebViewLogic {
	// private static final int DELAYED_TIME = 3000;
	/** 带选择的对话框. */
	public static final String CHOOSEBOX = "\u5E26\u9009\u62E9\u7684\u5BF9\u8BDD\u6846";

	/** 提示对话框. */
	public static final String PROMPT = "\u63D0\u793A\u5BF9\u8BDD\u6846";

	/** 服务器返回URL错误. */
	public static final String BACKURLERROR = "\u670D\u52A1\u5668\u8FD4\u56DEURL\u9519\u8BEF";

	/** 非免流量专区提示 */
	public static final int DIALOG_UNFREE_FLOW = 100;

	/** 免流量专区的提示 */
	public static final int DIALOG_CHANGE_3GNET = 200;

	String proxyid = "";

	protected static final String TAG = "WebViewLogic";

	private XWalkView currentWebView;
	private XWalkExoMediaPlayer mXWalkExoMediaPlayer;
	private Bitmap currentBitmap;
	private LinearLayout webViewContainer;
	private View errorPager;
	private List<XWalkStateBean> webViewLists;
	/**
	 * 标题集合-用于存储webview有多级页面的时候的标题
	 */
	private List<String> titles = new ArrayList<String>();

	private View mainTitle;
	private LinearLayout bottomView;

	private ProgressBar webViewProgress;

	private String webViewUrl;

	private String clickEvent;

	private Context mContext;

	private View rootView;

	/**
	 * 打点标记
	 */
	private String categoryName;
	// private RelativeLayout bottomBar;

	// 设置
	public SharedPreferences mPreferences;
	// 是否允许加载图片
	public static Boolean iswebpageProp;
	// 是否允许cooklie
	public static Boolean iscooklie;
	// 是否允许退出时清除历史记录
	public static Boolean ishistory;
	// 是否启用桌面模式
	public static Boolean isusedesktopview;

	public String currentProxyStatus;

	public XWalkView getCurrentWebView() {
		return currentWebView;
	}

	public XWalkExoMediaPlayer getmXWalkExoMediaPlayer() {
		return mXWalkExoMediaPlayer;
	}

	public void setmXWalkExoMediaPlayer(XWalkExoMediaPlayer mXWalkExoMediaPlayer) {
		this.mXWalkExoMediaPlayer = mXWalkExoMediaPlayer;
	}

	private boolean isWebViewLoading = false;

	private UpdateBackAndForwardInterface updateBackAndForwardInterface;

	public boolean isWebViewLoading() {
		return isWebViewLoading;
	}

	public void setUpdateBackAndForwardInterface(
			UpdateBackAndForwardInterface updateBackAndForwardInterface) {
		this.updateBackAndForwardInterface = updateBackAndForwardInterface;
	}

	public interface UpdateBackAndForwardInterface {
		void handleWebViewGoBackOrPreviousUI();

		void updateCurrentUrl(String url);

		void closeFindStatus();

		void packageOrderSuccess();
	}

	public static Boolean getIswebpageProp() {
		return iswebpageProp;
	}

	public static void setIswebpageProp(Boolean iswebpageProp) {
		NewWebViewLogic.iswebpageProp = iswebpageProp;
	}

	public static Boolean getIscooklie() {
		return iscooklie;
	}

	public static void setIscooklie(Boolean iscooklie) {
		NewWebViewLogic.iscooklie = iscooklie;
	}

	public static Boolean getIshistory() {
		return ishistory;
	}

	public static void setIshistory(Boolean ishistory) {
		NewWebViewLogic.ishistory = ishistory;
	}

	public static Boolean getIsusedesktopview() {
		return isusedesktopview;
	}

	public static void setIsusedesktopview(Boolean isusedesktopview) {
		NewWebViewLogic.isusedesktopview = isusedesktopview;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getWebViewUrl() {
		return webViewUrl;
	}

	public String getCurrentUrl() {
		return currentWebView.getUrl();
	}

	public void setWebViewUrl(String webViewUrl) {
		this.webViewUrl = webViewUrl;
	}

	public void addNewWebView(int pos) {
		initWebView(pos);
	}

	public void removeWebView(int pos) {
		if (pos != -1 && pos < webViewLists.size()) {
			webViewLists.remove(pos);
		} else if (pos == -1) {
			webViewLists.remove(0);
			addNewWebView(0);
		}
	}

	public void showCurrentWebView(int pos) {
		webViewContainer.removeAllViews();
		currentWebView = webViewLists.get(pos).getWebView();
		webViewContainer.addView(currentWebView);
	}

	public void setViewSetting() {
		if (mPreferences != null) {
			setWebView();
		}
	}

	public NewWebViewLogic(Context context, View root) {
		this.mContext = context;
		this.rootView = root;
		findID();
	}

	@SuppressWarnings("deprecation")
	private void findID() {
		XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);
		webViewLists = new ArrayList<XWalkStateBean>();
		webViewContainer = (LinearLayout) rootView
				.findViewById(R.id.activity_home_webview_container);
		errorPager = (View) rootView.findViewById(R.id.error);
		mainTitle = (View) rootView.findViewById(R.id.main_title);
		bottomView = (LinearLayout) rootView.findViewById(R.id.BottomBarLayout);

		mPreferences = mContext.getSharedPreferences("setting", 0);

		webViewProgress = (ProgressBar) rootView
				.findViewById(R.id.WebViewProgress);

		// 初始化设置Preferences的值
		setIswebpageProp(mPreferences.getBoolean("iswebpageProp", false));
		setIscooklie(mPreferences.getBoolean("iscooklie", true));
		setIshistory(mPreferences.getBoolean("ishistory", false));
		setIsusedesktopview(mPreferences.getBoolean("isusedesktopview", false));
		WebIconDatabase.getInstance().open(
				mContext.getApplicationInfo().dataDir + "/icons/");

		errorPager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				currentWebView.reload(XWalkView.RELOAD_NORMAL);
				hideErrorPage();
			}
		});

	}

	private void showErrorPage() {
		errorPager.setVisibility(View.VISIBLE);
		webViewContainer.setVisibility(View.GONE);
	}

	private void hideErrorPage() {
		errorPager.setVisibility(View.GONE);
		webViewContainer.setVisibility(View.VISIBLE);
	}

	private void initWebView(final int pos) {
		final XWalkView webView = new XWalkView(mContext);
		final XWalkStateBean bean = new XWalkStateBean();
		XWalkSettings webSettings = webView.getSettings();
		webSettings.setUseWideViewPort(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		// 让编辑框能弹出键盘
		webView.requestFocus(View.FOCUS_DOWN);

		webView.setResourceClient(new XWalkResourceClient(webView) {
			@Override
			public boolean shouldOverrideUrlLoading(XWalkView view,
					final String url) {
				return false;
			}

			@Override
			public void onReceivedLoadError(XWalkView view, int errorCode,
					String description, String failingUrl) {
				if (null != updateBackAndForwardInterface) {
					isWebViewLoading = false;
					updateBackAndForwardInterface
							.handleWebViewGoBackOrPreviousUI();
				}
				showErrorPage();
				switch (errorCode) {
				case 1:
					// 策略待定
					// finish();
					return;

				default:
					break;
				}
				super.onReceivedLoadError(view, errorCode, description,
						failingUrl);

			}

			@Override
			public void onLoadStarted(XWalkView view, String url) {
				super.onLoadStarted(view, url);
			}

			@Override
			public void onLoadFinished(XWalkView view, String url) {
				super.onLoadFinished(view, url);
				if (webView != null) {
					webView.resumeTimers();
					webView.onShow();
				}
				if (null != updateBackAndForwardInterface) {
					updateBackAndForwardInterface.updateCurrentUrl(url);
				}
				if (!TextUtils.isEmpty(clickEvent)
						&& TextUtils.equals(url, webViewUrl)) {
					if ("clickEvent00027".equals(clickEvent)) {
						// LogPush.sendLog4Banner(-1, -1, url);
					} else if ("clickEvent00037".equals(clickEvent)) {
					}
				}
			}

			@Override
			public void onProgressChanged(XWalkView view, int progressInPercent) {
				if (progressInPercent == 100) {
					if (null != updateBackAndForwardInterface) {
						isWebViewLoading = false;
						updateBackAndForwardInterface
								.handleWebViewGoBackOrPreviousUI();
						updateBackAndForwardInterface.closeFindStatus();
					}
					webViewProgress.setVisibility(View.INVISIBLE);
				} else {
					if (null != updateBackAndForwardInterface) {
						isWebViewLoading = true;
						updateBackAndForwardInterface
								.handleWebViewGoBackOrPreviousUI();
					}
					if (webViewProgress.getVisibility() == View.INVISIBLE) {
						webViewProgress.setVisibility(View.VISIBLE);
					}
					webViewProgress.setProgress(progressInPercent);
				}
				super.onProgressChanged(view, progressInPercent);
			}

			@Override
			public void onReceivedSslError(XWalkView view,
					ValueCallback handler, SslError error) {
				if (null != updateBackAndForwardInterface) {
					isWebViewLoading = false;
					updateBackAndForwardInterface
							.handleWebViewGoBackOrPreviousUI();
					updateBackAndForwardInterface.closeFindStatus();
				}
			}
		});

		webView.setUIClient(new XWalkUIClient(webView) {
			@Override
			public boolean onJsAlert(XWalkView view, String url,
					String message, final XWalkJavascriptResult result) {
				// 构建一个Builder来显示网页中的alert对话框
				Builder builder = new Builder(mContext);
				builder.setTitle(PROMPT);
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			@Override
			public boolean onJsConfirm(XWalkView view, String url,
					String message, final XWalkJavascriptResult result) {
				Builder builder = new Builder(mContext);
				builder.setTitle(CHOOSEBOX);
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						});
				builder.setNeutralButton(android.R.string.cancel,
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			}

			@Override
			public boolean onJsPrompt(XWalkView view, String url,
					String message, String defaultValue,
					final XWalkJavascriptResult result) {
				return true;
			}

			@Override
			public void onFullscreenToggled(XWalkView view,
					boolean enterFullscreen) {
				// TODO Auto-generated method stub
				super.onFullscreenToggled(view, enterFullscreen);
				Message msg = handler.obtainMessage();
				if (enterFullscreen) {
					msg.what = 3;
				} else {
					msg.what = 4;
				}
				handler.sendMessage(msg);
			}

			// 设置应用程序的标题
			@Override
			public void onReceivedTitle(XWalkView view, String title) {
				if (title == null || title.length() <= 0) {
					titles.add(titles.get(titles.size() - 1));
				} else {
					titles.add(title);

				}

			}

			@Override
			public void onIconAvailable(XWalkView arg0, String arg1,
					Message arg2) {
				// TODO Auto-generated method stub
				if (!arg0.getUrl().equals(bean.getCurrentUrl())) {
					// 预防网页在滑动过程中重复执行相同方法
					arg2.sendToTarget();
					bean.setCurrentUrl(arg0.getUrl());
				}
			}

			@Override
			public void onReceivedIcon(XWalkView arg0, String arg1, Bitmap arg2) {
				// TODO Auto-generated method stub
				currentBitmap = arg2;
			}
		});
		bean.setWebView(webView);
		webViewLists.add(pos, bean);
		showCurrentWebView(pos);
	}

	/**
	 * 开启代理
	 */
	public void openProxy() {
		// 仅非WIFI状态下开启代理
		currentProxyStatus = "open";
		String[] a = { "*.intel.com" };
		String proxy = "122.96.25.242";
		int proxyPort = 9399;
		Toast.makeText(mContext, "开启代理", Toast.LENGTH_SHORT).show();
		for (XWalkStateBean bean : webViewLists) {
			setProxy(bean.getWebView(), proxy, proxyPort, a);
		}
	}

	/**
	 * 
	 * @param webview
	 * @param proxy
	 *            :ip
	 * @param proxyPort
	 *            :端口号
	 * @param a
	 */
	private void setProxy(XWalkView webview, String proxy, int proxyPort,
			String[] a) {
		if (TextUtils.isEmpty(proxy)) {
			currentProxyStatus = null;
			return;
		}
		webview.proxySettingsChanged(proxy, proxyPort, "", a);

		mXWalkExoMediaPlayer = new XWalkExoMediaPlayer(mContext, webview);
		mXWalkExoMediaPlayer.updateProxySetting(proxy, proxyPort);
		webview.setExMediaPlayer(mXWalkExoMediaPlayer);

	}

	/**
	 * 关闭代理
	 */
	public void closeProxy() {
		currentProxyStatus = "closed";
		Toast.makeText(mContext, "关闭代理", Toast.LENGTH_SHORT).show();
		for (XWalkStateBean bean : webViewLists) {
			XWalkView webview = bean.getWebView();
			setProxy(webview, null, -1, null);
		}
	}

	/**
	 * 返回当前代理状态
	 * 
	 * @return
	 */
	public String getCurrentProxyStatus() {
		return currentProxyStatus;
	}

	/** 判断传过来的是不是正确的网址 */
	private static boolean webviewURL(String url) {
		return URLUtil.isNetworkUrl(url);
	}

	public void loadUrl() {
		/** 记载URL */
		if (webviewURL(webViewUrl)) {
			currentWebView.load(webViewUrl, null);
		} else {
			Toast.makeText(mContext, BACKURLERROR, Toast.LENGTH_SHORT).show();
			((Activity) mContext).finish();
		}
	}

	public void webviewActivityResume() {
	}

	public void onDestory() {
		for (int i = 0; i < webViewContainer.getChildCount(); i++) {
			XWalkView webView = (XWalkView) webViewContainer.getChildAt(i);
			webView.removeAllViews();
			webView.onDestroy();
			webView = null;
		}
		webViewContainer.removeAllViews();
	}

	public void onPause() {
		for (int i = 0; i < webViewContainer.getChildCount(); i++) {
			XWalkView webView = (XWalkView) webViewContainer.getChildAt(i);
			if (webView != null) {
				onHidden();
				webView.load("javascript:KY.ine.stop()", null);
			}

		}
	}

	static public String getBufferDir() {
		String bufferDir = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/ProxyBuffer/files";
		return bufferDir;
	}

	public String getClickEvent() {
		return clickEvent;
	}

	public void setClickEvent(String clickEvent) {
		this.clickEvent = clickEvent;
	}

	public boolean canGoBack() {
		// return oldCanGoBack();
		return newCanGoBack();
	}

	/**
	 * v502 fix1847 bug以后的<br/>
	 * add by xuesong
	 * 
	 * @return
	 */
	private boolean newCanGoBack() {
		boolean canGoBack = false;
		if (null != currentWebView
				&& (canGoBack = currentWebView.getNavigationHistory()
						.canGoBack())) {
			currentWebView.goBack();

		}
		return canGoBack;
	}

	@SuppressWarnings("deprecation")
	public void onResume() {
		onShown();
	}

	private void onShown() {
		if (mXWalkExoMediaPlayer != null) {
			mXWalkExoMediaPlayer.setBackgrounded(false);
		}
	}

	private void onHidden() {
		if (mXWalkExoMediaPlayer != null) {
			mXWalkExoMediaPlayer.onHideCustomView();
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				mainTitle.setVisibility(View.GONE);
				bottomView.setVisibility(View.GONE);
				webViewProgress.setVisibility(View.GONE);
				break;
			case 4:
				mainTitle.setVisibility(View.VISIBLE);
				bottomView.setVisibility(View.VISIBLE);
				webViewProgress.setVisibility(View.VISIBLE);
				break;
			}
		};
	};

	public void goBack() {
		if (isWebViewLoading && null != updateBackAndForwardInterface) {
			currentWebView.stopLoading();
			isWebViewLoading = false;
			updateBackAndForwardInterface.handleWebViewGoBackOrPreviousUI();
		}
		currentWebView.goBack();
	}

	public void goForward() {
		if (isWebViewLoading && null != updateBackAndForwardInterface) {
			currentWebView.stopLoading();
			isWebViewLoading = false;
			updateBackAndForwardInterface.handleWebViewGoBackOrPreviousUI();
		} else {
			currentWebView.goForward();
		}
	}

	public void reload() {
		currentWebView.reload(XWalkView.RELOAD_NORMAL);
	}

	public void findNext() {
		// currentWebView.findNext(true);
	}

	public void findLast() {
		// currentWebView.findNext(false);
	}

	public void findAll(String s) {
		// currentWebView.findAll(s);
	}

	public Bitmap getFavicon() {
		return currentBitmap;
	}

	public String getTitle() {
		return currentWebView.getTitle();
	}

	public String getUrl() {
		return currentWebView.getUrl();
	}

	public void clearMatches() {
		// currentWebView.clearMatches();
	}

	// 设置功能
	public void setWebView() {

		if (!getIswebpageProp()) {
			// currentWebView.getSettings().setLoadsImagesAutomatically(true);
		} else {
			// currentWebView.getSettings().setLoadsImagesAutomatically(false);
		}

		if (!getIscooklie()) {
			CookieSyncManager.createInstance(mContext);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(false);
		} else {
			CookieSyncManager.createInstance(mContext);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setAcceptCookie(true);
		}

		if (getIsusedesktopview()) {
			currentWebView.getSettings().setUserAgentString(
					createUserAgentString("desktop"));
			// currentWebView.getSettings().setLoadWithOverviewMode(true);
		} else {
			currentWebView.getSettings().setUserAgentString(
					createUserAgentString("mobile"));
			// currentWebView.getSettings().setLoadWithOverviewMode(false);
		}

	}

	// 退出浏览器时是否清除浏览器记录
	public void exitBrowser() {
		if (getIshistory()) {
			currentWebView.clearHistory();
			currentWebView.clearCache(true);
		}
		onHidden();

	}

	public boolean webViewCanGoForward() {
		return currentWebView.canGoForward();
	}

	public boolean webViewCanGoBack() {
		return currentWebView.canGoBack();
	}

	public String createUserAgentString(String mode) {
		String ua = "";

		// TODO Test with different user agents
		// For now copied Chrome user agents and adapt them to the user's device
		if (mode.equals("mobile")) {

			ua = "Mozilla/5.0 (" + System.getProperty("os.name", "Linux")
					+ "; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL
					+ "; Build/" + Build.ID
					+ ") AppleWebKit/537.36 (KHTML, like Gecko) "
					+ "Chrome/34.0.1847.114 Mobile Safari/537.36";
		} else if (mode.equals("desktop")) {
			ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";
		}
		return ua;
	}

}
