
# Auro-Proctoring-SDK

Proctoring is a process used to monitor and supervise online exams or assessments to ensure their integrity and prevent cheating. It involves the use of technology and human invigilators to monitor test-takers remotely.

## Installation

Step 1.  build file


```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```kotlin
dependencies {
    implementation 'com.github.azzadpandit1:Auro-Proctoring-SDK:0.0.21'
}
```

## Setup ProctoringSDK

```kotlin
// for activity 
// OnProctoringResultListener for detector result
class MainActivity : AppCompatActivity(), OnProctoringResultListener {

    //init permission
    private var proctoringPermissionRequest = ProctroringPermissionRequest(this)



    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Permissions already granted
        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {


            binding.mainLayout.observeLifecycle(this.lifecycle)

        } else {
            proctoringPermissionRequest.requestPermissions()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onReuestPermissionResult(requestCode,permissions,grantResults)

    }


}

override fun onResume() {
    super.onResume()
    if (proctoringPermissionRequest.checkPermissionGranted()) {
        binding.mainLayout.startProctoring(this)
    }

}


//Fragment
class QuizTestNativeFragment : Fragment , ProctoringSDK.onProctorListener {

    //onCreate 
    befor all permission are done
    binding.mainLayout.observeLifecycle(getViewLifecycleOwner());

    start proctor onResume
    binding.mainLayout.startProctoring(this);

}


```
## Handle Result ``` OnProctoringResultListener```

```kotlin

override fun onVoiceDetected(
    amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String
) {
    // detect voice and type of voice

}

override fun onFaceCount(faceCount: Int) {
    // getting face count
    binding.textView.text = faceCount.toString()

}

override fun isRunningDetector(boolean: Boolean?) {
    // detect running status
}

override fun onSuccess(faceBounds: Int) {
    // getting face count
}

override fun onFailure(exception: Exception) {
    // error on SDK level
}

override fun onLipMovementDetection(face: Boolean) {
    // Lips Movement is mouth is open and close

}

override fun onObjectDetection(faceError: String) {
    // object detection on camera
}

override fun onEyeDetectionOnlyOneFace(faceError: String) {
    // eye open and close status

}

override fun onUserWallDistanceDetector(distance: Float) {
    // user pose detection
}

override fun onFaceDirectionMovement(faceDirection: String?) {
    // user Face Direction movement left and right movement

}

override fun onFaceNotReal(faceDirection: String) {
    //Spoof Face
}

override fun onMultipleFaceCaptureImage(faceDirection: Bitmap?) {
    // multiple face found image 
}



```
## Custom Control

```kotlin
//speed controller for image clicking
proctoringWithDealy(dealInMilliseconds :Long)

//stop and start detection 
startStopDetection()

//stop Proctoring
stopProctoring()

//stopCamera
stopCamera()

```
## TensoFLow Models  Download Models into assets folder 
```

Url = https://www.tensorflow.org/lite/examples/object_detection/overview

```