package org.wheelmap.android.net;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.holoeverywhere.preference.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.util.UriUtils;
import org.wheelmap.android.activity.StartupActivity;
import org.wheelmap.android.app.WheelmapApp;
import org.wheelmap.android.mapping.Base;
import org.wheelmap.android.mapping.node.SingleNode;
import org.wheelmap.android.net.request.TotalNodeCountRequestBuilder;
import org.wheelmap.android.service.RestServiceException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import roboguice.RoboGuice;

public class MarkerIconExecutor extends SinglePageExecutor<SingleNode> implements
        IExecutor {

    private static final String MODIFIED_AT = "modified_at";
    private static final String TYPE = "type";
    private static final String MARKER = "marker";

    public static final String CACHE_DIR = "filesdir";

    SharedPreferences prefs;

    public MarkerIconExecutor(Context context, Bundle bundle) {
        super(context, bundle, SingleNode.class);
    }

    @Override
    public void prepareContent() {
    }

    @Override
    public void execute(long id) throws RestServiceException {
        if(true){
           return;
        }
        prefs = WheelmapApp.getDefaultPrefs();

        String request = "http://"+getServer()+"/api/assets?api_key="+getApiKey();

        try{
            org.apache.http.client.HttpClient client =  mRequestProcessor.getRequestFactory().getHttpClient();
            HttpGet get = new HttpGet(request);
            HttpResponse response = client.execute(get);
            if(response.getStatusLine().getStatusCode() == 200){
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(json);
                JSONArray assets = jsonObject.getJSONArray("assets");
                for(int i=0;i<assets.length();i++){
                    JSONObject item = assets.getJSONObject(i);

                    //search marker
                    if(item.optString(TYPE,item.optString("name","")).equals(MARKER)){
                         long modified_at = item.getLong(MODIFIED_AT);
                         long current_data = prefs.getLong(MARKER+MODIFIED_AT,-1);

                         if(modified_at > current_data || StartupActivity.LOAD_AGAIN_DEBUG){
                            boolean successfully = reloadMarkerAssets(item.getString("url"));
                            if(successfully){
                               prefs.edit().putLong(MARKER+MODIFIED_AT,modified_at).commit();
                            }
                            break;
                         }
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            processException(
                    RestServiceException.ERROR_NETWORK_FAILURE,
                    new NetworkErrorException(), true);
        }
    }

    private boolean reloadMarkerAssets(String url){

        Context context = getContext();

        File cachedir = context.getDir(CACHE_DIR, Context.MODE_PRIVATE);

        File markerZip = new File(cachedir+"/marker.zip");
        boolean download = downloadFile(markerZip,url);
        if(!download){
           return false;
        }

        File marker_path = new File(cachedir+"/marker");
        try{
            unzipFile(markerZip,marker_path);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        markerZip.delete();

        return true;

    }

    public static boolean markerIconsDownloaded(){
        return WheelmapApp.getDefaultPrefs().getLong(MARKER+MODIFIED_AT,-1) > 0;
    }

    public static File getMarkerPath(Context context){
        File cachedir = context.getDir(CACHE_DIR, Context.MODE_PRIVATE);
        File marker_path = new File(cachedir+"/marker");
        return marker_path;
    }


    private boolean downloadFile(File to,String fromUrl){

            try
            {
                URL url = new URL(fromUrl);

                URLConnection ucon = url.openConnection();
                ucon.setReadTimeout(5000);
                ucon.setConnectTimeout(10000);

                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

                File file = to;

                if (file.exists())
                {
                    file.delete();
                }
                file.createNewFile();

                FileOutputStream outStream = new FileOutputStream(file);
                byte[] buff = new byte[5 * 1024];

                int len;
                while ((len = inStream.read(buff)) != -1)
                {
                    outStream.write(buff, 0, len);
                }

                outStream.flush();
                outStream.close();
                inStream.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }

            return true;
    }


    private void unzipFile(File zipfile, File to) throws Exception{
        FileInputStream is;
        ZipInputStream zis;

        String filename;
        is = new FileInputStream(zipfile);
        zis = new ZipInputStream(is);

        ZipEntry ze;
        byte[] buffer = new byte[2048];
        int count;
        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();
            Log.d(getClass().getSimpleName(), "Full Issue Unzipping: " + filename);

            File file = new File(to +"/"+ filename);

            if(file.exists()){
               file.delete();
            }

            // make directory if necessary
            new File(file.getParent()).mkdirs();

            if (!ze.isDirectory() && !file.isDirectory()) {
                FileOutputStream fout = new FileOutputStream(file);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
            }
            zis.closeEntry();
        }

        is.close();
        zis.close();
    }


    @Override
    public void prepareDatabase() throws RestServiceException {
    }
}
