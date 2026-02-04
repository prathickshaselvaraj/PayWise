package com.example.paywise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paywise.R;
import com.example.paywise.database.VaultDao;
import com.example.paywise.models.Transaction;
import com.example.paywise.models.Vault;
import com.example.paywise.utils.Constants;
import com.example.paywise.utils.DateUtils;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private VaultDao vaultDao;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.vaultDao = new VaultDao(context);
    }

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

        // Get and set vault name
        Vault vault = vaultDao.getVaultById(transaction.getVaultId());
        if (vault != null) {
            holder.tvVaultName.setText(vault.getVaultName());
        }

        // Format and set transaction date
        String formattedDate = DateUtils.formatDateForDisplay(transaction.getTransactionDate());
        holder.tvTransactionDate.setText(formattedDate);

        // Set amount with appropriate sign
        String amountText;
        int amountColor;

        if (transaction.getTransactionType().equals(Constants.TRANSACTION_TYPE_DEBIT)) {
            amountText = String.format("- ₹%.2f", transaction.getAmount());
            amountColor = context.getResources().getColor(R.color.statusFailed);
        } else {
            amountText = String.format("+ ₹%.2f", transaction.getAmount());
            amountColor = context.getResources().getColor(R.color.statusSuccess);
        }

        holder.tvAmount.setText(amountText);
        holder.tvAmount.setTextColor(amountColor);

        // Set status
        holder.tvStatus.setText(transaction.getStatus());

        // Set status color
        int statusColor;
        switch (transaction.getStatus()) {
            case Constants.TRANSACTION_STATUS_SUCCESS:
                statusColor = context.getResources().getColor(R.color.statusSuccess);
                break;
            case Constants.TRANSACTION_STATUS_FAILED:
                statusColor = context.getResources().getColor(R.color.statusFailed);
                break;
            case Constants.TRANSACTION_STATUS_PENDING:
                statusColor = context.getResources().getColor(R.color.statusPending);
                break;
            default:
                statusColor = context.getResources().getColor(R.color.textSecondary);
        }
        holder.tvStatus.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateTransactions(List<Transaction> newTransactionList) {
        this.transactionList = newTransactionList;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchantName, tvVaultName, tvTransactionDate, tvAmount, tvStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerchantName = itemView.findViewById(R.id.tvMerchantName);
            tvVaultName = itemView.findViewById(R.id.tvVaultName);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}