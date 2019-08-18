package org.onedevblog.bark.detector

import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


class MotionDetector {
    private val TAG = "MotionDetector"
    private var avg: Mat? = null

    fun processMat(inputFrame: CameraBridgeViewBase.CvCameraViewFrame, detectorPref: DetectorPreferences): DetectorFrame {
        var outFrame = inputFrame.rgba()

        // resize the frame, convert it to grayscale, and blur it
        var frame = Mat()
        val blurSize = Size(21.0, 21.0)
        //Imgproc.cvtColor(image, frame, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(inputFrame.gray(), frame, blurSize, 0.0)

        // if the average frame is None, initialize it
        if (avg == null) {
            avg = Mat.zeros(frame.size(), CvType.CV_32F)
        }

        /*
        accumulate the weighted average between the current frame and
        previous frames, then compute the difference between the current
        frame and running average
         */

        Imgproc.accumulateWeighted(frame, avg, 0.5)
        var frameScaleAbs = Mat()
        Core.convertScaleAbs(avg, frameScaleAbs)
        var frameDelta = Mat()
        Core.absdiff(frame, frameScaleAbs, frameDelta)

        /*
        threshold the delta image, dilate the thresholded image to fill
        in holes, then find contours on thresholded image
        */
        var frameTresh = Mat()
        Imgproc.threshold(frameDelta, frameTresh, 5.0, 255.0, Imgproc.THRESH_BINARY)
        var frameDilate = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        val anchor = Point(-1.0, -1.0)
        Imgproc.dilate(frameTresh, frameDilate, kernel, anchor, 2)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(frameDilate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var isMotionDetected = false
        for (c in contours) {
            if (Imgproc.contourArea(c) > detectorPref.sensitivity) { //big enough motion
                val cRect = Imgproc.boundingRect(c)
                Imgproc.rectangle(outFrame, cRect.tl(), cRect.br(), Scalar(0.0, 255.0, 0.0), 2)
                isMotionDetected = true
            }
        }

        frame.release()
        frameScaleAbs.release()
        frameDelta.release()
        frameTresh.release()
        frameDilate.release()
        hierarchy.release()
        contours.clear()

        return DetectorFrame(outFrame, isMotionDetected)
    }
}