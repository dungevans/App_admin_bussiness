package com.lethanh.ql_com_dao_bk.ui.product;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.model.Product;
import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductEditFragment extends ProductAddFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etId.setVisibility(View.VISIBLE);
        binding.btnSubmit.setText(R.string.btn_save);
        binding.btnDeleteProduct.setVisibility(View.VISIBLE);
        binding.btnDeleteProduct.setOnClickListener(v -> {
            if (getArguments() != null && getArguments().containsKey("product_json")) {
                Product product = new Gson().fromJson(getArguments().getString("product_json"), Product.class);
                if (product.getId() != null) {
                    confirmDelete(product.getId());
                }
            }
        });

        if (getArguments() != null && getArguments().containsKey("product_json")) {
            Product product = new Gson().fromJson(getArguments().getString("product_json"), Product.class);
            populateFields(product);
        }
    }

    private void populateFields(Product product) {
        binding.etId.setText(String.valueOf(product.getId()));
        binding.etLabel.setText(product.getLabel());
        binding.etDescription.setText(product.getDescription());
        binding.etPrice.setText(String.valueOf(product.getPrice()));
        binding.etCurrency.setText(product.getCurrency());
        binding.etUnit.setText(product.getUnit());
        binding.etBadge.setText(product.getBadge());
        binding.cbRetrievable.setChecked(product.isRetrievable());
        
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(product.getImageUrl()).into(binding.ivProductPreview);
        }
    }

    private void confirmDelete(int productId) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá sản phẩm này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProductFromServer(productId))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteProductFromServer(int productId) {
        ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        viewModel.deleteProductLocally(productId);

        RetrofitClient.getApiService().deleteProduct(productId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xoá sản phẩm", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi xoá", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            }
        });
    }

    @Override
    protected void submitProduct() {
        String idStr = binding.etId.getText().toString().trim();
        String label = binding.etLabel.getText().toString();
        String desc = binding.etDescription.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String currency = binding.etCurrency.getText().toString();
        String unit = binding.etUnit.getText().toString();
        String badge = binding.etBadge.getText().toString();

        if (idStr.isEmpty() || label.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập ID, nhãn và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(label, desc, Double.parseDouble(priceStr), currency, unit, badge, null);
        product.setId(idStr);
        product.setRetrievable(binding.cbRetrievable.isChecked());

        Gson gson = new Gson();
        String json = gson.toJson(product);
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), json);

        MultipartBody.Part filePart = getMultipartFromUri(selectedImageUri);

        RetrofitClient.getApiService().updateProduct(dataPart, filePart).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    if (getContext() != null) {
                        ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
                        viewModel.fetchProducts();

                        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
