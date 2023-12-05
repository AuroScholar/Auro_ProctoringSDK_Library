package com.example.auroproctoringsdk

data class ControlModel(

    // run time proccess
    var isProctoringStart: Boolean = true,
    var isBlockNotification: Boolean = true,
    var isScreenshotEnable: Boolean = true,
    var isStopScreenRecording: Boolean = true,
    var isCopyPaste: Boolean = false,
    var isSaveImageHideFolder: Boolean = true,
    var isStatusBarLock: Boolean = true,
    var isCaptureImage: Boolean = true,

    // alerts dialogs
    var isAlert: Boolean = true,
    var isAlertMultipleFaceCount: Boolean = true,
    var isAlertFaceNotFound: Boolean = true,
    var isAlertVoiceDetection: Boolean = true,
    var isAlertLipMovement: Boolean = true,
    var isAlertObjectDetection: Boolean = true,
    var isAlertDeveloperModeOn: Boolean = true,
    var isAlertEyeDetection: Boolean = true,
    var isAlertEmulatorDetector: Boolean = true,
    var isAlertUserWallDistanceDetector: Boolean = true,
    var isAlertFaceDirectionMovement: Boolean = true,

    // settings
    var isScreenReadingOn: Boolean = false,
    var isWaitingDelayInMillis: Long = 30000,
    var accuracyType: String = "high",
    var isDndStatusOn: Boolean = true,
    var isDeveloperModeOn: Boolean = false,
    var blockedEmulatorDevicesList: List<String>,
    var isBlockedObjectList: List<String>,

    //core for MLkit
    var rightEyeOpenProbability: Float = 0.5f,
    var leftEyeOpenProbability: Float = 0.5f,
    var leftEyeCloseProbability: Float = 0.2f,
    var rightEyeCloseProbability: Float = 0.2f,
    var upperLipBottomSize: Int = 3,
    var lowerLipTopSize: Int = 3,
    var faceDirectionAccuracy: Int = 50,

    )
