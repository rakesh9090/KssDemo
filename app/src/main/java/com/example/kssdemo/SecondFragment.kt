package com.example.kssdemo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.kssdemo.databinding.FragmentSecondBinding
import com.example.kssdemo.utils.viewBindings

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(R.layout.fragment_second) {

    private val _binding by viewBindings(FragmentSecondBinding::bind)
    private val binding get() = _binding

    private val TAG = "SecondFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            //findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            activity?.onBackPressed()
        }

        Glide.with(this)
            .load("https://rakesh-try.s3.ap-south-1.amazonaws.com/wecandoit.png")
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "Glide: $dataSource")
                    return false
                }

            })
            //.signature(ObjectKey(System.currentTimeMillis()))
            .into(binding.img);
    }
}