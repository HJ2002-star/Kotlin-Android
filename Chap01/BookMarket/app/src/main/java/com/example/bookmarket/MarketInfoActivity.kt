package com.example.bookmarket

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MarketInfoActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_market_info)

        val root = findViewById<View>(R.id.marketInfoRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Toolbar>(R.id.marketInfoToolbar).apply {
            title = "마켓정보"
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            navigationIcon?.setTint(Color.WHITE)
            navigationContentDescription = "이전 화면"
            setNavigationOnClickListener { finish() }
        }

        val status = findViewById<TextView>(R.id.marketMapStatus)
        val metadata = packageManager.getApplicationInfo(
            packageName, PackageManager.GET_META_DATA
        ).metaData
        val apiKey = metadata?.getString("com.google.android.geo.API_KEY").orEmpty()
        if (apiKey.isBlank() || apiKey == "DEFAULT_API_KEY") {
            status.text = "지도를 표시하려면 지도 서비스 설정이 필요합니다"
            return
        }

        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            status.text = "Google Play 서비스를 확인해 주세요"
            if (availability.isUserResolvableError(result)) {
                availability.getErrorDialog(this, result, 13)?.show()
            }
            return
        }

        status.visibility = View.GONE
        val mapFragment = supportFragmentManager.findFragmentByTag("marketMap")
            as? SupportMapFragment ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction()
                    .add(R.id.marketMapContainer, it, "marketMap")
                    .commitNow()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val market = LatLng(37.5591022, 126.9278237)
        val source = BitmapFactory.decodeResource(resources, R.drawable.hanbit_location)
        val size = (40 * resources.displayMetrics.density).toInt()
        val icon = Bitmap.createScaledBitmap(source, size, size, true)

        googleMap.addMarker(
            MarkerOptions()
                .position(market)
                .title("한빛아카데미")
                .snippet("서울 서대문구 연희로2길 62")
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(market, 15f))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                return layoutInflater.inflate(R.layout.market_info_marker, null).apply {
                    findViewById<TextView>(R.id.marketMarkerInfo).text =
                        "${marker.title}\n위도: ${marker.position.latitude}\n경도: ${marker.position.longitude}"
                }
            }
        })
    }
}