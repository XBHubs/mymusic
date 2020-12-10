package com.example.mymusic.http;

import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.Callback;
import okhttp3.Response;
import java.io.IOException;

/*
Json封装
 */
public abstract class JsonCallback<T> extends Callback<T> {
    private Class<T> clazz;
    private Gson gson;

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
        gson = new Gson();
    }

    @Override
    public T parseNetworkResponse(Response response, int id) throws IOException {
        try {
            String jsonString = response.body().string();
            return gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
