package com.gursharn.centennialpro.activity;

/**
 * Created by Gursharnbir on 2015-12-04.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gursharn.centennialpro.R;
import com.gursharn.centennialpro.app.AppConfig;
import com.gursharn.centennialpro.helper.SQLiteHandler;
import com.gursharn.centennialpro.helper.SessionManager;

public class EventsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;

    private Button btnLogout;
    private TextView txtName;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> productsList;

    // url to get all products list
    //private static String url_all_products = "http://10.0.2.2:8888/SCApi/events.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSEGE = "message";
    private static final String TAG_EVENTS = "events";
    private static final String EVENTS_ID = "events_id";
    private static final String EVENTS_TOPIC = "events_topic";
    private static final String EVENTS_DESC = "events_desc";
    private static final String EVENTS_DATE = "events_date";
    private static final String EVENTS_MONTH = "events_month";

    // products JSONArray
    JSONArray products = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_events);

        btnLogout = (Button) findViewById(R.id.btnLogout);
        txtName = (TextView) findViewById(R.id.userName);


        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("user_name");
        txtName.setText(name);
        // Hashmap for ListView
        productsList = new ArrayList<HashMap<String, String>>();

        // Loading products in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
    // lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      //      @Override
      //      public void onItemClick(AdapterView<?> parent, View view,
           //                         int position, long id) {
                // getting values from selected ListItem
        //        String pid = ((TextView) view.findViewById(R.id.events_id)).getText()
                //        .toString();

                // Starting new intent
            //    Intent in = new Intent(getApplicationContext(),
              //          EditProductActivity.class);
                // sending pid to next activity
               // in.putExtra(TAG_PID, pid);

                // starting new activity and expecting some response back
              //  startActivityForResult(in, 100);
     //       }
   //     });
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EventsActivity.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(AppConfig.URL_EVENTS, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Events: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
                //String error = json.getString("message");
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_EVENTS);

                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(EVENTS_ID);
                        String name = c.getString(EVENTS_TOPIC);

                        String dsc = c.getString(EVENTS_DESC);
                        String desc = Html.fromHtml(dsc).toString();

                                String date = c.getString(EVENTS_DATE);
                        String month = c.getString(EVENTS_MONTH);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(EVENTS_ID, id);
                        map.put(EVENTS_TOPIC, name);
                        map.put(EVENTS_DATE, date);
                        map.put(EVENTS_DESC, desc);
                        map.put(EVENTS_MONTH, month);

                        // adding HashList to ArrayList
                        productsList.add(map);
                    }
                } else {

                    String error = json.getString(TAG_MESSEGE);
                    Toast.makeText(getApplicationContext(),
                            error, Toast.LENGTH_LONG).show();
                    // no products found
                    // Launch Add New product Activity
              //      Intent i = new Intent(getApplicationContext(),
                //            NewProductActivity.class);
                    // Closing all previous activities
                //    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               //     startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            EventsActivity.this, productsList,
                            R.layout.events_layout, new String[] {
                            EVENTS_TOPIC, EVENTS_DESC, EVENTS_DATE, EVENTS_MONTH},
                            new int[] { R.id.name, R.id.desc, R.id.date, R.id.month });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(EventsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
