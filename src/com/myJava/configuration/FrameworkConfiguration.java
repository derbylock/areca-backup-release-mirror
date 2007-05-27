package com.myJava.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class FrameworkConfiguration {
    private static FrameworkConfiguration instance = new FrameworkConfiguration(); 
    
    private static String KEY_FTP_MAX_PROXIES = "ftp.max.proxies";
    private static String KEY_FTP_DEBUG = "ftp.debug";
    private static String KEY_FT_DELAY = "filetool.delay";
    private static String KEY_FT_BUFFER_SIZE = "filetool.buffer.size";
    private static String KEY_FTP_NOOP_DELAY = "ftp.noop.delay";    
    private static String KEY_FTP_CACHE_SIZE = "ftp.cache.size";    
    private static String KEY_FTP_USE_CACHE = "ftp.use.cache";
    private static String KEY_OS_BROWSERS = "os.browsers";  
    private static String KEY_SSE_PROTOCOLS = "sse.protocols";  
    
    private static int DEF_FTP_MAX_PROXIES = 3;
    private static long DEF_FTP_NOOP_DELAY = 30000;    
    private static boolean DEF_FTP_DEBUG = false;
    private static int DEF_FT_DELAY = 100;
    private static int DEF_FT_BUFFER_SIZE = 65536;
    private static int DEF_FTP_CACHE_SIZE = 100;    
    private static boolean DEF_FTP_USE_CACHE = true;    
    private static String[] DEF_OS_BROWSERS = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
    private static String[] DEF_SSE_PROTOCOLS = {"TLS", "SSL"};  

    private String strUrl = null;
    private Properties props = new Properties();

    public static synchronized FrameworkConfiguration getInstance() {
        return instance;
    }
    
    public static synchronized void setInstance(FrameworkConfiguration i) {
        instance = i;
    }
    
    public FrameworkConfiguration() {
        init();
    }
    
    public FrameworkConfiguration(String url) {
        this.strUrl = url;
        init();
    }
    
    public Properties getProperties() {
        return props;
    }
    
    protected void init() {
        if (this.strUrl != null) {
            InputStream in = null;
            try {
                URL url = ClassLoader.getSystemClassLoader().getResource(strUrl);
                if (url != null) {
	                in = url.openStream();
	                if (in != null) {
	                    props.load(in);
	                }
                }
            } catch (IOException e) {
                Logger.defaultLogger().error("Error during framework properties loading", e);
            }  finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public int getFileToolDelay() {
        return getProperty(KEY_FT_DELAY, DEF_FT_DELAY);
    }
    
    public boolean isFTPDebugMode() {
        return getProperty(KEY_FTP_DEBUG, DEF_FTP_DEBUG);
    }
    
    public boolean isFTPCacheMode() {
        return getProperty(KEY_FTP_USE_CACHE, DEF_FTP_USE_CACHE);
    }
    
    public long getFTPNoopDelay() {
        return getProperty(KEY_FTP_NOOP_DELAY, DEF_FTP_NOOP_DELAY);
    }
    
    public int getFTPCacheSize() {
        return getProperty(KEY_FTP_CACHE_SIZE, DEF_FTP_CACHE_SIZE);
    }
    
    public int getMaxFTPProxies() {
        return getProperty(KEY_FTP_MAX_PROXIES, DEF_FTP_MAX_PROXIES);
    }
    
    public int getFileToolBufferSize() {
        return getProperty(KEY_FT_BUFFER_SIZE, DEF_FT_BUFFER_SIZE);
    }
    
    public String[] getOSBrowsers() {
        return getProperty(KEY_OS_BROWSERS, DEF_OS_BROWSERS);
    }
    
    public String[] getSSEProtocols() {
        return getProperty(KEY_SSE_PROTOCOLS, DEF_SSE_PROTOCOLS);
    }
    
    protected String getProperty(String key, String defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return p;
        }
    }
    
    protected boolean getProperty(String key, boolean defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return p.equalsIgnoreCase("true");
        }
    }
    
    protected int getProperty(String key, int defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(p);
        }
    }
    
    protected long getProperty(String key, long defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Long.parseLong(p);
        }
    }
    
    protected double getProperty(String key, double defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            return Double.parseDouble(p);
        }
    }
    
    protected String[] getProperty(String key, String[] defaultValue) {
        String p = props.getProperty(key);
        if (p == null) {
            return defaultValue;
        } else {
            List data = new ArrayList();
            
            StringTokenizer stt = new StringTokenizer(p, ",");
            while (stt.hasMoreTokens()) {
                String t = stt.nextToken().trim();
                if (t.length() != 0) {
                    data.add(t);
                }
            }
            
            return (String[])data.toArray(new String[0]);
        }
    }
    
    public String toString() {
        return this.props.toString();
    }
}