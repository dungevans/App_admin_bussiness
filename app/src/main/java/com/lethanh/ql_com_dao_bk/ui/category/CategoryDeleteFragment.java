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

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentCategoryDeleteBinding;
import com.lethanh.ql_com_dao_bk.model.Category;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryDeleteFragment extends Fragment {

    private FragmentCategoryDeleteBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryDeleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnDeleteCategory.setOnClickListener(v -> {
            String idStr = binding.etDeleteCategoryId.getText().toString();
            if (idStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập ID danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "ID phải là số", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Delete locally from ViewModel immediately
            CategoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
            viewModel.deleteCategoryLocally(id);

            // 2. Silently try to delete from server
            RetrofitClient.getApiService().deleteCategory(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // We don't care about the result as per user request
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // We don't care about the result as per user request
                }
            });

            Toast.makeText(getContext(), "Đã xoá danh mục khỏi ứng dụng", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}