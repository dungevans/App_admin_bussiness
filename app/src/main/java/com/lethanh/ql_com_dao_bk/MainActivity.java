package com.lethanh.ql_com_dao_bk;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lethanh.ql_com_dao_bk.api.RetrofitClient;
import com.lethanh.ql_com_dao_bk.api.StompClientManager;
import com.lethanh.ql_com_dao_bk.databinding.ActivityMainBinding;
import com.lethanh.ql_com_dao_bk.utils.TokenManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = binding.appBarMain.contentMain.bottomNavView;
        
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notice, R.id.nav_product, R.id.nav_category)
                .build();
        
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Auto-connect STOMP if logged in
        String jwt = TokenManager.getJwt(this);
        String serverUrl = TokenManager.getServerUrl(this);
        StompClientManager.getInstance().init(this);
        if (jwt != null && serverUrl != null) {
            RetrofitClient.setBaseUrl(serverUrl);
            RetrofitClient.setAuthToken(jwt);
            StompClientManager.getInstance().connect(serverUrl, jwt);
        }

        // Hide bottom nav on login screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_login) {
                bottomNav.setVisibility(View.GONE);
                if (getSupportActionBar() != null) getSupportActionBar().hide();
            } else {
                bottomNav.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) getSupportActionBar().show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        if (item.getItemId() == R.id.nav_order_list) {
            navController.navigate(R.id.nav_order_list);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            TokenManager.clear(this);
            StompClientManager.getInstance().disconnect();
            navController.navigate(R.id.nav_login);
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}