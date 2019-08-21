package org.onedevblog.bark.detector.actions

import android.graphics.Bitmap
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.Log
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendPhoto
import org.onedevblog.bark.BarkApplication
import org.onedevblog.bark.R
import org.onedevblog.bark.detector.DetectorFrame
import org.onedevblog.bark.detector.DetectorPreferences
import org.opencv.android.Utils
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SendToTelegram : AsyncTask<DetectorFrame, Void, String>() {
    val TAG = "SendToTelegram"

    override fun doInBackground(vararg p0: DetectorFrame): String {
        val detectorPrefs = DetectorPreferences()
        detectorPrefs.load()
        val telegramApiToken = BarkApplication.context.resources.getString(R.string.telegram_bot_api_token)
        val telegramBot = TelegramBot(telegramApiToken)
        val formater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
        val imageText = detectorPrefs.name + " " + formater.format(Date())
        val frame = p0[0].frame
        val bmp = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frame, bmp)
        Log.d(TAG, "sending bitmap to telegram")
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val telegramPhoto = SendPhoto(detectorPrefs.telegramChatId, stream.toByteArray())
        telegramPhoto.caption(imageText)
        telegramBot.execute(telegramPhoto)
        frame.release()
        return "Sent image to telegram"
    }
}