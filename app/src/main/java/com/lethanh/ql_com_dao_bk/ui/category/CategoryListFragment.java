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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentListBinding;
import com.lethanh.ql_com_dao_bk.model.Category;
import com.lethanh.ql_com_dao_bk.ui.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryListFragment extends Fragment {

    private FragmentListBinding binding;
    private GenericAdapter<Category> adapter;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GenericAdapter<>(categoryList, (category, holder) -> {
            holder.title.setText(category.getLabel());
            holder.subtitle.setText(category.getDescription());
            holder.image.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                if (category.getId() != null) {
                    args.putInt("category_id", category.getId());
                    Navigation.findNavController(v).navigate(R.id.nav_category_detail, args);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (category.getId() != null) {
                    confirmDeleteCategory(category);
                }
            });
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.fetchCategories());

        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            categoryList.clear();
            categoryList.addAll(categories);
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

        if (categoryList.isEmpty()) {
            viewModel.fetchCategories();
        }
    }

    private void fetchCategories() {
        viewModel.fetchCategories();
    }

    private void confirmDeleteCategory(Category category) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá danh mục \"" + category.getLabel() + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteCategory(Category category) {
        if (category.getId() == null) return;

        String jwt = com.lethanh.ql_com_dao_bk.utils.TokenManager.getJwt(requireContext());
        String authHeader = jwt != null ? "Bearer " + jwt : "";

        // 1. Permanent Local Hide
        com.lethanh.ql_com_dao_bk.utils.LocalHideManager.hideCategory(requireContext(), category.getId());

        // 2. Immediate UI Update
        viewModel.deleteCategoryLocally(category.getId());

        // 3. Silent API Call
        RetrofitClient.getApiService().deleteCategory(authHeader, category.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
            }
        });

        Toast.makeText(getContext(), "Đã ẩn danh mục (Local)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}