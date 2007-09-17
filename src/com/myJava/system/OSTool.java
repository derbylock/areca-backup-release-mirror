package com.myJava.system;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.myJava.configuration.FrameworkConfiguration;


/**
 * Utility class for all system calls
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class OSTool {
    
    private static String APPLE_FILE_MGR = "com.apple.eio.FileManager";
    private static final long MAX_MEMORY = Runtime.getRuntime().maxMemory();
    private static final long MAX_MEMORY_KB = MAX_MEMORY / 1024;
    private static final long MAX_MEMORY_MB = MAX_MEMORY / 1048576;
    private static final String[] BROWSERS = FrameworkConfiguration.getInstance().getOSBrowsers();
    
    private static boolean IS_SYSTEM_WINDOWS;
    private static boolean IS_SYSTEM_MAC;
    private static int[] JAVA_VERSION;
    private static String FORMATTED_JAVA_VERSION;
    
    private static String JAVA_FILE_ENCODING;
    private static String IANA_FILE_ENCODING;
    private static String USER_HOME;
    private static String USER_NAME;
    private static String TMP_DIR;
    
    private static Charset[] CHARSETS;
    
    static {
        JAVA_FILE_ENCODING = System.getProperty("file.encoding");
        Charset cs = Charset.forName(JAVA_FILE_ENCODING);
        IANA_FILE_ENCODING = cs.name();
        
        String os = System.getProperty("os.name");

        IS_SYSTEM_WINDOWS = (os.toLowerCase().indexOf("windows") != -1);
        IS_SYSTEM_MAC = (os.toLowerCase().indexOf("mac os") != -1);
        
        FORMATTED_JAVA_VERSION = System.getProperty("java.version");
        if (FORMATTED_JAVA_VERSION == null) {
            JAVA_VERSION = new int[0];
        } else {
            StringTokenizer stt = new StringTokenizer(FORMATTED_JAVA_VERSION, "._-,;/\\ ");
            List lst = new ArrayList();
            while (stt.hasMoreTokens()) {
                lst.add(stt.nextToken());
            }
            
            JAVA_VERSION = new int[lst.size()];
            for (int i=0; i<lst.size(); i++) {
                try {
                    JAVA_VERSION[i] = Integer.parseInt((String)lst.get(i));
                } catch (NumberFormatException e) {
                    JAVA_VERSION[i] = 0;
                }
            }
        }
        
        TMP_DIR = System.getProperty("java.io.tmpdir");
        USER_HOME = System.getProperty("user.home");
        USER_NAME = System.getProperty("user.name");
        
        Map map = Charset.availableCharsets();
        Iterator iter = map.values().iterator();
        CHARSETS = new Charset[map.size()];
        for (int i=0; iter.hasNext(); i++) {
            Charset charset = (Charset)iter.next();
            CHARSETS[i] = charset;
        }
        Arrays.sort(CHARSETS);
    }
    
    public static Charset[] getCharsets() {
        return CHARSETS;
    }
    
    public static String getOSDescription() {
        return System.getProperty("os.name") + " - " + System.getProperty("os.version");
    }
    
    public static String getJavaFileEncoding() {
        return JAVA_FILE_ENCODING;
    }
    
    public static String getIANAFileEncoding() {
        return IANA_FILE_ENCODING;
    }
    
    public static String getUserLanguage() {
        return System.getProperty("user.language");
    }
    
    public static String getUserHome() {
        return USER_HOME;
    }
    
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }
    
    public static String getUserName() {
        return USER_NAME;
    }
    
    public static String getTempDirectory() {
        return TMP_DIR;
    }
    
    /**
     * Attempts to launch the default external browser.
     */
    public static void launchBrowser(URL url) 
    throws OSToolException, NoBrowserFoundException {
        launchBrowser(url.toExternalForm());
    }
    
    /**
     * Attempts to launch the default external browser.
     */
    public static void launchBrowser(String url) 
    throws OSToolException, NoBrowserFoundException {
        
        try {
            if (isSystemMACOS()) {
                Class fileMgr = Class.forName(APPLE_FILE_MGR);
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
                
            } else if (isSystemWindows()) {
                // Workaround : there is a bug in Win2K and certain WinXP releases which prevents the help url to be loaded properly.
                if (url.startsWith("file:/") && url.charAt(6) != '/') {
                    url = "file:///" + url.substring(6);
                    url = URLDecoder.decode(url);
                }
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                
            } else {
                String browser = null;
                for (int count = 0; count < BROWSERS.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[] {"which", BROWSERS[count]}).waitFor() == 0) {
                        browser = BROWSERS[count];
                    }
                }
                
                if (browser != null) {
                    // Browser found --> Go !
                    Runtime.getRuntime().exec(new String[] {browser, url});
                } else {
                    throw new NoBrowserFoundException("No browser cound be found.");
                }
            }
        } catch (Throwable e) {
            throw new OSToolException("Error during external browser invocation.", e);
        }
    }
    
    public static boolean isSystemWindows() {
        return IS_SYSTEM_WINDOWS;
    }
    
    public static boolean isSystemMACOS() {
        return IS_SYSTEM_MAC;
    }
    
    public static long getMaxMemory() {
        return MAX_MEMORY;
    }
    
    public static long getMaxMemoryKB() {
        return MAX_MEMORY_KB;
    }
    
    public static long getMaxMemoryMB() {
        return MAX_MEMORY_MB;
    }
    
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    public static long getMemoryUsage() {
        return getTotalMemory() - getFreeMemory();
    }
    
    public static int[] getJavaVersion() {
        return JAVA_VERSION;
    }
    
    public static String getFormattedJavaVersion() {
        return FORMATTED_JAVA_VERSION;
    }
    
    public static String getVMDescription() {
        return System.getProperty("java.runtime.name") + " - " + System.getProperty("java.runtime.version") + " - " + getJavaVendor();
    }
    
    public static boolean isJavaVersionGreaterThanOrEquals(int[] referenceVersion) {
        int maxIndex = Math.min(referenceVersion.length, JAVA_VERSION.length);
        for (int i=0; i<maxIndex; i++) {
            if (referenceVersion[i] < JAVA_VERSION[i]) {
                return true;
            } else if (referenceVersion[i] > JAVA_VERSION[i]) {
                return false;
            }
        }
        return (referenceVersion.length <= JAVA_VERSION.length);
    }
    
    public static String getJavaVendor() {
        String vd = System.getProperty("java.vm.vendor"); 
        if (vd == null || vd.trim().length() == 0) {
            vd = System.getProperty("java.vendor"); 
        }
        
        return vd;
    }
    
    public static String formatJavaVersion(int[] version) {
        String s = "";
        for (int i=0; i<version.length; i++) {
            if (i != 0) {
                s += ".";
            }
            s += version[i];
        }
        return s;
    }
}