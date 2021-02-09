package com.example.rssfeedreader

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate: called")

        val downloadData = DownloadData()

        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(TAG, "onCreate: done")
    }

    companion object {
        private class DownloadData : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"
            override fun doInBackground(vararg params: String?): String {
                Log.d(TAG, "doInBackground: starts with ${params[0]}")
                val rssFeed = downloadXML(params[0]!!)
                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error Downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.d(TAG, "onPostExecute: parameter is $result")
            }

            private fun downloadXML(urlPath: String): String {
                val xmlResult = StringBuilder()
                try {
                    val url = URL(urlPath)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val response = connection.responseCode
                    Log.d(TAG, "downloadXML: The response code was: $response")


//                  val stream = connection.inputStream
                    connection.inputStream.buffered().reader().use {
                        xmlResult.append(it.readText())
                    }

                    Log.d(TAG, "downloadXML: Received ${xmlResult.length} bytes")
                    return xmlResult.toString()

                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is MalformedURLException -> "downloadXML: Invalid URL ${e.message}"
                        is IOException -> "downdownloadXML: IO Exception reading data: ${e.message}"
                        is SecurityException -> {
                            e.printStackTrace()
                            "downloadXML: SecurityException: Needs permissions? ${e.message}"
                        }
                        else -> "downloadXML: Unknown Error: ${e.message}"
                    }
                }
                return ""
            }
        }
    }


}