package vshapovalov.arproject.tagantour3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*


class PlaceHolder(var context: Context) {
    private val TAG = "PlaceHolder"
    var placeChanges = 0
    var tourChanges = 0

    val icon_size = 50

    var meMarker : Marker? = null
    var meLatLng = LatLng(0.0, 0.0)

    var places : MutableList<Place> = mutableListOf()
    var placesInTour : MutableList<Place> = mutableListOf()
    var currentPlaceNumber : Int = 0 // 0 = tour finished, 1-n - not finished

    var markers : MutableList<Marker> = mutableListOf()

    var tours : MutableList<Tour> = mutableListOf()
    var tour: Tour? = null

    val TOUR_CIRCULAR = "circular"
    val TOUR_LINEAR = "linear"
    val TOUR_PROGRESSION = "progression"

    val INFINITY = -1

    // Only for init
    fun addPlace(p : Place) {
        placeChanges++
        places.add(p)
    }

    // Only for init
    fun addTour(tour : Tour) {
        tourChanges++
        tours.add(tour)
    }

    // Remove markers from map
    fun clearMarkers() {
        for (m in markers)
            m.remove()
        markers.clear()
    }

    // Return unsorted list of places in tour
    fun placesInTour(tour : String) : MutableList<Place> {
        val found = mutableListOf<Place>()
        for(p in places) {
            if(p.tours.contains(tour)) {
                found.add(p)
            }
        }
        return found
    }

    // Return marker based on place
    fun marker(p : Place) : Marker? {
        for(m in markers) {
            if(m.title == p.name) {
                return m
            }
        }
        return null
    }

    // Return place based on name
    fun place(name: String) : Place? {
        for(p in places) {
            if(p.name == name)
                return p
        }
        return null
    }

    // Return list of places in distance of me
    fun placesInDistance(radius : Double) : MutableList<Place> {
        val nearPlaces = mutableListOf<Place>()
        for(p in places) {
            if(distanceFromMeToPlace(p) <= radius) {
                //Toast.makeText(context, "Found: " + p.name, Toast.LENGTH_LONG).show()
                Log.i(TAG, p.name + distanceFromMeToPlace(p).toString())
                nearPlaces.add(p)
            }
        }
        return nearPlaces
    }

    // Return closest uncollected place
    fun firstUncollectedPlaceInRadius(radius: Double) : Place? {
        var closestPlace: Place? = null
        var dist = 1000000000.0
        for (p in placesInDistance(radius)) {
            val newDist = distanceFromMeToPlace(p)
            if (newDist < dist) {
                dist = newDist
                closestPlace = p
            }
        }
        return closestPlace
    }

    // Save my location
    fun saveMe(lat : Double, lng : Double) {
        meLatLng = LatLng(lat, lng)
    }

    // Put my marker on map
    fun putMeOnMap(map: GoogleMap) {
        meMarker?.remove()
        meMarker = map.addMarker(MarkerOptions()
            .position(meLatLng)
            .title(context.getString(R.string.me_title))
            .icon(bitmapDescriptorFromVector(context, R.drawable.ic_my_location_new))
            .anchor(0.5f, 0.5f)
        )
    }

    // Move map to location
    fun focus(map: GoogleMap,latLng: LatLng, zoom: Float, time: Int) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom), time, null)
    }

    // Move map to location
    fun focus(map: GoogleMap,latLng: LatLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f), 200, null);
    }

    // Selecting tour (from TourFragment)
    fun selectTour(t : Tour, showCollected: Boolean) : Place? {
        //Log.i(TAG, "Selecting Tour: tour type = " + t.type)
        tour = t
        currentPlaceNumber = 0
        placesInTour = placesInTour(tour!!.uniqueId!!)
        placesInTour.sortBy { it.number }
        if (t.type == TOUR_CIRCULAR) {
            //TODO nearest place
            //Log.i(TAG, "Selecting Tour: Doing circular")
            var dist = 1000000 .toDouble()
            var nearestPlace : Place? = null
            for (p in placesInTour) {
                var newDist = distanceFromMeToPlace(p)
                //Log.i(TAG, "Distance cycle: place = " + p.name + "; dist = $newDist")
                if (newDist < dist && (!collected(p) || showCollected)) {
                    nearestPlace = p
                    dist = newDist
                }
            }
            if (nearestPlace != null) {
                currentPlaceNumber = nearestPlace.number!!
                return nearestPlace
            }
        }
        if (t.type == TOUR_LINEAR) {
            //TODO nearest from size 0->n || 0<-n
            //Log.i(TAG, "Selecting Tour: Doing linear")
            var firstP : Place? = null
            loop@ for(p in placesInTour) {
                //Log.i(TAG, "Selecting Tour: Checking $p")
                if (!collected(p) || showCollected) {
                    //Log.i(TAG, "Selecting Tour: First $p")
                    firstP = p
                    break@loop
                }
            }

            var lastP : Place? = null
            loop@ for(p in placesInTour.reversed()) {
                //Log.i(TAG, "Selecting Tour: Checking $p")
                if (!collected(p) || showCollected) {
                    //Log.i(TAG, "Selecting Tour: Last $p")
                    lastP = p
                    break@loop
                }
            }

            if (firstP == null) {
                //Log.i(TAG, "Selecting Tour: Found nothing")
                return null
            }
            if (firstP == lastP) {
                //Log.i(TAG, "Selecting Tour: Found one $firstP")
                currentPlaceNumber = firstP.number!!
                return firstP
            }
            val distF = distanceFromMeToPlace(firstP)
            val distL = distanceFromMeToPlace(lastP!!)
            return if (distF < distL) {
                //Log.i(TAG, "Selecting Tour: Found first $firstP")
                currentPlaceNumber = firstP.number!!
                firstP
            } else {
                //Log.i(TAG, "Selecting Tour: Found last $lastP")
                currentPlaceNumber = lastP.number!!
                lastP
            }
        }
        if (t.type == TOUR_PROGRESSION) {
            //Log.i(TAG, "Selecting Tour: Doing progression")
            //TODO first uncollected
            for (p in placesInTour) {
                if (!collected(p) || showCollected) {
                    currentPlaceNumber = p.number
                    return p
                }
            }
        }
        return null
    }

    // Make no tour selected
    fun deselectTour() {
        tour = null
        currentPlaceNumber = 0
        placesInTour.clear()
    }

    // Put marker on map
    fun putMarker(map: GoogleMap, p: Place): Marker {
        if(!collected(p)) {
            val m = map.addMarker(
                MarkerOptions()
                    .position(LatLng(p.lat, p.lng))
                    .title(p.name)
                    .icon(bitmapDescriptorFromVector(context, R.drawable.ic_place_new))
            )
            markers.add(m)
            return m
        }
        else {
            val m = map.addMarker(
                MarkerOptions()
                    .position(LatLng(p.lat, p.lng))
                    .title(p.name)
                    .icon(bitmapDescriptorFromVector(context, R.drawable.ic_place_gray_new))
            )
            markers.add(m)
            return m
        }
    }

    // Put marker on map wih mode
    fun putMarker(map: GoogleMap, p: Place, mode: String): Marker {
        //TODO(icons for modes)
        val m = map.addMarker(MarkerOptions()
            .position(LatLng(p.lat!!, p.lng!!))
            .title(p.name)
        )
        markers.add(m)
        return m
    }

    // Get distance between me and place
    private fun distanceFromMeToPlace(p: Place) : Double {
        val loc1 = Location("")
        loc1.latitude = meLatLng.latitude
        loc1.longitude = meLatLng.longitude
        val loc2 = Location("")
        loc2.latitude = p.lat!!
        loc2.longitude = p.lng!!

        return loc1.distanceTo(loc2).toDouble()
    }

    // Check if place is collected
    fun collected(p : Place) : Boolean {
        return p.collected!!
    }

    // Collect place
    fun collectPlace(p: Place) {
        placeChanges += 1
        for(np in places) {
            if (np.name == p.name) {
                p.collected = true
            }
        }
    }

    //How many places in tour left next
    fun howManyNext(showCollected: Boolean) : Int {
        //Log.i(TAG, "Current     = $currentPlaceNumber")
        //Log.i(TAG, "Checking how many next")
        //Log.i(TAG, "Current place = $currentPlaceNumber")
        if (currentPlaceNumber == 0) {
            //Log.i(TAG, "current place = 0")
            return 0
        }
        if (tour!!.type == TOUR_CIRCULAR) {
            //Log.i(TAG, "how many in circular")
            var count = 0
            for(p in placesInTour) {
                //Log.i(TAG, "p in tour: " + p.name)
                if (!collected(p) || showCollected) {
                    //Log.i(TAG, "p is not collected or showing collected")
                    count++
                }
            }
            if (count == 1) {
                //Log.i(TAG, "Circular only one place left")
                return 0
            }
            if (count == 0) {
                //Log.i(TAG, "Circular no places left")
                return 0
            }
            //Log.i(TAG, "Circular at least 2 places left")
            return INFINITY
        }
        // linear and progression in this case the same
        if (tour!!.type == TOUR_LINEAR || tour!!.type == TOUR_PROGRESSION) {
            //Log.i(TAG, "Start linear")
            var startCounting = false
            var count = 0
            for(p in placesInTour) {
                //Log.i(TAG, "Woring on $p")
                if ((!collected(p) || showCollected) && startCounting) {
                    //Log.i(TAG, "Count $p")
                    count++
                }
                if(p.number == currentPlaceNumber) {
                    //Log.i(TAG, "Found start $p")
                    startCounting = true
                }
            }
            return count
        }
        return 0
    }

    fun howManyPrev(showCollected: Boolean) : Int {
        if (currentPlaceNumber == 0) {
            return 0
        }
        if (tour!!.type == TOUR_CIRCULAR) {
            var count = 0
            for(p in placesInTour) {
                if (!collected(p) || showCollected) {
                    count++
                }
            }
            if (count == 1) {
                return 0
            }
            if (count == 0) {
                return 0
            }
            return INFINITY
        }
        // linear and progression in this case the same
        if (tour!!.type == TOUR_LINEAR || tour!!.type == TOUR_PROGRESSION) {
            var startCounting = false
            var count = 0
            for(p in placesInTour.reversed()) {
                if ((!collected(p) || showCollected) && startCounting) {
                    count++
                }
                if(p.number == currentPlaceNumber) {
                    startCounting = true
                }
            }
            return count
        }
        return 0
    }

    fun moveToNextPlace(showCollected: Boolean) : Place? {
        if (currentPlaceNumber == 0) {
            Toast.makeText(context, "How did you get here? Tour is finished", Toast.LENGTH_LONG).show()
            return null
        }
        if (howManyNext(showCollected) == 0) {
            return null
        }

        var nextNumber = (currentPlaceNumber % placesInTour.size) + 1
        var nextPlace = placesInTour.find{it.number == nextNumber}
        while(nextNumber != currentPlaceNumber) {
            nextPlace = placesInTour.find{it.number == nextNumber}
            if (nextPlace == null) {
                Toast.makeText(context, "How did you get here? Ask admins to fix DB", Toast.LENGTH_LONG).show()
                return null
            }
            if(!collected(nextPlace) || showCollected) {
                currentPlaceNumber = nextNumber
                return nextPlace
            }
            nextNumber = (nextNumber % placesInTour.size) + 1
        }
        return nextPlace
    }

    fun moveToPrevPlace(showCollected: Boolean) : Place? {
        if (currentPlaceNumber == 0) {
            Toast.makeText(context, "How did you get here? Tour is finished", Toast.LENGTH_LONG).show()
            return null
        }
        if (howManyPrev(showCollected) == 0) {
            return null
        }

        var prevNumber = currentPlaceNumber - 1
        if (prevNumber == 0) {
            prevNumber = placesInTour.size
        }

        var prevPlace = placesInTour.find{it.number == currentPlaceNumber}
        while(prevNumber != currentPlaceNumber) {
            prevPlace = placesInTour.find{it.number == prevNumber}
            if (prevPlace == null) {
                Log.i(TAG, "$prevNumber number is null")
                Toast.makeText(context, "How did you get here? Ask admins to fix DB", Toast.LENGTH_LONG).show()
                return null
            }
            if(!collected(prevPlace) || showCollected) {
                currentPlaceNumber = prevNumber
                return prevPlace
            }

            prevNumber -= 1
            if (prevNumber == 0) {
                prevNumber = placesInTour.size
            }
        }
        return prevPlace
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}