package com.lethanh.ql_com_dao_bk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.lethanh.ql_com_dao_bk.model.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductViewModel extends ViewModel {
    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<List<Product>> products = _products;
    public LiveData<Boolean> isLoading = _isLoading;
    public LiveData<String> error = _error;

    public void fetchProducts() {
        _isLoading.setValue(true);
        RetrofitClient.getApiService().getProducts("", 100, 0).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _products.setValue(response.body().getContent());
                } else {
                    _error.setValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Lỗi: " + t.getMessage());
            }
        });
    }

    public void addProductLocally(Product product) {
        List<Product> currentList = _products.getValue();
        if (currentList != null) {
            List<Product> newList = new ArrayList<>(currentList);
            newList.add(0, product); // Add to top
            _products.setValue(newList);
        }
    }

    public void deleteProductLocally(int productId) {
        List<Product> currentList = _products.getValue();
        if (currentList != null) {
            List<Product> newList = new ArrayList<>();
            for (Product p : currentList) {
                if (p.getId() != null && p.getId() != productId) {
                    newList.add(p);
                }
            }
            _products.setValue(newList);
        }
    }
}
