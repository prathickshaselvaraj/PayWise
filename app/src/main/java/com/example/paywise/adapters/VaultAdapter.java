package com.example.paywise.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.models.Vault;
import java.util.List;

/**
 * VaultAdapter - RecyclerView adapter for displaying vault cards
 *
 * Features:
 * - Displays vault icon, name, spending progress
 * - Color-coded progress bars
 * - Click listener for vault selection
 * - Emergency vault lock indicator
 */
public class VaultAdapter extends RecyclerView.Adapter<VaultAdapter.VaultViewHolder> {

    private Context context;
    private List<Vault> vaultList;
    private OnVaultClickListener listener;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public VaultAdapter(Context context, List<Vault> vaultList) {
        this.context = context;
        this.vaultList = vaultList;
    }

    // ============================================================
    // INTERFACE FOR CLICK EVENTS
    // ============================================================

    public interface OnVaultClickListener {
        void onVaultClick(Vault vault, int position);
        void onVaultLongClick(Vault vault, int position);
    }

    public void setOnVaultClickListener(OnVaultClickListener listener) {
        this.listener = listener;
    }

    // ============================================================
    // VIEWHOLDER
    // ============================================================

    public static class VaultViewHolder extends RecyclerView.ViewHolder {
        CardView cardVault;
        TextView tvVaultIcon;
        TextView tvVaultName;
        TextView tvVaultType;
        TextView tvCurrentSpent;
        TextView tvMonthlyLimit;
        TextView tvRemainingBalance;
        ProgressBar progressBarSpending;
        TextView tvSpendingPercentage;
        TextView tvLockIndicator;

        public VaultViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVault = itemView.findViewById(R.id.cardVault);
            tvVaultIcon = itemView.findViewById(R.id.tvVaultIcon);
            tvVaultName = itemView.findViewById(R.id.tvVaultName);
            tvVaultType = itemView.findViewById(R.id.tvVaultType);
            tvCurrentSpent = itemView.findViewById(R.id.tvCurrentSpent);
            tvMonthlyLimit = itemView.findViewById(R.id.tvMonthlyLimit);
            tvRemainingBalance = itemView.findViewById(R.id.tvRemainingBalance);
            progressBarSpending = itemView.findViewById(R.id.progressBarSpending);
            tvSpendingPercentage = itemView.findViewById(R.id.tvSpendingPercentage);
            tvLockIndicator = itemView.findViewById(R.id.tvLockIndicator);
        }
    }

    // ============================================================
    // RECYCLERVIEW METHODS
    // ============================================================

    @NonNull
    @Override
    public VaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vault, parent, false);
        return new VaultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaultViewHolder holder, int position) {
        Vault vault = vaultList.get(position);

        // Set vault icon (emoji or drawable)
        holder.tvVaultIcon.setText(vault.getVaultIcon());

        // Set vault name
        holder.tvVaultName.setText(vault.getDisplayName());

        // Set vault type (hide if custom and has custom category)
        if (vault.getVaultType().equals("Custom") && vault.getCustomCategoryName() != null) {
            holder.tvVaultType.setVisibility(View.GONE);
        } else {
            holder.tvVaultType.setText(vault.getVaultType());
            holder.tvVaultType.setVisibility(View.VISIBLE);
        }

        // Set spending amounts
        holder.tvCurrentSpent.setText(String.format("₹%.0f", vault.getCurrentSpent()));
        holder.tvMonthlyLimit.setText(String.format("₹%.0f", vault.getMonthlyLimit()));

        // Set remaining balance
        double remaining = vault.getRemainingBalance();
        holder.tvRemainingBalance.setText(String.format("Remaining: ₹%.0f", remaining));

        // Set spending percentage
        int percentage = vault.getSpendingPercentage();
        holder.tvSpendingPercentage.setText(percentage + "%");
        holder.progressBarSpending.setProgress(percentage);

        // Color-code progress bar based on spending
        int progressColor;
        if (percentage < 70) {
            progressColor = Color.parseColor("#4CAF50"); // Green
        } else if (percentage < 90) {
            progressColor = Color.parseColor("#FF9800"); // Orange
        } else {
            progressColor = Color.parseColor("#F44336"); // Red
        }
        holder.progressBarSpending.getProgressDrawable().setColorFilter(
                progressColor, android.graphics.PorterDuff.Mode.SRC_IN);

        // Set card background color
        try {
            holder.cardVault.setCardBackgroundColor(Color.parseColor(vault.getVaultColor()));
        } catch (IllegalArgumentException e) {
            // Fallback to default color if parsing fails
            holder.cardVault.setCardBackgroundColor(Color.parseColor("#EEEEEE"));
        }

        // Show lock indicator for emergency vault
        if (vault.isEmergency()) {
            holder.tvLockIndicator.setVisibility(View.VISIBLE);
            holder.tvLockIndicator.setText("🔒 PIN Protected");
        } else {
            holder.tvLockIndicator.setVisibility(View.GONE);
        }

        // Click listener
        holder.cardVault.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVaultClick(vault, position);
            }
        });

        // Long click listener
        holder.cardVault.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onVaultLongClick(vault, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return vaultList != null ? vaultList.size() : 0;
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Update vault list and refresh adapter
     */
    public void updateVaultList(List<Vault> newVaultList) {
        this.vaultList = newVaultList;
        notifyDataSetChanged();
    }

    /**
     * Add a vault to the list
     */
    public void addVault(Vault vault) {
        vaultList.add(vault);
        notifyItemInserted(vaultList.size() - 1);
    }

    /**
     * Remove a vault from the list
     */
    public void removeVault(int position) {
        if (position >= 0 && position < vaultList.size()) {
            vaultList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update a specific vault
     */
    public void updateVault(int position, Vault vault) {
        if (position >= 0 && position < vaultList.size()) {
            vaultList.set(position, vault);
            notifyItemChanged(position);
        }
    }
}