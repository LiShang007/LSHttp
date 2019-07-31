package com.lishang.http.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class LSHttpActivityLifecycleCallBacks extends LSHttpLifecycleCallBacks implements Application.ActivityLifecycleCallbacks {


    private LSHttpFragmentLifecycleCallBacks fragmentLifecycleCallBacks;

    public LSHttpActivityLifecycleCallBacks() {
        this.fragmentLifecycleCallBacks = new LSHttpFragmentLifecycleCallBacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

        if (activity instanceof FragmentActivity) {

            FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
            fm.registerFragmentLifecycleCallbacks(fragmentLifecycleCallBacks, true);

        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

        String key = activity.getClass().getName();
        destroyed(key);
    }


}
