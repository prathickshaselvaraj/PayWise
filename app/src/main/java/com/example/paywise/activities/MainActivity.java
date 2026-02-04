package com.example.paywise.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.adapters.VaultAdapter;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.ImageUtils;
import com.example.paywise.utils.PreferenceManager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VaultAdapter.OnVaultClickListener {

    private Toolbar toolbar;
    private ImageView ivProfilePic;
    private TextView tvUserName, tvBalanceAmount;
    private Button btnAddVault, btnMakePayment;
    private RecyclerView rvVaults;

    private PreferenceManager preferenceManager;
    private VaultManager vaultManager;
    private VaultAdapter vaultAdapter;
    private List<Vault> vaultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();

        preferenceManager = new PreferenceManager(this);
        vaultManager = new VaultManager(this);

        loadUserProfile();
        setupRecyclerView();
        loadVaults();

        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        tvBalanceAmount = findViewById(R.id.tvBalanceAmount);
        btnAddVault = findViewById(R.id.btnAddVault);
        btnMakePayment = findViewById(R.id.btnMakePayment);
        rvVaults = findViewById(R.id.rvVaults);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.dashboard_title);
        }
    }

    private void loadUserProfile() {
        String userName = preferenceManager.getUserName();
        String profileImagePath = preferenceManager.getProfileImage();

        tvUserName.setText(userName);

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            Bitmap bitmap = ImageUtils.loadBitmapFromPath(profileImagePath);
            if (bitmap != null) {
                ivProfilePic.setImageBitmap(bitmap);
            }
        }

        updateTotalBalance();
    }

    private void setupRecyclerView() {
        vaultList = new ArrayList<>();
        vaultAdapter = new VaultAdapter(this, vaultList, this);

        rvVaults.setLayoutManager(new LinearLayoutManager(this));
        rvVaults.setAdapter(vaultAdapter);
    }

    private void loadVaults() {
        int userId = preferenceManager.getUserId();
        vaultList = vaultManager.getUserVaults(userId);
        vaultAdapter.updateVaults(vaultList);
    }

    private void updateTotalBalance() {
        int userId = preferenceManager.getUserId();
        double totalBalance = vaultManager.getTotalBalance(userId);
        tvBalanceAmount.setText(String.format("₹%.2f", totalBalance));
    }

    private void setupClickListeners() {
        btnAddVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VaultManagementActivity.class);
                startActivity(intent);
            }
        });

        btnMakePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onVaultClick(Vault vault) {
        // Navigate to transaction history for this vault
        Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
        intent.putExtra(com.example.paywise.utils.Constants.EXTRA_VAULT_ID, vault.getVaultId());
        startActivity(intent);
    }

    @Override
    public void onVaultLongClick(Vault vault) {
        // Show options to edit or delete vault
        // You can implement a dialog here
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVaults();
        updateTotalBalance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // Navigate to profile
            return true;
        } else if (id == R.id.action_transactions) {
            Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}