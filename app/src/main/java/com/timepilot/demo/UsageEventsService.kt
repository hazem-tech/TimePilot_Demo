package com.timepilot.demo

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

class UsageEventsService : Service() {
//    private val customApps = hashSetOf("com.google.android.youtube")
//    private lateinit var allAppsPackage: HashSet<String>
//    private lateinit var allowedAppsPackage: HashSet<String>
//    private lateinit var blockedWebsList: HashSet<String>
//    private lateinit var allowedWebsList: HashSet<String>
//    private var blockAllWebs = false
//    private val noBlockWebs = hashSetOf("timepilot", "DONT_BLOCK", BLOCK_ALL_YT_VIDEOS, BLOCK_YT_SHORTS, "ALLOWED_VIDEO")
//    private var blockShorts = false
//    private var blockAllVideos = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var sharedPref: SharedPreferences
    private var activeEvent: Event? = null

    companion object {
        var ytAllowedVideos = listOf<String>()
            private set
        var isServiceRunning = false
            private set
        // var lastTimeServiceStarted = Date()

        val browserApps = hashSetOf("com.android.chrome", "org.mozilla.firefox", "com.microsoft.bing",
            "com.opera.browser", "com.opera.mini.native", "com.duckduckgo.mobile.android", "com.brave.browser",
            "com.microsoft.emmx", "com.microsoft.bing", "com.microsoft.emmx.canary")

        private const val ACTIVE_EVENT_NOTIFICATION = 10
        private const val NO_EVENT_NOTIFICATION = 11
        private const val NO_PERMISSION_NOTIFICATION = 12

        const val BLOCK_YT_SHORTS = "BLOCK_YT_SHORTS"
        const val BLOCK_ALL_YT_VIDEOS = "BLOCK_ALL_YT_VIDEOS"

        private const val MISSING_OVERLAY_PERMISSION = 24
        private const val MISSING_USAGE_PERMISSION = 25
        private const val MISSING_ACCESSIBILITY_PERMISSION = 26

        fun missingPermissions(context: Context): List<Int> {
            val isUsageAccessEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
                appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
            } else { false }
            val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager

            val missingPermissions = mutableListOf<Int>().apply {
                if (!Settings.canDrawOverlays(context)) {
                    add(MISSING_OVERLAY_PERMISSION)
                }
                if (!isUsageAccessEnabled) {
                    add(MISSING_USAGE_PERMISSION)
                }
                if (!am.isEnabled) {
                    add(MISSING_ACCESSIBILITY_PERMISSION)
                }
            }
            return missingPermissions
        }
    }

    private val blockApps: Runnable = object : Runnable {
        override fun run() {
            // it will add 0.5f no matter what every 500ms if it's timer count
//            if (activeEvent?.timerMode == TIMER_EVENT_MODE) {
//                activeEvent!!.timeSpent += 0.5f
//                Log.d("TimePilot_Service", "added 0.5 to event tim spent cuz")
//            }

            // If the device is locked, check the break status and stop it if needed
            if (keyguardManager.isDeviceLocked || !permissionsGranted) {
                Log.d(
                    "TimePilot_Service",
                    "Blocking is disabled because device is locked or permissions isn't granted"
                )
                handler.postDelayed(this, 500)
                return
            }

//            val status = when {
//                // Settings and other apps have special blocking method
//                currentApp == "com.android.settings" || currentApp == "com.samsung.accessibility" -> WARNING_APPS
//                // if browser is in an allowed apps and a supported browser
//                browserApps.contains(currentApp) -> {
//                    // sorting matters, it first check for allowed web aka exceptions then blocked if not blocked if it contains then allowed if not then not
//                    when {
//                        noBlockWebs.any { currentPage.contains(it, ignoreCase = true) } -> NO_BLOCK
//                        allowedWebsList.any { currentPage.contains(it, ignoreCase = true) } -> ALLOWED
//                        blockAllWebs || blockedWebsList.any { currentPage.contains(it, ignoreCase = true) } -> BLOCKED_WEBSITE // if not in exceptionsWebsList and blockAllWebs is true
//                        allowedAppsPackage.contains(currentApp) -> ALLOWED
//                        // if it's one of the apps that is launch-able from the home screen, block it
//                        allAppsPackage.contains(currentApp) -> BLOCKED_APP
//                        // any other system app don't block but it's not ALLOWED
//                        else -> NO_BLOCK
//                    }
//                }
//                // if the app is custom there is some attributes
//                customApps.contains(currentApp) && allowedAppsPackage.contains(currentApp) -> {
//                    when {
//                        currentPage == BLOCK_YT_SHORTS && blockShorts || currentPage == BLOCK_ALL_YT_VIDEOS && blockAllVideos -> BLOCK_CUSTOM_APP
//                        blockAllVideos && currentPage != "ALLOWED_VIDEO" -> NO_BLOCK
//                        else -> ALLOWED // if it's an allowed video or the attributes is only shorts so other than shorts is allowed
//                    }
//                }
//                // if not a browser then it might be an allowed apps
//                allowedAppsPackage.contains(currentApp) -> ALLOWED
//                // if it's one of the apps that is launch-able from the home screen, block it
//                allAppsPackage.contains(currentApp) -> BLOCKED_APP
//                // any other system app don't block but it's not ALLOWED
//                else -> NO_BLOCK
//            }

//            when (status) {
//                WARNING_APPS -> {
//                    // Start checking for permissions because there is a chance the user will remove them when they open the settings
//                    isPermissionsGranted()
//                    // Block settings if it wasn't allowed
//                    if (!allowedAppsPackage.contains(currentApp) && emergencyAmount == null) {
//                        MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME)
//                        showOverlay(BLOCKED_APP)
//                    }
//                }
//
//                BLOCKED_APP, BLOCKED_WEBSITE, BLOCK_CUSTOM_APP -> {
//                    // if not emergency use then block
//                    if (emergencyAmount == null) {
//                        showOverlay(status) // either app or web
//                    } else {
//                        emergencyAmount = emergencyAmount!! - 0.5f
//
//                        val remainingMins = emergencyAmount!!.toInt() / 60
//                        val remainingSec = emergencyAmount!!.toInt() % 60
//                        countDownTextView.text = String.format(Locale.getDefault(), "%02d:%02d", remainingMins, remainingSec)
//
//                        countDownTextView.setTextColor(getColor(R.color.red))
//
//                        // When emergencyAmount reaches zero, end event and no emergency
//                        if (emergencyAmount!! <= 0) {
//                            emergencyAmount = null
//                            countDownOverlay(show = false, updateNotification = false)
//                        }
//                        countDownTextView.alpha = 1f
//                    }
//                }
//
//                ALLOWED -> {
//                    if (activeEvent?.timerMode == APP_USAGE_EVENT_MODE) {
//                        activeEvent!!.timeSpent += 0.5f
//                    }
//                    // check if time spent reaches max time so if yes end the event and block all, reset
//                    if (activeEvent != null && activeEvent!!.timeSpent >= activeEvent!!.maxTime * 60) { // convert maxTime to seconds
//                        val eventsList = getListFromShared(this@UsageEventsService)
//                        val activeEventIndex = getListFromShared(this@UsageEventsService).indexOfFirst { it.status == ACTIVE_EVENT_STATUS }
//
//                        eventsList.forEach { if (it.status == DONE_EVENT_STATUS) it.status = PAUSED_EVENT_STATUS }
//                        activeEvent!!.status = DONE_EVENT_STATUS
//                        activeEvent!!.timeSpent = 0F
//                        eventsList[activeEventIndex] = activeEvent!!
//
//                        saveNewList(this@UsageEventsService, eventsList)
//
//                        // DO handler so toast can be made and after that continue with the code
//                        Handler(Looper.getMainLooper()).post {
//                            Toast.makeText(applicationContext, "${activeEvent!!.name} marked as done. No event is active", LENGTH_LONG).show()
//
//                            // count what? nothing so remove it
//                            countDownOverlay(show = false, updateNotification = false)
//                            // Assign empty cuz no event is active now
//                            assignActiveEvent()
                            startNewEvent()
//                        }
//                    }
//                    countDownTextView.alpha = 1f
//                }
//
//                NO_BLOCK -> countDownTextView.alpha = 0.3f
//            }
//            handler.postDelayed(this, 500)
//        }
        }

//        private lateinit var paramsCountDown: WindowManager.LayoutParams
//        private lateinit var countDownTextView: TextView
//        private val countingOverlay = object : Runnable {
//            override fun run() {
//                val remainingMins =
//                    (activeEvent!!.maxTime - activeEvent!!.timeSpent / 60) // convert time spent from seconds to minutes
//                val remainingSec =
//                    (activeEvent!!.maxTime * 60 - activeEvent!!.timeSpent) % 60 // convert to seconds then remainder of 60
//                countDownTextView.text =
//                    String.format(Locale.getDefault(), "%02d:%02d", remainingMins, remainingSec)
//
//                handler.postDelayed(this, 1000)
//            }
//        }

//    private lateinit var windowManager: WindowManager
//    private lateinit var paramsOverlay: WindowManager.LayoutParams
//    private lateinit var overlayView: View
//    private lateinit var overlayText: TextView
//    private lateinit var overlayPButton: Button
//    private lateinit var overlayNButton: Button
//    private lateinit var emergencyButton: Button
//    private var previousApp = ""
//    private var sameAppCounts = 0
//    fun showOverlay(mode: Int) {
//        if (overlayView.rootView.parent != null) return
//        emergencyButton.isVisible = true
//        overlayNButton.isVisible = false
//        overlayPButton.text = getString(R.string.ok)
//        overlayView.alpha = 0f // Set initial alpha value
//        windowManager.addView(overlayView, paramsOverlay)
//        overlayView.animate().alpha(1f).duration = 240 // Set final alpha value
//
//        if (emergencyUsed < 3) {
//            emergencyButton.setOnClickListener {
//                emergencyButton.isVisible = false
//                overlayText.text = getString(R.string.emergency_dialog_text, emergencyUsed)
//                overlayNButton.text = getString(R.string.emergency_ok_button)
//                overlayNButton.isVisible = true
//                overlayPButton.isEnabled = true
//                overlayPButton.text = getString(R.string.cancel)
//
//                overlayNButton.setOnClickListener {
//                    overlayNButton.isEnabled = false
//                    emergencyAmount = 60f
//                    emergencyUsed += 1
//                    // We don't want to post the handler update overlay
//                    countDownOverlay(show = false, updateNotification = false)
//                    windowManager.addView(countDownTextView, paramsCountDown)
//                    closeOverlay(300)
//                }
//
//                overlayPButton.setOnClickListener {
//                    overlayPButton.isEnabled = false
//                    MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME)
//                    closeOverlay(300)
//                }
//            }
//        } else {
//            emergencyButton.setOnClickListener {
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(applicationContext, getString(R.string.no_extra_use), LENGTH_SHORT).show()
//                }
//            }
//            emergencyButton.alpha = 0.35f
//        }
//
//        if (activeEvent == null) {
//            overlayText.text = getString(R.string.no_event_overlay_text)
//
//            overlayPButton.text = getString(R.string.ok)
//            overlayPButton.setOnClickListener {
//                overlayPButton.isEnabled = false
//                MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME)
//                closeOverlay(300)
//            }
//            return
//        }
//
//        if (currentApp == previousApp) {
//            sameAppCounts++
//            if (sameAppCounts > 2) {
//                overlayPButton.isEnabled = false
//                val runnable = object : Runnable {
//                    var counter = sameAppCounts * 15 - 30
//                    override fun run() {
//                        overlayPButton.text = getString(R.string.wait, counter)
//                        counter--
//                        if (counter < 0) {
//                            overlayPButton.isEnabled = true
//                            overlayPButton.text = getString(R.string.ok)
//                        } else {
//                            handler.postDelayed(this, 1000)
//                        }
//                    }
//                }
//                handler.post(runnable)
//            }
//        } else {
//            // previousApp = currentApp
//            sameAppCounts = 0
//        }
//
//        when (mode) {
//            BLOCKED_APP -> {
//                val appName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(currentApp, PackageManager.ApplicationInfoFlags.of(0))) as String
//                } else {
//                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(currentApp, 0)) as String
//                }
//                // overlayText.text = getString(R.string.app_unavailable_overlay_text, appName, activeEvent!!.name)
//
//                overlayPButton.setOnClickListener {
//                    overlayPButton.isEnabled = false
//                    MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME)
//                    // val startMain = Intent(Intent.ACTION_MAIN)
//                    // startMain.addCategory(Intent.CATEGORY_HOME)
//                    // startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    // startActivity(startMain)
//                    closeOverlay(300)
//                }
//            }
//            BLOCKED_WEBSITE -> {
//                overlayText.text = getString(R.string.app_unavailable_overlay_text, currentPage, activeEvent!!.name)
//                overlayPButton.setOnClickListener {
//                    overlayPButton.isEnabled = false
////                    if (browserApps.contains(currentApp)) {
////                        val intent = Intent(Intent.ACTION_VIEW)
////                        intent.data = Uri.parse("https://timepilot.wtf/blockedwebsite")
////                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                        intent.`package` = currentApp
////                        startActivity(intent)
////                    } else {
////                        MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_HOME)
////                    }
//                    closeOverlay(700)
//                }
//            }
//            BLOCK_CUSTOM_APP -> {
//                overlayText.text = getString(R.string.no_youtube_overlay_text, activeEvent!!.name)
//                overlayPButton.setOnClickListener {
//                    overlayPButton.isEnabled = false
//                    MyAccessibilityServiceInstance?.performGlobalAction(GLOBAL_ACTION_BACK)
//                    closeOverlay(700)
//                }
//            }
//        }
//    }
//
//    private fun closeOverlay(duration: Long) {
//        overlayView.alpha = 1f // Set initial alpha value
//        overlayView.animate().alpha(0f).duration = duration // Set final alpha value
//        handler.postDelayed({
//            windowManager.removeView(overlayView)
//            overlayPButton.isEnabled = true
//            overlayNButton.isEnabled = true
//            emergencyButton.isEnabled = true
//        }, duration)
//    }
//
//    fun countDownOverlay(show: Boolean, updateNotification: Boolean) {
//        if (show && countDownTextView.rootView.parent == null) {
//            windowManager.addView(countDownTextView, paramsCountDown)
//            handler.removeCallbacks(countingOverlay)
//            handler.post(countingOverlay)
//            countDownTextView.setTextColor(getColor(R.color.white))
//        } else if (!show && countDownTextView.rootView.parent != null) {
//            windowManager.removeView(countDownTextView)
//            handler.removeCallbacks(countingOverlay)
//        }
//        if (updateNotification) updateNotification(false)
//        Log.d("TimePilot_Service", "is timer overlay shown: ${countDownTextView.rootView.parent != null}")
//        Handler(Looper.getMainLooper()).post {
//            Toast.makeText(applicationContext, "is timer overlay shown: ${countDownTextView.rootView.parent != null}", LENGTH_SHORT).show()
//        }
//    }

        //fun assignActiveEvent() {
            // Assign new from sharedPref every time the function is called
//        allAppsPackage = sharedPref.getString("allApps", "err")!!.removeSurrounding("[", "]").split(",").map { it.trim() }.toHashSet()
//        activeEvent = getListFromShared(this).firstOrNull { it.status == ACTIVE_EVENT_STATUS }
//
//        if (activeEvent == null) {
//            // block all cuz no active event, so don't add any allowed apps or sites
//            allowedAppsPackage = HashSet<String>()
//            blockedWebsList = HashSet<String>()
//            allowedWebsList = HashSet<String>()
//        } else {
//            val alwaysAllowedApps = sharedPref.getString("alwaysAllowedApps", "err")!!.removeSurrounding("[", "]").split(",").map { it.trim() }
//            // Remove always allowed to the all apps because I don't want to block them and I don't want to make them allowed so it won't count as allowed
//            allAppsPackage = allAppsPackage.filter { app -> !alwaysAllowedApps.contains(app) }.toHashSet()
//            allowedAppsPackage = activeEvent!!.selectedAppsPackage.toHashSet()
//            blockedWebsList = activeEvent!!.blockedWebsites.toHashSet()
//            blockAllWebs = blockedWebsList.contains(BLOCK_ALL_KEYWORD)
//            allowedWebsList = activeEvent!!.allowedWebsites.toHashSet()
//            ytAllowedVideos = activeEvent!!.youtube
//            blockShorts = activeEvent!!.youtube.contains(BLOCK_YT_SHORTS)
//            blockAllVideos = activeEvent!!.youtube.contains(BLOCK_ALL_YT_VIDEOS)
//        }
//
//        Log.d("TimePilot_Service", "assignActiveEvent() called, allAppsPackage $allAppsPackage")
//        Log.d("TimePilot_Service", "allowedAppsPackage $allowedAppsPackage")
//        Log.d("TimePilot_Service", "blockedWebsList $blockedWebsList")
//        Log.d("TimePilot_Service", "exceptionsWebsList $allowedWebsList")
      //  }

        fun startNewEvent() {
            isPermissionsGranted()

            Handler(Looper.getMainLooper()).post {
                if (activeEvent != null) {
                    // Toast.makeText(applicationContext, "${activeEvent!!.name} is started from service", LENGTH_LONG).show()
                    Toast.makeText(
                        applicationContext,
                        "You spent ${activeEvent!!.timeSpent}s",
                        LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(applicationContext, "No event is Active. Service", LENGTH_LONG)
                        .show()
                }
            }
        }

        private var permissionsGranted = false
        private var checkingPermissionsRunning = false
        private var counter = 0
//        private lateinit var appOps: AppOpsManager
//        private lateinit var am: AccessibilityManager
        private fun isPermissionsGranted() {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "isPermissionsGranted() called, counter $counter",
                    LENGTH_LONG
                ).show()
            }
            permissionsGranted = missingPermissions(this@UsageEventsService).isEmpty()
            Log.d(
                "TimePilot_Service",
                "isPermissionsGranted() called, permissionsGranted: $permissionsGranted"
            )

            if (permissionsGranted) {
                // if granted update notification
                if (activeEvent == null && currentNotification != NO_EVENT_NOTIFICATION) {
                    notification(NO_EVENT_NOTIFICATION)
                } else if (activeEvent != null && currentNotification != ACTIVE_EVENT_NOTIFICATION) {
                    notification(ACTIVE_EVENT_NOTIFICATION)
                }
            } else {
                // if not granted show warning notification and main activity
                if (currentNotification != NO_PERMISSION_NOTIFICATION) {
                    notification(NO_PERMISSION_NOTIFICATION)
                }
//                if (!keyguardManager.isDeviceLocked) {
//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.putExtra("warning", true)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                }
            }

            Log.d(
                "TimePilot_Service",
                "will call the checking fun after 20s: $checkingPermissionsRunning"
            )
            // Keep calling this fun for 2 min (20s 6 times) or call it if the device is locked
            if ((counter < 6 && !checkingPermissionsRunning || keyguardManager.isDeviceLocked)) {
                handler.postDelayed({ isPermissionsGranted() }, 20 * 1000)
                checkingPermissionsRunning = true
            } else if (counter >= 6) {
                // Reset everything and don't call this fun anymore
                counter = 0
                checkingPermissionsRunning = false
                Log.d(
                    "TimePilot_Service",
                    "2 mins passed and won't check for permissions again as a loop"
                )

                // if not granted, reset and return from the fun
//            if (!permissionsGranted) {
//                with(sharedPref.edit()) {
//                    putString("events", Gson().toJson(mutableListOf<Event>()))
//                    apply()
//                }
//
//                notificationBuilder.setContentTitle(getString(R.string.all_events_removed))
//                    .setContentText(null)
//                    .setOngoing(false)
//                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(applicationContext, getString(R.string.all_events_removed), LENGTH_LONG).show()
//                }
//                Log.d("TimePilot_Service", "All events cleared")
//            }
                return
            }

            counter++
        }

//        private fun updateNotification(loop: Boolean) {
//            if (activeEvent == null || !permissionsGranted) return

            // save new event with new time spent every minute
            //val activeEventIndex = getListFromShared(this).indexOfFirst { it.status == ACTIVE_EVENT_STATUS }
            //saveEventToList(this, activeEvent!!, activeEventIndex)
            //Log.d("TimePilot_Service", "current active event saved to shared, event name: ${activeEvent!!.name}, index: $activeEventIndex")

//        notificationBuilder.clearActions()
//            .addAction(
//                R.drawable.baseline_timelapse_24,
//                if (countDownTextView.rootView.parent == null) getString(R.string.show_timer) else getString(R.string.hide_timer),
//                if (countDownTextView.rootView.parent == null) showOverlayNotification else hideOverlayNotification
//            )


//            if (activeEvent!!.maxTime * 60 - activeEvent!!.timeSpent > 80) { // convert max time to seconds
//                notificationBuilder.setContentText("You spent about ${(activeEvent!!.timeSpent / 60)} minutes")
//            } else { // if less than 90 secs for the event, change message and alert
//                notificationBuilder.setContentText("Less than minute and the event will be marked as done and ended")
//                if (!keyguardManager.isDeviceLocked) notificationBuilder.setOnlyAlertOnce(false) else notificationBuilder.setOnlyAlertOnce(
//                    true
//                )
                // Show overlay if it was hidden
//                if (countDownTextView.rootView.parent == null) countDownOverlay(
//                    show = true,
//                    updateNotification = true
//                )
                // countDownTextView.setTextColor(getColor(R.color.red))
                // Update and alert more often
//                if (loop) handler.postDelayed({ updateNotification(true) }, 20000)
//                // notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//                return
//            }
//            if (loop) handler.postDelayed({ updateNotification(true) }, 50000)
//            // notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//        }

        private var currentNotification: Int? = null
        private lateinit var notificationBuilder: NotificationCompat.Builder
        private fun notification(reason: Int) {
            currentNotification = reason
            when (reason) {
                ACTIVE_EVENT_NOTIFICATION -> {
//                notificationBuilder.setContentTitle(getString(R.string.event_running_title, activeEvent!!.name))
//                    .setContentIntent(null)
//                    .setOngoing(true)

                    // updateNotification(true)
                }

                NO_EVENT_NOTIFICATION -> {
//                notificationBuilder.setContentTitle(getString(R.string.no_event_selected_title))
//                    .setContentText(getString(R.string.no_event_selected_text))
//                    .setOngoing(true)
//                    .setContentIntent(fullScreenPendingIntent)
//                    .clearActions()
                }

                NO_PERMISSION_NOTIFICATION -> {
//                notificationBuilder.setContentTitle(getString(R.string.warning_reset_events_title))
//                    //.setContentText(getString(R.string.warning_reset_events_text))
//                    .setOngoing(false)
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setCategory(NotificationCompat.CATEGORY_CALL)
//                    .setContentIntent(fullScreenPendingIntent)
//                    .setFullScreenIntent(fullScreenPendingIntent, true)
//                    .clearActions()

                    Handler(Looper.getMainLooper()).post {
                        // Toast.makeText(applicationContext, getString(R.string.warning_reset_events_title), LENGTH_LONG).show()
                    }
                }
            }
            if (activeEvent != null && activeEvent!!.maxTime * 60 - activeEvent!!.timeSpent > 80 || !permissionsGranted || activeEvent == null) notificationBuilder.setOnlyAlertOnce(
                true
            )
            // notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            Log.d("TimePilot_Service", "Notification called reason number is $reason")
        }
    }

        override fun onCreate() {
            super.onCreate()
            Log.d("TimePilot_Service", "onCreate started service")
            // Assigning some variables just first time
            //      fullScreenPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
//        endBreakNotification = PendingIntent.getBroadcast(this, 1, Intent(this, NotificationReceiver::class.java).apply { action = "endBreak" }, PendingIntent.FLAG_IMMUTABLE)
//        hideOverlayNotification = PendingIntent.getBroadcast(this, 2, Intent(this, NotificationReceiver::class.java).apply { action = "hideOverlay" }, PendingIntent.FLAG_IMMUTABLE)
//        showOverlayNotification = PendingIntent.getBroadcast(this, 3, Intent(this, NotificationReceiver::class.java).apply { action = "showOverlay" }, PendingIntent.FLAG_IMMUTABLE)
//        // First notification
//            val notificationChannel = NotificationChannel(
//                "CHANNEL_ID",
//                "Break Amount Notification",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationChannel.setShowBadge(false)
//            notificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(notificationChannel)
//            notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
//                .setContentTitle(getString(R.string.app_name))
//            //.setSmallIcon(R.drawable.baseline_timelapse_24)
//            startForeground(NOTIFICATION_ID, notificationBuilder.build())


            // Assign this stuff only once
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            sharedPref = getSharedPreferences("time_pilot", MODE_PRIVATE)
            // windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            //appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            //am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

            // Overlay settings
//        val contextThemeWrapper = ContextThemeWrapper(this, R.style.Theme_TimePilot)
//        overlayView = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.overlay_activity, FrameLayout(contextThemeWrapper), false)
//        overlayText = overlayView.findViewById(R.id.overlayText)
//        overlayPButton = overlayView.findViewById(R.id.overlayPositiveBtn)
//        overlayNButton = overlayView.findViewById(R.id.overlayNegativeBtn)
//        emergencyButton = overlayView.findViewById(R.id.emergencyBtn)

            // Setting the theme manually to be able to use Material components
//            paramsOverlay = WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                PixelFormat.TRANSLUCENT
//            )
//            // To expand the overlay over the status and nav bar
//            overlayView.systemUiVisibility = 512
//            paramsOverlay.layoutInDisplayCutoutMode =
//                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES


            // Setting the count down overlay
//            paramsCountDown = WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, // not touchable
//                PixelFormat.TRANSLUCENT
//            )
//            paramsCountDown.gravity = Gravity.BOTTOM or Gravity.END
//            paramsCountDown.x = 40
//            paramsCountDown.y = 45
//            countDownTextView = TextView(this)
//            countDownTextView.setTextColor(getColor(R.color.white))
//            // countDownTextView.setBackgroundResource(R.drawable.pill_shape_bg)
//            countDownTextView.setPadding(25, 20, 25, 20)
//            countDownTextView.textSize = 20f
//            countDownTextView.includeFontPadding = false
//
//            assignActiveEvent()
//            startNewEvent()
            handler.post(blockApps)
            // TODO() only when isStrict mode on
            // The warning only when you remove a permission while there was an active event or you enable block all the time in the settings
            //isPermissionsGranted()

            UsageEventsServiceInstance = this
            isServiceRunning = true
        }

//    override fun onDestroy() {
//        super.onDestroy()
//        handler.removeCallbacksAndMessages(null)
//        isServiceRunning = false
//    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}