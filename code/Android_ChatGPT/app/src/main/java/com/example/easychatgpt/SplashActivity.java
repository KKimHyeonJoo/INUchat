package com.example.easychatgpt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SERVER_URL = "http://172.30.1.16:5000/chat";

    // 로딩 화면을 보여줄 시간(밀리초)
    private static final int LOADING_DELAY = 3000;

    private boolean responseReceived = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        // GIF를 로드하여 ImageView에 표시합니다.
        ImageView imageView = findViewById(R.id.loading_gif);
        Glide.with(this).load(R.drawable.loading).into(imageView);

        // 질문을 직접 설정합니다.
        String question = "2학기 기말고사가 언제야?";

        // 서버에 질문을 보내고 응답을 처리합니다.
        sendQuestionToServer(question);

        // 최소 3초 동안 로딩 화면을 유지하고, 그 후에 다음 화면으로 이동하는 작업을 예약합니다.
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (responseReceived) {
                    navigateToNextScreen();
                } else {
                    responseReceived = true;  // Mark the response as received to ensure no duplicate navigation
                }
            }
        }, LOADING_DELAY); // 최소 3초 유지
    }

    private void sendQuestionToServer(String question) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("question", question);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(10, TimeUnit.MINUTES) // 타임아웃 설정
                    .build();

            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e(TAG, "Failed to send question to server: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            // 서버로부터 받은 응답을 처리하는 함수 호출
                            processResponse(jsonResponse);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse JSON response: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Server responded with error: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON request: " + e.getMessage());
        }
    }

    private void processResponse(JSONObject response) {
        try {
            // 응답에서 "response" 키에 해당하는 문자열을 가져옵니다.
            String questionResponse = response.getString("response");

            // 응답을 로그에 출력합니다.
            Log.d(TAG, "Response: " + questionResponse);

            // 최소 3초가 지난 후 응답을 받았는지 확인하고 이동
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (responseReceived) {
                        navigateToNextScreen();
                    } else {
                        responseReceived = true;  // Mark the response as received to ensure no duplicate navigation
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Failed to process response: " + e.getMessage());
        }
    }

    private void navigateToNextScreen() {
        // 이동할 화면으로 인텐트를 생성합니다.
        Intent intent = new Intent(SplashActivity.this, SelectActivity.class);
        // 화면 전환
        startActivity(intent);
        // 현재 액티비티 종료
        finish();
    }
}
