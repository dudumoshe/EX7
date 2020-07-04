package com.example.ex7;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyServer {

    public class TokenResponse {
        public String data;
    }

    public class UserResponse {
        public User data;
    }

    public class SetUserPrettyNameRequest {
        public String pretty_name;

        public SetUserPrettyNameRequest(String pretty_name) {
            this.pretty_name = pretty_name;
        }
    }

    @GET("/users/{username}/token/")
    public Call<TokenResponse> getToken(@Path("username") String username);

    @GET("/user/")
    public Call<UserResponse> getUserInfo(@Header("Authorization") String token);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/")
    public Call<UserResponse> setUserPrettyName(@Header("Authorization") String token,
                                                @Body SetUserPrettyNameRequest request);
}
