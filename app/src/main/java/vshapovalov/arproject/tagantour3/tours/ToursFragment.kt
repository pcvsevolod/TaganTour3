package vshapovalov.arproject.tagantour3.tours

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_tours.*
import kotlinx.android.synthetic.main.fragment_tours.view.*
import vshapovalov.arproject.tagantour3.MainActivity
import vshapovalov.arproject.tagantour3.PlaceHolder
import vshapovalov.arproject.tagantour3.R

class ToursFragment : Fragment() {
    var TAG = "ToursFragment"

    private var rv_list: MutableList<ToursItem>? = null
    private var recyclerView: RecyclerView? = null

    var ph : PlaceHolder? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        tours_btn_refresh.hide()
        tours_btn_refresh.setOnClickListener {
            refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_tours, container, false)
        recyclerView = view.findViewById<View>(R.id.tours_rv) as RecyclerView
        recyclerView!!.setLayoutManager(LinearLayoutManager(getActivity()))
        rv_list = ArrayList<ToursItem>()
        //rv_list!!.add(CollectionItem("Home", R.drawable.ic_home_black_24dp))
        val mAdapter = ToursRecyclerAdapter(rv_list as ArrayList<ToursItem>, activity as MainActivity)
        recyclerView!!.setAdapter(mAdapter)
        recyclerView!!.setItemAnimator(DefaultItemAnimator())
        return view
    }

    fun refresh() {
        ph = (activity as MainActivity?)!!.getPH()
        if (ph!!.tourChanges != 0) {
            ph!!.tourChanges = 0
            Log.i(TAG, "Force refresh")
            forceRefresh()
        }
    }

    fun forceRefresh() {
        //ph = (activity as MainActivity?)!!.getPH()
        //Toast.makeText(context, ph!!.places.size.toString(), Toast.LENGTH_LONG).show()
        rv_list!!.clear()
        for(t in ph!!.tours) {
            rv_list!!.add(ToursItem(t))
            val mAdapter = ToursRecyclerAdapter(rv_list as ArrayList<ToursItem>, activity as MainActivity)
            recyclerView!!.setAdapter(mAdapter)
            recyclerView!!.setItemAnimator(DefaultItemAnimator())
            //}
        }
    }
}