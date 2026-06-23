package com.lethanh.ql_com_dao_bk.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lethanh.ql_com_dao_bk.model.Notice;
import com.lethanh.ql_com_dao_bk.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class StompClientManager {
    private static final String TAG = "StompClientManager";
    private static StompClientManager instance;
    private StompClient mStompClient;
    private CompositeDisposable compositeDisposable;
    private final Gson gson = new Gson();
    private Context context;

    private StompClientManager() {}

    public static synchronized StompClientManager getInstance() {
        if (instance == null) {
            instance = new StompClientManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public void connect(String baseUrl, String token) {
        if (mStompClient != null && mStompClient.isConnected()) {
            return;
        }

        // Docs: const socket = new SockJS(`${BASE_URL}$/ws`);
        String url = baseUrl;
        if (!url.endsWith("/")) url += "/";
        url += "ws/websocket"; // SockJS transport fallback

        String wsUrl = url.replace("http", "ws");
        Log.d(TAG, "Connecting to WebSocket: " + wsUrl);

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", "Bearer " + token));

        compositeDisposable = new CompositeDisposable();

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            subscribeToTopics();
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d(TAG, "Stomp connection closed");
                            break;
                    }
                }, throwable -> Log.e(TAG, "Error in lifecycle", throwable));

        mStompClient.connect(headers);
    }

    private void subscribeToTopics() {
        // 1. Notification when order is paid: /user/topic/order
        compositeDisposable.add(mStompClient.topic("/user/topic/order")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Order Message: " + topicMessage.getPayload());
                    Notice notice = gson.fromJson(topicMessage.getPayload(), Notice.class);
                    if (context != null) {
                        NotificationHelper.showNotification(context, notice.getTitle(), notice.getContent());
                    }
                }, throwable -> Log.e(TAG, "Error on /user/topic/order", throwable)));

        // 2. Notification of payment status: /user/topic/pay
        compositeDisposable.add(mStompClient.topic("/user/topic/pay")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Payment status: " + topicMessage.getPayload());
                    JsonObject json = gson.fromJson(topicMessage.getPayload(), JsonObject.class);
                    boolean isEnough = json.get("is_enough").getAsBoolean();
                    double unpaid = json.get("unpaid_amount").getAsDouble();
                    String msg = isEnough ? "Thanh toán thành công" : "Còn thiếu: " + unpaid + " VND";
                    if (context != null) {
                        NotificationHelper.showNotification(context, "Trạng thái thanh toán", msg);
                    }
                }, throwable -> Log.e(TAG, "Error on /user/topic/pay", throwable)));

        // 3. Notification from admin: /topic/global
        compositeDisposable.add(mStompClient.topic("/topic/global")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Global Message: " + topicMessage.getPayload());
                    Notice notice = gson.fromJson(topicMessage.getPayload(), Notice.class);
                    if (context != null) {
                        NotificationHelper.showNotification(context, notice.getTitle(), notice.getContent());
                    }
                }, throwable -> Log.e(TAG, "Error on /topic/global", throwable)));
    }

    public void sendGlobalMessage(Notice notice) {
        if (mStompClient == null || !mStompClient.isConnected()) {
            Log.e(TAG, "Cannot send: Stomp client not connected");
            return;
        }

        // Docs: ADMIN sending channel: /app/global-message
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SYSTEM");
        payload.put("title", notice.getTitle());
        payload.put("summary", notice.getSummary());
        payload.put("content", notice.getContent());

        String jsonPayload = gson.toJson(payload);

        mStompClient.send("/app/global-message", jsonPayload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d(TAG, "Global message sent successfully"),
                        throwable -> Log.e(TAG, "Error sending global message", throwable));
    }

    public void disconnect() {
        if (mStompClient != null) {
            mStompClient.disconnect();
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}
