package com.example.aeps_sdk.unifiedaeps.bankspinner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.aeps_sdk.R
import com.example.aeps_sdk.utils.SdkConstants
import java.util.*

class BankNameListAdapter(
    var bankNameModelList: List<BankNameModel>,
    recyclerViewClickListener: RecyclerViewClickListener
) :
    RecyclerView.Adapter<BankNameListAdapter.BankViewHolder>(), Filterable {
    private var lastSelectedPosition = -1
    var filterList: List<BankNameModel>
    var recyclerViewClickListener: RecyclerViewClickListener

    interface RecyclerViewClickListener {
        fun recyclerViewListClicked(v: View?, position: Int)
    }

    inner class BankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var bankName: RadioButton

        init {
            bankName = view.findViewById<View>(R.id.bankName) as RadioButton
            bankName.setOnClickListener { v ->
                lastSelectedPosition = adapterPosition
                notifyDataSetChanged()
                recyclerViewClickListener.recyclerViewListClicked(v, lastSelectedPosition)
            }
        }
    }

    fun getItem(position: Int): BankNameModel {
        return filterList[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        var layout = R.layout.bank_list_item
        if (SdkConstants.bankItem !== 0) {
            layout = SdkConstants.bankItem
        }
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return BankViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bankNameModel = filterList[position]
        holder.bankName.text = bankNameModel.bankName
        holder.bankName.isChecked = lastSelectedPosition == position
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterList = if (charString.isEmpty()) {
                    bankNameModelList
                } else {
                    val filteredList: MutableList<BankNameModel> = ArrayList()
                    for (row in bankNameModelList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.bankName!!.toLowerCase()
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (filterResults.values != null) {
                    filterList = filterResults.values as ArrayList<BankNameModel>
                    notifyDataSetChanged()
                }
            }
        }
    }

    init {
        filterList = bankNameModelList
        this.recyclerViewClickListener = recyclerViewClickListener
    }
}
