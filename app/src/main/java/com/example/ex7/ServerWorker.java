package com.example.ex7;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import retrofit2.Response;

public class ServerWorker extends Worker {
    public static final int GET_TOKEN = 0;
    public static final int GET_INFO = 1;
    public static final int SET_PRETTY_NAME = 2;

    public ServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @SuppressLint("RestrictedApi")
    @Override
    public Result doWork() {
        Data outputData = null;
        int type = getInputData().getInt("type", - 1);
        String usernameOrToken = getInputData().getString("usernameOrToken");
        String prettyName = getInputData().getString("prettyName");
        Log.d("ServerWorker", "got type " + type);
        try {
            switch(type) {
                case GET_TOKEN:
                    Response<MyServer.TokenResponse> res1 = ServerHolder.getInstance().server
                            .getToken(usernameOrToken).execute();
                    Log.d("ServerWorker", "got response: " + res1.code());
                    if (res1.code() != 200 || !res1.isSuccessful()) {
                        Log.e("ServerWorker", "no ok response from server");
                        return Result.failure();
                    }
                    MyServer.TokenResponse result = res1.body();
                    if (result == null) {
                        Log.e("ServerWorker", "no result");
                        return Result.failure();
                    }
                    Log.d("ServerWorker", "got response with result" + result.data);
                    outputData = new Data.Builder().putString("token", result.data).build();
                    break;
                case GET_INFO:
                    Response<MyServer.UserResponse> res2 = ServerHolder.getInstance().server
                            .getUserInfo("token " + usernameOrToken).execute();
                    Log.d("ServerWorker", "got response: " + res2.code());
                    if (res2.code() != 200 || !res2.isSuccessful()) {
                        Log.e("ServerWorker", "no ok response from server");
                        return Result.failure();
                    }
                    MyServer.UserResponse result2 = res2.body();
                    if (result2 == null) {
                        Log.e("ServerWorker", "no result");
                        return Result.failure();
                    }
                    Log.d("ServerWorker", "got response with result" + result2.data);
                    outputData = new Data.Builder()
                            .putString("username", result2.data.username)
                            .putString("image_url", ServerHolder.baseURL + result2.data.image_url)
                            .putString("pretty_name", result2.data.pretty_name)
                            .build();
                    break;
                case SET_PRETTY_NAME:
                    Response<MyServer.UserResponse> res3 = ServerHolder.getInstance().server
                            .setUserPrettyName("token " +usernameOrToken,
                            new MyServer.SetUserPrettyNameRequest(prettyName)).execute();
                    Log.d("ServerWorker", "got response: " + res3.code());
                    if (res3.code() != 200 || !res3.isSuccessful()) {
                        Log.e("ServerWorker", "no ok response from server");
                        return Result.failure();
                    }
                    MyServer.UserResponse result3 = res3.body();
                    if (result3 == null) {
                        Log.e("ServerWorker", "no result");
                        return Result.failure();
                    }
                    Log.d("ServerWorker", "got response with result" + result3.data);
                    outputData = new Data.Builder()
                            .putString("username", result3.data.username)
                            .putString("image_url", ServerHolder.baseURL + result3.data.image_url)
                            .putString("pretty_name", result3.data.pretty_name)
                            .build();
                    break;
            }
        } catch (Exception e) {
            return Result.failure();
        }
        return Result.success(outputData);
    }
}
