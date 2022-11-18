package com.example.wapapp2.view.friends


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.R
import com.example.wapapp2.commons.classes.ListAdapterDataObserver
import com.example.wapapp2.commons.interfaces.ListOnClickListener
import com.example.wapapp2.databinding.FragmentFriendsBinding
import com.example.wapapp2.model.FriendDTO
import com.example.wapapp2.view.friends.adapter.MyFriendsAdapter
import com.example.wapapp2.view.login.Profiles
import com.example.wapapp2.view.myprofile.MyprofileFragment
import com.example.wapapp2.view.main.MainHostFragment
import com.example.wapapp2.viewmodel.FriendsViewModel
import com.example.wapapp2.viewmodel.MyAccountViewModel

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val friendsViewModel by activityViewModels<FriendsViewModel>()
    private val myAccountViewModel by activityViewModels<MyAccountViewModel>()

    companion object {
        const val TAG = "FriendsFragment"
    }

    private val friendOnClickListener = ListOnClickListener<FriendDTO> { item, position ->
        val fragment = FriendProfileFragment.newInstance(item)
        fragment.show(childFragmentManager, FriendProfileFragment.TAG)
    }

    private lateinit var myFriendsAdapter: MyFriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myFriendsAdapter = MyFriendsAdapter(friendOnClickListener, friendsViewModel.getMyFriendsOptions())

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        binding.loadingView.setContentView(binding.myFriendsList)

        binding.addFriendBtn.setOnClickListener {
            val fragment = AddMyFriendFragment()
            val fragmentManager = requireParentFragment().parentFragmentManager
            fragmentManager
                    .beginTransaction()
                    .hide(fragmentManager.findFragmentByTag(MainHostFragment.TAG) as Fragment)
                    .add(R.id.fragment_container_view, fragment, AddMyFriendFragment.TAG)
                    .addToBackStack(AddMyFriendFragment.TAG)
                    .commit()
        }

        val dataObserver = ListAdapterDataObserver(binding.myFriendsList, binding.myFriendsList.layoutManager as
                LinearLayoutManager, myFriendsAdapter)
        dataObserver.registerLoadingView(binding.loadingView, getString(R.string.empty_my_friends))
        myFriendsAdapter.registerAdapterDataObserver(dataObserver)

        binding.myFriendsList.adapter = myFriendsAdapter
        myFriendsAdapter.startListening()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMyProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        myFriendsAdapter.stopListening()
    }

    private fun setMyProfile() {
        binding.rvMyprofile.setOnClickListener {
            val fragment = MyprofileFragment()
            val fragmentManager = requireParentFragment().parentFragmentManager
            fragmentManager
                    .beginTransaction()
                    .hide(fragmentManager.findFragmentByTag(MainHostFragment.TAG) as Fragment)
                    .add(R.id.fragment_container_view, fragment, MyprofileFragment.TAG)
                    .addToBackStack(MyprofileFragment.TAG)
                    .commit()
        }

        myAccountViewModel.myProfileData.observe(viewLifecycleOwner) {
            binding.myProfileName.text = it.name
            binding.myAccountId.text = it.email
        }

    }
}