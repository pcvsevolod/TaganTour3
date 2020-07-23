package vshapovalov.arproject.tagantour3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_notifications.*


/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment() {
    lateinit var navigation : BottomNavigationView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var frag = (activity as MainActivity?)!!.fragmentMap
        var fm = (activity as MainActivity?)!!.fm
        //var frag = MapFragment()

        //navigation = view!!.findViewById(R.id.navigation) as BottomNavigationView
        navigation = (activity as MainActivity?)!!.navigation


        not_btn_me.setOnClickListener{
            //navigation.selectedItemId = R.id.navigation_map
            //(activity as MainActivity?)!!.doIt()

            //frag.meButtonTest()


            //var active = (activity as MainActivity?)!!.active
            //fm.beginTransaction().hide(active).show(frag).commit()
            //(activity as MainActivity?)!!.active = (activity as MainActivity?)!!.fragmentMap
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
}