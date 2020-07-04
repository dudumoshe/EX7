package com.example.ex7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerHolder {
    private static ServerHolder instance = null;
    public static final String baseURL = "https://hujipostpc2019.pythonanywhere.com";

    synchronized static ServerHolder getInstance(){
        if (instance == null) {
            instance = new ServerHolder();
        }
        return instance;
    }

    public final MyServer server;

    private ServerHolder(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.server = retrofit.create(MyServer.class);
    }
}