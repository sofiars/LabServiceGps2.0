package cr.ac.labservicegps

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import cr.ac.labservicegps.databinding.ActivityMapsBinding
import cr.ac.labservicegps.db.LocationDatabase
import cr.ac.labservicegps.entity.Location
import cr.ac.labservicegps.service.GpsService

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygon
import org.json.JSONObject

private lateinit var mMap: GoogleMap
private lateinit var locationDatabase: LocationDatabase
private lateinit var layer : GeoJsonLayer

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationDatabase=LocationDatabase.getInstance(this)


        validaPermisos()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        iniciaServicio()
        definePoligono(googleMap)
        recuperarPuntos(mMap)
    }
    /**
     * Obtener los puntos almacenados en la bd y mostrarlos en el mapa
     */
    fun recuperarPuntos(googleMap:GoogleMap){
        mMap = googleMap

        for(location in locationDatabase.locationDao.query()){
            val punto = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(punto).title("Marker in Costa Rica"))
        }

    }

    /**
     *hace un filtro del broadcast GPS(cr.ac.gpsservice.GPS_EVENT)
     * E inicia el servicio(starService) GpsService
     */

    fun iniciaServicio(){

        val filter= IntentFilter()
        filter.addAction(GpsService.GPS)
        val progressReceiver = ProgressReceiver()
        registerReceiver(progressReceiver,filter)
        startService(Intent(this,GpsService::class.java))
    }

    /**
     *Valida si la app tiene permisos de ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION
     * si no tiene permisos solicita al usuario permisos(requestPermission)
     */
    fun validaPermisos(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // NO TENGO PERMISOS
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), SOLICITAR_GPS)
        }
    }

    /**
     * validar que se le dieron los permisos a la app, en caso contrario salir
     */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SOLICITAR_GPS -> {
                if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.exit(1)
                }
            }
        }
    }

    /**
     * es la clase para recibir los mensajes de broadcast
     */
    class ProgressReceiver:BroadcastReceiver(){

        fun getPolygon(layer: GeoJsonLayer): GeoJsonPolygon? {
            for (feature in layer.features) {
                return feature.geometry as GeoJsonPolygon
            }
            return null
        }

        override fun onReceive(p0: Context, p1: Intent) {
            if(p1.action==GpsService.GPS){
                val ubicacion: Location = p1.getSerializableExtra("Localizacion") as Location
                val punto=LatLng(ubicacion.latitude,ubicacion.longitude)
                mMap.addMarker(MarkerOptions().position(punto).title("Marker in Costa Rica"))

                if(PolyUtil.containsLocation(ubicacion.latitude, ubicacion.longitude, getPolygon(layer)!!.outerBoundaryCoordinates, false)){
                    Toast.makeText(p0,"Si se encuentra en el punto ",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(p0,"NO se encuentra en el punto ",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun definePoligono(googleMap: GoogleMap){
        val geoJsonData= JSONObject("{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              -85.7098388671875,\n" +
                "              11.081384602413062\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.9295654296875,\n" +
                "              10.87107045949965\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.6494140625,\n" +
                "              10.617418067950293\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.9075927734375,\n" +
                "              10.271681232946728\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.6658935546875,\n" +
                "              9.85521608608867\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.341796875,\n" +
                "              9.822741867037168\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.1275634765625,\n" +
                "              9.51949682550827\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.8583984375,\n" +
                "              9.833566961339805\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.25390625,\n" +
                "              10.158153268244805\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.242919921875,\n" +
                "              10.223031355670871\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.0506591796875,\n" +
                "              10.147338971518469\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.957275390625,\n" +
                "              10.055402736564236\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.759521484375,\n" +
                "              9.958029972336439\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.649658203125,\n" +
                "              9.746956312582398\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.649658203125,\n" +
                "              9.584500864717143\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.144287109375,\n" +
                "              9.421967599204864\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.638916015625,\n" +
                "              9.031577879631772\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.60595703125,\n" +
                "              8.814510680542021\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.7542724609375,\n" +
                "              8.5918844057982\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.5784912109375,\n" +
                "              8.450638800331001\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.46313476562499,\n" +
                "              8.434337884404306\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.2598876953125,\n" +
                "              8.379996538486912\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.309326171875,\n" +
                "              8.564725847771095\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.4466552734375,\n" +
                "              8.716788630258742\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.3367919921875,\n" +
                "              8.722218306198739\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.1390380859375,\n" +
                "              8.5918844057982\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.1170654296875,\n" +
                "              8.379996538486912\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.045654296875,\n" +
                "              8.314776904399855\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.04977416992188,\n" +
                "              8.340594311750309\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.9248046875,\n" +
                "              8.432979443683774\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.84790039062499,\n" +
                "              8.461505694920898\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.83966064453124,\n" +
                "              8.547071744923766\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.83416748046875,\n" +
                "              8.650268711890488\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.9302978515625,\n" +
                "              8.754794702435618\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.84790039062499,\n" +
                "              8.862005139056482\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.72018432617188,\n" +
                "              8.925773751363266\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.73666381835938,\n" +
                "              8.965114787058173\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.86300659179686,\n" +
                "              9.041071608987645\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.93991088867188,\n" +
                "              9.077687932437932\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.93991088867188,\n" +
                "              9.480217552429565\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.83966064453124,\n" +
                "              9.501889432784724\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.89596557617188,\n" +
                "              9.577730190169342\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.85614013671875,\n" +
                "              9.61429022649668\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.78884887695312,\n" +
                "              9.606166114941981\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.6556396484375,\n" +
                "              9.524914302345891\n" +
                "            ],\n" +
                "            [\n" +
                "              -82.562255859375,\n" +
                "              9.579084335882534\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.0511474609375,\n" +
                "              10.001310360636928\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.6773681640625,\n" +
                "              10.927708206534676\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.67324829101562,\n" +
                "              10.792838759247182\n" +
                "            ],\n" +
                "            [\n" +
                "              -83.92868041992188,\n" +
                "              10.703791711680736\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.03030395507812,\n" +
                "              10.787442717183227\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.122314453125,\n" +
                "              10.768555807732437\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.232177734375,\n" +
                "              10.806328440476824\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.36126708984375,\n" +
                "              10.981638871023351\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.46426391601562,\n" +
                "              10.96006778449538\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.57687377929688,\n" +
                "              11.039603349707097\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.67437744140625,\n" +
                "              11.069255175003283\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.91744995117188,\n" +
                "              10.943888437257428\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.37200927734375,\n" +
                "              11.123159888717236\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.47500610351561,\n" +
                "              11.131244737050944\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.55877685546875,\n" +
                "              11.205345367851104\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.60821533203125,\n" +
                "              11.21612206309125\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.7098388671875,\n" +
                "              11.081384602413062\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}")

        layer = GeoJsonLayer(googleMap, geoJsonData)
        layer.addLayerToMap()

    }


}