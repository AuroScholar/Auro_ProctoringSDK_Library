package com.example.auroproctoringsdk

import android.util.Log

/**
 * Controls
 *
 * @constructor Create empty Controls
 */
class Controls {

    private var initControlModel = ControlModel()
    init {
        initControlModel.isAlert = true
        initControlModel.isProctoringStart = true
        initControlModel.isBlockNotification = true
        initControlModel.isScreenshotEnable = true
        initControlModel.isStopScreenRecording = true
        initControlModel.isCopyPaste = false
        initControlModel.isSaveImageHideFolder = true
        initControlModel.isStatusBarLock = true
        initControlModel.isCaptureImage = true
        initControlModel.isAlert = true
        initControlModel.isAlertMultipleFaceCount = true
        initControlModel.isAlertFaceNotFound = true
        initControlModel.isAlertVoiceDetection = true
        initControlModel.isAlertLipMovement = false
        initControlModel.isAlertObjectDetection = true
        initControlModel.isAlertDeveloperModeOn = true
        initControlModel.isAlertEyeDetection = false
        initControlModel.isAlertEmulatorDetector = true
        initControlModel.isAlertUserWallDistanceDetector = true
        initControlModel.isAlertFaceDirectionMovement = true
        initControlModel.isScreenReadingOn = false
        initControlModel.isWaitingDelayInMillis = 30000
        initControlModel.accuracyType = "high"
        initControlModel.isDndStatusOn = true
        initControlModel.isDeveloperModeOn = false
        initControlModel.blockedEmulatorDevicesList = listOf("Pc,Emulator")
        initControlModel.isBlockedObjectList = listOf("Mobile phone", "Computer", "Camera")
        initControlModel.rightEyeOpenProbability = 0.5f
        initControlModel.leftEyeOpenProbability = 0.5f
        initControlModel.leftEyeCloseProbability = 0.2f
        initControlModel.rightEyeCloseProbability = 0.2f
        initControlModel.upperLipBottomSize = 3
        initControlModel.lowerLipTopSize = 3
        initControlModel.faceDirectionAccuracy = 50
    }

    /**
     * Update control
     *
     * @param controlModel
     */
    fun updateControl(controlModel: ControlModel) {
        this.initControlModel = controlModel
    }

    /**
     * Get controls
     *
     * @return
     */
    fun getControls(): ControlModel {
        return initControlModel
    }


}