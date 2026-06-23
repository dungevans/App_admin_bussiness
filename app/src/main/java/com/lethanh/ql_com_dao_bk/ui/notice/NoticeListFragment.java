package com.lethanh.ql_com_dao_bk.ui.notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentListBinding;
import com.lethanh.ql_com_dao_bk.model.Notice;
import com.lethanh.ql_com_dao_bk.model.NoticeResponse;
import com.lethanh.ql_com_dao_bk.ui.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeListFragment extends Fragment {

    private FragmentListBinding binding;
    private GenericAdapter<Notice> adapter;
    private List<Notice> noticeList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new GenericAdapter<>(noticeList, (notice, holder) -> {
            holder.title.setText(notice.getTitle());
            holder.subtitle.setText(notice.getSummary());
            holder.image.setVisibility(View.GONE);
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        fetchNotices();
    }

    private void fetchNotices() {
        RetrofitClient.getApiService().getNotices().enqueue(new Callback<NoticeResponse>() {
            @Override
            public void onResponse(Call<NoticeResponse> call, Response<NoticeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    noticeList.clear();
                    noticeList.addAll(response.body().getContent());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<NoticeResponse> call, Throwable t) {
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