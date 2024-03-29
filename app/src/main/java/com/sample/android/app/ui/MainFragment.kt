package com.sample.android.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.sample.android.app.BR
import com.sample.android.app.R
import com.sample.android.app.databinding.FragmentMainBinding
import com.sample.android.app.util.Resource
import com.sample.android.app.util.setupActionBar
import com.sample.android.app.viewmodels.MainViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class MainFragment @Inject
constructor() // Required empty public constructor
    : DaggerFragment() {

    @Inject
    lateinit var factory: MainViewModel.Factory

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        _binding = FragmentMainBinding.inflate(inflater, container, false).apply {
            setVariable(BR.vm, viewModel)
            // Set the lifecycleOwner so DataBinding can observe LiveData
            lifecycleOwner = viewLifecycleOwner
        }

        val viewModelAdapter =
            MainAdapter(MainAdapter.OnClickListener { post, imageView ->
                val extras = FragmentNavigatorExtras(
                    imageView to post.id.toString()
                )
                val destination =
                    MainFragmentDirections.actionMainFragmentToDetailFragment(post)
                with(findNavController()) {
                    currentDestination?.getAction(destination.actionId)
                        ?.let { navigate(destination, extras) }
                }

            })

        viewModel.liveData.observe(viewLifecycleOwner, {
            if (it is Resource.Success)
                viewModelAdapter.submitList(it.data)
        })

        with(binding) {
            recyclerView.apply {
                setHasFixedSize(true)
                adapter = viewModelAdapter
                postponeEnterTransition()
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
            }

            (activity as AppCompatActivity).setupActionBar(toolbar) {
                setTitle(R.string.app_name)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}