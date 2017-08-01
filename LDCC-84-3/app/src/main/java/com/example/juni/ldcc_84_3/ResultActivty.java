package com.example.juni.ldcc_84_3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juni.ldcc_84_3.Speech.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class ResultActivty extends AppCompatActivity {
    ArrayList<String> EInames;
    Context mContext;

    final String type = "successtype";
    final String sevenid = "seven_id";
    final String pointx = "pointx";
    final String pointy = "pointy";
    final String count = "count";
    final String recommend = "recommend";
    final String curx = "cux";
    final String cury = "cuy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_activty);
        mContext = getApplicationContext();
        Intent intent = getIntent();

        String result = intent.getStringExtra("res");
        EInames = intent.getStringArrayListExtra("entitiynames");
        ArrayList<String> EItypes = intent.getStringArrayListExtra("entitiytypes");
        int sevenid = intent.getIntExtra("sevenid", 0);

        //Entity로 서버 통신 쓰레드 오픈
        InsertData task = new InsertData();
        String postParameters = String.format("seven_id=%d", sevenid);
        String names = "&name=";
        String values = "&value=";
        for(int i = 0; i < EInames.size(); i++){
            names += EInames.get(i);
            values += EItypes.get(i);
            if(i != EInames.size() - 1){
                names += "|";
                values += "|";
            }
        }

        postParameters = String.format("%s%s%s",postParameters, names, values);
        task.execute(postParameters);
        ((ImageView)findViewById(R.id.resultad)).setImageResource(getResources().getIdentifier("ad" + String.valueOf(new Random().nextInt(6)), "drawable", getPackageName()));

        TextView t = (TextView) findViewById(R.id.resulttext);
        t.setText("[Debug]" + result + "\nProcessing...");
    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ResultActivty.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray ar = new JSONArray(result);
                for(int i =0; i < ar.length(); i++){
                    JSONObject jo = ar.getJSONObject(i);
                    SetItem(jo, i);
                }
            }
            catch (Exception e){
                Log.e("Error!!", e.getMessage());
            }
            super.onPostExecute(result);
            if(MainActivity.Instance != null)
                MainActivity.Instance.finish();
            progressDialog.dismiss();
            Log.e("Server", "완료");
        }

        @Override
        protected String doInBackground(String... params) {
            String postParameters = (String)params[0];
            String serverURL = "http://13.124.31.50/project_findproduct.php";

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {
                return new String("Error: " + e.getMessage());
            }

        }

        void SetItem(JSONObject jo, int index){
            try {
                int id = 0;
                switch(index){
                    case 0:
                        id = R.id.item1;
                        break;
                    case 1:
                        id = R.id.item2;
                        break;
                    case 2:
                        id = R.id.item3;
                        break;
                }

                View itemView = findViewById(id);
                itemView.setVisibility(View.VISIBLE);
                ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
                TextView title = (TextView) itemView.findViewById(R.id.item_title);
                final TextView seven = (TextView) itemView.findViewById(R.id.item_seven);
                Button mapBt = (Button) itemView.findViewById(R.id.item_map);

                int resId = getResources().getIdentifier( EInames.get(index).toLowerCase(), "drawable", getPackageName());
                if(resId == 0){
                    imageView.setImageResource(R.drawable.notfound);
                }
                else{
                    imageView.setImageResource(resId);
                }
                title.setText(EInames.get(index));
                switch (jo.getInt(type)){
                    case 1: // 여기 세븐 일레븐에 있음
                        seven.setText(R.string.welcome);
                        mapBt.setVisibility(View.INVISIBLE);
                        break;

                    case 2 : // 주변 세븐일레븐에 있음
                        ArrayList<String> sevenArr = new ArrayList<>();
                        ArrayList<String> xposArr = new ArrayList<>();
                        ArrayList<String> yposArr = new ArrayList<>();

                        String sevenjson = jo.getString(sevenid);
                        JSONArray ar = new JSONArray(sevenjson);
                        for(int i =0; i < ar.length(); i++){
                            JSONObject inner = ar.getJSONObject(i);
                            sevenArr.add(inner.getString(sevenid));
                        }
                        String xposjson = jo.getString(pointx);
                        ar = new JSONArray(xposjson);
                        for(int i =0; i < ar.length(); i++){
                            JSONObject inner = ar.getJSONObject(i);
                            xposArr.add(inner.getString(pointx));
                        }
                        String yposjson = jo.getString(pointy);
                        ar = new JSONArray(yposjson);
                        for(int i =0; i < ar.length(); i++){
                            JSONObject inner = ar.getJSONObject(i);
                            yposArr.add(inner.getString(pointy));
                        }
                        //String countjson = jo.getString(count);
                        //String recommendjson = jo.getString(recommend);

                        String sevenstr = "";
                        for(String i : sevenArr)
                            sevenstr += getResources().getStringArray(R.array.seven_name)[Integer.valueOf(i) - 1] + " ";
                        seven.setText(sevenstr);

                        mapBt.setVisibility(View.VISIBLE);
                        BtClickListener listener = new BtClickListener(sevenArr, xposArr, yposArr, jo.getString(curx), jo.getString(cury));
                        mapBt.setOnClickListener(listener);
                        break;

                    case 3:
                        seven.setText(R.string.recommend);
                        mapBt.setVisibility(View.INVISIBLE);
                        break;

                    case 4:
                        seven.setText(getResources().getString(R.string.notfound));                                                                                                                                                                                                                                                                                                                        seven.setText("여기에 있습니다! 어서오세요~");
                        mapBt.setVisibility(View.INVISIBLE);
                        Log.e("test", getResources().getString(R.string.notfound));
                        break;
                }
            }
            catch(Exception e){

            }

        }
    }

    class BtClickListener implements View.OnClickListener{
        ArrayList<String> sevenArr;
        ArrayList<String> xposArr;
        ArrayList<String> yposArr;
        String curx;
        String cury;

        public BtClickListener(){}
        public BtClickListener(ArrayList<String> sevenArr, ArrayList<String> xposArr, ArrayList<String> yposArr, String curx, String cury){
            this.curx = curx;
            this.cury = cury;
            this.sevenArr = sevenArr;
            this.xposArr = xposArr;
            this.yposArr = yposArr;
        }
        @Override
        public void onClick(View v) {
            if(sevenArr == null || xposArr == null || yposArr == null){
                Log.e("ServerErr", "이름, 좌표가 제대로 적용되지 않음");
                return;
            }
            Intent intent = new Intent(mContext, MapsActivity.class);
            intent.putExtra("sevenarr", sevenArr);
            intent.putExtra("xposarr", xposArr);
            intent.putExtra("yposarr", yposArr);
            intent.putExtra("curx", curx);
            intent.putExtra("cury", cury);
            startActivity(intent);
        }
    }
}