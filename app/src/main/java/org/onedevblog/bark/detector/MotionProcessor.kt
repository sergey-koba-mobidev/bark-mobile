package org.onedevblog.bark.detector

import org.onedevblog.bark.detector.actions.SaveToMediaLibrary
import org.onedevblog.bark.detector.actions.SendToTelegram
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.text.SimpleDateFormat
import java.util.*


class MotionProcessor {
    private val TAG = "MotionProcessor"
    private var lastFrameTime = System.currentTimeMillis()

    fun handleFrame(detectorFrame: DetectorFrame, detectorPrefs: DetectorPreferences) {
        if (!detectorFrame.isMotionDetected) {
            return
        }
        val frame = detectorFrame.frame
        if (lastFrameTime + detectorPrefs.delay > System.currentTimeMillis()) {
            return
        }
        lastFrameTime = System.currentTimeMillis()

        if (!detectorPrefs.saveToMediaLibrary && !detectorPrefs.sendToTelegram) {
            return
        }

        if (detectorPrefs.rotation != 0) {
            when(detectorPrefs.rotation) {
                90 -> Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE)
                180 -> Core.rotate(frame, frame, Core.ROTATE_180)
                270 -> Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE)
            }
        }
        val sz = Size(detectorPrefs.imageWidth.toDouble(), detectorPrefs.imageHeight.toDouble())
        Imgproc.resize(frame, frame, sz)
        val formater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
        val imageText = detectorPrefs.name + " " + formater.format(Date())
        if (detectorPrefs.putDateOnImage) {
            Imgproc.putText(frame, imageText, Point(20.0, 20.0), 4, 1.0, Scalar(255.0, 0.0, 0.0, 255.0), 2)
        }

        if (detectorPrefs.saveToMediaLibrary) {
            SaveToMediaLibrary().execute(DetectorFrame(frame.clone(), detectorFrame.isMotionDetected))
        }
        if (detectorPrefs.sendToTelegram) {
            SendToTelegram().execute(DetectorFrame(frame.clone(), detectorFrame.isMotionDetected))
        }

        frame.release()
        detectorFrame.release()
    }
}