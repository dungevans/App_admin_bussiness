package com.lethanh.ql_com_dao_bk.ui.product;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentProductFormBinding;
import com.lethanh.ql_com_dao_bk.model.Product;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAddFragment extends Fragment {

    protected FragmentProductFormBinding binding;
    protected Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivProductPreview.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.btnSubmit.setOnClickListener(v -> submitProduct());
    }

    protected void submitProduct() {
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

        Product product = new Product(label, desc, Double.parseDouble(priceStr), currency, unit, badge, null);
        product.setRetrievable(binding.cbRetrievable.isChecked());

        Gson gson = new Gson();
        String json = gson.toJson(product);
        RequestBody dataPart = RequestBody.create(MediaType.parse("application/json"), json);

        MultipartBody.Part filePart = getMultipartFromUri(selectedImageUri);

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        if (jwt == null) return;
        String authHeader = "Bearer " + jwt;

        RetrofitClient.getApiService().addProduct(authHeader, dataPart, filePart).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    if (getContext() != null) {
                        Product savedProduct = response.body() != null ? response.body() : product;
                        ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
                        viewModel.addProductLocally(savedProduct);

                        Toast.makeText(getContext(), "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
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

    protected MultipartBody.Part getMultipartFromUri(Uri uri) {
        if (uri == null) {
            // Create an empty part if no file is selected, or handle as needed
            RequestBody emptyBody = RequestBody.create(MediaType.parse("image/*"), new byte[0]);
            return MultipartBody.Part.createFormData("file", "", emptyBody);
        }
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            byte[] bytes = getBytes(inputStream);
            String mimeType = requireContext().getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            return MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}