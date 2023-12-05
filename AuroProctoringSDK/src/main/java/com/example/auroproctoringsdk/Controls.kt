package com.example.auroproctoringsdk

/**
 * Controls
 *
 * @constructor Create empty Controls
 */
class Controls{

     private var initControlModel = ControlModel(
         isProctoringStart = true,
         isBlockNotification = true,
         isScreenshotEnable = true,
         isStopScreenRecording = true,
         isCopyPaste = false,
         isSaveImageHideFolder = true,
         isStatusBarLock = true,
         isCaptureImage = true,
         isAlert = true,
         isAlertMultipleFaceCount = true,
         isAlertFaceNotFound = true,
         isAlertVoiceDetection = true,
         isAlertLipMovement = true,
         isAlertObjectDetection = true,
         isAlertDeveloperModeOn = true,
         isAlertEyeDetection = false,
         isAlertEmulatorDetector = true,
         isAlertUserWallDistanceDetector = true,
         isAlertFaceDirectionMovement = true,
         isScreenReadingOn = false,
         isWaitingDelayInMillis = 30000,
         accuracyType = "high",
         isDndStatusOn = true,
         isDeveloperModeOn = false,
         blockedEmulatorDevicesList = listOf(),
         isBlockedObjectList = listOf("Mobile phone", "Computer", "Camera"),
         rightEyeOpenProbability = 0.5f,
         leftEyeOpenProbability = 0.5f,
         leftEyeCloseProbability = 0.2f,
         rightEyeCloseProbability = 0.2f,
         upperLipBottomSize = 3, lowerLipTopSize = 3, faceDirectionAccuracy = 50
    )

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
        return this.initControlModel
    }

}