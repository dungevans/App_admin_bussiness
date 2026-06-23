package com.lethanh.ql_com_dao_bk.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static String BASE_URL = "http://your-api-url.com/";
    private static Retrofit retrofit = null;
    private static String authToken = null;

    public static void setBaseUrl(String newUrl) {
        if (newUrl == null || newUrl.isEmpty()) return;

        String url = newUrl.trim();
        // Remove trailing slashes first for consistent processing
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        url = url + "/"; // Add exactly one trailing slash

        BASE_URL = url;
        retrofit = null;
    }

    public static void setAuthToken(String token) {
        authToken = token;
        retrofit = null; // Rebuild to include the new token in the interceptor
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            // Add Authorization header if token exists
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }

                return chain.proceed(requestBuilder.build());
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
