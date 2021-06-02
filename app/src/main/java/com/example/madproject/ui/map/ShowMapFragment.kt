package com.example.madproject.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.BuildConfig
import com.example.madproject.MainActivity
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.ui.yourtrips.TripListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import org.osmdroid.api.IGeoPoint
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.util.*

@Suppress("DEPRECATION")
class ShowMapFragment : Fragment(R.layout.fragment_show_map) {
    var count = 0
    private lateinit var mMapView: MapView
    private var mScaleBarOverlay: ScaleBarOverlay? = null
    private var mRotationGestureOverlay: RotationGestureOverlay? = null
    private var path = mutableListOf<GeoPoint>()
    private var trip = Trip()
    private val mapModel: MapViewModel by activityViewModels()
    private val tripListViewModel : TripListViewModel by activityViewModels()
    private var arr = ""
    private var dep = ""
    private var interStops = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Note! we are programmatically construction the map view
        //be sure to handle application lifecycle correct (see note in on pause)
        mMapView = MapView(inflater.context)
        mMapView.setDestroyMode(false)
        mMapView.tag = "mapView" // needed for OpenStreetMapViewTest
        mMapView.setOnGenericMotionListener(View.OnGenericMotionListener { _, event ->

            if (0 != event.source and InputDevice.SOURCE_CLASS_POINTER) {
                when (event.action) {
                    MotionEvent.ACTION_SCROLL -> {
                        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) mMapView.controller
                            .zoomOut() else {
                            //this part just centers the map on the current mouse location before the zoom action occurs
                            val iGeoPoint: IGeoPoint = mMapView.projection.fromPixels(
                                event.x
                                    .toInt(), event.y.toInt()
                            )
                            mMapView.controller.animateTo(iGeoPoint)
                            mMapView.controller.zoomIn()
                        }
                        return@OnGenericMotionListener true
                    }
                }
            }
            false
        })

        return mMapView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setting the proper title
        when (mapModel.pathManagement) {
            "selectDeparture" -> (requireActivity() as MainActivity).supportActionBar?.title = "Select Departure"
            "selectArrival" -> (requireActivity() as MainActivity).supportActionBar?.title = "Select Destination"
            "selectIntStops" -> (requireActivity() as MainActivity).supportActionBar?.title = "Select Intermediate Stops"
        }

        if (mapModel.pathManagement == "showRoute") {
            // Here we have to show the route of the trip
            Dispatchers.IO.dispatch(GlobalScope.coroutineContext) {

                if ((trip.departureCoordinates != null) && (trip.arrivalCoordinates != null)) {
                    // Insert the departure coordinate
                    val depGp = GeoPoint(trip.departureCoordinates!!.latitude, trip.departureCoordinates!!.longitude)
                    val depStartMarker = Marker(mMapView)
                    depStartMarker.position = depGp
                    depStartMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    depStartMarker.icon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.segnaposto_black_100, null)
                    mMapView.overlays.add(depStartMarker)

                    for (point in trip.intermediateCoordinates) {
                        val gp = GeoPoint(point.latitude, point.longitude)
                        path.add(gp)
                        val startMarker = Marker(mMapView)
                        startMarker.position = gp
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        startMarker.icon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.segnaposto_black_100, null)

                        mMapView.overlays.add(startMarker)
                    }

                    // Insert the arrival coordinate
                    val arrGp = GeoPoint(trip.departureCoordinates!!.latitude, trip.departureCoordinates!!.longitude)
                    val arrStartMarker = Marker(mMapView)
                    arrStartMarker.position = arrGp
                    arrStartMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    arrStartMarker.icon = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.segnaposto_red_100, null)
                    mMapView.overlays.add(arrStartMarker)

                    // Draw the Route of trip
                    val roadManager: RoadManager =
                        OSRMRoadManager(requireContext(), BuildConfig.APPLICATION_ID)

                    val array = arrayListOf<GeoPoint>()
                    array.addAll(path)
                    val road = roadManager.getRoad(array)
                    val roadOverlay = RoadManager.buildRoadOverlay(road)
                    roadOverlay.outlinePaint.color =
                        ContextCompat.getColor(requireContext(), R.color.red)
                    roadOverlay.outlinePaint.strokeWidth = 15.0F
                    mMapView.overlays.add(roadOverlay)

                    mMapView.invalidate()
                }
            }
        } else {
            // Here we have to select a new position
            mMapView.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(
                    e: MotionEvent,
                    mapView: MapView
                ): Boolean {
                    Dispatchers.IO.dispatch(GlobalScope.coroutineContext) {

                        if(mapModel.pathManagement!="selectIntStops") {
                            mapView.overlays.removeLast()
                            mapView.invalidate()
                        }

                        val projection = mapView.projection
                        val geoPoint = projection.fromPixels(
                            e.x.toInt(),
                            e.y.toInt()
                        )

                        val gp: GeoPoint = geoPoint as GeoPoint

                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val addresses: List<Address> = geocoder.getFromLocation(gp.latitude, gp.longitude, 1)
                        var cityname = ""
                        var state = ""
                        var address1 = ""

                        if (addresses.isNotEmpty()) {
                            cityname = addresses[0].locality ?: ""
                            state = addresses[0].countryCode ?: ""
                            address1 = addresses[0].thoroughfare ?: ""
                        }
                        val final = "$cityname ($state) $address1"
                        Log.d("test", "$cityname ($state) $address1")

                        if(mapModel.pathManagement=="selectDeparture") {
                            dep=final
                        }
                        else if(mapModel.pathManagement=="selectArrival") {
                            arr=final
                        }
                        else {
                            interStops.add(final)
                        }

                        val startMarker = Marker(mapView)
                        startMarker.position = geoPoint
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        if (mapModel.pathManagement=="selectDeparture") startMarker.icon = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.segnaposto_black_100,
                            null
                        )
                        else if(mapModel.pathManagement=="selectArrival") startMarker.icon = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.segnaposto_red_100,
                            null
                        )
                        else startMarker.icon = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.segnaposto_blue_100,
                            null
                        )

                        mapView.overlays.add(startMarker)

                        mapView.invalidate()

                    }
                    return true
                }
            })
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context: Context? = this.activity
        val dm: DisplayMetrics = context?.resources!!.displayMetrics
        //map scale
        mScaleBarOverlay = ScaleBarOverlay(mMapView)
        mScaleBarOverlay!!.setCentred(true)
        mScaleBarOverlay!!.setScaleBarOffset(dm.widthPixels / 2, 10)
        mMapView.overlays.add(mScaleBarOverlay)

        //support for map rotation
        mRotationGestureOverlay = RotationGestureOverlay(mMapView)
        mRotationGestureOverlay!!.isEnabled = true
        mMapView.overlays.add(mRotationGestureOverlay)

        //needed for pinch zooms
        mMapView.setMultiTouchControls(true)

        //scales tiles to the current screen's DPI, helps with readability of labels
        mMapView.isTilesScaledToDpi = true

        //the rest of this is restoring the last map location the user looked at
        val zoomLevel = if (mapModel.pathManagement == "showRoute") 5.5 else 7.0
        mMapView.controller.setZoom(zoomLevel)
        mMapView.setMapOrientation(0.0F, false)

        var latitude = java.lang.Double.valueOf("45.056628")
        var longitude = java.lang.Double.valueOf("7.671299")
        if ((mapModel.pathManagement == "showRoute")) {
            latitude = tripListViewModel.selectedLocal.departureCoordinates?.latitude ?: 45.056628
            longitude = tripListViewModel.selectedLocal.departureCoordinates?.longitude ?: 7.671299
        }

        Log.d("test", "$latitude")

        mMapView.setExpectedCenter(GeoPoint(latitude, longitude))
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (mapModel.pathManagement != "showRoute")
            inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                when (mapModel.pathManagement) {
                    "selectDeparture" -> tripListViewModel.selectedLocal.from = dep
                    "selectArrival" -> tripListViewModel.selectedLocal.to = arr
                    "selectIntStops" -> tripListViewModel.selectedLocal.intermediateStops = interStops.joinToString("\n- ","- ")
                }

                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        //save the current location
        mMapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //this part terminates all of the overlays and background threads for osmdroid
        //only needed when you programmatically create the map
        mMapView.onDetach()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    fun invalidateMapView() {
        mMapView.invalidate()
    }
}