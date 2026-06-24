package com.lethanh.ql_com_dao_bk.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentCategoryDetailBinding;
import com.lethanh.ql_com_dao_bk.model.Category;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.lethanh.ql_com_dao_bk.ui.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryDetailFragment extends Fragment {

    private FragmentCategoryDetailBinding binding;
    private GenericAdapter<Product> adapter;
    private List<Product> productList = new ArrayList<>();
    private int categoryId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            categoryId = getArguments().getInt("category_id");
            fetchCategoryDetails(categoryId);
        }

        adapter = new GenericAdapter<>(productList, (product, holder) -> {
            holder.title.setText(product.getLabel());
            String priceInfo = String.format("%.0f %s/%s", product.getPrice(), product.getCurrency(), product.getUnit());
            holder.subtitle.setText(priceInfo + "\n" + product.getDescription());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(this).load(product.getImageUrl()).into(holder.image);
            } else {
                holder.image.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("product_json", new com.google.gson.Gson().toJson(product));
                Navigation.findNavController(v).navigate(R.id.nav_product_edit, args);
            });
        });

        binding.rvCategoryProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCategoryProducts.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> fetchCategoryDetails(categoryId));

        binding.btnEditCategory.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("category_id", categoryId);
            Navigation.findNavController(v).navigate(R.id.nav_category_edit, args);
        });

        binding.btnDeleteCategory.setOnClickListener(v -> confirmDeleteCategory());
    }

    private void confirmDeleteCategory() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá danh mục này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteCategoryFromServer())
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteCategoryFromServer() {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        // 1. Permanent Local Hide
        com.lethanh.ql_com_dao_bk.utils.LocalHideManager.hideCategory(requireContext(), categoryId);

        // 2. Immediate UI Update
        CategoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        viewModel.deleteCategoryLocally(categoryId);

        // 3. Silent API Call
        RetrofitClient.getApiService().deleteCategory(authHeader, categoryId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });

        Toast.makeText(getContext(), "Đã ẩn danh mục (Local)", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }

    private void fetchCategoryDetails(int id) {
        if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(true);

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt == null) return;
        String authHeader = "Bearer " + jwt;

        // Try Admin endpoint first
        RetrofitClient.getApiService().getCategoryAdmin(authHeader, id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                    displayCategory(response.body());
                } else {
                    // Try Non-Admin endpoint
                    fetchCategoryDetailsNonAdmin(id);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                fetchCategoryDetailsNonAdmin(id);
            }
        });
    }

    private void fetchCategoryDetailsNonAdmin(int id) {
        RetrofitClient.getApiService().getCategory(id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                    displayCategory(response.body());
                } else {
                    // Try Path Variable format
                    fetchCategoryDetailsPath(id);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                fetchCategoryDetailsPath(id);
            }
        });
    }

    private void fetchCategoryDetailsPath(int id) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        RetrofitClient.getApiService().getCategoryAdminPath(authHeader, id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayCategory(response.body());
                } else {
                    String errorMsg = "Không tìm thấy thông tin danh mục ID: " + id;
                    if (getContext() != null) {
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayCategory(Category category) {
        binding.tvDetailLabel.setText(category.getLabel());
        binding.tvDetailDescription.setText(category.getDescription());

        productList.clear();
        if (category.getProducts() != null) {
            productList.addAll(category.getProducts());
        }

        if (productList.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Danh mục này chưa có sản phẩm", Toast.LENGTH_SHORT).show();
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
