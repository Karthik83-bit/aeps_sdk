package com.example.aeps_sdk.unifiedaeps.bankspinner

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aeps_sdk.application.AppController
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankIIN
import com.example.aeps_sdk.unifiedaeps.bankspinner.viewmodel.BankListViewModel
import com.example.aeps_sdk.unifiedaeps.bankspinner.viewmodel.BankListViewModelFactory
import com.example.aeps_sdk.databinding.FragmentBankSpinnerBinding
import com.example.aeps_sdk.utils.NetworkResults
import com.example.aeps_sdk.utils.SdkConstants
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONException

class BankSpinnerFragment : Fragment(), BankNameContract.View {
    private var _binding: FragmentBankSpinnerBinding? = null
    private val binding get() = _binding!!
    private var bankNameModelList: List<BankNameModel> = java.util.ArrayList()
    private var bankNameListAdapter: BankNameListAdapter? = null
    lateinit var bankListViewModel: BankListViewModel
    private val bankNamesArrayList = java.util.ArrayList<BankNameModel>()
    private var banknameView: BankNameContract.View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBankSpinnerBinding.inflate(inflater, container, false)
        val bankListRepo = (requireActivity().application as AppController).bankListRepo
        bankListViewModel = ViewModelProvider(
            requireActivity(),
            BankListViewModelFactory(bankListRepo)
        )[BankListViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        banknameView = this
        val mLayoutManager = LinearLayoutManager(requireActivity())
        binding.bankNameRecyclerView.layoutManager = mLayoutManager
        binding.bankNameRecyclerView.itemAnimator = DefaultItemAnimator()
        loadBankNameList()
        bindObserver()
        onClickListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClickListener() {
        binding.searchView.setOnTouchListener(View.OnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.searchView.right - binding.searchView.compoundDrawables[drawableRight].bounds.width()
                ) {
                    refreshBankList()
                    return@OnTouchListener true
                }
            }
            false
        })
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    if (!TextUtils.isEmpty(p0) && bankNameListAdapter != null) {
                        bankNameListAdapter!!.filter.filter(p0)
                    }
                }
            }

        })
    }

    private fun refreshBankList() {
        val arrayList: List<BankIIN> = bankListViewModel.allBankList
        bankNamesArrayList.clear()
        if (arrayList.isEmpty()) {
            bankListViewModel.getBankList()
        } else {
            bankListViewModel.deleteBankListFromDB()
            binding.bankNameRecyclerView.adapter!!.notifyDataSetChanged()
            bankListViewModel.getBankList()
        }
    }

    private fun loadBankNameList() {
        val arrayList: List<BankIIN> = bankListViewModel.allBankList
        if (arrayList.isEmpty()) {
            bankListViewModel.getBankList()
        } else {
            for (i in arrayList) {
                val bankNameModel = BankNameModel()
                bankNameModel.bankName = i.BANKNAME
                bankNameModel.iin = i.IIN.toString()
                SdkConstants.bankIIN = i.IIN.toString()
                bankNamesArrayList.add(bankNameModel)
                banknameView!!.bankNameListReady(bankNamesArrayList)
                banknameView!!.showBankNames()
                banknameView!!.hideLoader()
            }

        }
    }

    private fun bindObserver() {
        lifecycleScope.launchWhenStarted {
            bankListViewModel.getBankListLiveData.collectLatest {
                when (it) {
                    is NetworkResults.Success -> {
                        try {
                            val arrayList: List<BankIIN> = it.data!!.bankIINs
                            SdkConstants.BANK_NAME = it.data.toString()
                            for (i in arrayList) {
                                val bankNameModel = BankNameModel()
                                bankNameModel.bankName = i.BANKNAME
                                bankNameModel.iin = i.IIN.toString()
                                bankNamesArrayList.add(bankNameModel)
                                SdkConstants.bankIIN = i.IIN.toString()
                                banknameView!!.bankNameListReady(bankNamesArrayList)
                                banknameView!!.showBankNames()
                                banknameView!!.hideLoader()

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            banknameView!!.hideLoader()
                        }

                    }
                    is NetworkResults.Loading -> {
                        banknameView!!.showLoader()
                    }
                    is NetworkResults.Error -> {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG)
                            .show()
                        banknameView!!.hideLoader()
                    }
                }

            }
        }

    }

    override fun bankNameListReady(bankNameModelArrayList: ArrayList<BankNameModel>) {
        if (bankNameModelArrayList.size > 0) {
            bankNameModelList = bankNameModelArrayList
        }
    }

    override fun showBankNames() {
        if (bankNameModelList.isNotEmpty()) {
            bankNameListAdapter =
                BankNameListAdapter(
                    bankNameModelList,
                    object : BankNameListAdapter.RecyclerViewClickListener {
                        override fun recyclerViewListClicked(v: View?, position: Int) {
                            try {
                                val bundle = Bundle()
                                val data = bankNameListAdapter!!.getItem(position)
                                val gson = Gson()
                                val getData = gson.toJson(data)
                                bundle.putString("bankData", getData)
                                setFragmentResult("requestKey", bundle)
                                findNavController().navigateUp()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
            binding.bankNameRecyclerView.adapter = bankNameListAdapter
        }
    }

    override fun showLoader() {
        if (!binding.bankNameShimmerLayoout.isShimmerStarted) {
            binding.bankNameShimmerLayoout.startShimmer()
            binding.bankNameShimmerLayoout.visibility = View.VISIBLE
        }
    }

    override fun hideLoader() {
        if (binding.bankNameShimmerLayoout.isShimmerStarted) {
            binding.bankNameShimmerLayoout.stopShimmer()
        }
        binding.bankNameShimmerLayoout.visibility = View.GONE
    }

    override fun emptyBanks() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
