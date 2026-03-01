package com.sniplink.util;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;

public class UserAgentParser {

    public static String getDevice(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            DeviceType deviceType = userAgent.getOperatingSystem().getDeviceType();
            return deviceType != null ? deviceType.getName() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getBrowser(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            Browser browser = userAgent.getBrowser();
            return browser != null ? browser.getName() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
