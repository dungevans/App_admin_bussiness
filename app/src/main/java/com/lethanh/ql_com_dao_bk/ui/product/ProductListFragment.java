package com.lethanh.ql_com_dao_bk.ui.product;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentListBinding;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.lethanh.ql_com_dao_bk.model.ProductResponse;
import com.lethanh.ql_com_dao_bk.ui.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListFragment extends Fragment {

    private FragmentListBinding binding;
    private GenericAdapter<Product> adapter;
    private List<Product> productList = new ArrayList<>();
    private ProductViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GenericAdapter<>(productList, (product, holder) -> {
            holder.title.setText(product.getLabel());
            String priceStr = String.format("%.0f %s/%s", product.getPrice(), product.getCurrency(), product.getUnit());
            holder.subtitle.setText(priceStr + "\n" + product.getDescription());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(this).load(product.getImageUrl()).into(holder.image);
            } else {
                holder.image.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                if (product.getId() != null) {
                    args.putString("product_json", new com.google.gson.Gson().toJson(product));
                    Navigation.findNavController(v).navigate(R.id.nav_product_edit, args);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (product.getId() != null) {
                    confirmDeleteProduct(product);
                }
            });
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.fetchProducts());

        viewModel.products.observe(getViewLifecycleOwner(), products -> {
            productList.clear();
            productList.addAll(products);
            adapter.notifyDataSetChanged();
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        if (productList.isEmpty()) {
            viewModel.fetchProducts();
        }
    }

    private void fetchProducts() {
        viewModel.fetchProducts();
    }

    private void confirmDeleteProduct(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá sản phẩm \"" + product.getLabel() + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteProduct(Product product) {
        if (product.getId() == null) return;

        // 1. Delete locally immediately
        viewModel.deleteProductLocally(product.getId());

        // 2. Call API
        RetrofitClient.getApiService().deleteProduct(product.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi server khi xoá: " + response.code(), Toast.LENGTH_SHORT).show();
                    // Optional: re-fetch if we want to be sure
                    viewModel.fetchProducts();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối khi xoá", Toast.LENGTH_SHORT).show();
                viewModel.fetchProducts();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}