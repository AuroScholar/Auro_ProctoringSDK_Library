package com.example.auroproctoringsdk

/**
 * Controls
 *
 * @constructor Create empty Controls
 */
class Controls{

    private var initControlModel = ControlModel(
        true,
        true,
        true,
        true,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        false,
        true,
        true,
        true,
        false,
        30000,
        "high",
        true,
        false,
        listOf(),
        listOf("Mobile phone", "Computer", "Camera"),
        0.5f,
        0.5f,
        0.2f,
        0.2f,
        3,3,50
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