package com.sononehouse.powerpacks.ui.main

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sononehouse.powerpacks.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


/**
 * A placeholder fragment containing a simple view.
 */
class OlaFragment : Fragment() {

    private lateinit var pageViewModel: OlaPageViewModel
    private lateinit var coordinateET: EditText;
    private lateinit var urlET: TextView;
    private lateinit var pastButton: Button;

    var client = OkHttpClient().newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(OlaPageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }

        viewModel.selectedUrl.observe(this) {
            if (!it.isNullOrEmpty()) {
                // urlET.setText(it)
                expandShortUrl(it)
            }
        }
    }


    @Throws(IOException::class)
    fun expandShortUrl(url: String) {
        Log.d("``OlaFragment", "expandShortUrl url: $url")
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).execute().use {
            response ->
            // if (!response.isSuccessful) throw IOException("Unexpected code $response")
            processHttpResponse(response)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ola, container, false)
        val textView: TextView = root.findViewById(R.id.section_label)
        pageViewModel.text.observe(viewLifecycleOwner, Observer<String> {
            textView.text = it
        })

        coordinateET = root.findViewById(R.id.coordinates)
        pastButton = root.findViewById(R.id.paste)
        pastButton.setOnClickListener {

            doStart()

            val clipValue = getClipboard()
            if (! clipValue.isNullOrEmpty()) {
                Log.d("``OlaFragment", "clipboard value: $clipValue")

                val r = Regex("""https[\S]+""")
                val m = r.find(clipValue)
                if (m != null && ! m.groups.isEmpty()) {
                    val url = m.groups[0]?.value !!
                    Log.d("``OlaFragment", "extracted url from clipboard: $url")

                    viewModel.setUrl(url)
                    return@setOnClickListener
                }
            }
            doFail("No link found")
        }
        urlET = root.findViewById(R.id.url)

        val bookOlaButton: Button = root.findViewById(R.id.book)
        bookOlaButton.setOnClickListener {
            bookOla()
        }

        return root
    }

    fun openOla(lat: String, long: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://olawebcdn.com/assets/ola-universal-link.html?utm_source=xapp_token&landing_page=bk&drop_lat=$lat&drop_lng=$long&affiliate_uid=12345"))
         startActivity(browserIntent)
    }

    fun bookOla(){
        val coordianateText = coordinateET.text.toString()
        val tokens = coordianateText.split(",")
        val lat = tokens.get(0).trim()
        val long = tokens.get(1).trim()

        // val url = "https://goo.gl/maps/WSefEi3TPvQ3BV5t8"
        // this.extractCoordinates(url)
        openOla(lat, long)
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): OlaFragment {
            return OlaFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


    fun getClipboard() : String? {
        val clipBoardManager = this.context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return clipBoardManager.primaryClip?.getItemAt(0)?.text?.toString()
    }


    fun extractCoordinatesFromPlaceLocationUrl(location: String): Pair<String, String>? {
        // https://www.google.com/maps/place/Ak+Chinese+...+Karnataka+560068/data=!4m6!3m5!1s0x3bae1533ab208859:0xbd066f40b29e8109!7e2!8m2!3d12.9089961!4d77.6467188?utm_source=mstt_1...
        // !3d12.9089961!4d77.6467188
        // 12.9089961, 77.6467188

        val regex = """!3d[+]?(\d+\.\d+)!4d[+]?(\d+\.\d+)"""
        val latLong = extractCoordinatesFromTextGivenRegex(location, regex)
        if (latLong != null) {
            Log.d("``OlaFragment", "[success] matched location /place")
        }
        return latLong
    }

    fun extractCoordinatesFromSearchLocationUrl(location: String) : Pair<String, String>? {
        // https://www.google.com/maps/search/12.916527,+77.644803?shorturl=1
        val regex = """[+]?(\d+\.\d+)[,][+]?(\d+\.\d+)"""
        val latLong = extractCoordinatesFromTextGivenRegex(location, regex)
        if (latLong != null) {
            Log.d("``OlaFragment", "[success] matched location /search")
        }
        return latLong
    }

    // https://www.google.com/maps/place/Ak+.../data=!4m6!3m5!1s0x3bae1533ab208859:0xbd066f40b29e8109!7e2!8m2!3d12.9089961!4d77.6467188?utm_source=mstt_1&entry=gps&g_ep=CAESCTExLjYwLjcwMxgAIIgnKgBCAklO
    // https://www.google.com/maps/place/Cakes...+560102/data=!4m2!3m1!1s0x3bae152232c713bd:0x2a8fb042859e823a?utm_source=mstt_1&entry=gps&g_ep=CAESCTExLjYwLjcwMxgAIIgnKgBCAklO
    fun extractPlaceIdFromLocationUrl(location: String): String? {
        if (location.contains("/place")) {
            val regex = """!1s(0x\w+:0x\w+)"""
            val r = Regex(regex)
            Log.d("``OlaFragment", "matching with regex $regex")
            // val r2 = Regex(".*(\\d+),")
            val mr = r.find(location)
            Log.d("``OlaFragment", "groups ${mr?.groupValues}")
            if (mr != null && !mr.groups.isEmpty()) {
                return mr.groups[1]?.value
            }
        }
        return null
    }

    fun extractCoordinatesFromTextGivenRegex(text: String, regex: String) : Pair<String, String>? {
        val r = Regex(regex)
        Log.d("``OlaFragment", "matching with regex $regex")
        // val r2 = Regex(".*(\\d+),")
        val mr = r.find(text)
        Log.d("``OlaFragment", "groups ${mr?.groupValues}")
        if (mr != null && !mr.groups.isEmpty()) {
            val lat = mr.groups[1]?.value!!
            val long = mr.groups[2]?.value!!

            return Pair(lat, long)
        }

        return null
    }

    fun extractCoordinatesFromMapsHtml(htmlText: String) : Pair<String, String>? {
        //parse html
        // ;ll=12.915171,77.647809
        val regex1 = """;ll=[+]?(\d+\.\d+)[,][+]?(\d+\.\d+)"""
        val p1 = extractCoordinatesFromTextGivenRegex(htmlText, regex1)
        if (p1 != null){
            Log.d("``OlaFragment", "[success] matched body regex 1")
            return p1
        }

        // staticmap?center=12.915725%2C77.647851
        val regex2 = """staticmap[?]center=[+]?(\d+\.\d+)%2C[+]?(\d+\.\d+)"""
        val p2 = extractCoordinatesFromTextGivenRegex(htmlText, regex2)
        if (p2 != null){
            Log.d("``OlaFragment", "[success] matched body regex 2")
            return p2
        }
        // Log.d("``OlaFragment", "$htmlText")

        return null
    }

    fun processHttpResponse(response: Response) {
//        for ((name, value) in response.headers) {
//            Log.d("``OlaFragment", "$name: $value")
//        }

        val httpStatusCode = response.code
        Log.d("``OlaFragment", "code = $httpStatusCode")

        if (httpStatusCode == 200) {
            val latLong = extractCoordinatesFromMapsHtml(response.body?.string()!!)
            if (latLong != null){
                doSuccess(latLong)
            }
            else {
                doFail("Can't extract location coordinates")
            }
        }
        else if (httpStatusCode >= 301 && httpStatusCode <= 302) {
            val location = response.headers.get("location")
            Log.d("``OlaFragment", "location: $location")
            if (! location.isNullOrEmpty()) {
                if (location.contains("/search")) {
                    val latLong = extractCoordinatesFromSearchLocationUrl(location)
                    if (latLong != null) {
                        doSuccess(latLong)
                    } else {
                        doFail("Can't extract location coordinates")
                    }
                }
                else if (location.contains("/place")){
                    // try to extract from location url
                    val latLong = extractCoordinatesFromPlaceLocationUrl(location)
                    if (latLong != null) {
                        doSuccess(latLong)
                    }
                    else {
                        // extract placeId and find from the html body
                        val placeId = extractPlaceIdFromLocationUrl(location)
                        if (placeId != null) {
                            val newUrl =
                                "https://www.google.com/maps/place/1+2/data=!4m2!3m1!1s$placeId"
                            expandShortUrl(newUrl)
                        }
                        else {
                            doFail("Not able to find placeId. Abort.")
                        }
                    }
                }
                else {
                    showSmallToast("Redirect Url not supported $location")
                }
            }
            else {
                doFail("Empty location in url. Abort.")
            }
        }
        else {
            doFail("server error or no internet")
        }
    }

    fun doStart() {
        urlET.setText("[error] Processing")
        coordinateET.setText("-,-")
    }

    fun doSuccess(latLong: Pair<String, String>) {
        val lat = latLong.first
        val long = latLong.second
        Log.d("``OlaFragment", "latitude: $lat")
        Log.d("``OlaFragment", "longitude: $long")

        urlET.setText("[success] coordinates extracted")
        coordinateET.setText("$lat,$long")
        showSmallToast("[success] Coordinates extracted")
    }

    fun doFail(error: String) {
        urlET.setText("[error] $error")
        // coordinateET.setText("-,-")
        showSmallToast("[fail] $error")
    }

    fun showSmallToast(x: String) {
        Toast.makeText(
            this.context,
            x,
            Toast.LENGTH_SHORT
        ).show()
    }
}