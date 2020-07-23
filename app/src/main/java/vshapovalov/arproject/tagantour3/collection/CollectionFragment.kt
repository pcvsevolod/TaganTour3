package vshapovalov.arproject.tagantour3.collection

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_collection.*
import kotlinx.android.synthetic.main.fragment_collection.view.*
import vshapovalov.arproject.tagantour3.MainActivity
import vshapovalov.arproject.tagantour3.PlaceHolder
import vshapovalov.arproject.tagantour3.R
import java.util.*

class CollectionFragment : Fragment() {
    val TAG = "CollectionFragment"

    private var rv_list: MutableList<CollectionItem>? = null
    private var recyclerView: RecyclerView? = null

    var ph : PlaceHolder? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        col_btn_refresh.hide()
        col_btn_refresh.setOnClickListener {
            refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_collection, container, false)
        recyclerView = view.findViewById<View>(R.id.col_rv) as RecyclerView
        recyclerView!!.setLayoutManager(GridLayoutManager(getActivity(), 2))
        rv_list = ArrayList<CollectionItem>()
        val mAdapter = CollectionRecyclerAdapter(rv_list as ArrayList<CollectionItem>)
        recyclerView!!.setAdapter(mAdapter)
        recyclerView!!.setItemAnimator(DefaultItemAnimator())
        return view
    }

    fun refresh() {
        ph = (activity as MainActivity?)!!.getPH()
        if(ph!!.placeChanges != 0) {
            ph!!.placeChanges = 0
            Log.i(TAG, "Force refresh")
            forceRefresh()
        }
    }

    fun forceRefresh() {
        //ph = (activity as MainActivity?)!!.getPH()
        rv_list!!.clear()
        for(p in ph!!.places) {
            if (ph!!.collected(p)) {
                rv_list!!.add(CollectionItem(p))
                val mAdapter = CollectionRecyclerAdapter(rv_list as ArrayList<CollectionItem>)
                recyclerView!!.setAdapter(mAdapter)
                recyclerView!!.setItemAnimator(DefaultItemAnimator())
            }
        }
    }
}
