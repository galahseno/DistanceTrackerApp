package com.example.distancetrackerapp.utils

object Constant {

    const val KM = "km"
    const val INTENT_TYPE = "text/plain"

    const val ZERO = 0L
    const val TEN_FLOAT = 10f
    const val ONE_SECOND = 1000
    const val TWO_SECOND = 2000
    const val ONE_HUNDRED = 100
    const val DELAY = 2500L

    const val PERMISSION_LOCATION_REQUEST_CODE = 1
    const val PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 2

    const val PERMISSION_DENY_LOCATION_MESSAGE =
        "This Application cant work without Location Permission"
    const val PERMISSION_DENY_BACKGROUND_LOCATION_MESSAGE =
        "This Application cant work without Background Location Permission"

    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
    const val NOTIFICATION__ID = 3
    const val NOTIFICATION__TITLE = "Distance Traveled"

    const val PENDING_INTENT_REQUEST_CODE = 99

    const val LOCATION_UPDATE_INTERVAL = 4000L
    const val COUNT_DOWN_INTERVAL = 1000L
    const val LOCATION_FASTEST_UPDATE_INTERVAL = 2000L
}