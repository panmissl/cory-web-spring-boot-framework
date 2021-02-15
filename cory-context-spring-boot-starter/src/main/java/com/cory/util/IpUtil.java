package com.cory.util;

import java.net.InetAddress;

/**
 *
 */
public class IpUtil {

	public static String getHostIp() {
		String localhost = "";
		try {
			localhost = InetAddress.getByName(getHostName()).getHostAddress();
		} catch (Exception e) {
		}
		return localhost;
	}

	private static String getHostName() {
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		}
		return hostName;
	}
}
