package org.onedevblog.bark.detector

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import org.onedevblog.bark.BarkApplication
import org.onedevblog.bark.R

class DetectorPreferences {
    var sharedPref: SharedPreferences
    var name: String = ""
    var sensitivity: Int = 0
    var delay: Int = 0
    var rotation: Int = 0
    var imageWidth: Int = 0
    var imageHeight: Int = 0
    var camera: String = ""
    var putDateOnImage: Boolean = false
    var saveToMediaLibrary: Boolean = false
    var sendToTelegram: Boolean = false
    var telegramChatId: String = ""

    init {
        sharedPref = BarkApplication.context.getSharedPreferences(BarkApplication.context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    fun load() {
        val defaultName = BarkApplication.context.resources.getString(R.string.detector_name_hint)
        val defaultSensitivity = BarkApplication.context.resources.getString(R.string.default_detector_sensitivity).toInt()
        val defaultDelay = BarkApplication.context.resources.getString(R.string.default_delay_between_photos).toInt()
        val defaultRotation = BarkApplication.context.resources.getString(R.string.default_rotation_degrees).toInt()
        val defaultImageWidth = BarkApplication.context.resources.getString(R.string.default_image_width).toInt()
        val defaultImageHeight = BarkApplication.context.resources.getString(R.string.default_image_height).toInt()
        val defaultCamera = getCamerasIds().first()
        val defaultPutDateOnImage = false
        val defaultSaveToMediaLibrary = false
        val defaultSendToTelegram = false
        val defaultTelegramChatId = ""

        name = sharedPref.getString("detector_name", defaultName)
        sensitivity = sharedPref.getInt("detector_sensitivity", defaultSensitivity)
        delay = sharedPref.getInt("detector_delay", defaultDelay)
        rotation = sharedPref.getInt("detector_rotation", defaultRotation)
        imageWidth = sharedPref.getInt("detector_image_width", defaultImageWidth)
        imageHeight = sharedPref.getInt("detector_image_height", defaultImageHeight)
        camera = sharedPref.getString("detector_camera", defaultCamera)
        putDateOnImage = sharedPref.getBoolean("detector_put_date_on_image", defaultPutDateOnImage)
        saveToMediaLibrary = sharedPref.getBoolean("actions_save_to_medialibrary", defaultSaveToMediaLibrary)
        sendToTelegram = sharedPref.getBoolean("actions_send_to_telegram", defaultSendToTelegram)
        telegramChatId = sharedPref.getString("actions_telegram_chat_id", defaultTelegramChatId)
    }

    fun save() {
        with (sharedPref.edit()) {
            putString("detector_name", name)
            putInt("detector_sensitivity", sensitivity)
            putInt("detector_delay", delay)
            putInt("detector_rotation", rotation)
            putInt("detector_image_width", imageWidth)
            putInt("detector_image_height", imageHeight)
            putString("detector_camera", camera)
            putBoolean("detector_put_date_on_image", putDateOnImage)
            putBoolean("actions_save_to_medialibrary", saveToMediaLibrary)
            putBoolean("actions_send_to_telegram", sendToTelegram)
            putString("actions_telegram_chat_id", telegramChatId)
            commit()
        }
    }

    fun getCamerasIds(): Array<String> {
        val manager = BarkApplication.context.getSystemService(CAMERA_SERVICE) as CameraManager
        return manager.getCameraIdList()
    }

}