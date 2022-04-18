package com.example.wallpaperapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText searchEdit;
    private ImageView searchImg;
    private RecyclerView wallpaperRV;
    private ProgressBar loadingProgress;
    private ArrayList<String> wallpaperArrayList;
    private ArrayList<String> originalSizeWallpaper;
    private WallpaperRVAdapter wallpaperRVAdapter;
    protected int a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        Random random=new Random();
        a=random.nextInt(100);
        a++;

        searchEdit=findViewById(R.id.idEditSearch);
        searchImg=findViewById(R.id.idSearch);
        wallpaperRV=findViewById(R.id.wallpapers);
        loadingProgress=findViewById(R.id.progBar);

        wallpaperArrayList=new ArrayList<>();
        originalSizeWallpaper=new ArrayList<>();

        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2);
        wallpaperRVAdapter =new WallpaperRVAdapter(wallpaperArrayList,originalSizeWallpaper,this);
        wallpaperRV.setLayoutManager(gridLayoutManager);
        wallpaperRV.setAdapter(wallpaperRVAdapter);

        getWallpapers();

        searchImg.setOnClickListener(v -> {
            String searchStr=searchEdit.getText().toString();
            if (searchStr.isEmpty()){
                Toast.makeText(MainActivity.this,"Empty Search", Toast.LENGTH_LONG).show();
            }else {
                getWallpaperBySearch(searchStr);
            }
        });
    }

    private void getWallpapers(){

        wallpaperArrayList.clear();
        originalSizeWallpaper.clear();
        loadingProgress.setVisibility(View.VISIBLE);

        String url="https://api.pexels.com/v1/curated?per_page=100&page="+a+"";
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingProgress.setVisibility(View.GONE);
            try {
                JSONArray photoArray=response.getJSONArray("photos");
                for (int i=0;i<photoArray.length();i++){
                    JSONObject photoObject=photoArray.getJSONObject(i);
                    String originalImgUrl=photoObject.getJSONObject("src").getString("original");
                    String imgUrl=photoObject.getJSONObject("src").getString("portrait");
                    wallpaperArrayList.add(imgUrl);
                    originalSizeWallpaper.add(originalImgUrl);
                }
                wallpaperRVAdapter.notifyDataSetChanged();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }, error -> Toast.makeText(MainActivity.this,"Fail to load Wallpaper",Toast.LENGTH_LONG).show()){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("Authorization","563492ad6f91700001000001523631ff3ccc48fd91af2c52ea1e92ae");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }

    private void getWallpaperBySearch(String search){
        wallpaperArrayList.clear();
        originalSizeWallpaper.clear();
        loadingProgress.setVisibility(View.VISIBLE);

        String url;
        if(search.equals("Random")){
            getWallpapers();
            return;
        }
        else{
            if(search.length()<20){
                url="https://api.pexels.com/v1/search/?page="+a+"&per_page=80&query="+search+"";
            }
            else{
                url=search;
            }
        }

        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingProgress.setVisibility(View.GONE);
            try {
                JSONArray photoArray=response.getJSONArray("photos");
                if(photoArray.length()==0){
                    if(photoArray.length()==0){
                        String url1 ="https://api.pexels.com/v1/search/?page=1&per_page=80&query="+search+"";
                        getWallpaperBySearch(url1);
                    }
                }
                for (int i=0;i<photoArray.length();i++){
                    JSONObject photoObject=photoArray.getJSONObject(i);
                    String imgUrl=photoObject.getJSONObject("src").getString("portrait");
                    String originalImgUrl=photoObject.getJSONObject("src").getString("original");
                    wallpaperArrayList.add(imgUrl);
                    originalSizeWallpaper.add(originalImgUrl);
                }
                wallpaperRVAdapter.notifyDataSetChanged();

            }catch (JSONException e){
                e.printStackTrace();
            }

        }, error -> {

        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String,String> headers=new HashMap<>();
                headers.put("Authorization","563492ad6f91700001000001523631ff3ccc48fd91af2c52ea1e92ae");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}