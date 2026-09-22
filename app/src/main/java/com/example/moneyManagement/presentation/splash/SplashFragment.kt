package com.example.moneyManagement.presentation.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moneyManagement.R
import com.example.moneyManagement.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val SPLASH_DISPLAY_DURATION_MS = 1300L

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playEntranceAnimation()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DISPLAY_DURATION_MS.milliseconds)
            navigateToDashboard()
        }
    }

    private fun playEntranceAnimation() {
        binding.logoCard.apply {
            scaleX = 0.6f
            scaleY = 0.6f
            alpha = 0f
        }
        binding.appNameText.alpha = 0f
        binding.taglineText.alpha = 0f

        val logoScaleX = ObjectAnimator.ofFloat(binding.logoCard, View.SCALE_X, 0.6f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.logoCard, View.SCALE_Y, 0.6f, 1f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.logoCard, View.ALPHA, 0f, 1f)
        val textAlpha = ObjectAnimator.ofFloat(binding.appNameText, View.ALPHA, 0f, 1f)
        val taglineAlpha = ObjectAnimator.ofFloat(binding.taglineText, View.ALPHA, 0f, 1f)

        AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 500
            interpolator = OvershootInterpolator(1.1f)
            start()
        }
        AnimatorSet().apply {
            playTogether(textAlpha, taglineAlpha)
            startDelay = 250
            duration = 400
            start()
        }
    }

    private fun navigateToDashboard() {
        findNavController().navigate(SplashFragmentDirections.actionSplashToDashboard())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
