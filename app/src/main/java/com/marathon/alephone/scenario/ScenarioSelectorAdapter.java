package com.marathon.alephone.scenario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marathon.alephone.R;
import com.marathon.alephone.ScenarioSelectorActivity;

import java.util.List;

public class ScenarioSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_OBJECT_VIEW = 1;

    private List<ScenarioEntry> dataEntries;
    private final ScenarioSelectorActivity activity;

    public ScenarioSelectorAdapter(ScenarioSelectorActivity activity) {
        this.activity = activity;
    }

    public void setDataEntries(List<ScenarioEntry> dataEntries)
    {
        this.dataEntries = dataEntries;
        notifyDataSetChanged();
    }

    public List<ScenarioEntry> getScenarioEntries()
    {
        return this.dataEntries;
    }

    @Override
    public int getItemViewType(int position) {
        if (getScenarioEntries() == null || getScenarioEntries().isEmpty()) {
            return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
        } else {
            return VIEW_TYPE_OBJECT_VIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;

        switch (i) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.scenario_view_empty, viewGroup, false)
                ;

                return new ScenarioSelectorEmptyItem(v);

            case VIEW_TYPE_OBJECT_VIEW:
                v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.scenario_view, viewGroup, false)
                ;

                return new ScenarioSelectorItem(v, this.activity);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder dataSelectorItem, int i) {
        if (getScenarioEntries() != null && !getScenarioEntries().isEmpty()) {
            ((ScenarioSelectorItem)dataSelectorItem).setData(this.dataEntries.get(i));
        }
    }

    @Override
    public int getItemCount() {
        if (this.dataEntries == null) {
            return 0;
        }

        return !this.dataEntries.isEmpty() ? this.dataEntries.size() : 1;
    }
}
