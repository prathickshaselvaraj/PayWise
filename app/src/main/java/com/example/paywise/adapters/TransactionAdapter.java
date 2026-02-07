package com.example.paywise.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.database.VaultDao;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.DateUtils;
import java.util.List;

/**
 * TransactionAdapter - RecyclerView adapter for displaying transactions
 *
 * Features:
 * - Displays merchant name, amount, date
 * - Color-coded status (success/failed/pending)
 * - Shows vault name and icon
 * - Payment method indicator
 * - Vault change indicator
 * - Click listener for transaction details
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private VaultDao vaultDao;
    private OnTransactionClickListener listener;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.vaultDao = new VaultDao(context);
    }

    // ============================================================
    // INTERFACE FOR CLICK EVENTS
    // ============================================================

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction, int position);
        void onChangeVaultClick(Transaction transaction, int position);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    // ============================================================
    // VIEWHOLDER
    // ============================================================

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        CardView cardTransaction;
        TextView tvMerchantName;
        TextView tvAmount;
        TextView tvVaultInfo;
        TextView tvTransactionDate;
        TextView tvTransactionTime;
        TextView tvStatus;
        TextView tvPaymentMethod;
        TextView tvVaultChanged;
        TextView tvDescription;
        TextView tvChangeVaultButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTransaction = itemView.findViewById(R.id.cardTransaction);
            tvMerchantName = itemView.findViewById(R.id.tvMerchantName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvVaultInfo = itemView.findViewById(R.id.tvVaultInfo);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionTime = itemView.findViewById(R.id.tvTransactionTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvVaultChanged = itemView.findViewById(R.id.tvVaultChanged);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvChangeVaultButton = itemView.findViewById(R.id.tvChangeVaultButton);
        }
    }

    // ============================================================
    // RECYCLERVIEW METHODS
    // ============================================================

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set merchant name
        holder.tvMerchantName.setText(transaction.getMerchantName());

        // Set amount with formatting
        holder.tvAmount.setText(transaction.getFormattedAmount());

        // Color code amount based on transaction type
        if (transaction.getTransactionType().equals("debit")) {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red for debit
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green for credit
        }

        // Get vault information
        Vault vault = vaultDao.getVaultById(transaction.getVaultId());
        if (vault != null) {
            String vaultInfo = vault.getVaultIcon() + " " + vault.getDisplayName();
            holder.tvVaultInfo.setText(vaultInfo);
        } else {
            holder.tvVaultInfo.setText("Unknown Vault");
        }

        // Set transaction date and time
        String dateLabel = DateUtils.getTransactionDateLabel(transaction.getTransactionDate());
        holder.tvTransactionDate.setText(dateLabel);

        String time = DateUtils.formatTimeForDisplay(transaction.getTransactionDate());
        holder.tvTransactionTime.setText(time);

        // Set status with color coding
        String status = transaction.getStatus();
        holder.tvStatus.setText(getStatusText(status));
        holder.tvStatus.setTextColor(getStatusColor(status));

        // Set payment method badge
        String paymentMethod = getPaymentMethodLabel(transaction.getPaymentMethod());
        holder.tvPaymentMethod.setText(paymentMethod);
        holder.tvPaymentMethod.setBackgroundResource(
                getPaymentMethodBackground(transaction.getPaymentMethod()));

        // Show vault changed indicator
        if (transaction.isVaultChanged()) {
            holder.tvVaultChanged.setVisibility(View.VISIBLE);

            // Get original vault name if available
            if (transaction.getOriginalVaultId() != 0) {
                Vault originalVault = vaultDao.getVaultById(transaction.getOriginalVaultId());
                if (originalVault != null) {
                    String changedText = "Changed from " + originalVault.getDisplayName();
                    holder.tvVaultChanged.setText(changedText);
                }
            } else {
                holder.tvVaultChanged.setText("Vault changed");
            }
        } else {
            holder.tvVaultChanged.setVisibility(View.GONE);
        }

        // Set description
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(transaction.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Show "Change Vault" button only for successful instant payments
        if (transaction.isSuccessful() && transaction.isInstantPayment()) {
            holder.tvChangeVaultButton.setVisibility(View.VISIBLE);
            holder.tvChangeVaultButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChangeVaultClick(transaction, position);
                }
            });
        } else {
            holder.tvChangeVaultButton.setVisibility(View.GONE);
        }

        // Click listener for entire card
        holder.cardTransaction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Get status display text
     */
    private String getStatusText(String status) {
        switch (status) {
            case "success":
                return "✓ Success";
            case "failed":
                return "✗ Failed";
            case "pending":
                return "⏳ Pending";
            default:
                return status;
        }
    }

    /**
     * Get status color
     */
    private int getStatusColor(String status) {
        switch (status) {
            case "success":
                return Color.parseColor("#4CAF50"); // Green
            case "failed":
                return Color.parseColor("#F44336"); // Red
            case "pending":
                return Color.parseColor("#FF9800"); // Orange
            default:
                return Color.parseColor("#757575"); // Grey
        }
    }

    /**
     * Get payment method label
     */
    private String getPaymentMethodLabel(String paymentMethod) {
        switch (paymentMethod) {
            case "vault_based":
                return "Vault Payment";
            case "instant_pay":
                return "⚡ Instant Pay";
            case "emergency":
                return "🚨 Emergency";
            default:
                return paymentMethod;
        }
    }

    /**
     * Get payment method background drawable
     */
    private int getPaymentMethodBackground(String paymentMethod) {
        switch (paymentMethod) {
            case "vault_based":
                return R.drawable.bg_payment_method_vault;
            case "instant_pay":
                return R.drawable.bg_payment_method_instant;
            case "emergency":
                return R.drawable.bg_payment_method_emergency;
            default:
                return R.drawable.bg_payment_method_vault;
        }
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Update transaction list and refresh adapter
     */
    public void updateTransactionList(List<Transaction> newTransactionList) {
        this.transactionList = newTransactionList;
        notifyDataSetChanged();
    }

    /**
     * Add a transaction to the list
     */
    public void addTransaction(Transaction transaction) {
        transactionList.add(0, transaction); // Add to top
        notifyItemInserted(0);
    }

    /**
     * Update a specific transaction
     */
    public void updateTransaction(int position, Transaction transaction) {
        if (position >= 0 && position < transactionList.size()) {
            transactionList.set(position, transaction);
            notifyItemChanged(position);
        }
    }

    /**
     * Clear all transactions
     */
    public void clearTransactions() {
        transactionList.clear();
        notifyDataSetChanged();
    }
}