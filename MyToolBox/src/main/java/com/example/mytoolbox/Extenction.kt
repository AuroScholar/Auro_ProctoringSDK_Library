package com.example.mytoolbox

import java.math.RoundingMode
import java.text.DecimalFormat


fun Double.tbx_toTwodecimal():String{
    val df = DecimalFormat("#.##")
    df.roundingMode=RoundingMode.DOWN
    return df.format(this)
}