package com.lethanh.ql_com_dao_bk.ui.product;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.model.Product;

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
                    confirmDelete(product);
                }
            }
        });

        if (getArguments() != null) {
            if (getArguments().containsKey("product_json")) {
                Product product = new Gson().fromJson(getArguments().getString("product_json"), Product.class);
                populateFields(product);
            } else if (getArguments().containsKey("product_id")) {
                int productId = getArguments().getInt("product_id");
                binding.etId.setText(String.valueOf(productId));
                fetchProductDetails(productId);
            }
        }
    }

    private void fetchProductDetails(int id) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt == null) return;
        String authHeader = "Bearer " + jwt;

        RetrofitClient.getApiService().getProducts(authHeader, 0, 10, null, id).enqueue(new Callback<com.lethanh.ql_com_dao_bk.model.ProductResponse>() {
            @Override
            public void onResponse(Call<com.lethanh.ql_com_dao_bk.model.ProductResponse> call, Response<com.lethanh.ql_com_dao_bk.model.ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Product p : response.body().getContent()) {
                        if (p.getId() != null && p.getId() == id) {
                            populateFields(p);
                            // Store the fetched product in arguments for submitProduct()
                            getArguments().putString("product_json", new Gson().toJson(p));
                            return;
                        }
                    }
                }
                if (getContext() != null) {
                    System.out.println("Fetch Product");
                    Toast.makeText(getContext(), "Không tìm thấy sản phẩm trên server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.lethanh.ql_com_dao_bk.model.ProductResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void confirmDelete(Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá sản phẩm này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProductFromServer(product))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteProductFromServer(Product product) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        // 1. Permanent Local Hide
        com.lethanh.ql_com_dao_bk.utils.LocalHideManager.hideProduct(requireContext(), product.getId());

        // 2. Immediate UI Update
        ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        viewModel.deleteProductLocally(product.getId());

        // 3. Silent API Call
        RetrofitClient.getApiService().deleteProduct(authHeader, product.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });

        Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    protected void submitProduct() {
        Product product = null;
        if (getArguments() != null && getArguments().containsKey("product_json")) {
            product = new Gson().fromJson(getArguments().getString("product_json"), Product.class);
        }

        if (product == null) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin sản phẩm để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        String label = binding.etLabel.getText().toString();
        String desc = binding.etDescription.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String currency = binding.etCurrency.getText().toString();
        String unit = binding.etUnit.getText().toString();
        String badge = binding.etBadge.getText().toString();

        if (label.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nhãn và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        product.setLabel(label);
        product.setDescription(desc);
        product.setPrice(Double.parseDouble(priceStr));
        product.setCurrency(currency);
        product.setUnit(unit);
        product.setBadge(badge);
        product.setRetrievable(binding.cbRetrievable.isChecked());

        Gson gson = new Gson();
        String json = gson.toJson(product);
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), json);

        MultipartBody.Part filePart = getMultipartFromUri(selectedImageUri);

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt == null) return;
        String authHeader = "Bearer " + jwt;

        RetrofitClient.getApiService().updateProduct(authHeader, dataPart, filePart).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (getContext() == null) return;

                if (response.isSuccessful()) {
                    ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
                    viewModel.fetchProducts(jwt);
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
            public void onFailure(Call<Product> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
