package com.lethanh.ql_com_dao_bk.ui.category;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.model.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryEditFragment extends CategoryAddFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.etCategoryId.setVisibility(View.VISIBLE);
        binding.btnSubmitCategory.setText(R.string.btn_save);
        binding.btnDeleteCategory.setVisibility(View.VISIBLE);
        binding.btnDeleteCategory.setOnClickListener(v -> {
            if (getArguments() != null && getArguments().containsKey("category_id")) {
                confirmDelete(getArguments().getInt("category_id"));
            }
        });

        if (getArguments() != null && getArguments().containsKey("category_id")) {
            int categoryId = getArguments().getInt("category_id");
            fetchCategoryDetails(categoryId);
        }
    }

    private void confirmDelete(int categoryId) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá danh mục này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteCategoryFromServer(categoryId))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteCategoryFromServer(int categoryId) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        CategoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        viewModel.deleteCategoryLocally(categoryId);

        RetrofitClient.getApiService().deleteCategory(authHeader, categoryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xoá danh mục", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi xoá", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }
        });
    }

    private void fetchCategoryDetails(int id) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        // 1. Try Admin with ID query param
        RetrofitClient.getApiService().getCategoryAdmin(authHeader, id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                } else {
                    // 2. Try Admin with Path variable
                    fetchCategoryDetailsPath(id, authHeader);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                fetchCategoryDetailsPath(id, authHeader);
            }
        });
    }

    private void fetchCategoryDetailsPath(int id, String authHeader) {
        RetrofitClient.getApiService().getCategoryAdminPath(authHeader, id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                } else {
                    // 3. Try Public with ID query param
                    fetchCategoryDetailsPublic(id);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                fetchCategoryDetailsPublic(id);
            }
        });
    }

    private void fetchCategoryDetailsPublic(int id) {
        RetrofitClient.getApiService().getCategory(id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body());
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không tìm thấy danh mục ID: " + id, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateFields(Category category) {
        if (category.getId() != null) {
            binding.etCategoryId.setText(String.valueOf(category.getId()));
        }
        binding.etCategoryLabel.setText(category.getLabel());
        binding.etCategoryDescription.setText(category.getDescription());
        binding.etCategoryBadge.setText(category.getBadge());
        if (category.isRetrievable() != null) {
            binding.cbCategoryRetrievable.setChecked(category.isRetrievable());
        }

        // Clear existing product ID fields
        binding.llProductIdsContainer.removeAllViews();
        productIdFields.clear();

        if (category.getProducts() != null) {
            for (com.lethanh.ql_com_dao_bk.model.Product product : category.getProducts()) {
                if (product.getId() != null) {
                    addIdField(String.valueOf(product.getId()));
                }
            }
        }

        // Always have at least one empty field if no products
        if (productIdFields.isEmpty()) {
            addIdField();
        }
    }

    @Override
    protected void submitData() {
        String id = binding.etCategoryId.getText().toString().trim();
        String label = binding.etCategoryLabel.getText().toString();
        String desc = binding.etCategoryDescription.getText().toString();

        if (id.isEmpty() || label.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập ID và nhãn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> productIds = new ArrayList<>();
        for (EditText et : productIdFields) {
            String pIdStr = et.getText().toString().trim();
            if (!pIdStr.isEmpty()) {
                try {
                    productIds.add(Integer.parseInt(pIdStr));
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "ID sản phẩm phải là số: " + pIdStr, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Create a clean Category object for update to match Documentation 4.5 exactly
        Category category = new Category(label, desc, productIds);
        category.setId(id);
        category.setBadge(binding.etCategoryBadge.getText().toString());
        category.setRetrievable(binding.cbCategoryRetrievable.isChecked());
        category.setProducts(null); // Ensure the list of objects is not sent, only product_ids

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt == null) return;
        String authHeader = "Bearer " + jwt;

        RetrofitClient.getApiService().updateCategory(authHeader, category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (getContext() == null) return;

                if (response.isSuccessful()) {
                    CategoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
                    viewModel.fetchCategories();
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    String errorMsg = "Lỗi server: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (java.io.IOException ignored) {
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}