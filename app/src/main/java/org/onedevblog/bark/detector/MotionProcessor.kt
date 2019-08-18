package org.onedevblog.bark.detector

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import org.onedevblog.bark.BarkApplication
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import java.util.*


class MotionProcessor {
    private val TAG = "MotionProcessor"
    private var lastFrameTime = System.currentTimeMillis()

    //TODO: run async task and actions
    fun handleFrame(detectorFrame: DetectorFrame, detectorPrefs: DetectorPreferences) {
        if (!detectorFrame.isMotionDetected) {
            return
        }
        val frame = detectorFrame.frame
        if (lastFrameTime + detectorPrefs.delay > System.currentTimeMillis()) {
            return
        }
        lastFrameTime = System.currentTimeMillis()

        if (detectorPrefs.rotation != 0) {
            when(detectorPrefs.rotation) {
                90 -> Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
                180 -> Core.rotate(frame, frame, Core.ROTATE_180)
                270 -> Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE)
            }
        }
        val sz = Size(detectorPrefs.imageWidth.toDouble(), detectorPrefs.imageHeight.toDouble())
        Imgproc.resize(frame, frame, sz)
        if (detectorPrefs.putDateOnImage) {
            val formater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
            val imageText = detectorPrefs.name + " " + formater.format(Date())
            Imgproc.putText(frame, imageText, Point(20.0, 20.0), 4, 1.0, Scalar(255.0, 0.0, 0.0, 255.0), 2)
        }
        val bmp = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frame, bmp)
        Log.d(TAG, "saving bitmap to galelry")
        MediaStore.Images.Media.insertImage(BarkApplication.context.contentResolver, bmp, detectorPrefs.name, "test")
        frame.release()
        detectorFrame.release()
    }
}