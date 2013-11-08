package com.example.json_ex;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnQueryTextListener {
	
	TextView fetchText;
	String user_search_term = "";
	SearchView searchView;
	
	HttpClient client;
	final static String URL = "http://itunes.apple.com/search?term=";
	JSONObject json;
	InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fetchText = (TextView) findViewById(R.id.fetchText);
		
		client = new DefaultHttpClient();
		
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    
		
		searchView = (SearchView) findViewById(R.id.searchView);
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(true);
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	public JSONObject search( String searchItem ) throws ClientProtocolException, IOException, JSONException, URISyntaxException{
		String r_searchItem = searchItem.replaceAll("\\s+","");
		StringBuilder url = new StringBuilder(URL);
		url.append(r_searchItem);
		
		String finished_url = url.toString();
		
		HttpGet get = new HttpGet(finished_url);
		HttpResponse r = client.execute(get);
		
		int status = r.getStatusLine().getStatusCode();
		
		if (status == 200){
			HttpEntity e = r.getEntity();
			String data = EntityUtils.toString(e);
			JSONObject json = new JSONObject(data);
			return json;
		}else{
			Log.e("Search","fetch error");
			return null;
		}
		
		
	}
	
	public class Read extends AsyncTask<String, Integer, JSONObject>{

		@Override
		protected JSONObject doInBackground(String... params) {
			try {
				json = search(params[0]);
				return json;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(JSONObject fullArray) {
			String TAG_searched = "trackCensoredName";
			StringBuilder result = new StringBuilder();
			try {
				JSONArray array = fullArray.getJSONArray("results"); // first name in object
				for (int i = 0; i < array.length(); i++){
					JSONObject resultEntry = array.getJSONObject(i);
					result.append(String.valueOf(i+1)+". "+String.valueOf(resultEntry.get(TAG_searched) + "\n")); // One item from object found
					
				}
				
			} catch (JSONException e1) {
				Log.e("onPost ArrayException", e1.getMessage());
				result.append("no information");
			}
			
			fetchText.setText(result.toString());
			
			
		}
		
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		if (haveNetworkConnection()){
			new Read().execute(query);
		}else{
			Toast.makeText(MainActivity.this, "No network signal found", Toast.LENGTH_SHORT).show();
		}

		imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		return false;
	}
	
	private boolean haveNetworkConnection() {

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                return true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                return true;
	    }
	    return false;
	}
	
	

}
