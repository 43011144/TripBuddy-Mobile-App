package com.example.tripbuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.core.Money;
import com.example.tripbuddy.model.Trip;
import com.example.tripbuddy.model.UserStats;
import com.example.tripbuddy.repo.TripRepository;
import com.example.tripbuddy.ui.PagedScrollListener;
import com.example.tripbuddy.ui.TripAdapter;

import java.util.ArrayList;
import java.util.List;

/** "Track trips": every planned and confirmed trip, newest first, loaded 20 at a time. */
public class TripHistoryActivity extends BaseActivity implements TripAdapter.Listener {

    private TextView summary, empty;
    private ProgressBar progress;
    private RecyclerView list;
    private TripAdapter adapter;
    private TripRepository repo;

    private final List<Trip> items = new ArrayList<>();
    private long lastId = Long.MAX_VALUE;
    private boolean loading;
    private boolean endReached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);
        if (isFinishing()) return;

        repo = ServiceLocator.trips(this);
        summary = findViewById(R.id.tvHistorySummary);
        empty = findViewById(R.id.tvHistoryEmpty);
        progress = findViewById(R.id.historyProgress);
        list = findViewById(R.id.historyList);

        adapter = new TripAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
        list.addOnScrollListener(new PagedScrollListener() {
            @Override protected void onLoadMore() { loadNextPage(); }
        });

        loadNextPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isValid()) refreshSummary();
    }

    private void refreshSummary() {
        repo.loadStats(session.userId(), safe(new Callback<UserStats>() {
            @Override public void onSuccess(UserStats s) {
                summary.setText(getString(R.string.tb_history_summary,
                        s.confirmedTrips, Money.format(s.confirmedTotalCents), s.draftTrips));
            }
            @Override public void onError(AppException e) { summary.setText(e.getMessage()); }
        }));
    }

    private void loadNextPage() {
        if (loading || endReached) return;
        loading = true;
        progress.setVisibility(View.VISIBLE);
        repo.loadPage(session.userId(), lastId, safe(new Callback<List<Trip>>() {
            @Override public void onSuccess(List<Trip> page) {
                loading = false;
                progress.setVisibility(View.GONE);
                if (page.size() < TripRepository.PAGE_SIZE) endReached = true;
                if (!page.isEmpty()) {
                    items.addAll(page);
                    lastId = page.get(page.size() - 1).id;
                    adapter.submitList(new ArrayList<>(items));
                }
                updateEmptyState();
            }
            @Override public void onError(AppException e) {
                loading = false;
                progress.setVisibility(View.GONE);
                toast(e.getMessage());
            }
        }));
    }

    private void updateEmptyState() {
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(Trip t) {
        String body = getString(R.string.tb_trip_detail,
                t.activity, Money.format(t.activityCostCents),
                Money.format(t.customExpenseCents), Money.format(t.mealsExpenseCents),
                Money.format(t.discountCents), Money.format(t.totalCents),
                t.notes.isEmpty() ? getString(R.string.tb_no_notes) : t.notes);
        new AlertDialog.Builder(this)
                .setTitle(t.destination)
                .setMessage(body)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onLongClick(Trip t) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tb_delete_trip_title)
                .setMessage(getString(R.string.tb_delete_trip_message, t.destination))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.tb_delete, (d, w) -> deleteTrip(t))
                .show();
    }

    private void deleteTrip(Trip t) {
        repo.delete(session.userId(), t.id, safe(new Callback<Boolean>() {
            @Override public void onSuccess(Boolean removed) {
                if (removed) {
                    items.remove(t);
                    adapter.submitList(new ArrayList<>(items));
                    updateEmptyState();
                    refreshSummary();
                }
            }
            @Override public void onError(AppException e) { toast(e.getMessage()); }
        }));
    }
}
