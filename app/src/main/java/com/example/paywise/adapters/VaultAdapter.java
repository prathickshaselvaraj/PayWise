package com.example.paywise.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import java.util.List;

public class VaultAdapter extends RecyclerView.Adapter<VaultAdapter.VaultViewHolder> {

    private Context context;
    private List<Vault> vaultList;
    private OnVaultClickListener listener;

    public interface OnVaultClickListener {
        void onVaultClick(Vault vault);
        void onVaultLongClick(Vault vault);
    }

    public VaultAdapter(Context context, List<Vault> vaultList, OnVaultClickListener listener) {
        this.context = context;
        this.vaultList = vaultList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vault, parent, false);
        return new VaultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VaultViewHolder holder, int position) {
        Vault vault = vaultList.get(position);

        // Set vault name
        holder.tvVaultName.setText(vault.getVaultName());

        // Set spent and limit
        holder.tvVaultSpent.setText(String.format("₹%.0f", vault.getCurrentSpent()));
        holder.tvVaultLimit.setText(String.format("₹%.0f", vault.getMonthlyLimit()));

        // Set progress bar
        int percentage = vault.getSpendingPercentage();
        holder.progressBar.setProgress(percentage);

        // Change progress bar color based on percentage
        if (percentage >= 90) {
            holder.progressBar.getProgressDrawable().setColorFilter(
                    context.getResources().getColor(android.R.color.holo_red_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (percentage >= 70) {
            holder.progressBar.getProgressDrawable().setColorFilter(
                    context.getResources().getColor(android.R.color.holo_orange_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            holder.progressBar.getProgressDrawable().setColorFilter(
                    context.getResources().getColor(android.R.color.holo_green_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Set vault icon based on type
        setVaultIcon(holder.ivVaultIcon, vault.getVaultType());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVaultClick(vault);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onVaultLongClick(vault);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return vaultList.size();
    }

    public void updateVaults(List<Vault> newVaultList) {
        this.vaultList = newVaultList;
        notifyDataSetChanged();
    }

    private void setVaultIcon(ImageView imageView, String vaultType) {
        switch (vaultType) {
            case Constants.VAULT_TYPE_FOOD:
                imageView.setImageResource(R.drawable.ic_vault_food);
                break;
            case Constants.VAULT_TYPE_TRAVEL:
                imageView.setImageResource(R.drawable.ic_vault_travel);
                break;
            case Constants.VAULT_TYPE_LIFESTYLE:
                imageView.setImageResource(R.drawable.ic_vault_lifestyle);
                break;
            case Constants.VAULT_TYPE_BUSINESS:
                imageView.setImageResource(R.drawable.ic_vault_business);
                break;
            case Constants.VAULT_TYPE_EMERGENCY:
                imageView.setImageResource(R.drawable.ic_vault_emergency);
                break;
        }
    }

    static class VaultViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVaultIcon, ivMoreOptions;
        TextView tvVaultName, tvVaultSpent, tvVaultLimit;
        ProgressBar progressBar;

        public VaultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVaultIcon = itemView.findViewById(R.id.ivVaultIcon);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
            tvVaultName = itemView.findViewById(R.id.tvVaultName);
            tvVaultSpent = itemView.findViewById(R.id.tvVaultSpent);
            tvVaultLimit = itemView.findViewById(R.id.tvVaultLimit);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
