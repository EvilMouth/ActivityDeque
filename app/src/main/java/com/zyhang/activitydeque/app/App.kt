@file:Suppress("UNUSED_PARAMETER", "unused")

package com.zyhang.activitydeque.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zyhang.activitydeque.ActivityDeque
import com.zyhang.activitydeque.ActivityDequeDelegate
import kotlinx.android.synthetic.main.activity_base.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ActivityDeque.getInstance().setActivityDequeDelegate(object : ActivityDequeDelegate {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?): Activity? {
                if (activity is BaseActivity) {
                    return activity
                }
                return null
            }

            override fun onActivityDestroyed(activity: Activity?): Activity? {
                if (activity is BaseActivity) {
                    return activity
                }
                return null
            }
        })
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun start(view: View) {
        startActivity(Intent(this, ActivityOne::class.java))
    }
}

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        Log.i(javaClass.simpleName, "onCreate")
        val stringBuilder = StringBuilder()
        for (activity in ActivityDeque.getInstance().arrayDeque) {
            stringBuilder.append(activity.javaClass.simpleName)
            stringBuilder.append("\n")
        }
        textView.text = stringBuilder.toString()
    }

    override fun onStart() {
        super.onStart()
        Log.i(javaClass.simpleName, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(javaClass.simpleName, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(javaClass.simpleName, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(javaClass.simpleName, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(javaClass.simpleName, "onDestroy")
    }

    fun nextActivity(view: View) {
        nextActivity()
    }

    abstract fun nextActivity()

    fun recreateAll(view: View) {
        ActivityDeque.getInstance().recreateAll()
    }

    fun destroyAll(view: View) {
        ActivityDeque.getInstance().finishAll()
    }
}

class ActivityOne : BaseActivity() {
    override fun nextActivity() {
        startActivity(Intent(this, ActivityTwo::class.java))
    }
}

class ActivityTwo : BaseActivity() {
    override fun nextActivity() {
        startActivity(Intent(this, ActivityThree::class.java))
    }
}

class ActivityThree : BaseActivity() {
    override fun nextActivity() {
        startActivity(Intent(this, ActivityFour::class.java))
    }
}

class ActivityFour : BaseActivity() {
    override fun nextActivity() {
        startActivity(Intent(this, ActivityFive::class.java))
    }
}

class ActivityFive : BaseActivity() {
    override fun nextActivity() {
        Toast.makeText(this, "no next", Toast.LENGTH_SHORT).show()
    }
}
