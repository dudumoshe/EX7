package com.example.ex7;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private String token;
    private SharedPreferences sp;
    public static final String SP_DATA = "UserDetailsEx7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences(SP_DATA, MODE_PRIVATE);
        String username = sp.getString("username", null);
        if (username != null) {
            showViewsAfterLogin();
            getToken(username);
        }

        findViewById(R.id.set_username).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username_view = findViewById(R.id.edit_username);
                String username = username_view.getText().toString();
                if (username.isEmpty() || !username.matches("^[a-zA-Z0-9]+$")) {
                    showErrorAlert("Invalid Username! Only letters and number please");
                } else {
                    sp.edit().putString("username", username).apply();
                    showViewsAfterLogin();
                    getToken(username);
                }
            }
        });

        findViewById(R.id.set_pretty_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editPrettyName = findViewById(R.id.edit_pretty_name);
                String prettyName = editPrettyName.getText().toString();
                TextView userMsg = findViewById(R.id.user_msg);
                userMsg.setText("Loading...");
                setUserPrettyName(MainActivity.this.token, prettyName);
            }
        });
    }

    private void showViewsAfterLogin() {
        findViewById(R.id.edit_username).setVisibility(View.GONE);
        findViewById(R.id.set_username).setVisibility(View.GONE);
        findViewById(R.id.user_msg).setVisibility(View.VISIBLE);
        findViewById(R.id.user_img).setVisibility(View.VISIBLE);
        findViewById(R.id.edit_pretty_name).setVisibility(View.VISIBLE);
        findViewById(R.id.set_pretty_name).setVisibility(View.VISIBLE);
        TextView userMsg = findViewById(R.id.user_msg);
        userMsg.setText("Loading...");
    }

    private void showErrorAlert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    private void getToken(String username) {
        requestFromServer(ServerWorker.GET_TOKEN, username, null);
    }

    private void getUser(String token) {
        requestFromServer(ServerWorker.GET_INFO, token, null);
    }

    private void setUserPrettyName(String token, String prettyName) {
        requestFromServer(ServerWorker.SET_PRETTY_NAME, token, prettyName);
    }

    private void requestFromServer(int type, String param, String extraParam) {
        Data data = null;
        switch (type) {
            case ServerWorker.GET_TOKEN:
                data = new Data.Builder()
                        .putInt("type", ServerWorker.GET_TOKEN).putString("usernameOrToken", param)
                        .build();
                break;
            case ServerWorker.GET_INFO:
                data = new Data.Builder().putInt("type", ServerWorker.GET_INFO)
                        .putString("usernameOrToken", param)
                        .build();
                break;
            case ServerWorker.SET_PRETTY_NAME:
                data = new Data.Builder().putInt("type", ServerWorker.SET_PRETTY_NAME)
                        .putString("usernameOrToken", param)
                        .putString("prettyName", extraParam)
                        .build();
                break;
            default:
                Log.e("Main Activity", "Undefined request");
                return;
        }
        requestHelper(type, data);
    }

    private void requestHelper(final int type, Data data) {
        final WorkRequest workerRequest = new OneTimeWorkRequest.Builder(ServerWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(data)
                .build();
        WorkManager.getInstance(this).enqueue(workerRequest);
        Log.d("MainActivity", "did enqueue worker of type ServerWorker");
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo == null || workInfo.getState() == WorkInfo.State.FAILED) {
                            Log.e("MainActivity", "failed");
                            showErrorAlert("Couldn't load data from server!");
                            return;
                        }
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.d("MainActivity", "success getting data from server");
                            if (type == ServerWorker.GET_TOKEN) {
                                String token = workInfo.getOutputData().getString("token");
                                MainActivity.this.token = token;
                                Log.d("MainActivity", "token is " + token);
                                getUser(token);
                            } else {
                                setUIByUserData(workInfo.getOutputData());
                            }
                        }
                    }
                });
    }

    private void setUIByUserData(Data userData) {
        String username = userData.getString("username");
        String prettyName = userData.getString("pretty_name");
        String imgURL = userData.getString("image_url");
        Log.d("MainActivity", "username is " + username);
        Log.d("Main Activity", "image url is " + imgURL);
        TextView userMsg = MainActivity.this.findViewById(R.id.user_msg);
        if (prettyName == null || prettyName.isEmpty()) {
            userMsg.setText("Welcome, " + username+"!");
        } else {
            userMsg.setText("Welcome again, " + prettyName+"!");
        }
        ImageView userImg = findViewById(R.id.user_img);
        Picasso.with(MainActivity.this).load(imgURL).resize(200, 200).into(userImg);
    }
}
