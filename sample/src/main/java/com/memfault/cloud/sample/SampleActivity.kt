package com.memfault.cloud.sample

import android.os.Bundle
import android.view.View
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.ViewModelProvider

class SampleActivity : AppCompatActivity() {
    private lateinit var viewModel: SampleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        viewModel = ViewModelProvider(this).get(SampleViewModel::class.java)
    }

    fun getLatestRelease(v: View) = viewModel.getLatestRelease(currentSelection())

    fun addChunks(v: View) = viewModel.addChunks(currentSelection())

    fun sendChunks(v: View) = viewModel.sendChunks(currentSelection())

    private fun currentSelection() = this.findViewById<Spinner>(R.id.device_spinner).selectedItem.toString()
}
