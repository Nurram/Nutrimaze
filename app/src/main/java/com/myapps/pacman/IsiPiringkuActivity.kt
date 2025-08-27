package com.myapps.pacman

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.myapps.pacman.databinding.ActivityIsiPiringkuBinding

class IsiPiringkuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIsiPiringkuBinding
    private var hasScrolledToBottom = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIsiPiringkuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupScrollListener()
        setupPlayButton()
    }

    private fun setupScrollListener() {
        binding.scrollView.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, _ ->
            val bottom = binding.contentLayout.height - v.height
            if (scrollY >= bottom && !hasScrolledToBottom) {
                hasScrolledToBottom = true
                binding.playButton.apply {
                    isEnabled = true
                    alpha = 1.0f
                    text = getString(R.string.mulai_bermain)
                }
            }
        }
    }

    private fun setupPlayButton() {
        binding.playButton.apply {
            isEnabled = false
            alpha = 0.5f
            text = getString(R.string.scroll_untuk_melanjutkan)

            setOnClickListener {
                if (hasScrolledToBottom) {
                    startActivity(Intent(this@IsiPiringkuActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}