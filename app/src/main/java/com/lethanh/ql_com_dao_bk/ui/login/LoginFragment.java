package com.lethanh.ql_com_dao_bk.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.lethanh.ql_com_dao_bk.R;
import com.lethanh.ql_com_dao_bk.api.ApiService;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.databinding.FragmentLoginBinding;
import com.lethanh.ql_com_dao_bk.model.LoginRequest;
import com.lethanh.ql_com_dao_bk.model.LoginResponse;
import com.lethanh.ql_com_dao_bk.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnLogin.setOnClickListener(v -> {
            String serverUrl = binding.etServerUrl.getText().toString().trim();
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (serverUrl.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập địa chỉ Server", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!username.isEmpty() && !password.isEmpty()) {
                // 1. Update the Base URL in RetrofitClient
                RetrofitClient.setBaseUrl(serverUrl);

                // 2. Perform actual network Login
                ApiService apiService = RetrofitClient.getApiService();
                LoginRequest loginRequest = new LoginRequest(username, password);

                apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. Save Token and URL
                            String jwt = response.body().getJwt();
                            if (getContext() != null) {
                                TokenManager.saveJwt(requireContext(), jwt);
                                TokenManager.saveServerUrl(requireContext(), serverUrl);
                            }
                            
                            // 2. Set token in RetrofitClient for subsequent calls
                            RetrofitClient.setAuthToken(jwt);
                            
                            // 3. Connect to WebSocket
                            com.lethanh.ql_com_dao_bk.api.StompClientManager.getInstance().init(requireContext());
                            com.lethanh.ql_com_dao_bk.api.StompClientManager.getInstance().connect(serverUrl, jwt);

                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            }

                            // 3. Navigate to Notice screen
                            if (getView() != null) {
                                Navigation.findNavController(view).navigate(R.id.nav_notice);
                            }
                        } else {
                            if (getContext() != null) {
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Toast.makeText(getContext(), "Error " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không thể kết nối tới server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                });
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}