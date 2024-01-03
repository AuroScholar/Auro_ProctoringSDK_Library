package com.example.auroproctoringsdk.detector

import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

/*
fun compareFaces(face1: Face, face2: Face): Float {

    val face1Landmarks = face1.allLandmarks
    val face2Landmarks = face2.allLandmarks

    // Compare the positions of specific facial landmarks
    val leftEyeDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_EYE), face2Landmarks.getOrNull(FaceLandmark.LEFT_EYE))
    val rightEyeDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_EYE), face2Landmarks.getOrNull(FaceLandmark.RIGHT_EYE))
    val noseDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.NOSE_BASE), face2Landmarks.getOrNull(FaceLandmark.NOSE_BASE))
    val mouthLeftDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.MOUTH_LEFT), face2Landmarks.getOrNull(FaceLandmark.MOUTH_LEFT))
    val mouthRightDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.MOUTH_RIGHT), face2Landmarks.getOrNull(FaceLandmark.MOUTH_RIGHT))
    val leftEarDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_EAR), face2Landmarks.getOrNull(FaceLandmark.LEFT_EAR))
    val rightEarDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_EAR), face2Landmarks.getOrNull(FaceLandmark.RIGHT_EAR))
    val leftCheekDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_CHEEK), face2Landmarks.getOrNull(FaceLandmark.LEFT_CHEEK))
    val rightCheekDistance = calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_CHEEK), face2Landmarks.getOrNull(FaceLandmark.RIGHT_CHEEK))

    // Calculate the average distance between the landmarks
    val averageDistance = (leftEyeDistance + rightEyeDistance + noseDistance + mouthLeftDistance +
            mouthRightDistance + leftEarDistance + rightEarDistance + leftCheekDistance +
            rightCheekDistance) / 9

    // Return the average distance as a measure of similarity
    return averageDistance
}
fun calculateDistance(landmark1: FaceLandmark?, landmark2: FaceLandmark?): Float {
    if (landmark1 != null && landmark2 != null) {
        val x1 = landmark1.position.x
        val y1 = landmark1.position.y
        val x2 = landmark2.position.x
        val y2 = landmark2.position.y
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
    }
    return 0f
}
*/

fun compareFaces(face1: Face, face2: Face): Float {
    val face1Landmarks = face1.allLandmarks
    val face2Landmarks = face2.allLandmarks

    val distances = mutableListOf<Float>()

    // Compare the positions of specific facial landmarks
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_EYE), face2Landmarks.getOrNull(FaceLandmark.LEFT_EYE)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_EYE), face2Landmarks.getOrNull(FaceLandmark.RIGHT_EYE)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.NOSE_BASE), face2Landmarks.getOrNull(FaceLandmark.NOSE_BASE)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.MOUTH_LEFT), face2Landmarks.getOrNull(FaceLandmark.MOUTH_LEFT)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.MOUTH_RIGHT), face2Landmarks.getOrNull(FaceLandmark.MOUTH_RIGHT)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_EAR), face2Landmarks.getOrNull(FaceLandmark.LEFT_EAR)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_EAR), face2Landmarks.getOrNull(FaceLandmark.RIGHT_EAR)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.LEFT_CHEEK), face2Landmarks.getOrNull(FaceLandmark.LEFT_CHEEK)))
    distances.add(calculateDistance(face1Landmarks.getOrNull(FaceLandmark.RIGHT_CHEEK), face2Landmarks.getOrNull(FaceLandmark.RIGHT_CHEEK)))

    // Calculate the average distance between the landmarks
    val averageDistance = distances.average().toFloat()

    // Return the average distance as a measure of similarity
    return averageDistance
}

fun calculateDistance(landmark1: FaceLandmark?, landmark2: FaceLandmark?): Float {
    if (landmark1 != null && landmark2 != null) {
        val x1 = landmark1.position.x
        val y1 = landmark1.position.y
        val x2 = landmark2.position.x
        val y2 = landmark2.position.y
        return Math.sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat()
    }
    return 0f
}
