package com.vanikad.hw.londonlivetraffic.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by HW on 5/3/2015.
 */
public class FileDownloader extends AsyncTask<String, Integer, String> {
    private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;
    private final HttpClient httpClient = new DefaultHttpClient();
    final HttpParams params = httpClient.getParams();
    HttpResponse response;
    private String content = null;
    private ProgressDialog dialog;
    private Context context;
    /* Open a connection to that URL. */
    URLConnection urlConnection;
    InputStream inputStream = null;
    public AsyncResponse delegate=null;

    public FileDownloader(Context context, AsyncResponse delegate){
        dialog = new ProgressDialog(context);
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Loading data... Please wait...");
        dialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        String URL = urls[0];
        String fileName = urls[1];
        downloadFile(URL, fileName);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String s) {
        dialog.dismiss();
        delegate.downloadFinished(true);

        try {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Toast.makeText(context, "File Loaded", Toast.LENGTH_LONG).show();

            } else {
                //Closes the connection.
                Log.w("HTTP1:", statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
        }

    }

    @Override
    protected void onCancelled() {
        dialog.dismiss();
        Toast toast = Toast.makeText(context,
                "Error connecting to Server", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();
    }


    /** This method will download the file and save it in internal phone memory under app data. If file exists
    * it will be overwritten.
    */
    public boolean downloadFile(String downloadUrl, String fileName) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            URL url = new URL(downloadUrl); //you can write here any link
            urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(5000);

            long startTime = System.currentTimeMillis();
            Log.d("DownloadManager", "download begining");
            Log.d("DownloadManager", "download url:" + url);
            Log.d("DownloadManager", "downloaded file name:" + fileName);

           /*
            * Define InputStreams to read from the URLConnection.
            */
            inputStream = urlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);

           /*
            * Read bytes to the Buffer until there is nothing more to read(-1).
            */
            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
           /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();
            Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
            String[] files = context.getFilesDir().list();
            for (int i = 0; i < files.length; i++ ){
                Log.d(Integer.toString(i), files[i]);
            }

        } catch (IOException e) {
            Log.d("DownloadManager", "Error: " + e);
        }

        return true;
    }

}
