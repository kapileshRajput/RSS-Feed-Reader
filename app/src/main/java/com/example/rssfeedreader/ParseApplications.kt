package com.example.rssfeedreader

import android.util.Log
import androidx.core.graphics.createBitmap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.Exception

class ParseApplications {
    private val TAG = "ParseApplications"
    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean {
        Log.d(TAG, "parse: called with $xmlData")
        var status = true
        var inEntry = false
        var gotImage = false
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var curretRecord = FeedEntry()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName =
                    xpp.name?.toLowerCase() // ToDo: should your the safe-call operator "?"
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        Log.d(TAG, "parse: Starting tag for $tagName")
                        if (tagName == "entry") {
                            inEntry = true
                        } else if ((tagName == "image") && inEntry) {
                            val imageResolution = xpp.getAttributeValue(null, "height")
                            if (imageResolution.isNotEmpty()) {
                                gotImage = imageResolution == "53"
                            }
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text
                    XmlPullParser.END_TAG -> {
                        Log.d(TAG, "parse: Ending tag for $tagName")
                        if (inEntry) {
                            when (tagName) {
                                "entry" -> {
                                    applications.add(curretRecord)
                                    inEntry = false
                                    curretRecord = FeedEntry() // create a new object
                                }
                                "name" -> curretRecord.name = textValue
                                "artist" -> curretRecord.artist = textValue
                                "releasedate" -> curretRecord.releaseDate = textValue
                                "summary" -> curretRecord.summary = textValue
                                "image" -> if (gotImage) {
                                    curretRecord.imageURL = textValue
                                }
                            }
                        }
                    }
                }
                // Nothing else to do
                eventType = xpp.next()
            }

            for (app in applications) {
                Log.d(TAG, "parse: *****************************")
                Log.d(TAG, "parse: ${app}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }

        return status
    }
}