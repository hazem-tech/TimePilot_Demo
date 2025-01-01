package com.timepilot.demo

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.timepilot.demo.UsageEventsService.Companion.BLOCK_ALL_YT_VIDEOS
import com.timepilot.demo.UsageEventsService.Companion.BLOCK_YT_SHORTS
import com.timepilot.demo.UsageEventsService.Companion.browserApps
import com.timepilot.demo.UsageEventsService.Companion.ytAllowedVideos

var MyAccessibilityServiceInstance: MyAccessibilityService? = null
var UsageEventsServiceInstance: UsageEventsService? = null

class MyAccessibilityService : AccessibilityService() {
    companion object {
        var currentApp = "null"
            private set
        var currentPage = "DONT_BLOCK"
            private set
        private var wasVideoOpened = false
        // private var lastEventSourceHashCode = 0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i("AccessibilityService", "onAccessibilityEvent() called")
        val source = event?.source ?: return
        //currentApp = event.packageName?.toString() ?: return
        // val currentEventSourceHashCode = source.hashCode()
        // if (currentEventSourceHashCode != lastEventSourceHashCode) lastEventSourceHashCode = currentEventSourceHashCode else return // if the same

        Log.d("AccessibilityService", "checking for the currentApp")

        when (currentApp) {
            "com.google.android.youtube" -> {
                Log.d("AccessibilityService", "${source.text} ${source.className}")
                Log.d("AccessibilityService", "${source.javaClass}")
                Log.d("AccessibilityService", "${event.className}")
                Log.d("AccessibilityService","${event.javaClass}")

                val isShorts =
                    source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_pivot_button").isNotEmpty() // or reel_scrim_shorts_while_bottom_gradient???
                if (isShorts) {
                    currentPage = BLOCK_YT_SHORTS
                    Toast.makeText(this, "yt shorts opened", LENGTH_SHORT).show()
                    Log.d("TimePilot_Accessibility", "yt shorts opened")
                    return
                }

                val isVideoOpened = (source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/watch_while_time_bar_view") +
                        //source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/video_info_loading_layout") +
                        //source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/engagement_panel_wrapper") +
                        //source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/video_metadata_layout") +
                        //source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/watch_panel_scrim") +
                        source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/watch_panel")).isNotEmpty()
                if (isVideoOpened) {
                    Log.d("TimePilot_Accessibility", "video page opened, wasVideoOpened: $wasVideoOpened")
                    if (!wasVideoOpened) {
                        for (video in ytAllowedVideos) {
                            // check by text not title cuz that will also check the yt channel name
                            val titleNode = source.findAccessibilityNodeInfosByText(video)
                            if (titleNode.isNotEmpty()) {
                                currentPage = "ALLOWED_VIDEO"
                                wasVideoOpened = true
                                Toast.makeText(this, "allowed yt video", LENGTH_SHORT).show()
                                Log.d("TimePilot_Accessibility", "allowed youtube video, ${titleNode[0].text}")
                                // if we found any video from the allowed videos list, make page allowed and return from the whole code
                                return
                            }
                        }
                        // if nothing returned
                        currentPage = BLOCK_ALL_YT_VIDEOS
                        Toast.makeText(this, "blocked yt video", LENGTH_SHORT).show()
                        // Log.d("TimePilot_Accessibility", "blocked youtube video, can't find element contains any of ${ytAllowedVideos.joinToString()}")
                    }
                    return
                }

                val isNotVideoScreen =
                    source.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/pivot_bar").isNotEmpty() // or bottom_bar_container
                if (isNotVideoScreen) {
                    currentPage = "DONT_BLOCK"
                    wasVideoOpened = false
                    return
                }
            }

            in browserApps -> {
                val urlNode = when (currentApp) {
                    "com.android.chrome" -> source.findAccessibilityNodeInfosByViewId("com.android.chrome:id/url_bar")
                    "com.microsoft.bing" -> source.findAccessibilityNodeInfosByViewId("com.microsoft.bing:id/iab_address_bar_text_view")
                    "com.microsoft.emmx" -> source.findAccessibilityNodeInfosByViewId("com.microsoft.emmx:id/url_bar")
                    "com.microsoft.emmx.canary" -> source.findAccessibilityNodeInfosByViewId("com.microsoft.emmx.canary:id/url_bar")
                    "com.brave.browser" -> source.findAccessibilityNodeInfosByViewId("com.brave.browser:id/url_bar")
                    "com.duckduckgo.mobile.android" -> source.findAccessibilityNodeInfosByViewId("com.duckduckgo.mobile.android:id/omnibarTextInput")
                    "org.mozilla.firefox" -> source.findAccessibilityNodeInfosByViewId("org.mozilla.firefox:id/mozac_browser_toolbar_url_view")
                    "com.opera.browser" -> source.findAccessibilityNodeInfosByViewId("com.opera.browser:id/url_field")
                    "com.opera.mini.native" -> source.findAccessibilityNodeInfosByViewId("com.opera.mini.native:id/url_field")
                    else -> source.findAccessibilityNodeInfosByViewId("${event.packageName}:id/url_bar")
                }

                Log.d("TimePilot_Accessibility", "urlNode: $urlNode")

                if (urlNode.isNotEmpty()) {
                    val urlBarNode = urlNode[0]
                    Log.d("TimePilot_Accessibility", "tab detected ${urlBarNode.text}")
                    if (urlBarNode.text != null && !urlBarNode.isFocused) {
                        currentPage = urlBarNode.text.toString()
                        Log.d("TimePilot_Accessibility", "current url is $currentPage")
                        return
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        MyAccessibilityServiceInstance = this

//        if (!isServiceRunning) {
//            val serviceIntent = Intent(this, UsageEventsService::class.java)
//            startForegroundService(serviceIntent)
//            Log.d("TimePilot_Accessibility", "Service Called from AccessibilityService")
//        }
    }

    override fun onInterrupt() {
        // Handle interruptions here
    }
}