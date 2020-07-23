package vshapovalov.arproject.tagantour3


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_map.*
import vshapovalov.arproject.tagantour3.data.SavedPlace
import vshapovalov.arproject.tagantour3.data.SavedPlaceDb
import java.time.LocalDateTime
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val TAG = "MapsActivity"
    val GOD_MODE = true

    private lateinit var mMap: GoogleMap

    private lateinit var fbRef : DatabaseReference
    var placesRead = false
    var toursRead = false
    var ph : PlaceHolder? = null
    var collectionRadius : Double = 20.0 //In meters
    private val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var mapNavMenuOpen = false

    private val MAP_NONE = "none"
    private val MAP_ALL = "all"
    private val MAP_TOUR = "tour"
    var mapFilter = MAP_ALL // all, tour, none
    var mapShowingCollected = false

    lateinit var constraintLayout : ConstraintLayout
    var constraintSetClosed: ConstraintSet = ConstraintSet()
    var constraintSetOpen: ConstraintSet = ConstraintSet()

    var followMe = false
    var followHandler = Handler()
    var foundMe:Boolean by Delegates.observable(true) { _, oldValue, newValue ->
        if(newValue && !oldValue){
            findRequestComplete()
        }
    }
    var near = false
    var nearestPlace : Place? = null

    var rotateAnimation : Animation? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        map_view.onCreate(savedInstanceState)

        map_view.onResume()
        map_view.getMapAsync(this)
        ph = PlaceHolder(context!!)

        // When menu is open && menu item names
        map_menu_btn.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.inflate(R.menu.map_menu)
            // Changing 1st menu item
            if(mapFilter != MAP_NONE) {
                popupMenu.menu.getItem(0).title = resources.getString(R.string.map_hide_all)
            }
            else {
                popupMenu. menu.getItem(0).title = resources.getString(R.string.map_show_all)
            }
            // Changing 2nd menu item
            popupMenu.menu.getItem(1).isEnabled = ph!!.tour != null
            // Changing 3rd menu item
            if(mapShowingCollected) {
                popupMenu.menu.getItem(2).title = resources.getString(R.string.map_hide_collected)
            }
            else {
                popupMenu.menu.getItem(2).title = resources.getString(R.string.map_show_collected)
            }
            // Changing 4th menu item
            if(followMe) {
                popupMenu.menu.getItem(3).title = resources.getString(R.string.map_menu_turn_off_folliwing)
            }
            else {
                popupMenu.menu.getItem(3).title = resources.getString(R.string.map_menu_turn_on_folliwing)
            }
            // Changing 5th menu item
            popupMenu.menu.getItem(4).isEnabled = ph!!.tour != null
            // Adding item listener
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId){
                    R.id.map_menu_all -> {
                        mapSwitchAll()
                        true
                    }
                    R.id.map_menu_tour -> {
                        mapShowOnlyTour()
                        true
                    }
                    R.id.map_menu_collected -> {
                        mapSwitchCollected()
                        true
                    }
                    R.id.map_menu_follow_me -> {
                        switchFollow()
                        true
                    }
                    R.id.map_menu_deselect_tour -> {
                        ph!!.deselectTour()
                        updateUI()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        constraintLayout = view!!.findViewById(R.id.map_layout)
        constraintSetClosed.clone(constraintLayout)
        constraintSetOpen.clone(context, R.layout.fragment_map_open)

        // Open nav menu
        map_btn_nav_open.setOnClickListener {
            switchMapNavMenu()
        }
        // Click nav_me btn
        map_btn_nav_me.setOnClickListener {
            findMe()
        }
        // Click nav_next btn
        map_btn_nav_next.setOnClickListener {
            nextPlace()
        }
        // Click nav_prev btn
        map_btn_nav_prev.setOnClickListener {
            prevPlace()
        }
        // Near the place, focus btn
        map_btn_found.hide()
        map_btn_found.setOnClickListener {
            placeFound()
        }
        // Near the place, info btn
        map_btn_info.hide()
        map_btn_info.setOnClickListener {
            // Opening info dialog
            val dialogPlace = DialogPlace(context!!, "real")
            dialogPlace.setActivity(activity as MainActivity)
            dialogPlace.openInfo(nearestPlace!!)
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        fireBaseInit()
        updateUI()
        switchFollow()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        findMe()
    }

    override fun onMarkerClick(m: Marker?): Boolean {
        if (m != null) {
            Log.i(TAG, "Marker clicked")
            if (GOD_MODE) {
                Log.i(TAG, "TP to lat:" + m.position!!.latitude + "; lng:" +  m.position!!.longitude)
                gotLocation(m.position!!.latitude, m.position!!.longitude)
            }
            if(m.title != getString(R.string.me_title)) {
                if(ph!!.currentPlaceNumber != 0) {
                    ph!!.currentPlaceNumber = ph!!.place(m.title)!!.number
                }
            }
        }
        m?.showInfoWindow()
        //Toast.makeText(context, m?.title, Toast.LENGTH_LONG).show()
        return false
    }

    fun updateUI() {
        updateMap()
        updateNavMenu()
        updateFoundBtns()
    }

    fun updateMap() {
        if(mapFilter == MAP_NONE) {
            ph!!.clearMarkers()
            return
        }
        if(mapFilter == MAP_ALL) {
            ph!!.clearMarkers()
            for (p in ph!!.places) {
                //Log.i(TAG, "Putting tour, check place=" + p.name + "; collected = " + p.collected)
                if (!ph!!.collected(p) || mapShowingCollected) {
                    //Log.i(TAG, "Putting all, put place=" + p.name)
                    ph!!.putMarker(mMap, p)
                }
            }
        }
        if(mapFilter == MAP_TOUR) {
            ph!!.clearMarkers()
            for(p in ph!!.placesInTour) {
                //Log.i(TAG, "Putting tour, check place=" + p.name)
                if (!ph!!.collected(p) || mapShowingCollected) {
                    //.i(TAG, "Putting all, put place=" + p.name + "; collected = " + p.collected)
                    ph!!.putMarker(mMap, p)
                }
            }
        }
    }

    fun updateNavMenu() {
        if(ph!!.howManyNext(mapShowingCollected) != 0 && mapFilter != MAP_NONE) {
            //Log.i(TAG, "Next: " + ph!!.howManyNext(mapShowingCollected).toString())
            //Log.i(TAG, "Showing next_btn")
            map_btn_nav_next.show()
        }
        else {
            //Log.i(TAG, "Next: " + ph!!.howManyNext(mapShowingCollected).toString())
            //Log.i(TAG, "Hiding next_btn")
            map_btn_nav_next.hide()
        }
        if(ph!!.howManyPrev(mapShowingCollected) != 0 && mapFilter != MAP_NONE) {
            //Log.i(TAG, "Prev: " + ph!!.howManyNext(mapShowingCollected).toString())
            map_btn_nav_prev.show()
        }
        else {
            //Log.i(TAG, "Prev: " + ph!!.howManyNext(mapShowingCollected).toString())
            map_btn_nav_prev.hide()
        }
    }

    fun updateFoundBtns() {
        if(near) {
            map_btn_found.show()
            map_btn_info.show()
        }
        else {
            map_btn_found.hide()
            map_btn_info.hide()
        }
    }

    // Button when there is place near
    fun placeFound() {
        nearestPlace = ph!!.firstUncollectedPlaceInRadius(collectionRadius)
        if (nearestPlace == null) {
            map_btn_found.hide()
            map_btn_info.hide()
            near = false
        }
        else {
            selectPlace(nearestPlace!!)
            near = true
            map_btn_info.show()
        }
    }

    // Constant locationg on/off
    fun switchFollow() {
        val delay = 1000 //milliseconds
        followHandler.postDelayed(object : Runnable {
            override fun run() {
                if (followMe) {
                    getLastLocation()
                    followHandler.postDelayed(this, delay.toLong())
                }
            }
        }, delay.toLong())
        followMe = !followMe
    }

    // For foundMe listener
    fun findRequestComplete() {
        ph!!.focus(mMap, ph!!.meLatLng)
    }

    fun findMe() {
        foundMe = false
        getLastLocation()
    }

    //Just opening/closing nav menu + found&info buttons
    fun switchMapNavMenu() {
        TransitionManager.beginDelayedTransition(constraintLayout)
        if(mapNavMenuOpen) {
            //map_btn_nav_open.setImageResource(R.drawable.ic_open_with_black_24dp)
            constraintSetClosed.applyTo(constraintLayout)
            //rotateIcon()
            updateUI()
        }
        else {
            //map_btn_nav_open.setImageResource(R.drawable.ic_close_black_24dp)
            constraintSetOpen.applyTo(constraintLayout)
            //rotateIcon()
            updateUI()
        }
        mapNavMenuOpen = !mapNavMenuOpen
    }

    fun mapSwitchAll() {
        Log.i(TAG, "Filter: Switching all")
        if (mapFilter == MAP_NONE) {
            mapFilter = MAP_ALL
            updateUI()
        }
        else {
            mapFilter = MAP_NONE
            updateUI()
        }
    }

    // Can only be used if tour is selected
    fun mapShowOnlyTour() {
        Log.i(TAG, "Filter: Only tour")
        mapFilter = MAP_TOUR
        updateUI()
    }

    fun mapSwitchCollected() {
        Log.i(TAG, "Filter: Switching collected")
        mapShowingCollected = !mapShowingCollected
        updateUI()
    }

    fun nextPlace() {
        if (!checkNav())
            return
        val nextPlace = ph!!.moveToNextPlace(mapShowingCollected)
        if(nextPlace != null) {
            updateUI()
            selectPlace(nextPlace)
        }
    }

    fun prevPlace() {
        if (!checkNav())
            return
        val prevPlace = ph!!.moveToPrevPlace(mapShowingCollected)
        if(prevPlace != null) {
            updateUI()
            selectPlace(prevPlace)
        }
    }

    // Checking if tour is finished, if tour is selected, if filter is active
    fun checkNav() : Boolean {
        if(ph!!.currentPlaceNumber == 0) {
            Toast.makeText(context, "Tour is finished!", Toast.LENGTH_LONG).show()
            return false
        }
        if(ph!!.tour == null) {
            Toast.makeText(context, "Tour is not selected!", Toast.LENGTH_LONG).show()
            return false
        }
        if(mapFilter == "none") {
            Toast.makeText(context, "Map filter is active!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    //Selecting Marker on map, based on place
    private fun selectPlace(p : Place) {
        val m = ph!!.marker(p)
        if(m != null) {
            Log.i(TAG, "Selecting: Marker is active")
            ph!!.focus(mMap, LatLng(p.lat, p.lng))
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.lat, p.lng), 15.0f), 200, null)
            m.showInfoWindow()
        }
        else {
            Log.i(TAG, "Selecting: New Marker")
            Toast.makeText(context, "Can't select place, Marker is not on the map", Toast.LENGTH_LONG).show()
            //mapFilter = "some"
            //m = ph!!.putMarker(mMap, p)
        }
    }

    // Checking if there is a place near me
    fun checkPlacesNear() {
        nearestPlace = ph!!.firstUncollectedPlaceInRadius(collectionRadius)
        if (nearestPlace == null) {
            near = false
            map_btn_found.hide()
            map_btn_info.hide()
        }
        else {
            near = true
            map_btn_found.show()
            map_btn_info.show()
        }
    }

    fun gotLocation(lat: Double, lng: Double) {
        ph!!.saveMe(lat, lng)
        ph!!.putMeOnMap(mMap)
        foundMe = true
        checkPlacesNear()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(activity!!) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Log.i(TAG, "Got Last Location")
                        gotLocation(location.latitude, location.longitude)
                    }
                }
            } else {
                Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            ph!!.saveMe(mLastLocation.latitude, mLastLocation.longitude)
            ph!!.putMeOnMap(mMap)
        }
    }

    private fun isLocationEnabled(): Boolean {
        //TODO Changed
        //var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationManager: LocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (context == null) {
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        if (activity == null) {
            return
        }
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun fireBaseInit() {
        //Log.i(TAG, "Start")
        fbRef = FirebaseDatabase.getInstance().getReference("places")
        fbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(dbError: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Log.i(TAG, "Canceled")
            }

            override fun onDataChange(dbSnapshot: DataSnapshot) {
                if (!placesRead) {
                    if (dbSnapshot.exists()) {
                        for (p in dbSnapshot.children) {
                            val place = p.getValue(Place::class.java)
                            ph!!.addPlace(place!!)
                        }
                        //fbRef.removeEventListener(this)
                    }
                    Toast.makeText(context, "Places Ready!", Toast.LENGTH_LONG).show()
                    //map_btn_here.setBackgroundColor(Color.GREEN)
                    fbRef.removeEventListener(this)
                    placesRead = true
                    initRoomDB()
                    updateUI()
                }
            }
        })

        fbRef = FirebaseDatabase.getInstance().getReference("tours")
        fbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(dbError: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                Log.i(TAG, "Canceled")
            }

            override fun onDataChange(dbSnapshot: DataSnapshot) {
                if (!toursRead) {
                    if (dbSnapshot.exists()) {
                        for (t in dbSnapshot.children) {
                            val tour = t.getValue(Tour::class.java)
                            ph!!.addTour(tour!!)
                            //Log.i(TAG, "db added " + tour.name)
                        }
                    }
                    Toast.makeText(context, "Tours Ready!", Toast.LENGTH_LONG).show()
                    fbRef.removeEventListener(this)
                    toursRead = true
                    updateUI()
                }
            }
        })
    }

    // Getting roomDB places and crossing in ph.places
    fun initRoomDB() {
        val roomDb = SavedPlaceDb(context!!)
        val savedPlaces = roomDb.daoSavedPlace().getAllSavedPlaces()
        for(sp in savedPlaces) {
            for (p in ph!!.places) {
                if (sp.name == p.name) {
                    p.collected = true
                }
            }
        }
    }

    // Inserting place in roomDB && changing PH
    fun savePlace(p : Place) {
        ph!!.collectPlace(p)
        val roomDb = SavedPlaceDb(context!!)
        val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        roomDb.daoSavedPlace().insertSavedPlace(SavedPlace(name = p.name, date = currentDateTime.toString()))
    }

    // Select tour from outside
    fun selectTour(tour: Tour) {
        Log.i(TAG, "Selecting tour $tour")
        ph!!.deselectTour()
        val staringPlace = ph!!.selectTour(tour, mapShowingCollected)
        if (staringPlace == null) {
            Toast.makeText(context, "Tour is complete", Toast.LENGTH_LONG).show()
            return
        }
        mapAddTourMarkers()
        selectPlace(staringPlace)
    }

    fun mapAddTourMarkers() {
        if(mapFilter == MAP_NONE) {
            mapFilter = MAP_TOUR
        }
        updateUI()
    }

    fun getReward(p: Place) {
        savePlace(p)
        updateMap()
    }
}
