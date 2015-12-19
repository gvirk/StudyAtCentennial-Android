package com.gursharn.centennialpro.activity;

/**
 * Created by Gursharnbir on 2015-12-13.
 */
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import android.os.Bundle;
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

public class Topic_Comments extends Activity{
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private Button btnLogout;
    private TextView txtName;
    String response;
    String fetch_url = "http://a.indianfort.co.nz/get_post_bytopic.php?post_topic=";
    String url="http://a.indianfort.co.nz/post_on_topic.php";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
    ListView listView;
    TextView createTask;
    String topic_id;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
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
        topic_id = bundle.getString("topic_id");
        fetch_url = fetch_url+topic_id;

        Log.e("topic_id",topic_id);
        findId();
        viewTask();
        createTask.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                final Dialog dialog = new Dialog(Topic_Comments.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.post);
                final EditText editText_title = (EditText) dialog.findViewById(R.id.editText1);
                editText_title.setVisibility(View.INVISIBLE);
                final EditText editText_desc = (EditText) dialog.findViewById(R.id.editText2);
                Button click = (Button) dialog.findViewById(R.id.btn);
                click.setText("Post");
                click.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        //  String cat_title = editText_title.getText().toString().trim();
                        String post_desc = editText_desc.getText().toString().trim();
                     //   Session session = new Session(Topic_Comments.this);
                        createTask(post_desc, topic_id, name);
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
    public void createTask(final String postdesc,final String postid, final String userid)
    {

        class LongOperation extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {


                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                    parameters.add(new BasicNameValuePair("post_content", postdesc));
                    parameters.add(new BasicNameValuePair("post_topic", postid));
                    parameters.add(new BasicNameValuePair("post_by", userid));

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
        class LongOperation extends AsyncTask<String, Void, String> {
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(Topic_Comments.this);
                pDialog.setMessage("Loading Topic. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }
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
                pDialog.dismiss();
                Log.e("view task", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    if(success.equals("1"))
                    {
                        JSONArray jsonArray = jsonObject.getJSONArray("posts ");
                        for(int i=0;i<jsonArray.length();i++)
                        {
                            String topic_id = jsonArray.getJSONObject(i).getString("post_topic");
                            String post_id = jsonArray.getJSONObject(i).getString("post_id");
                           // String topic_s =
                            String post_content=  jsonArray.getJSONObject(i).getString("post_content");

                                    //Html.fromHtml(topic_s).toString();
                           // fromHtml(String source, Html.ImageGetter imageGetter, Html.TagHandler tagHandler)
                            String topic_name = jsonArray.getJSONObject(i).getString("post_title");

                            String topic_d = jsonArray.getJSONObject(i).getString("post_date");
                            String post_date= Html.fromHtml(topic_d).toString();
                            String post_by = jsonArray.getJSONObject(i).getString("post_by");

                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("topic_name",topic_name);
                            hashMap.put("topic_id",topic_id);
                            hashMap.put("post_id",post_id);
                            hashMap.put("topic_subject",post_content);
                            hashMap.put("topic_date",post_date);
                            hashMap.put("topic_by",post_by);
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
            TextView topicName = (TextView)findViewById(R.id.textView4);
            topicName.setText(arrayList.get(arg0).get("topic_name"));
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowview = inflater.inflate(R.layout.forum_listitem_date, null);
            TextView title = (TextView)rowview.findViewById(R.id.textView_title);
            TextView desc = (TextView)rowview.findViewById(R.id.textView_user);
            desc.setTextColor(Color.BLUE);
            TextView date = (TextView)rowview.findViewById(R.id.textView_date);
            date.setText(arrayList.get(arg0).get("topic_date"));
            String post_content = arrayList.get(arg0).get("topic_subject");
            title.setText(Html.fromHtml(post_content, null, null));
            desc.setText(arrayList.get(arg0).get("topic_by"));
            return rowview;
        }

    }
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Topic_Comments.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
