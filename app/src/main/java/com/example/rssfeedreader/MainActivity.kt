package com.example.rssfeedreader

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import com.example.rssfeedreader.databinding.ActivityMainBinding
import java.net.URL
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var downloadData: DownloadData? = null
    private var feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10
    private val STATUS_FEED_LIMIT = "FeedLimit"
    private val STATUS_FEED_URL = "FeedURL"
    private var feedCachedUrl = "INVALIDATED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        downloadUrl(feedUrl.format(feedLimit))
        Log.d(TAG, "onCreate: done")
    }

    private fun downloadUrl(feedUrl: String) {
        if (feedUrl != feedCachedUrl) {
            Log.d(TAG, "downloadUrl: Starting AsyncTask")
            downloadData = DownloadData(this, binding.xmlListView)
            downloadData?.execute(feedUrl)
            feedCachedUrl = feedUrl
            Log.d(TAG, "downloadUrl: done")
        } else {
            Log.d(TAG, "downloadUrl: URL not changed")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)

        if (feedLimit == 10) {
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuFree ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 -> {
                if (!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedlimit to $feedLimit")
                } else {
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedlimit unchanged.")
                }
            }
            R.id.mnuRefresh ->
                feedCachedUrl = "INVALIDATED"
            else ->
                return super.onOptionsItemSelected(item)
        }
        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView) :
                AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun doInBackground(vararg params: String?): String {
                Log.d(TAG, "doInBackground: starts with ${params[0]}")
                val rssFeed = downloadXML(params[0]!!)
                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error Downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
            }

            private fun downloadXML(urlPath: String): String {
                return URL(urlPath).readText()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        feedLimit = savedInstanceState.getInt(STATUS_FEED_LIMIT)
        feedUrl = savedInstanceState.getString(STATUS_FEED_URL, feedUrl)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATUS_FEED_LIMIT, feedLimit)
        outState.putString(STATUS_FEED_URL, feedUrl)
    }
}