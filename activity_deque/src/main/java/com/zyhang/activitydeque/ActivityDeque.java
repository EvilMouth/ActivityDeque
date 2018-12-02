package com.zyhang.activitydeque;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created by zyhang on 2018/12/1.21:59
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ActivityDeque implements Application.ActivityLifecycleCallbacks {

    private Application application;
    private ActivityDequeDelegate activityDequeDelegate = new DefaultActivityDequeDelegate();
    private ArrayDeque<Activity> arrayDeque = new ArrayDeque<>();
    private WeakReference<Activity> currentActivityWeakReference;

    private static final class LazyLoad {
        private static final ActivityDeque INSTANCE = new ActivityDeque();
    }

    public static ActivityDeque getInstance() {
        return LazyLoad.INSTANCE;
    }

    /**
     * 已在ContentProvider初始化{@link ActivityDequeContentProvider#onCreate()}
     * 如果项目需要支持多进程，请在自定义Application调用{@link ActivityDeque#initMultiProcess(Context)}
     *
     * @param application {@link Application}
     */
    void init(Application application) {
        this.application = application;
        application.unregisterActivityLifecycleCallbacks(this);
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * 在Application初始化使ActivityDeque支持多线程
     *
     * @param context {@link Context}
     */
    public void initMultiProcess(Context context) throws NullPointerException {
        Objects.requireNonNull(context.getContentResolver().query(
                Uri.parse("content://" + context.getPackageName() + ".lifecycle-activity-deque"),
                null, null, null, null)).close();
    }

    /**
     * 自定义堆栈存储条件
     * 默认堆栈存储条件看{@link DefaultActivityDequeDelegate}
     * <p>
     * 建议在{@link Application#onCreate()}设置(如果需要的话)
     */
    public void setActivityDequeDelegate(ActivityDequeDelegate activityDequeDelegate) {
        this.activityDequeDelegate = activityDequeDelegate;
    }

    /**
     * @return Activity队列
     */
    @NonNull
    public ArrayDeque<Activity> getArrayDeque() {
        return arrayDeque;
    }

    /**
     * @return 栈顶Activity
     */
    @Nullable
    public Activity getTopActivity() {
        return arrayDeque.peekFirst();
    }

    /**
     * @return 当前Activity
     */
    @Nullable
    public Activity getCurrentActivity() {
        return currentActivityWeakReference != null ? currentActivityWeakReference.get() : null;
    }

    /**
     * 从栈顶启动Activity
     *
     * @param cls activityClass
     */
    public void startActivity(Class<?> cls) {
        startActivity(new Intent(application, cls));
    }

    /**
     * 从栈顶启动Activity
     * 如果栈顶Activity为空，则以application打开
     *
     * @param intent {@link Intent}
     */
    public void startActivity(Intent intent) {
        Activity top = getTopActivity();
        if (top != null) {
            top.startActivity(intent);
        } else if (application != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
        }
    }

    /**
     * 获取Activity是否还存活
     *
     * @param cls activityClass
     * @return 是否存活
     */
    public boolean isActivityAlive(Class<?> cls) {
        for (Activity activity : arrayDeque) {
            if (activity.getClass().equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取Activity实例
     *
     * @param cls activityClass
     * @return 匹配Activity，找不到返回空
     */
    @Nullable
    public Activity findActivity(Class<?> cls) {
        for (Activity activity : arrayDeque) {
            if (activity.getClass().equals(cls)) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 重新创建所有Activity
     * 从栈底开始，保持重建后堆栈顺序一致
     *
     * @see Activity#recreate()
     */
    public void recreateAll() {
        Iterator<Activity> iterator = arrayDeque.descendingIterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            iterator.remove();
            activity.recreate();
        }
    }

    /**
     * 重新创建所有Activity
     * 从栈底开始，保持重建后堆栈顺序一致
     *
     * @param excludeClasses 排除activityClass
     * @see Activity#recreate()
     */
    public void recreateAll(Class<?>... excludeClasses) {
        List<Class<?>> excludeClassList = Arrays.asList(excludeClasses);
        Iterator<Activity> iterator = arrayDeque.descendingIterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (excludeClassList.contains(activity.getClass())) {
                continue;
            }
            iterator.remove();
            activity.recreate();
        }
    }

    /**
     * 关闭指定Activity
     * 从栈顶开始
     *
     * @param cls activityClass
     * @see Activity#finish()
     */
    public void finishActivity(Class<?> cls) {
        Iterator<Activity> iterator = arrayDeque.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (activity.getClass().equals(cls)) {
                iterator.remove();
                activity.finish();
            }
        }
    }

    /**
     * 关闭所有Activity
     * 从栈顶开始
     *
     * @see Activity#finish()
     */
    public void finishAll() {
        Iterator<Activity> iterator = arrayDeque.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            iterator.remove();
            activity.finish();
        }
    }

    /**
     * 关闭所有Activity
     * 从栈顶开始
     *
     * @param excludeClasses 排除activityClass
     * @see Activity#finish()
     */
    public void finishAll(Class<?>... excludeClasses) {
        List<Class<?>> excludeClassList = Arrays.asList(excludeClasses);
        Iterator<Activity> iterator = arrayDeque.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (excludeClassList.contains(activity.getClass())) {
                continue;
            }
            iterator.remove();
            activity.finish();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activity = activityDequeDelegate.onActivityCreated(activity, savedInstanceState);
        if (activity != null) {
            arrayDeque.addFirst(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity == getCurrentActivity()) {
            currentActivityWeakReference = null;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activity = activityDequeDelegate.onActivityDestroyed(activity);
        if (activity != null) {
            arrayDeque.remove(activity);
        }
    }
}
