package vshapovalov.arproject.tagantour3

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import vshapovalov.arproject.tagantour3.collection.CollectionFragment
import vshapovalov.arproject.tagantour3.tours.ToursFragment


class MainActivity : AppCompatActivity() {
    val fragmentHome: Fragment = CollectionFragment()
    val fragmentMap: Fragment = MapFragment()
    //val fragmentNotifications: Fragment = NotificationsFragment()
    val fragmentTours: Fragment = ToursFragment()
    val fm: FragmentManager = supportFragmentManager
    lateinit var navigation : BottomNavigationView
    var active: Fragment = fragmentHome
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation = findViewById(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fm.beginTransaction().add(R.id.main_container, fragmentTours, "3").hide(fragmentTours).commit()
        fm.beginTransaction().add(R.id.main_container, fragmentMap, "2").hide(fragmentMap).commit()
        fm.beginTransaction().add(R.id.main_container, fragmentHome, "1").commit()
        navigation.selectedItemId = R.id.navigation_map
    }

    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_collection -> {
                        fm.beginTransaction().hide(active).show(fragmentHome).commit()
                        val f = fm.fragments[2] as CollectionFragment
                        f.refresh()
                        active = fragmentHome
                        return true
                    }
                    R.id.navigation_map -> {
                        fm.beginTransaction().hide(active).show(fragmentMap).commit()
                        active = fragmentMap
                        return true
                    }
                    R.id.navigation_tours -> {
                        fm.beginTransaction().hide(active).show(fragmentTours).commit()
                        val f = fm.fragments[0] as ToursFragment
                        f.refresh()
                        active = fragmentTours
                        return true
                    }
                }
                return false
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun getPH() : PlaceHolder {
        val f = fm.fragments[1] as MapFragment
        return f.ph!!
    }

    fun startTour(t: Tour) {
        navigation.selectedItemId = R.id.navigation_map
        val f = fm.fragments[1] as MapFragment
        f.selectTour(t)
    }

    fun getReward(p: Place) {
        navigation.selectedItemId = R.id.navigation_map
        val f = fm.fragments[1] as MapFragment
        f.getReward(p)
    }
}
