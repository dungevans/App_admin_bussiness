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

import com.bumptech.glide.Glide;
import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentListBinding;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.lethanh.ql_com_dao_bk.ui.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

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

        binding.swipeRefresh.setOnRefreshListener(this::fetchProducts);

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
            fetchProducts();
        }
    }

    private void fetchProducts() {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt != null) {
            viewModel.fetchProducts(jwt);
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
        }
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

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : null;

        // 1. Permanent Local Hide (Even after app restart)
        com.lethanh.ql_com_dao_bk.utils.LocalHideManager.hideProduct(requireContext(), product.getId());

        // 2. Immediate UI Update
        viewModel.deleteProductLocally(product.getId());

        // 3. Silent API Call (We don't care if it fails with 500)
        RetrofitClient.getApiService().deleteProduct(authHeader, product.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                // Background success/fail doesn't affect local app state anymore
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                // Background success/fail doesn't affect local app state anymore
            }
        });

        Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}