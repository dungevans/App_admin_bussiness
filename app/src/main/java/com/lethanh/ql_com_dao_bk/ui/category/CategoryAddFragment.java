package com.lethanh.ql_com_dao_bk.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentCategoryAddBinding;
import com.lethanh.ql_com_dao_bk.model.Category;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAddFragment extends Fragment {

    protected FragmentCategoryAddBinding binding;
    protected List<EditText> productIdFields = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add first ID field
        addIdField();

        binding.btnAddIdField.setOnClickListener(v -> addIdField());

        binding.btnSubmitCategory.setOnClickListener(v -> submitData());
    }

    protected void addIdField() {
        addIdField(null);
    }

    protected void addIdField(String initialValue) {
        EditText editText = new EditText(getContext());
        editText.setHint("ID sản phẩm " + (productIdFields.size() + 1));
        editText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (initialValue != null) {
            editText.setText(initialValue);
        }
        binding.llProductIdsContainer.addView(editText);
        productIdFields.add(editText);
    }

    protected void submitData() {
        String label = binding.etCategoryLabel.getText().toString();
        String desc = binding.etCategoryDescription.getText().toString();

        if (label.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập nhãn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> productIds = new ArrayList<>();
        for (EditText et : productIdFields) {
            String idStr = et.getText().toString().trim();
            if (!idStr.isEmpty()) {
                try {
                    productIds.add(Integer.parseInt(idStr));
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "ID sản phẩm phải là số: " + idStr, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        Category category = new Category(label, desc, productIds);
        category.setBadge(binding.etCategoryBadge != null ? binding.etCategoryBadge.getText().toString() : null);
        RetrofitClient.getApiService().addCategory(category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    if (getContext() != null) {
                        Category savedCategory = response.body() != null ? response.body() : category;
                        CategoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
                        viewModel.addCategoryLocally(savedCategory);

                        Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}