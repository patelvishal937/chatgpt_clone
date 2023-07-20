package com.example.chatgpt_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.reflect.KCallable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextView welcomeTextview;
    RecyclerView recycler_View;
    EditText message_edit_text;
    ImageButton send_btn;
    List<Chatgpt_messege> messageList;
    chatgpt_MessageAdapter chatgpt_MessageAdapter;
    String question;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();
        welcomeTextview = (TextView)findViewById(R.id.welcome_text);
        recycler_View = (RecyclerView)findViewById(R.id.recycler_view);
        message_edit_text = (EditText)findViewById(R.id.message_edit_text);
        send_btn = (ImageButton)findViewById(R.id.send_btn);

        //setup recycler view

        chatgpt_MessageAdapter = new chatgpt_MessageAdapter(messageList);
        recycler_View.setAdapter(chatgpt_MessageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recycler_View.setLayoutManager(llm);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               question = message_edit_text.getText().toString().trim();
               addTochat(question,Chatgpt_messege.SENT_BY_ME);
               message_edit_text.setText("");
               welcomeTextview.setVisibility(view.GONE);
               callAPI(question);

            }
        });
    }
    void addTochat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Chatgpt_messege(message, sentBy));
                chatgpt_MessageAdapter.notifyDataSetChanged();
                recycler_View.smoothScrollToPosition(chatgpt_MessageAdapter.getItemCount());

            }
        });
    }
    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addTochat(response,Chatgpt_messege.SENT_BY_BOT);
    }
    void callAPI(String question){
        //okhttp
        messageList.add(new Chatgpt_messege("Typing... ",Chatgpt_messege.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","text-davinci-003");
            jsonBody.put("prompt",question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject  jsonObject = null;
                    try {
                        String responseBody = response.body().string();
                        jsonObject = new JSONObject(responseBody);
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }else{
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown Error";
                    addResponse("Failed to load response due to: " + errorResponse);
                }
            }
        });
    }
}