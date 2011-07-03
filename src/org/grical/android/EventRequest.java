/*
 * Copyright 2011 Thomas Fricke, grical.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.grical.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;

import android.util.Log;

public class EventRequest {

	private static final String TAG = "EventRequest";
	final MapView mapView;

	public EventRequest(final MapView mapView) {
		this.mapView = mapView;
	}

	private String constructUri() {
		final GeoPoint center = this.mapView.getMapCenter();
		final double centerLat = center.getLatitude(), centerLng = center.getLongitude();

		// return "http://grical.org/s/?view=json&query=@" + centerLng + "," + centerLat;
		return "http://grical.org/s/@berlin/json/";
	}

	private HttpURLConnection connect() throws MalformedURLException, IOException,
			ProtocolException {
		final HttpURLConnection connection;
		final String uri = constructUri();
		final URL url = new URL(uri);
		connection = (HttpURLConnection) url.openConnection();
		connection.setReadTimeout(20000); // ms
		connection.setConnectTimeout(30000); // ms
		connection.setRequestMethod("GET");

		// Start the query
		connection.connect();
		return connection;
	}

	/**
	 * internally read the whole stream from the connection
	 * 
	 * @param connection
	 *            the Http Connection
	 * @return a {@link StringBuffer} containing the entire output
	 * @throws UnsupportedEncodingException
	 *             if encoding is wrong
	 * @throws IOException
	 *             if other IOException
	 */
	StringBuffer readInputStream(HttpURLConnection connection)
			throws UnsupportedEncodingException, IOException {
		final InputStreamReader reader = new InputStreamReader(connection.getInputStream(),
				"UTF-8");
		// read the result from the server
		final BufferedReader bufferedReader = new BufferedReader(reader, 1024);
		final StringBuffer readBuffer = new StringBuffer();
		for (String line = bufferedReader.readLine(); line != null; line = bufferedReader
				.readLine()) {
			// Log.e(TAG, "line=" + line);
			readBuffer.append(line).append('\n');
		}

		reader.close();
		return readBuffer;
	}

	public JSONArray retrieveObjects() {
		HttpURLConnection connection = null;

		try {
			connection = connect();

			final StringBuffer readBuffer = readInputStream(connection);
			final JSONArray jsonArray = new JSONArray(readBuffer.toString());
			return jsonArray;
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
		} catch (Exception e) {
			Log.e(TAG, "other Exception", e);
		} finally {

			if (connection != null) {
				connection.disconnect();
			}
		}

		return null;
	}
}
