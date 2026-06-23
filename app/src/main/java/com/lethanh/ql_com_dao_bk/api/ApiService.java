package com.lethanh.ql_com_dao_bk.api;

import com.lethanh.ql_com_dao_bk.model.Category;
import com.lethanh.ql_com_dao_bk.model.LoginRequest;
import com.lethanh.ql_com_dao_bk.model.LoginResponse;
import com.lethanh.ql_com_dao_bk.model.Notice;
import com.lethanh.ql_com_dao_bk.model.NoticeResponse;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.lethanh.ql_com_dao_bk.model.ProductResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/v1/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/v1/notice")
    Call<NoticeResponse> getNotices();

    @PUT("api/v1/notice")
    Call<Notice> addNotice(@Body Notice notice);

    @GET("api/v1/product/view/admin")
    Call<ProductResponse> getProducts(
            @Query("filter_content") String filterContent,
            @Query("size") Integer size,
            @Query("page") Integer page
    );

    @Multipart
    @POST("api/v1/product/create")
    Call<Product> addProduct(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part file
    );

    @Multipart
    @PUT("api/v1/product/update")
    Call<Product> updateProduct(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part file
    );

    @DELETE("api/v1/product/delete")
    Call<Void> deleteProduct(@Query("id") int id);

    @GET("api/v1/category/view/all")
    Call<List<Category>> getCategories();

    @GET("api/v1/category/view")
    Call<Category> getCategory(@Query("id") int id);

    @GET("api/v1/category/view/admin")
    Call<Category> getCategoryAdmin(@Query("id") int id);

    @GET("api/v1/category/view/admin/{id}")
    Call<Category> getCategoryAdminPath(@Path("id") int id);

    @POST("api/v1/category/create")
    Call<Category> addCategory(@Body Category category);

    @PUT("api/v1/category/update")
    Call<Category> updateCategory(@Body Category category);

    @DELETE("api/v1/category/delete")
    Call<Void> deleteCategory(@Query("id") int id);

    @GET("api/v1/order")
    Call<List<Object>> getOrders();
}