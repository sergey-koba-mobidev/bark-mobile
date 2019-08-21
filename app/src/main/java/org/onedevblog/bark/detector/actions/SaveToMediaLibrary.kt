package org.onedevblog.bark.detector.actions

import android.graphics.Bitmap
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import org.onedevblog.bark.BarkApplication
import org.onedevblog.bark.detector.DetectorFrame
import org.onedevblog.bark.detector.DetectorPreferences
import org.opencv.android.Utils
import java.text.SimpleDateFormat
import java.util.*

class SaveToMediaLibrary : AsyncTask<DetectorFrame, Void, String>() {
    val TAG = "SaveToMediaLibrary"

    override fun doInBackground(vararg p0: DetectorFrame): String {
        val detectorPrefs = DetectorPreferences()
        detectorPrefs.load()
        val frame = p0[0].frame
        val bmp = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frame, bmp)
        val formater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
        val imageText = detectorPrefs.name + " " + formater.format(Date())

        Log.d(TAG, "saving bitmap to gallery")
        MediaStore.Images.Media.insertImage(BarkApplication.context.contentResolver, bmp, detectorPrefs.name, imageText)
        frame.release()
        p0[0].release()
        return "Saved image to media library"
    }

}