package com.lethanh.ql_com_dao_bk.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.databinding.FragmentMenuListBinding;

public class ProductFragment extends Fragment {

    private FragmentMenuListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addButton(getString(R.string.view_product), v -> Navigation.findNavController(v).navigate(R.id.nav_product_list));
        addButton(getString(R.string.add_product), v -> Navigation.findNavController(v).navigate(R.id.nav_product_add));
        addButton(getString(R.string.edit_product), v -> Navigation.findNavController(v).navigate(R.id.nav_product_edit));
        addButton(getString(R.string.delete_product), v -> Navigation.findNavController(v).navigate(R.id.nav_product_delete));
    }

    private void addButton(String text, View.OnClickListener listener) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        button.setLayoutParams(params);
        binding.buttonContainer.addView(button);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}