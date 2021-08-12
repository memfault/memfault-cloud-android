package com.memfault.cloud.sample

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class SampleActivity : AppCompatActivity() {
    private lateinit var viewModel: SampleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        viewModel = ViewModelProvider(this).get(SampleViewModel::class.java)

        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, viewModel.spinnerEntries())
        findViewById<Spinner>(R.id.device_spinner).adapter = spinnerAdapter
    }

    fun getLatestRelease(@Suppress("UNUSED_PARAMETER") v: View) = viewModel.getLatestRelease(currentSelection())

    @ExperimentalUnsignedTypes
    fun addChunks(@Suppress("UNUSED_PARAMETER") v: View) = viewModel.addChunks(currentSelection())

    fun sendChunks(@Suppress("UNUSED_PARAMETER") v: View) = viewModel.sendChunks(currentSelection())

    private fun currentSelection() =
        this.findViewById<Spinner>(R.id.device_spinner).selectedItem.toString()
}
