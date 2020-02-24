package com.xjh.crashhelperdemo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    companion object {
        val instance: CrashHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CrashHandler()
        }
        private val PATH: String = Environment.getExternalStorageDirectory().path + "/crash"
        private val FILE_NAME: String = "crash"
        private val FILE_NAME_SUFFIX: String = ".txt"
    }

    private lateinit var mContext: Context
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    fun init(context: Context) {
        mContext = context
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (mDefaultHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        mDefaultHandler?.let {
            handleException(e)

            // 让系统捕获异常
            it.uncaughtException(t, e)
        }
    }

    private fun handleException(e: Throwable?): Boolean {
        e?.let {
            writeToSDCard(e)
            return true
        }
        return false
    }

    private fun writeToSDCard(e: Throwable) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return
        }

        val fileDir = File(PATH)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        val currenttime = System.currentTimeMillis()
        val time = SimpleDateFormat("yyyy-MM-DD HH:mm:ss").format(Date(currenttime))

        val file = File("$PATH/$FILE_NAME$time$FILE_NAME_SUFFIX")
        val pw = PrintWriter(BufferedWriter(FileWriter(file)))

        // 获取时间
        pw.println(time)

        // 获取版本号
        val info = mContext.packageManager.getPackageInfo(
            mContext.packageName,
            PackageManager.GET_ACTIVITIES
        )
        pw.println("App version = " + info.versionName + "_" + info.versionCode)

        // 打印系统版本
        pw.println("OS version = " + Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT)

        // 打印机型
        pw.println("Model:" + Build.MODEL + "\nmaler = " + Build.MANUFACTURER)

        // 打印报错
        e.printStackTrace(pw)
        pw.close()
    }
}