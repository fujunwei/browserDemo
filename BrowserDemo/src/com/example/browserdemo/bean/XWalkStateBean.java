package com.example.browserdemo.bean;

import org.xwalk.core.XWalkView;

import android.graphics.Bitmap;

public class XWalkStateBean {

	private XWalkView webView;

	private boolean proxyOpened;

	private Bitmap bitmap;

	private String currentUrl;

	public XWalkView getWebView() {
		return webView;
	}

	public void setWebView(XWalkView webView) {
		this.webView = webView;
	}

	public boolean isProxyOpened() {
		return proxyOpened;
	}

	public void setProxyOpened(boolean proxyOpened) {
		this.proxyOpened = proxyOpened;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

}
