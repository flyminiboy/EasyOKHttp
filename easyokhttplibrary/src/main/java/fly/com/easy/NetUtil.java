package fly.com.easy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 作者 ${郭鹏飞}.<br/>
 * <p/>
 * 判断网络状态工具类
 * @hide
 */
public class NetUtil {

    private NetUtil() {
    }

    /**
     * 判断网络是够可用
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            return info != null && info.isAvailable();
        }
        return false;
    }

    /**
     * 判断WiFi是否可用
     *
     * @param context Context
     * @return 表示网络可用
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            return info != null && info.getType() == ConnectivityManager.TYPE_WIFI
                    && info.isAvailable();
        }
        return false;
    }

    /**
     * 手机移动网络是否可用
     *
     * @param context Context
     * @return 表示网络可用
     */
    public static boolean isMobileAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE
                    && info.isAvailable();
        }
        return false;
    }

}
