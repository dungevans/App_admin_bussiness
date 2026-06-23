package com.lethanh.ql_com_dao_bk.ui.category;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.model.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryViewModel extends ViewModel {
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Category>> categories = _categories;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public void fetchCategories() {
        _isLoading.setValue(true);
        RetrofitClient.getApiService().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _categories.setValue(response.body());
                } else {
                    _error.setValue("Lỗi server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Lỗi: " + t.getMessage());
            }
        });
    }

    public void addCategoryLocally(Category category) {
        List<Category> currentList = _categories.getValue();
        if (currentList != null) {
            List<Category> newList = new ArrayList<>(currentList);
            newList.add(0, category);
            _categories.setValue(newList);
        }
    }

    public void deleteCategoryLocally(int categoryId) {
        List<Category> currentList = _categories.getValue();
        if (currentList != null) {
            List<Category> newList = new ArrayList<>();
            for (Category c : currentList) {
                if (c.getId() != null && c.getId() != categoryId) {
                    newList.add(c);
                }
            }
            _categories.setValue(newList);
        }
    }
}
