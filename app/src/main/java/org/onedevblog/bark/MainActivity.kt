package org.onedevblog.bark

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.PowerManager
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import androidx.camera.core.PreviewConfig

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.onedevblog.bark.detector.DetectorPreferences
import org.onedevblog.bark.detector.MotionDetectorService
import android.hardware.camera2.CameraCharacteristics
import android.view.ViewGroup
import androidx.camera.core.CameraX
import androidx.camera.core.Preview


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var screenWakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        screenWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bark:screenwakelock")

        if (MotionDetectorService.isServiceRunning) {
            fab.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            fab.setImageResource(android.R.drawable.ic_media_play)
        }

        fab.setOnClickListener { view ->
            val intent = Intent(view.context, MotionDetectorService::class.java)
            var snackbarText = getResources().getString(R.string.stopped)
            if (MotionDetectorService.isServiceRunning) {
                fab.setImageResource(android.R.drawable.ic_media_play)
                if(screenWakeLock.isHeld()) {
                    screenWakeLock.release()
                }
                //MotionDetectorService.isServiceRunning = false
                stopService(intent)
            } else {
                snackbarText = getResources().getString(R.string.started)
                fab.setImageResource(android.R.drawable.ic_media_pause)
                screenWakeLock.acquire()
                startService(intent)
            }
            Snackbar.make(view, snackbarText, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        texture.post { startCamera() }
    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also { texture.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val detectorPref = DetectorPreferences()
        detectorPref.load()
        val cameraChars = manager.getCameraCharacteristics(detectorPref.camera)
        val facing = cameraChars.get(CameraCharacteristics.LENS_FACING)
        var lensFacing = CameraX.LensFacing.BACK
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            lensFacing = CameraX.LensFacing.FRONT
        }

        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(screenSize)
            setLensFacing(lensFacing)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            val parent = texture.parent as ViewGroup
            parent.removeView(texture)
            parent.addView(texture, 0)
            texture.surfaceTexture = it.surfaceTexture
        }
        CameraX.bindToLifecycle(this, preview)
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun showSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> showSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }
}
