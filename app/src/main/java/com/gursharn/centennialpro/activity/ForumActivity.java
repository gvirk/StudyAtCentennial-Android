package com.gursharn.centennialpro.activity;

/**
 * Created by Gursharnbir on 2015-12-13.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gursharn.centennialpro.R;
import com.gursharn.centennialpro.helper.SQLiteHandler;
import com.gursharn.centennialpro.helper.SessionManager;

public class ForumActivity extends Activity{

    private SQLiteHandler db;
    private SessionManager session;
    private Button btnLogout;
    private TextView txtName;
    String response;
    String fetch_url = "http://a.indianfort.co.nz/get_topic_bycategory.php?topic_cat=";
    String url="http://a.indianfort.co.nz/create_topic.php";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
    ListView listView;
    TextView createTask;
    String cat_id;
    String userId;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        //Session session = new Session(ForumActivity.this);
        //userId = session.getUserId();
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

        name = user.get("user_name");
        txtName.setText(name);
        Bundle bundle = getIntent().getExtras();
        cat_id = bundle.getString("cat_id");
        Log.e("cat_id",cat_id);
        fetch_url = fetch_url+cat_id;
        findId();
        viewTask();
        createTask.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                final Dialog dialog = new Dialog(ForumActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog);
                final EditText editText_title = (EditText) dialog.findViewById(R.id.editText1);
                // final EditText editText_desc = (EditText) dialog.findViewById(R.id.editText2);
                Button click = (Button) dialog.findViewById(R.id.btn);
                click.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        String cat_title = editText_title.getText().toString().trim();
                        //       String cat_desc = editText_desc.getText().toString().trim();
                        createTask(cat_title);
                    }
                });
                dialog.show();
            }
        });
    }
    public void findId()
    {
        listView = (ListView)findViewById(R.id.listView1);
        createTask = (TextView)findViewById(R.id.textView3);
    }
    public void createTask(final String catname)
    {

        class LongOperation extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {


                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                    parameters.add(new BasicNameValuePair("topic_subject", catname));
                    parameters.add(new BasicNameValuePair("topic_cat", cat_id));
                    parameters.add(new BasicNameValuePair("topic_by", name));

                    httppost.setEntity(new UrlEncodedFormEntity(parameters));
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
                Log.e("forum task created",response);
                Toast.makeText(getApplicationContext(),"Success !!!",Toast.LENGTH_LONG).show();
                viewTask();

            }

            @Override
            protected void onPreExecute() {}

            @Override
            protected void onProgressUpdate(Void... values) {}
        }
        new LongOperation().execute();

    }

    public void viewTask()
    {
        arrayList.clear();
        class LongOperation extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(fetch_url);
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

                Log.e("view task", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if(success.equals("1"))
                    {
                        JSONArray jsonArray = jsonObject.getJSONArray("topics ");
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            String topic_id = jsonArray.getJSONObject(i).getString("topic_id");
                            String topic_subject = jsonArray.getJSONObject(i).getString("topic_subject");
                            String topic_date = jsonArray.getJSONObject(i).getString("topic_date");
                            String topic_by = jsonArray.getJSONObject(i).getString("topic_by");

                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("topic_id",topic_id);
                            hashMap.put("topic_subject",topic_subject);
                            hashMap.put("topic_date",topic_date);
                            hashMap.put("topic_by",topic_by);
                            arrayList.add(hashMap);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No topics in this category",Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listView.setAdapter(new CustomAdapter());

            }

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected void onProgressUpdate(Void... values) {}
        }
        new LongOperation().execute();
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
            View rowview = inflater.inflate(R.layout.activity_topic, null);
            TextView title = (TextView)rowview.findViewById(R.id.title);
            TextView user = (TextView)rowview.findViewById(R.id.name);
            final String topic_id = arrayList.get(arg0).get("topic_id");
            TextView date = (TextView)rowview.findViewById(R.id.date);
            date.setText(arrayList.get(arg0).get("topic_date"));
            title.setText(arrayList.get(arg0).get("topic_subject"));
            user.setText(arrayList.get(arg0).get("topic_by"));

            rowview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ForumActivity.this,Topic_Comments.class);
                    intent.putExtra("topic_id",topic_id);
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
        Intent intent = new Intent(ForumActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

