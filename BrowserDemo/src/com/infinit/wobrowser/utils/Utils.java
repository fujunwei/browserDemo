/**
 * @author yuhuan
 * 
 */
package com.infinit.wobrowser.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.TrafficStats;

/**
 * @author clm
 * 
 */
public class Utils {

	private static long lastClickTime = 0;
	private static long BETWEEN_TIME = 1000;

	/**
	 * 限制用户快速连续点击两次
	 * 
	 * @return
	 */
	public synchronized static boolean isFastDoubleClick() {
		boolean flag = false;
		if (System.currentTimeMillis() - lastClickTime < BETWEEN_TIME) {
			flag = true;
		}
		lastClickTime = System.currentTimeMillis();
		return flag;
	}

	/**
	 * 空格回车判断 -true: 纯空格或回车 不能提交
	 * 
	 * @param content
	 * @return
	 */
	public static boolean spacesEnterLimit(String content) {
		String regex = "[\n,\\s]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		int index = 0;
		while (m.find()) {
			index++;
		}
		if (index == content.length()) {
			return true;
		} else {
			return false;
		}
	}

	public static long getTrafficTotalFromClient() {
		return getMobileRxBytes() + getMobileToBytes();
	}

	/**
	 * 获取通过Mobile连接收到的字节总数，不包含WiFi 单位KB
	 */
	public static long getMobileRxBytes() {
		return TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getMobileRxBytes() / 1024);
	}

	/**
	 * 获取通过Mobile发送的字节总数, 不包含WiFi 单位KB
	 * 
	 * @return
	 */
	public static long getMobileToBytes() {
		return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0
				: (TrafficStats.getTotalTxBytes() / 1024);
	}

	public static int dip2px( Context context,float dpValue) {
		float mDensity = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * mDensity + 0.5f);
	}
}
