package com.example.publicationtest

import android.os.CountDownTimer

class CountdownTicker(private val duration: Long, private val interval: Long) {

    private var countDownTimer: CountDownTimer? = null

    fun start(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        countDownTimer = object : CountDownTimer(duration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                onTick.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
    }
}