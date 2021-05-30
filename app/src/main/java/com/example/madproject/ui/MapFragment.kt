package com.example.madproject.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.madproject.BuildConfig
import com.example.madproject.R
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import java.util.logging.Logger


class MapFragment : Fragment() {
    private lateinit var mapView : MapView

    private lateinit var locationOverlay: MyLocationNewOverlay

    private var scope: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        mapView = MapView(inflater.context)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.maxZoomLevel = 19.0

        scope = arguments?.getString("scope")

       /* mapView.overlays.add(object : Overlay() {

            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                mapView ?: return false
                e ?: return false
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                Logger.info("${geoPoint.latitude} ${geoPoint.longitude}")

                val location = GeocoderNominatim(Locale.getDefault(), BuildConfig.APPLICATION_ID)
                    .getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                    .firstOrNull()

                Logger.info(location, "Location selected")
                Logger.info(location?.maxAddressLineIndex, "Location selected max address line index")

                val address =
                    when {
                        location == null -> null
                        location.getAddressLine(0) != null -> location.getAddressLine(0)
                        else -> location.locality
                    }

                AlertDialog.Builder(requireContext())
                    .setTitle("Sicuro?")
                    .setMessage("Confermi questa posizione?\n\n${address}")
                    .setPositiveButton("Conferma") {_, _ ->
                        Logger.info(geoPoint.latitude, "latitude")
                        Logger.info(geoPoint.longitude, "longitude")
                        Logger.info(address, "location")
                        val bundle = bundleOf(
                            "scope" to scope,
                            "latitude" to geoPoint.latitude,
                            "longitude" to geoPoint.longitude,
                            "location" to address
                        )
                        this@MapViewFragment.findNavController().navigate(R.id.action_map_view_to_nav_trip_edit, bundle)
                    }
                    .setNegativeButton("Riprova") {_, _ -> }
                    .create()
                    .show()

                return true
            }
        })*/
        return mapView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = requireContext()

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        mapView.setMultiTouchControls(true)
        mapView.isTilesScaledToDpi = true

        mapView.controller.animateTo(GeoPoint(45.0809822,7.6643817,297.0))
        mapView.controller.zoomTo(12.0)

//        fixme osmdroid bug: can't animate inside runOnFirstFix becaus not on UI thread
//        locationOverlay.runOnFirstFix {
//            Logger.info(locationOverlay.myLocation, "my location")
//            mapView.controller.animateTo(locationOverlay.myLocation)
//
//        }

    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }

}

