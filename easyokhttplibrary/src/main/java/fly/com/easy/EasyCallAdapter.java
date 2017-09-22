package fly.com.easy;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public interface EasyCallAdapter<T extends EasyCall> {
    T adapt(EasyCall call);
}
