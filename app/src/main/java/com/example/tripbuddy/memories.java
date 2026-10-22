package com.example.tripbuddy;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripbuddy.core.AppException;
import com.example.tripbuddy.core.Callback;
import com.example.tripbuddy.model.Memory;
import com.example.tripbuddy.repo.MemoryRepository;
import com.example.tripbuddy.ui.MemoryAdapter;
import com.example.tripbuddy.ui.PagedScrollListener;

import java.util.ArrayList;
import java.util.List;

public class memories extends BaseActivity implements MemoryAdapter.Listener {

    ImageView image1, image2;
    Button nextBtn;
    TextView emptyView;
    RecyclerView memoryList;

    private MemoryAdapter adapter;
    private MemoryRepository repo;
    private final List<Memory> items = new ArrayList<>();
    private long lastId = Long.MAX_VALUE;
    private boolean loading;
    private boolean endReached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memories);
        if (isFinishing()) return;

        repo = ServiceLocator.memories(this);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        nextBtn=findViewById(R.id.nextBtn);
        emptyView = findViewById(R.id.tvMemoriesEmpty);
        memoryList = findViewById(R.id.memoryList);

        View.OnClickListener listener = v -> showFullScreen((ImageView) v);

        image1.setOnClickListener(listener);
        image2.setOnClickListener(listener);

        // NEXT used to do nothing. It now opens the screen for writing a new memory.
        nextBtn.setOnClickListener(v -> {
            if (!isDoubleClick()) open(SaveMemoryActivity.class);
        });

        adapter = new MemoryAdapter(this);
        memoryList.setLayoutManager(new LinearLayoutManager(this));
        memoryList.setAdapter(adapter);
        memoryList.addOnScrollListener(new PagedScrollListener() {
            @Override protected void onLoadMore() { loadNextPage(); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session == null || !session.isValid() || repo == null) return;
        // Reload from the start so a memory just saved on the next screen shows up.
        items.clear();
        lastId = Long.MAX_VALUE;
        endReached = false;
        adapter.submitList(new ArrayList<>());
        loadNextPage();
    }

    private void loadNextPage() {
        if (loading || endReached) return;
        loading = true;
        repo.loadPage(session.userId(), lastId, safe(new Callback<List<Memory>>() {
            @Override public void onSuccess(List<Memory> page) {
                loading = false;
                if (page.size() < MemoryRepository.PAGE_SIZE) endReached = true;
                if (!page.isEmpty()) {
                    items.addAll(page);
                    lastId = page.get(page.size() - 1).id;
                    adapter.submitList(new ArrayList<>(items));
                }
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(AppException e) {
                loading = false;
                toast(e.getMessage());
            }
        }));
    }

    @Override
    public void onLongClick(Memory memory) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tb_delete_memory_title)
                .setMessage(R.string.tb_delete_memory_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.tb_delete, (d, w) ->
                        repo.delete(session.userId(), memory.id, safe(new Callback<Boolean>() {
                            @Override public void onSuccess(Boolean removed) {
                                if (removed) {
                                    items.remove(memory);
                                    adapter.submitList(new ArrayList<>(items));
                                    emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            }
                            @Override public void onError(AppException e) { toast(e.getMessage()); }
                        })))
                .show();
    }

    private void showFullScreen(ImageView imageView) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView fullImage = new ImageView(this);
        fullImage.setImageDrawable(imageView.getDrawable());
        fullImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        dialog.setContentView(fullImage);

        fullImage.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
