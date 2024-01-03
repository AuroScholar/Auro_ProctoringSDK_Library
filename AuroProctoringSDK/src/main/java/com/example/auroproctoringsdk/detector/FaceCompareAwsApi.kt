package com.example.auroproctoringsdk.detector

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.rekognition.RekognitionClient
import aws.sdk.kotlin.services.rekognition.model.CompareFacesMatch
import aws.sdk.kotlin.services.rekognition.model.CompareFacesRequest
import aws.sdk.kotlin.services.rekognition.model.Image
import aws.sdk.kotlin.services.rekognition.model.InvalidParameterException
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import java.io.File
import kotlin.system.exitProcess

class FaceCompareAwsApi {

    private var onFaceCompareAwsApiListener: FaceCompareAwsApiListener? = null

    fun setFaceCompareAwsResult(listener: FaceCompareAwsApiListener) {
        onFaceCompareAwsApiListener = listener
    }

    suspend fun faceCompareProcess(args: ArrayList<String>) {

        val usage = """
        Usage: <pathSource> <pathTarget>

        Where:
            pathSource - The path to the source image (for example, C:\AWS\pic1.png). 
            pathTarget - The path to the target image (for example, C:\AWS\pic2.png). 
    """

        if (args.size != 2) {
            println(usage)
            onFaceCompareAwsApiListener?.onFaceError(usage)
//            exitProcess(0)
        }

        val similarityThreshold = 70f
        val sourceImage = args[0]
        val targetImage = args[1]
        compareTwoFaces(similarityThreshold, sourceImage, targetImage, onFaceCompareAwsApiListener)
    }

    suspend fun compareTwoFaces(
        similarityThresholdVal: Float,
        sourceImageVal: String,
        targetImageVal: String,
        onFaceCompareAwsApiListener: FaceCompareAwsApiListener?,
    ) {

        val sourceBytes = (File(sourceImageVal).readBytes())
        val targetBytes = (File(targetImageVal).readBytes())

        val souImage = Image {
            bytes = sourceBytes
        }

        val tarImage = Image {
            bytes = targetBytes
        }

        val facesRequest = CompareFacesRequest {
            sourceImage = souImage
            targetImage = tarImage
            similarityThreshold = similarityThresholdVal
        }

        val accessKeyId = "AKIAIWIFCITZNBA5CZ5Q"
        val secretAccessKey = "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"

        val awsCredentialsProvider = StaticCredentialsProvider(
            Credentials(accessKeyId, secretAccessKey)
        )

        try {
            // Existing code here...
            RekognitionClient {
                region = "us-east-1"
                credentialsProvider = awsCredentialsProvider
            }.use { rekClient ->

                try {
                    val compareFacesResult = rekClient.compareFaces(facesRequest)
                    val faceDetails = compareFacesResult.faceMatches

                    val uncompared = compareFacesResult.unmatchedFaces
                    if (uncompared != null && uncompared.isNotEmpty()) {
                        println("There were ${uncompared.size} face(s) that did not match")
                        onFaceCompareAwsApiListener?.onFaceResult("There were ${uncompared.size} face(s) that did not match")
                    } else if (faceDetails != null) {
                        for (match: CompareFacesMatch in faceDetails) {
                            val face = match.face
                            val position = face?.boundingBox
                            if (position != null) {
                                println("Face at ${position.left} ${position.top} matches with ${face.confidence} % confidence.")
                                val data =
                                    "Face at ${position.left} ${position.top} matches with ${face.confidence} % confidence."
                                onFaceCompareAwsApiListener?.onFaceResult(data)
                            }
                        }
                    } else {
                        onFaceCompareAwsApiListener?.onFaceResult("no result found")
                    }


                } catch (e: Exception) {
                    println("Error: --- >  ${e.message}")
                    onFaceCompareAwsApiListener?.onFaceError("Error: ${e.message}")
                }
            }
        } catch (e: InvalidParameterException) {
            println("Error: Invalid parameters - ${e.message}")
            onFaceCompareAwsApiListener?.onFaceError("Error: Invalid parameters - ${e.message}")
        } catch (e: Exception) {
            println("Error: ${e.message}")
            onFaceCompareAwsApiListener?.onFaceError("Error: ${e.message}")
        }


    }

    interface FaceCompareAwsApiListener {
        fun onFaceUnCompareResult(s: String)
        fun onFaceResult(s: String)
        fun onFaceError(s: String)
    }
}
