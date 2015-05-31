package com.vanikad.hw.londonlivetraffic;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HW on 5/5/2015.
 */
public class CamListXmlParser {

    // We don't use namespaces
    private static final String ns = null;
    private static String rootUrl = null;
    private static Context context = null;


    public CamListXmlParser(Context context){
        this.context = context;
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }

    }

    public List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List cameras = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "syndicatedFeed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }


            if (parser.getName().equals("cameraList")) {
                parser.require(XmlPullParser.START_TAG, ns, "cameraList");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    // Starts by looking for the entry tag
                    if (name.equals("camera")) {
                        cameras.add(readCamera(parser));
                    } else if (parser.getName().equals("rooturl")){
                        rootUrl = readText(parser);
                    } else {
                        skip(parser);
                    }
                }
            } else skip(parser);
        }
        return cameras;
    }

    public static class Cam {
        public final String id;
        public final String location;
        public final String file;
        public final String lat;
        public final String lng;
        public final String rootURL;

        private Cam(String id, String location, String file, String lat, String lng, String rootURL) {
            this.id = id;
            this.location = location;
            this.file = file;
            this.lat = lat;
            this.lng = lng;
            this.rootURL = rootURL;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Cam readCamera(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "camera");
        String id = parser.getAttributeValue(null, "id");
        String location = null;
        String file = null;
        String lat = null;
        String lng = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("location")) {
                location = readLocation(parser);
            } else if (name.equals("file")) {
                file = readFile(parser);
            } else if (name.equals("lat")) {
                lat = readLat(parser);
            } else if (name.equals("lng")) {
                lng = readLng(parser);
            } else skip(parser);
        }
        return new Cam(id, location, file, lat, lng, rootUrl);
    }

    // Processes location tags in the feed.
    private String readLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "location");
        String location = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "location");
        return location;
    }

    // Processes file tags in the feed.
    private String readFile(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "file");
        String file = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "file");
        return file;
    }

    // Processes lat tags in the feed.
    private String readLat(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "lat");
        String lat = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "lat");
        return lat;
    }

    // Processes lng tags in the feed.
    private String readLng(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "lng");
        String lng = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "lng");
        return lng;
    }

    // For the tags location file lat and lng, extracts their text values.
    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
