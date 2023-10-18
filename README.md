
# Auro Proctoring SDK

Proctoring is a process used to monitor and supervise online exams or assessments to ensure their integrity and prevent cheating. It involves the use of technology and human invigilators to monitor test-takers remotely.



## Environment Variables

To run this project, you will need to add the following environment variables to your .env file




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
	        implementation 'com.github.azzadpandit1:ProctoringSDK:0.0.9'
	}
```

## Run ProctoringSDK

```kotlin
    val proctoringSDK = ProctoringSDK(this, null)

    binding.mainLayout.gravity = Gravity.END

    binding.mainLayout.addView(proctoringSDK)
        
    proctoringSDK.startProctoring(this)
```
## Handle Result 

```kotlin

    override fun onVoiceDetected(
        amplitude: Double,
        isNiceDetected: Boolean,
        isRunning: Boolean,
        typeOfVoiceDetected: String
    ) {

    }

    override fun onFaceCount(faceCount: String) {

    }

    override fun isRunningDetector(boolean: Boolean?) {
        super.isRunningDetector(boolean)

    }

    override fun onSuccess(faceBounds: Int) {
        super.onSuccess(faceBounds)
    }

    override fun onLipMovementDetection(mouth: Boolean) {

    }

    override fun onObjectDetection(object: String) {

    }

    override fun onEyeDetectionOnlyOneFace(eye: String) {

    }

    override fun onUserWallDistanceDetector(distance: Float) {

    }

```

