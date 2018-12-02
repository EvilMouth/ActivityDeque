package com.zyhang.activitydeque;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Created by zyhang on 2018/12/1.22:41
 */
public class DefaultActivityDequeDelegate implements ActivityDequeDelegate {
    @Nullable
    @Override
    public Activity onActivityCreated(Activity activity, Bundle savedInstanceState) {
        return activity;
    }

    @Nullable
    @Override
    public Activity onActivityDestroyed(Activity activity) {
        return activity;
    }
}
