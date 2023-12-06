
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
Step 2. Add the dependency [![Latest Release](https://img.shields.io/github/v/release/azzadpandit1/Auro-Proctoring-SDK?include_prereleases&sort=semver)](https://github.com/azzadpandit1/Auro-Proctoring-SDK/releases/latest)

```kotlin
dependencies {
    implementation 'com.github.azzadpandit1:Auro-Proctoring-SDK:0.0.52'
}
```

## Setup ProctoringSDK 


```kotlin

// ProctoringSDK.onProctorListener for detector result
class MainActivity : AppCompatActivity(), ProctoringSDK.onProctorListener {


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    //init permission
    private var proctoringPermissionRequest = ProctoringPermissionRequest(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Permissions already granted
        if (proctoringPermissionRequest.checkPermissionGranted()) {

            binding.mainLayout.observeLifecycle(this.lifecycle)

        } else {
            //request permission
            proctoringPermissionRequest.requestPermissions()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        proctoringPermissionRequest.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

    }


}

 override fun onResume() {
        super.onResume()
        if (proctoringPermissionRequest.checkPermissionGranted()) {
            //if permission done then start proctoring // also you can control using ControlModel just add model startProctoring(this,ControlModel)
            binding.mainLayout.startProctoring(this,null)
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
        amplitude: Double, isNiceDetected: Boolean, isRunning: Boolean, typeOfVoiceDetected: String,
    ) {

    }

    override fun onFaceCount(faceCount: Int) {
        // getting face count
        // binding.textView.text = faceCount.toString()
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

    override fun onObjectDetection(face: ArrayList<String>) {
      //  binding.textView.text = face.toString()

    }

    override fun onEyeDetectionOnlyOneFace(face: String) {

    }

    override fun onUserWallDistanceDetector(distance: Float) {
    }

    override fun onFaceDirectionMovement(faceDirection: String?) {

    }

    override fun captureImage(faceDirection: Bitmap?) {

    }



```
## Custom Control

```kotlin
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

```

