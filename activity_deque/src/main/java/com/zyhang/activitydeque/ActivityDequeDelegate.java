package com.zyhang.activitydeque;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Created by zyhang on 2018/12/1.22:39
 */
public interface ActivityDequeDelegate {
    /**
     * @return 记录到堆栈的Activity，返回空则不记录
     */
    @Nullable
    Activity onActivityCreated(Activity activity, Bundle savedInstanceState);

    /**
     * @return 从堆栈移除的Activity，返回空则不移除
     */
    @Nullable
    Activity onActivityDestroyed(Activity activity);
}
