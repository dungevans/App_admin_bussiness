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
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        // 1. Try Admin with ID query param
        RetrofitClient.getApiService().getProductAdmin(authHeader, id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onFetchSuccess(response.body());
                } else {
                    // 2. Try Admin with Path variable
                    fetchProductDetailsPath(id, authHeader);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                fetchProductDetailsPath(id, authHeader);
            }
        });
    }

    private void fetchProductDetailsPath(int id, String authHeader) {
        RetrofitClient.getApiService().getProductAdminPath(authHeader, id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onFetchSuccess(response.body());
                } else {
                    // 3. Try Public with ID query param
                    fetchProductDetailsPublic(id);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                fetchProductDetailsPublic(id);
            }
        });
    }

    private void fetchProductDetailsPublic(int id) {
        RetrofitClient.getApiService().getProduct(id).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onFetchSuccess(response.body());
                } else {
                    // 4. Final attempt: Try searching in the list wrapper
                    fetchProductInList(id);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                fetchProductInList(id);
            }
        });
    }

    private void fetchProductInList(int id) {
        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        RetrofitClient.getApiService().getProducts(authHeader, 0, 10, "", id).enqueue(new Callback<com.lethanh.ql_com_dao_bk.model.ProductResponse>() {
            @Override
            public void onResponse(Call<com.lethanh.ql_com_dao_bk.model.ProductResponse> call, Response<com.lethanh.ql_com_dao_bk.model.ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getContent() != null) {
                    for (Product p : response.body().getContent()) {
                        if (p.getId() != null && p.getId() == id) {
                            onFetchSuccess(p);
                            return;
                        }
                    }
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không tìm thấy sản phẩm ID: " + id, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.lethanh.ql_com_dao_bk.model.ProductResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onFetchSuccess(Product p) {
        populateFields(p);
        updateArguments(p);
    }

    private void updateArguments(Product p) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
            setArguments(args);
        }
        args.putString("product_json", new Gson().toJson(p));
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
            String idStr = binding.etId.getText().toString().trim();
            if (!idStr.isEmpty()) {
                product = new Product();
                product.setId(idStr);
            }
        }

        if (product == null || product.getId() == null) {
            Toast.makeText(getContext(), "Vui lòng nhập ID sản phẩm hợp lệ", Toast.LENGTH_SHORT).show();
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

        // Create a clean map for update to match Documentation 3.4 exactly
        java.util.Map<String, Object> updateMap = new java.util.HashMap<>();
        updateMap.put("id", product.getId());
        updateMap.put("label", label);
        updateMap.put("description", desc);
        updateMap.put("price", product.getPrice());
        updateMap.put("unit", unit);
        updateMap.put("badge", badge);
        updateMap.put("retrievable", product.isRetrievable());

        Gson gson = new Gson();
        String json = gson.toJson(updateMap);
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
