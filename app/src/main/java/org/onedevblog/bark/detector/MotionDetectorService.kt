package org.onedevblog.bark.detector

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build

import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.onedevblog.bark.MainActivity
import org.onedevblog.bark.R
import android.graphics.ImageFormat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.media.ImageReader
import android.util.Log
import androidx.annotation.NonNull
import android.hardware.camera2.*
import java.util.*
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class MotionDetectorService: Service() {
    private val TAG = "MotionDetectorService"
    private val detectorPref = DetectorPreferences()
    private lateinit var cameraDevice: CameraDevice
    private lateinit var imageReader: ImageReader
    private lateinit var session: CameraCaptureSession
    private val motionDetector = MotionDetector()
    private val motionProcessor = MotionProcessor()
    private val motionVideoProcessor = MotionVideoProcessor()
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    initCamera()
                }
                else -> {
                    super.onManagerConnected(status)
                    isServiceRunning = false
                    stopSelf()
                }
            }
        }
    }

    private var cameraStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened")
            cameraDevice = camera
            actOnReadyCameraDevice()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected")
            isServiceRunning = false
            stopSelf()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "CameraDevice.StateCallback onError $error")
            isServiceRunning = false
            stopSelf()
        }
    }

    private var sessionStateCallback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {

        override fun onReady(session: CameraCaptureSession) {
            this@MotionDetectorService.session = session
            try {
                if (isServiceRunning) {
                    Log.d(TAG, "sessionStateCallback onReady")
                    session.setRepeatingRequest(createCaptureRequest(), null, null)
                    cameraCaptureStartTime = System.currentTimeMillis()
                }
            } catch (e: CameraAccessException) {
                isServiceRunning = false
                Log.e(TAG, e.message)
                stopSelf()
            }
        }

        override fun onConfigured(session: CameraCaptureSession) {
        }

        override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
            stopSelf()
        }
    }

    protected var onImageAvailableListener: ImageReader.OnImageAvailableListener =
        ImageReader.OnImageAvailableListener { reader ->
            val img = reader.acquireLatestImage()
            if (img != null) {
                if (System.currentTimeMillis() > cameraCaptureStartTime + CAMERA_CALIBRATION_DELAY) {
                    val frame = JavaCamera2Frame(img)
                    val detectorFrame = motionDetector.processMat(frame, detectorPref)
                    motionProcessor.handleFrame(detectorFrame, detectorPref)
                    motionVideoProcessor.handleImg(img, detectorFrame.isMotionDetected)
                    frame.release()
                }
                img.close()
            }
        }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCOmmand")
        handlerThread = HandlerThread("MotionDetectorServiceThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        initOpenCv()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        detectorPref.load()
        startForeground()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        isServiceRunning = false
        motionVideoProcessor.release()
        session.stopRepeating()
        session.abortCaptures()
        session.close()
        cameraDevice.close()
        super.onDestroy()
    }

    private fun initOpenCv() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private fun initCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            isServiceRunning = true
            manager.openCamera(detectorPref.camera, cameraStateCallback, null)
            imageReader = ImageReader.newInstance(detectorPref.imageWidth, detectorPref.imageHeight, ImageFormat.YUV_420_888, 2 /* images buffered */)
            imageReader.setOnImageAvailableListener(onImageAvailableListener, handler)
            Log.d(TAG, "imageReader created")
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
        }
    }

    private fun actOnReadyCameraDevice() {
        try {
            Log.d(TAG, "actOnReadyCameraDevice")
            val surfaces = ArrayList<Surface>().apply {
                add(imageReader.surface)
            }
            cameraDevice.createCaptureSession(surfaces, sessionStateCallback, null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
        }

    }

    private fun createCaptureRequest(): CaptureRequest? {
        try {
            Log.d(TAG, "createCaptureRequest")
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                addTarget(imageReader.surface)
            }

            return builder.build()
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
            return null
        }

    }

    private fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.online))
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.app_name))
            .setOngoing(true)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    companion object {
        val ONGOING_NOTIFICATION_ID = 7770
        val CHANNEL_ID = "motion_detector_service_channel_id"
        val CHANNEL_NAME = "motion_detector_service_channel_name"
        var isServiceRunning = false
        var cameraCaptureStartTime: Long = 0
        val CAMERA_CALIBRATION_DELAY = 500
    }
}