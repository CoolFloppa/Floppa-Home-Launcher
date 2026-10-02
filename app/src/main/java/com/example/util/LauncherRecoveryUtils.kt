package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object LauncherRecoveryUtils {

    fun disableDefaultLauncher(context: Context) {
        var launched = false
        // Try standard Home app settings
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            launched = true
        } catch (_: Exception) {}

        // Fallback to default apps settings
        if (!launched) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                launched = true
            } catch (_: Exception) {}
        }

        // Fallback to application details settings
        if (!launched) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                launched = true
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "Go to Android Settings > Apps > Default Apps > Home app to disable Floppa Launcher.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun restartLauncher(context: Context) {
        try {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                val restartIntent = Intent.makeRestartActivityTask(intent.component)
                context.startActivity(restartIntent)
                if (context is Activity) {
                    context.finishAffinity()
                }
                Runtime.getRuntime().exit(0)
            } else {
                if (context is Activity) {
                    context.recreate()
                }
            }
        } catch (_: Exception) {
            if (context is Activity) {
                context.recreate()
            }
        }
    }
}
