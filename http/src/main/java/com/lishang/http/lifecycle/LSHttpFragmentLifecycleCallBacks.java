package com.lishang.http.lifecycle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


public class LSHttpFragmentLifecycleCallBacks extends FragmentManager.FragmentLifecycleCallbacks {

    private LSHttpLifecycleCallBacks callBacks;

    public LSHttpFragmentLifecycleCallBacks(LSHttpLifecycleCallBacks callBacks) {
        this.callBacks = callBacks;
    }


    @Override
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
        super.onFragmentDestroyed(fm, f);

        String key = f.getClass().getName();
        callBacks.destroyed(key);


        fm.unregisterFragmentLifecycleCallbacks(this);

    }
}
