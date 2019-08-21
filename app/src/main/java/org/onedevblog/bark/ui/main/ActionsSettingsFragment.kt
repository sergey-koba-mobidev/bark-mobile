package org.onedevblog.bark.ui.main

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import org.onedevblog.bark.R
import org.onedevblog.bark.detector.DetectorPreferences

class ActionsSettingsFragment : Fragment(), CanBeSaved {
    private lateinit var detectorPref: DetectorPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_actions_settings, container, false)
        detectorPref = DetectorPreferences()
        detectorPref.load()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<CheckBox>(R.id.save_to_medialibrary_checkbox).setChecked(detectorPref.saveToMediaLibrary)
        view.findViewById<CheckBox>(R.id.send_to_telegram_checkbox).setChecked(detectorPref.sendToTelegram)
        view.findViewById<EditText>(R.id.telegram_chat_id_edit).setText(detectorPref.telegramChatId)
    }

    override fun save() {
        detectorPref.saveToMediaLibrary = activity!!.findViewById<CheckBox>(R.id.save_to_medialibrary_checkbox).isChecked
        detectorPref.sendToTelegram = activity!!.findViewById<CheckBox>(R.id.send_to_telegram_checkbox).isChecked
        detectorPref.telegramChatId = activity!!.findViewById<EditText>(R.id.telegram_chat_id_edit).text.toString()
        detectorPref.save()
        Snackbar.make(activity!!.findViewById(R.id.fab), activity!!.resources.getString(R.string.saved_settings), Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

}