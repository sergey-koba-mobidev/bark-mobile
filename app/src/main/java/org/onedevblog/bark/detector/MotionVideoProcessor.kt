package org.onedevblog.bark.detector

import android.graphics.*
import android.media.Image
import android.media.MediaRecorder
import android.util.Log
import org.onedevblog.bark.BarkApplication
import android.view.Surface
import org.onedevblog.bark.detector.actions.SendVideoToTelegram
import java.io.File

class MotionVideoProcessor {
    private val TAG = "MotionVideoProcessor"
    private var isRecordingVideo = false
    private var isReleased = false
    private var isPrepared = false
    private var lastFrameTime = System.currentTimeMillis()
    private val detectorPrefs = DetectorPreferences()
    private var nextVideoAbsolutePath = ""
    private val mediaRecorder = MediaRecorder()
    private lateinit var mSurface: Surface

    init {
        detectorPrefs.load()
    }

    fun handleImg(img: Image, isMotionDetected: Boolean) {
        if (!detectorPrefs.recordVideo && !detectorPrefs.sendVideoToTelegram) {
            return
        }
        if (isReleased) {
            return
        }
        if (isRecordingVideo) {
            if (lastFrameTime + detectorPrefs.delay > System.currentTimeMillis()) {
                Log.d(TAG, "handleImg")
                var bitmapImage = YuvImage2Bitmap.transform(img)
                val canvas = mSurface.lockCanvas(null)
                val matrix = Matrix()
                matrix.setRotate((detectorPrefs.rotation).toFloat())
                val rotatedNitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, matrix, true)
                canvas.drawBitmap(rotatedNitmap, (0).toFloat(), (0).toFloat(), null)
                mSurface.unlockCanvasAndPost(canvas)
                rotatedNitmap.recycle()
                bitmapImage.recycle()

                return
            }
            stopRecording()
            nextVideoAbsolutePath = ""
        } else if (isMotionDetected) {
            startRecording()
        }
    }

    fun release() {
        Log.d(TAG, "Release")
        if (isRecordingVideo) {
            isReleased = true
            isRecordingVideo = false
            mediaRecorder.stop()
            mediaRecorder.reset()
            mSurface.release()
        }
        isPrepared = false
        mediaRecorder.release()
    }

    private fun startRecording() {
        Log.d(TAG, "STARTED VIDEO RECORDING")
        if (!isPrepared) {
            setUpMediaRecorder()
        }
        isRecordingVideo = true
        lastFrameTime = System.currentTimeMillis()
        mediaRecorder.start()
    }

    private fun stopRecording() {
        Log.d(TAG, "STOPPED VIDEO RECORDING")
        if (isRecordingVideo && !isReleased) {
            isRecordingVideo = false
            isPrepared = false
            mediaRecorder.stop()
            mediaRecorder.reset()
            mSurface.release()
            if (detectorPrefs.sendVideoToTelegram) {
                SendVideoToTelegram().execute(nextVideoAbsolutePath)
            } else if (!detectorPrefs.recordVideo) {
                File(nextVideoAbsolutePath).delete()
            }
        }
    }

    private fun setUpMediaRecorder() {
        if (isPrepared) {
            return
        }
        if (nextVideoAbsolutePath == ""){
            nextVideoAbsolutePath = getVideoFilePath()
        }
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(nextVideoAbsolutePath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            if ( (detectorPrefs.rotation / 90) % 2 == 0) {
                setVideoSize(detectorPrefs.imageHeight, detectorPrefs.imageWidth)
            } else {
                setVideoSize(detectorPrefs.imageWidth, detectorPrefs.imageHeight)
            }
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
        mSurface = mediaRecorder.surface
        isPrepared = true
    }

    private fun getVideoFilePath(): String {
        val filename = "${System.currentTimeMillis()}.mp4"
        val dir = BarkApplication.context.getExternalFilesDir(null)

        return if (dir == null) {
            filename
        } else {
            "${dir.absolutePath}/$filename"
        }
    }
}