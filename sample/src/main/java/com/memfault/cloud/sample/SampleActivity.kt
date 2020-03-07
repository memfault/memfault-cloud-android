package com.memfault.cloud.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class SampleActivity : AppCompatActivity() {
    private lateinit var viewModel: SampleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        viewModel = ViewModelProvider(this).get(SampleViewModel::class.java)
    }

    fun getLatestRelease(v: View) = viewModel.getLatestRelease()

    fun postChunks(v: View) = viewModel.postChunks()
}
