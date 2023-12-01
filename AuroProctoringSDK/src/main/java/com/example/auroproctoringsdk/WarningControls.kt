package com.example.auroproctoringsdk

class WarningControls(private var controlModel: ControlModel) {
    private var initFaceCountWarning = -1

    private var defaultAlert = true
    private var statusBarLock: Boolean = false

    init {
        initFaceCountWarning = 0
    }

    fun updateControl(oldcontrolModel: ControlModel) {
        controlModel = oldcontrolModel
    }

    /*fun resetControl(): ControlModel {
       *//* return ControlModel()*//*
    }*/

    fun getFaceWarningCount(status: Boolean): Int {
        return if (status) {
            initFaceCountWarning++
        } else {
            initFaceCountWarning
        }
    }

    fun stopAlertRunningDetector(runningDetector: Boolean): Boolean {
        return runningDetector != runningDetector
    }

    fun getControls(): ControlModel {
        return controlModel
    }

}