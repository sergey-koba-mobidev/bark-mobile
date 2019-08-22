package org.onedevblog.bark.detector.actions

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendPhoto
import com.pengrad.telegrambot.request.SendVideo
import org.onedevblog.bark.BarkApplication
import org.onedevblog.bark.R
import org.onedevblog.bark.detector.DetectorFrame
import org.onedevblog.bark.detector.DetectorPreferences
import org.opencv.android.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SendVideoToTelegram : AsyncTask<String, Void, String>() {
    val TAG = "SendVideoToTelegram"

    override fun doInBackground(vararg p0: String): String {
        val detectorPrefs = DetectorPreferences()
        detectorPrefs.load()
        val telegramApiToken = BarkApplication.context.resources.getString(R.string.telegram_bot_api_token)
        val telegramBot = TelegramBot(telegramApiToken)
        val formater = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")
        val imageText = detectorPrefs.name + " " + formater.format(Date())
        val fileName = p0[0]

        Log.d(TAG, "sending video to telegram")

        val telegramVideo = SendVideo(detectorPrefs.telegramChatId, File(fileName))
        telegramVideo.caption(imageText)
        telegramBot.execute(telegramVideo)

        if (!detectorPrefs.recordVideo) {
            File(fileName).delete()
        }
        return "Sent video to telegram"
    }
}