package com.gursharn.centennialpro.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gursharn.centennialpro.R;
import com.gursharn.centennialpro.helper.SQLiteHandler;
import com.gursharn.centennialpro.helper.SessionManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gursharnbir on 2015-12-16.
 */
public class SemesterActivity extends Activity {

    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private Button btnLogout;
    private TextView txtName;
    String response;
    String url="http://a.indianfort.co.nz/get_all_sem.php";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
    ListView listView;
    TextView createTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sem_layout_main);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        txtName = (TextView) findViewById(R.id.userName);
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("user_name");
        txtName.setText(name);
        findId();
        // demo data
        loadTask();

    }
    public void findId()
    {
        listView = (ListView)findViewById(R.id.listView1);
    }
    public void loadTask()
    {

        class LongOperation extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(SemesterActivity.this);
                pDialog.setMessage("Loading Course Informatiom...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected String doInBackground(String... params) {

                // List<NameValuePair> params = new ArrayList<NameValuePair>();
                // getting JSON string from URL
                // JSONObject json = jParser.makeHttpRequest(AppConfig.URL_CAT, "GET", params);
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    HttpResponse responce = httpclient.execute(httppost);
                    HttpEntity httpEntity = responce.getEntity();


                    response = EntityUtils.toString(httpEntity);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                return "Executed";
            }

            @Override
            protected void onPostExecute(String result) {
                pDialog.dismiss();
                Log.e("forum Categories", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if (success.equals("1")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("categories");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String sem_id = jsonArray.getJSONObject(i).getString("sem_id");
                            String sem_name = jsonArray.getJSONObject(i).getString("sem_name");

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("sem_id", sem_id);
                            hashMap.put("sem_name", sem_name);
                            arrayList.add(hashMap);

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), " No data", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                viewTask();
            }



            @Override
            protected void onProgressUpdate(Void... values) {}
        }
        new LongOperation().execute();

    }

    public void viewTask()
    {
        listView.setAdapter(new CustomAdapter());
    }
    public class CustomAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return arrayList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowview = inflater.inflate(R.layout.activity_sem, null);
            TextView title = (TextView)rowview.findViewById(R.id.textView_title);

            title.setText(arrayList.get(arg0).get("sem_name"));
            final  String sem_id = arrayList.get(arg0).get("sem_id");

            rowview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SemesterActivity.this,CourseActivity.class);
                    intent.putExtra("sem_id",sem_id);
                    startActivity(intent);
                }
            });
            return rowview;
        }

    }
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(SemesterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}