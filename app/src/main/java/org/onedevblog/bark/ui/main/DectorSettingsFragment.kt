package org.onedevblog.bark.ui.main

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import org.onedevblog.bark.R
import org.onedevblog.bark.detector.DetectorPreferences

class DetectorSettingsFragment : Fragment(), CanBeSaved {
    private lateinit var detectorPref: DetectorPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_detector_settings, container, false)
        detectorPref = DetectorPreferences()
        detectorPref.load()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<EditText>(R.id.detector_name_edit).setText(detectorPref.name)
        view.findViewById<EditText>(R.id.detector_sensitivity_edit).setText(detectorPref.sensitivity.toString())
        view.findViewById<EditText>(R.id.delay_edit).setText(detectorPref.delay.toString())
        view.findViewById<EditText>(R.id.rotation_edit).setText(detectorPref.rotation.toString())
        view.findViewById<EditText>(R.id.image_width_edit).setText(detectorPref.imageWidth.toString())
        view.findViewById<EditText>(R.id.image_height_edit).setText(detectorPref.imageHeight.toString())
        view.findViewById<CheckBox>(R.id.put_date_checkbox).setChecked(detectorPref.putDateOnImage)
        val cameraSpinner = view.findViewById<Spinner>(R.id.camera_spinner)
        val dataAdapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, detectorPref.getCamerasIds())
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cameraSpinner.setAdapter(dataAdapter)
        val spinnerPos = dataAdapter.getPosition(detectorPref.camera)
        cameraSpinner.setSelection(spinnerPos)
    }

    override fun save() {
        detectorPref.name = activity!!.findViewById<EditText>(R.id.detector_name_edit).text.toString()
        detectorPref.sensitivity = activity!!.findViewById<EditText>(R.id.detector_sensitivity_edit).text.toString().toInt()
        detectorPref.delay = activity!!.findViewById<EditText>(R.id.delay_edit).text.toString().toInt()
        detectorPref.rotation = activity!!.findViewById<EditText>(R.id.rotation_edit).text.toString().toInt()
        detectorPref.imageWidth = activity!!.findViewById<EditText>(R.id.image_width_edit).text.toString().toInt()
        detectorPref.imageHeight = activity!!.findViewById<EditText>(R.id.image_height_edit).text.toString().toInt()
        detectorPref.putDateOnImage = activity!!.findViewById<CheckBox>(R.id.put_date_checkbox).isChecked
        detectorPref.camera = activity!!.findViewById<Spinner>(R.id.camera_spinner).selectedItem.toString()
        detectorPref.save()
        Snackbar.make(activity!!.findViewById(R.id.fab), activity!!.resources.getString(R.string.saved_settings), Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }

}