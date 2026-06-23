package com.lethanh.ql_com_dao_bk.ui.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentNoticeAddBinding;
import com.lethanh.ql_com_dao_bk.model.Notice;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeAddFragment extends Fragment {

    private FragmentNoticeAddBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNoticeAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddNotice.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString();
            String summary = binding.etSummary.getText().toString();
            String content = binding.etContent.getText().toString();

            if (title.isEmpty() || summary.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Notice notice = new Notice(title, summary, content);
            
            // 1. Send via WebSocket (Push notification to all users)
            com.lethanh.ql_com_dao_bk.api.StompClientManager.getInstance().sendGlobalMessage(notice);

            // 2. Save to database via REST API
            RetrofitClient.getApiService().addNotice(notice).enqueue(new Callback<Notice>() {
                @Override
                public void onResponse(Call<Notice> call, Response<Notice> response) {
                    if (response.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Notice> call, Throwable t) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}