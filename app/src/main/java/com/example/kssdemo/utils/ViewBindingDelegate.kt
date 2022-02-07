package com.example.kssdemo.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Rakesh Sah
 *
 *
 *  ViewBindingDelegate-KT contains a helper delegate for auto-clearing
 *  the binding variable when the Fragment view is destroyed.
 */
class ViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    private val viewBindingFactory: ((View) -> T)? = null
) : ReadWriteProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            val viewLifecycleOwnerLiveDataObserver =
                Observer<LifecycleOwner?> {
                    val viewLifecycleOwner = it ?: return@Observer

                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observeForever(
                    viewLifecycleOwnerLiveDataObserver
                )
            }

            override fun onDestroy(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.removeObserver(
                    viewLifecycleOwnerLiveDataObserver
                )
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory?.let { it(thisRef.requireView()) }.also { this.binding = it }
            ?: throw IllegalStateException("viewBinding is not yet initialized")
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        binding = value
    }
}

/**
 * Extension function for fragment
 *
 * syntax:
 * class SecondFragment : Fragment(R.layout.fragment_second) {
    private val binding by viewBindings(FragmentSecondBinding::bind)

    onCreateView and onDestroyView are no more required and can be deleted.
    ViewBindingDelegate will create and destroy binding by itself
 */
fun <T : ViewBinding> Fragment.viewBindings(viewBindingFactory: ((View) -> T)? = null) =
    ViewBindingDelegate(this, viewBindingFactory)

/**
 * Extension function for Activity
 *
 * private val binding by viewBindings(ActivityMainBinding::inflate)
 */
inline fun <T : ViewBinding> AppCompatActivity.viewBindings(
    crossinline bindingInflater: (LayoutInflater) -> T
) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

/**
 * Extension function for RecyclerView ViewHolder
 *
 * override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent.viewBindings(ItemFaqBinding::inflate))
    }
 */
inline fun <T : ViewBinding> ViewGroup.viewBindings(
    crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
    attachToParent: Boolean = false
) =
    bindingInflater.invoke(LayoutInflater.from(this.context), this, attachToParent)
