package com.sononehouse.powerpacks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.sononehouse.powerpacks.ui.main.MainViewModel
import com.sononehouse.powerpacks.ui.main.SectionsPagerAdapter


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
//        val fab: FloatingActionButton = findViewById(R.id.fab)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)

//        fab.setOnClickListener { view ->
////            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                    .setAction("Action", null).show()
//
//            val u1 = "https://goo.gl/maps/K4DKgyREKKwyymD2A"
//            val u2 = "https://goo.gl/maps/WSefEi3TPvQ3BV5t8"
//            // viewModel.setUrl(u1)
//        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val uri: Uri? = intent?.data
        Log.d("``MainActivity", "uri: $uri")

        if (uri != null) {
            val url = uri.toString()
            viewModel.setUrl(url)
        }
    }
}