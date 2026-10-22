package com.example.tripbuddy.ui;

import android.content.Context;
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
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.model.Trip;

/** Trip list. DiffUtil animates and updates only the rows that actually changed. */
public final class TripAdapter extends ListAdapter<Trip, TripAdapter.Holder> {

    public interface Listener {
        void onClick(Trip trip);
        void onLongClick(Trip trip);
    }

    private static final DiffUtil.ItemCallback<Trip> DIFF = new DiffUtil.ItemCallback<Trip>() {
        @Override public boolean areItemsTheSame(@NonNull Trip a, @NonNull Trip b) { return a.id == b.id; }
        @Override public boolean areContentsTheSame(@NonNull Trip a, @NonNull Trip b) { return a.equals(b); }
    };

    private final Listener listener;

    public TripAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) { return getItem(position).id; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Trip t = getItem(position);
        Context c = h.itemView.getContext();
        h.title.setText(t.destination);
        String date = DateUtils.formatDateTime(c, t.createdAt,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
        h.meta.setText(t.activity + "  |  " + date);
        h.total.setText(Money.format(t.totalCents));
        h.status.setText(t.isConfirmed() ? R.string.tb_status_confirmed : R.string.tb_status_draft);
        h.itemView.setOnClickListener(v -> listener.onClick(t));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(t);
            return true;
        });
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, meta, total, status;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTripTitle);
            meta = itemView.findViewById(R.id.tvTripMeta);
            total = itemView.findViewById(R.id.tvTripTotal);
            status = itemView.findViewById(R.id.tvTripStatus);
        }
    }
}
