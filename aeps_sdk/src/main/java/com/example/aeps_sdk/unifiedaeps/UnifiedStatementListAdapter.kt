package com.example.aeps_sdk.unifiedaeps

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aeps_sdk.R
import com.example.aeps_sdk.databinding.StatementListItemsBinding
import com.example.aeps_sdk.unifiedaeps.models.response.MiniStatement
import kotlin.reflect.KFunction0

class UnifiedStatementListAdapter(
    private val miniStatementsList: List<MiniStatement>,
    private val context: Context,
    private val hideLoader: KFunction0<Unit>

) : RecyclerView.Adapter<UnifiedStatementListAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: StatementListItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            StatementListItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    private var transactionType: TransactionType? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (miniStatementsList.isNotEmpty()) {
                with(holder) {
                    miniStatementsList[position].apply {
                        if (Date == null) {
                            binding.dateTxt.text = ""
                        } else {
                            binding.dateTxt.text = Date
                        }
                        if (Type == null) {
                            binding.remarkTxt.text = ""
                        } else {
                            binding.remarkTxt.text = Type
                        }
                        if (DebitCredit == null) {
                            binding.crdrTxt.text = ""
                        } else {
                            transactionType = TransactionType(DebitOrCredit.valueOf(DebitCredit))
                            binding.crdrTxt.text = transactionType!!.transactionType()
                        }
                        if (binding.crdrTxt.text.toString().equals("Cr ", ignoreCase = true)) {
                            binding.crdrTxt.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.green
                                )
                            )
                        } else {
                            binding.crdrTxt.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.red
                                )
                            )
                        }
                        val amount: Double? = Amount
                        binding.amountTxt.text = amount.toString()
                        hideLoader()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            hideLoader()
        }

    }

    override fun getItemCount(): Int {
        var size = 0
        try {
            if (miniStatementsList.isNotEmpty()) {
                size = miniStatementsList.size
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }
}