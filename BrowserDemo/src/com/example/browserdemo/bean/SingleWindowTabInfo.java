package com.example.browserdemo.bean;


/**
 * 浏览器窗口标签实体
 */
public class SingleWindowTabInfo {

	/**
	 * 新标签页图标
	 */
	private String tabIcon;

	/**
	 * 标签页标题
	 */
	private String tabTitle;

	/**
	 * 标签页链接地址
	 */
	private String tabUrl;

	/**
	 * 当前标签页选中状态
	 */
	private boolean curTabSelectedStatus = false;

	public String getTabIcon() {
		return tabIcon;
	}

	public void setTabIcon(String tabIcon) {
		this.tabIcon = tabIcon;
	}

	public String getTabTitle() {
		return tabTitle;
	}

	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}

	public String getTabUrl() {
		return tabUrl;
	}

	public void setTabUrl(String tabUrl) {
		this.tabUrl = tabUrl;
	}

	public boolean isCurTabSelectedStatus() {
		return curTabSelectedStatus;
	}

	/**
	 * 
	 * @param curTabSelectedStatus
	 *            : true 标签选中 false 标签未选中
	 */
	public void setCurTabSelectedStatus(boolean curTabSelectedStatus) {
		this.curTabSelectedStatus = curTabSelectedStatus;
	}

}
