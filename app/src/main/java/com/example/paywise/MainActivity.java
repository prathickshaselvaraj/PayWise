package com.example.paywise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paywise.adapters.TransactionAdapter;
import com.example.paywise.adapters.VaultAdapter;
import com.example.paywise.database.BankAccountDao;
import com.example.paywise.managers.PaymentManager;
import com.example.paywise.managers.VaultManager;
import com.example.paywise.models.BankAccount;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.ImageUtils;
import com.example.paywise.utils.SessionManager;
import com.example.paywise.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

// ✅ IMPORTANT: these are in activities package, so import them
import com.example.paywise.activities.LoginActivity;
import com.example.paywise.activities.PaymentEntryActivity;
import com.example.paywise.activities.TransactionHistoryActivity;
import com.example.paywise.activities.VaultManagementActivity;
import com.example.paywise.activities.VaultSelectionActivity;

public class MainActivity extends AppCompatActivity implements
        VaultAdapter.OnVaultClickListener,
        TransactionAdapter.OnTransactionClickListener {

    private Toolbar toolbar;
    private ImageView ivProfilePhoto;
    private TextView tvUserName;
    private TextView tvMobileNumber;
    private TextView tvTotalBalance;
    private TextView tvBankBalance;
    private MaterialButton btnMakePayment;
    private MaterialButton btnViewVaults;
    private TextView tvAddVault;
    private RecyclerView recyclerViewVaults;
    private RecyclerView recyclerViewRecentTransactions;
    private FloatingActionButton fabMakePayment;

    private SessionManager sessionManager;
    private VaultManager vaultManager;
    private PaymentManager paymentManager;
    private BankAccountDao bankAccountDao;

    private VaultAdapter vaultAdapter;
    private TransactionAdapter transactionAdapter;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeManagers();
        setupToolbar();
        loadUserData();
        loadVaults();
        loadRecentTransactions();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvBankBalance = findViewById(R.id.tvBankBalance);
        btnMakePayment = findViewById(R.id.btnMakePayment);
        btnViewVaults = findViewById(R.id.btnViewVaults);
        tvAddVault = findViewById(R.id.tvAddVault);
        recyclerViewVaults = findViewById(R.id.recyclerViewVaults);
        recyclerViewRecentTransactions = findViewById(R.id.recyclerViewRecentTransactions);
        fabMakePayment = findViewById(R.id.fabMakePayment);
    }

    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        vaultManager = new VaultManager(this);
        paymentManager = new PaymentManager(this);
        bankAccountDao = new BankAccountDao(this);
        userId = sessionManager.getUserId();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void loadUserData() {
        String userName = sessionManager.getUserName();
        String mobileNumber = sessionManager.getMobileNumber();

        tvUserName.setText(userName);
        tvMobileNumber.setText(ValidationUtils.formatMobileNumber(mobileNumber));

        String profileImagePath = sessionManager.getProfileImage();
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            Bitmap bitmap = ImageUtils.loadBitmapFromPath(profileImagePath);
            if (bitmap != null) {
                ivProfilePhoto.setImageBitmap(bitmap);
            }
        }

        BankAccount bankAccount = bankAccountDao.getPrimaryBankAccount(userId);
        if (bankAccount != null) {
            tvBankBalance.setText(bankAccount.getFormattedBalance());
        }

        double totalBalance = vaultManager.getTotalBalance(userId);
        tvTotalBalance.setText(ValidationUtils.formatAmountNoDecimals(totalBalance));
    }

    private void loadVaults() {
        List<Vault> vaults = vaultManager.getUserVaults(userId);

        recyclerViewVaults.setLayoutManager(new LinearLayoutManager(this));
        vaultAdapter = new VaultAdapter(this, vaults);
        vaultAdapter.setOnVaultClickListener(this);
        recyclerViewVaults.setAdapter(vaultAdapter);
    }

    private void loadRecentTransactions() {
        List<Transaction> recentTransactions = paymentManager.getRecentTransactions(userId, 5);

        recyclerViewRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(this, recentTransactions);
        transactionAdapter.setOnTransactionClickListener(this);
        recyclerViewRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        btnMakePayment.setOnClickListener(v -> navigateToPayment());
        fabMakePayment.setOnClickListener(v -> navigateToPayment());

        btnViewVaults.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        tvAddVault.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VaultManagementActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToPayment() {
        Intent intent = new Intent(MainActivity.this, PaymentEntryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onVaultClick(Vault vault, int position) {
        Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
        intent.putExtra("vault_id", vault.getVaultId());
        startActivity(intent);
    }

    @Override
    public void onVaultLongClick(Vault vault, int position) {
        // TODO
    }

    @Override
    public void onTransactionClick(Transaction transaction, int position) {
        // TODO
    }

    @Override
    public void onChangeVaultClick(Transaction transaction, int position) {
        Intent intent = new Intent(MainActivity.this, VaultSelectionActivity.class);
        intent.putExtra("transaction_id", transaction.getTransactionId());
        intent.putExtra("is_reassignment", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_profile) {
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadVaults();
        loadRecentTransactions();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
