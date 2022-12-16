package com.sononehouse.powerpacks.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sononehouse.powerpacks.R


/**
 * A placeholder fragment containing a simple view.
 */
class OlaFragment : Fragment() {

    private lateinit var pageViewModel: OlaPageViewModel
    private lateinit var latitudeET: EditText;
    private lateinit var longitudeET: EditText;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(OlaPageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ola, container, false)
        val textView: TextView = root.findViewById(R.id.section_label)
        pageViewModel.text.observe(this, Observer<String> {
            textView.text = it
        })

        latitudeET = root.findViewById(R.id.latitude)
        longitudeET = root.findViewById(R.id.longitude)
        val bookOlaButton: Button = root.findViewById(R.id.book)
        bookOlaButton.setOnClickListener {
            bookOla()
        }

        return root
    }

    fun bookOla(){
        val lat = latitudeET.text.toString()
        val long = longitudeET.text.toString()

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://olawebcdn.com/assets/ola-universal-link.html?utm_source=xapp_token&landing_page=bk&drop_lat=$lat&drop_lng=$long&affiliate_uid=12345"))
        startActivity(browserIntent)
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
}