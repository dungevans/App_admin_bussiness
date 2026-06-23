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

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentProductDeleteBinding;
import com.lethanh.ql_com_dao_bk.model.Product;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDeleteFragment extends Fragment {

    private FragmentProductDeleteBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDeleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnDeleteProduct.setOnClickListener(v -> {
            String idStr = binding.etDeleteId.getText().toString();
            if (idStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập ID sản phẩm", Toast.LENGTH_SHORT).show();
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
            ProductViewModel viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
            viewModel.deleteProductLocally(id);

            // 2. Silently try to delete from server
            RetrofitClient.getApiService().deleteProduct(id).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });

            Toast.makeText(getContext(), "Đã xoá sản phẩm khỏi ứng dụng", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}