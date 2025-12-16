package org.lsposed.hijack.util;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpUtils {

    private static CookieManager cookieManager = new CookieManager();

    static{
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    public static interface Async {
        void result(RequestResult RequestResult);
    }

    public static Request post(String url) {
        return new Request(url, true);
    }

    public static Request get(String url) {
        return new Request(url, false);
    }

    public static class RequestResult {
        private String Url;
        private String Cookie;
        private String Data;
        private String ResponseMessage;
        private int ResponseCode;
        private Map<String, List<String>> ResponeHeaders = new LinkedHashMap<>();
        private Map<String, String> ResponeHeader = new LinkedHashMap<>();

        public RequestResult(String Url, String Data, String Cookie, String ResponseMessage, int ResponseCode, Map<String, List<String>> ResponeHeaders, Map<String, String> ResponeHeader) {
            this.Url = Url;
            this.Data = Data;
            this.Cookie = Cookie;
            this.ResponseCode = ResponseCode;
            this.ResponeHeaders = ResponeHeaders;
            this.ResponeHeader = ResponeHeader;
            this.ResponseMessage = ResponseMessage;
        }

        public String getUrl() {
            return this.Url;
        }

        public String getCookie() {
            return this.Cookie;
        }

        public String getData() {
            return this.Data;
        }

        public int getCode() {
            return this.ResponseCode;
        }

        public String getResponseMessage() {
            return this.ResponseMessage;
        }

        public String getHeaderField(String key) {
            if (this.ResponeHeader.containsKey(key)) {
                return this.ResponeHeader.get(key);
            }
            return "";
        }

        public Map<String,List<String>> getHeaderFields() {
            if (this.ResponeHeaders != null) {
                return this.ResponeHeaders;
            }
            return new LinkedHashMap<String,List<String>>();
        }

        public long getLength() {
            return this.Data.length();
        }

        public String getSize() {
            long size = getLength();
            if (size < 1) {
                return size + " B";
            }
            String[] units = {"B", "KB", "MB", "GB", "TB"};
            int index = 0;
            float fileSize = size;
            while (fileSize > 1024 && index < units.length - 1) {
                fileSize /= 1024;
                index++;
            }
            return String.format("%.2f", fileSize) + " " + units[index];
        }

        public JSONObject toJsonObject() {
            try {
                return new JSONObject(this.Data);
            } catch (Throwable e) {
                return new JSONObject();
            }
        }

        public JSONArray toJsonArray() {
            try {
                return new JSONArray(this.Data);
            } catch (Throwable e) {
                return new JSONArray();
            }
        }
        
        public boolean isOk() {
            return HttpURLConnection.HTTP_OK == this.ResponseCode;
        }

        public boolean isCanOk() {
            if ((this.ResponseCode >= 200 && this.ResponseCode < 300) || this.ResponseCode == HttpURLConnection.HTTP_MOVED_PERM || this.ResponseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return this.Data;
        }
    }

    public static class Request {

        private int ReadTimeout, ConnectTimeout;
        private String url;
        private StringBuilder postData;
        private Map<String, String> headers = new LinkedHashMap<>();
        private String Cookie, Data, charset,ResponseMessage;
        private boolean post, isAutoDisconnect, isBufferedReader,allowUserInteraction,followRedirects;
        private long startDelayed, endDelayed;
        private Map<String, List<String>> ResponeHeaders = new LinkedHashMap<>();
        private Map<String, String> ResponeHeader = new LinkedHashMap<>();

        private Request(String url, boolean post) {
            this.url = url;
            this.post = post;
            this.postData = post ? new StringBuilder() : null;
            this.ConnectTimeout = 6000;
            this.ReadTimeout = 10000;
            this.Cookie = "";
            this.Data = "";
            this.ResponseMessage = "";
            this.charset = "UTF-8";
            this.isAutoDisconnect = true;
            this.followRedirects = true;
            this.allowUserInteraction = false;
            this.isBufferedReader = false;
            this.startDelayed = 0;
            this.endDelayed = 0;
            this.ResponeHeaders = new LinkedHashMap<>();
            this.ResponeHeader = new LinkedHashMap<>();
            headers.clear();
            CookieHandler.setDefault(CookieHandler.getDefault());
        }

        public Request setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Request header(String name, Object value) {
            headers.put(name, String.valueOf(value));
            return this;
        }

        public Request setConnectTimeout(int time) {
            this.ConnectTimeout = time;
            return this;
        }

        public Request setReadTimeout(int time) {
            this.ReadTimeout = time;
            return this;
        }

        public Request setInstanceFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Request setAllowUserInteraction(boolean allowUserInteraction) {
            this.allowUserInteraction = allowUserInteraction;
            return this;
        }

        public Request formData(String key, String value) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            String newvalue = "";
            try {
                newvalue = URLEncoder.encode(value, charset);
            } catch (Throwable e) {
                newvalue = value;
            }
            postData.append(key).append('=').append(newvalue);
            return this;
        }

        public Request autoDisconnect(boolean isAutoDisconnect) {
            this.isAutoDisconnect = isAutoDisconnect;
            return this;
        }

        public Request EnableBufferedReader() {
            this.isBufferedReader = true;
            return this;
        }

        public Request session() {
            CookieHandler.setDefault(cookieManager);
            return this;
        }

        public Request startDelayed(long time) {
            if (time > 3000) {
                this.startDelayed = 3000;
            } else if (time < 0) {
                this.startDelayed = 0;
            } else {
                this.startDelayed = time;
            }
            return this;
        }

        public Request endDelayed(long time) {
            if (time > 2000) {
                this.endDelayed = 2000;
            } else if (time < 0) {
                this.endDelayed = 0;
            } else {
                this.endDelayed = time;
            }
            return this;
        }

        private HttpURLConnection createConnection() throws Throwable {
            if (this.startDelayed > 0) {
                Thread.sleep(this.startDelayed);
            }
            URL mURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod(post ? "POST" : "GET");
            conn.setConnectTimeout(ConnectTimeout);
            conn.setReadTimeout(ReadTimeout);
            conn.setDoOutput(post);
            conn.setInstanceFollowRedirects(this.followRedirects);
            if (this.allowUserInteraction) {
                conn.setAllowUserInteraction(this.allowUserInteraction);
            }
            IgnoreCertificate(conn);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            if (post) {
                OutputStream out = conn.getOutputStream();
                out.write(postData.toString().getBytes(charset));
                out.flush();
                out.close();
            }
            conn.connect();
            return conn;
        }

        private String read(InputStream is) {
            if (this.isBufferedReader) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
                    StringBuilder sb = new StringBuilder();
                    char[] buffer = new char[1024 * 64];
                    int len;
                    while ((len = reader.read(buffer)) != -1) {
                        sb.append(buffer, 0, len);
                    }
                    reader.close();
                    return sb.toString();
                } catch (Throwable e) {}
            } else {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024 * 64];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    is.close();
                    return baos.toString(charset);
                } catch (Throwable e) {}
            }
            return "";
        }

        private void IgnoreCertificate(HttpURLConnection connection) {
            if (connection instanceof HttpsURLConnection) {
                try {
                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] p1, String p2) {
                            }
                            @Override
                            public void checkServerTrusted(X509Certificate[] p1, String p2) {
                            }
                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }};
                    // 初始化 SSLContext
                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    // 设置默认的 SSLSocketFactory，让所有 HTTPS 连接使用这个不验证的工厂
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    // 设置一个 HostnameVerifier，接受所有主机名
                    HostnameVerifier allowAllHosts = (hostname, session) -> true;
                    HttpsURLConnection.setDefaultHostnameVerifier(allowAllHosts);
                    // 设置忽略证书的 SSLSocketFactory
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
                } catch (Throwable e) {}
            }
        }

        public void openConnection(final Async Async) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final HttpURLConnection conn = createConnection();
                                        final int responseCode = conn.getResponseCode();
                                        if (responseCode >= 200 && responseCode < 300) {
                                            GetRequest().Data = read(conn.getInputStream());
                                            GetRequest().Cookie = conn.getHeaderField("Set-Cookie");
                                            GetRequest().ResponseMessage = conn.getResponseMessage();
                                            if (GetRequest().endDelayed > 0) {
                                                Thread.sleep(GetRequest().endDelayed);
                                            }
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Async.result(new RequestResult(GetRequest().url, GetRequest().Data, GetRequest().Cookie, GetRequest().ResponseMessage, responseCode, GetRequest().ResponeHeaders, GetRequest().ResponeHeader));
                                                    }
                                                });
                                        } else if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                                            GetRequest().Cookie = getCookie(conn);
                                            GetRequest().ResponseMessage = conn.getResponseMessage();
                                            if (GetRequest().endDelayed > 0) {
                                                Thread.sleep(GetRequest().endDelayed);
                                            }
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Async.result(new RequestResult(GetRequest().url, GetRequest().Data, GetRequest().Cookie, GetRequest().ResponseMessage, responseCode, GetRequest().ResponeHeaders, GetRequest().ResponeHeader));
                                                    }
                                                });
                                        } else {
                                            GetRequest().ResponseMessage = conn.getResponseMessage();
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Async.result(new RequestResult(GetRequest().url, GetRequest().Data, GetRequest().Cookie, GetRequest().ResponseMessage, responseCode, GetRequest().ResponeHeaders, GetRequest().ResponeHeader));
                                                    }
                                                });
                                        }
                                        disconnect(conn);
                                    }  catch (final Throwable e) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Async.result(new RequestResult(GetRequest().url, GetRequest().Data, GetRequest().Cookie, e.getMessage(), -1, GetRequest().ResponeHeaders, GetRequest().ResponeHeader));
                                                }
                                            });
                                    }
                                }
                            }).start();
                    }
                });
        }

        public RequestResult openConnection() {
            int responseCode = -1;
            try {
                HttpURLConnection conn = createConnection();
                responseCode = conn.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    this.Data = read(conn.getInputStream());
                    this.Cookie = getCookie(conn);
                } else if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                    this.Cookie = getCookie(conn);
                }
                this.ResponseMessage = conn.getResponseMessage();
                disconnect(conn);
                if (this.endDelayed > 0) {
                    Thread.sleep(this.endDelayed);
                }
            } catch (Throwable e) {
                this.ResponseMessage = e.getMessage();
            }
            return new RequestResult(this.url, this.Data, this.Cookie, this.ResponseMessage, responseCode, this.ResponeHeaders, this.ResponeHeader);
        }

        private String getCookie(HttpURLConnection conn) {
            String key = "Set-Cookie";
            this.ResponeHeaders = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : this.ResponeHeaders.entrySet()) {
                String Keys = entry.getKey();
                this.ResponeHeader.put(Keys, conn.getHeaderField(Keys));
            }
            if (this.ResponeHeaders.containsKey(key)) {
                List<String> Cookies = this.ResponeHeaders.get(key);
                if (Cookies.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    String LastCookie = Cookies.get(Cookies.size() - 1);
                    for (String Cookie : Cookies) {
                        builder.append(Cookie);
                        if (!Cookie.equals(LastCookie)) {
                            builder.append("; ");
                        }
                    }
                    return builder.toString();
                }
            }
            return conn.getHeaderField(key);
        }

        private void disconnect(HttpURLConnection conn) {
            if (this.isAutoDisconnect && conn != null) {
                conn.disconnect();
            }
        }

        private Request GetRequest() {
            return this;
        }
    }
}
