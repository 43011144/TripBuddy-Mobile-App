package com.example.tripbuddy.ui;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripbuddy.R;
import com.example.tripbuddy.model.Memory;

public final class MemoryAdapter extends ListAdapter<Memory, MemoryAdapter.Holder> {

    public interface Listener {
        void onLongClick(Memory memory);
    }

    private static final DiffUtil.ItemCallback<Memory> DIFF = new DiffUtil.ItemCallback<Memory>() {
        @Override public boolean areItemsTheSame(@NonNull Memory a, @NonNull Memory b) { return a.id == b.id; }
        @Override public boolean areContentsTheSame(@NonNull Memory a, @NonNull Memory b) { return a.equals(b); }
    };

    private final Listener listener;

    public MemoryAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) { return getItem(position).id; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memory, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Memory m = getItem(position);
        h.text.setText(m.content);
        h.date.setText(DateUtils.formatDateTime(h.itemView.getContext(), m.createdAt,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH
                        | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(m);
            return true;
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView text, date;

        Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tvMemoryText);
            date = itemView.findViewById(R.id.tvMemoryDate);
        }
    }
}
