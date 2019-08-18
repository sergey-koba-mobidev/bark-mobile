package org.onedevblog.bark.detector

import org.opencv.core.Mat

class DetectorFrame(frame: Mat, isMotionDetected: Boolean) {
    val frame = frame
    val isMotionDetected = isMotionDetected

    fun release() {
        frame.release()
    }
}