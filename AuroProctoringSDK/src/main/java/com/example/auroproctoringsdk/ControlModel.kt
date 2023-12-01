package com.example.auroproctoringsdk

data class ControlModel(

    // run time proccess
    var isProctoringStart: Boolean = true,
    var isBlockNotification: Boolean = true,
    var isScreenshotEnable: Boolean = true,
    var isScreenRecording: Boolean = true,
    var isCopyPaste: Boolean = false,
    var isSaveImageHideFolder: Boolean = true,

    // alerts dialogs
    var isAlert: Boolean = true,
    var isAlertMultipleFaceCount: Boolean = true,
    var isAlertFaceNotFound: Boolean = true,
    var isAlertVoiceDetection: Boolean = true,
    var isAlertLipMovement: Boolean = true,
    var isAlertObjectDetection: Boolean = true,
    var isAlertEyeDetection: Boolean = true,
    var isAlertUserWallDistanceDetector: Boolean = true,
    var isAlertFaceDirectionMovement: Boolean = true,
    var isAlertCaptureImage: Boolean = true,

    // settings
    var isTalkBackOn: Boolean = false,
    var isWaitingDelayInMillis: Long = 30000,
    var accuracyType: String = "high",
    var isDndStatusOn: Boolean = false,
    var isDeveloperModeOn: Boolean = false,
    var blockedEmulatorDevicesList: List<String>,

    //core for MLkit
    var rightEyeOpenProbability: Float = 0.5f,
    var leftEyeOpenProbability: Float = 0.5f,
    var leftEyeCloseProbability: Float = 0.2f,
    var rightEyeCloseProbability: Float = 0.2f,
    var upperLipBottomSize: Int = 3,
    var lowerLipTopSize: Int = 3,
    var faceDirectionAccuracy: Int = 50,
)
