package com.example.auroproctoringsdk

class Controls{
   /* private var controlModel = ControlModel(
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
        true,
        true,
        true,
        false,
        false,30000,
        "high",
        true,
        false,
        listOf(),
        listOf("Mobile phone", "Computer", "Camera"),
        0.0f,
        0.5f,
        0.2f,
        0.2f,
        3,3,50
    )*/

    private var controlModel = ControlModel(
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
        false,
        false,
        30000,
        "high",
        true,
        false,
        listOf(),
        listOf("Mobile phone", "Computer", "Camera"),
        0.0f,
        0.5f,
        0.2f,
        0.2f,
        3,3,50
    )
    fun updateControl(controlModel: ControlModel) {
        this.controlModel = controlModel
    }

    fun getControls(): ControlModel {
        return this.controlModel
    }

}