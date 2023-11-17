package com.example.publicationtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.publicationtest.databinding.ActivityMainBinding

// OnProctoringResultListener for detector result
class ProctoringActivity : AppCompatActivity(){

    //init permission
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val proctoringSDK = com.auro.proctoringsdk.ProctoringSDK(this, null)
        binding.mainLayout.addView(proctoringSDK)
        proctoringSDK.startProctoring()

    }
}

