package com.lishang.http.lifecycle;

import android.util.SparseArray;

import com.lishang.http.utils.LSLog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import okhttp3.Call;

/**
 * 绑定生命周期基类、对外提供put remove
 */
public class LSHttpLifecycleCallBacks {

    private ConcurrentMap<String, SparseArray<Call>> map = new ConcurrentHashMap<>();

    public void put(String key, Call call) {
        SparseArray<Call> calls = map.get(key);
        if (calls == null) {
            calls = new SparseArray<>();
        }
        calls.put(call.hashCode(), call);

        map.put(key, calls);

        LSLog.i("LifecycleCallBacks class:" + key + "call bind success");

    }

    public void remove(String key, Call call) {
        SparseArray<Call> calls = map.get(key);
        if (call != null) {
            calls.delete(call.hashCode());
            LSLog.i("LifecycleCallBacks class:" + key + "  call " + call.request().url().toString() + " remove success");
        }


    }

    public void destroyed(String key) {
        SparseArray<Call> calls = map.get(key);
        if (calls != null) {
            for (int i = 0; i < calls.size(); i++) {
                Call call = calls.valueAt(i);
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                    LSLog.i("LifecycleCallBacks class:" + key + "  destroyed call " + call.request().url().toString() + " cancel success");
                }
            }
            calls.clear();
            map.remove(key);

            LSLog.i("LifecycleCallBacks class:" + key + "  destroyed call remove success");

        }
    }

}
