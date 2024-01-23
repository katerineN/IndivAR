package com.raywenderlich.android.targetpractice


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.sceneform.ux.ArFragment

class GameFragment : ArFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val frameLayout =
            super.onCreateView(inflater, container, savedInstanceState) as FrameLayout

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)

        return frameLayout
    }
}
