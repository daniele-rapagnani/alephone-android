package com.marathon.alephone.scenario;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marathon.alephone.R;
import com.marathon.alephone.ScenarioSelectorActivity;

import java.util.List;

public class ScenarioSelectorAdapter extends RecyclerView.Adapter<ScenarioSelectorItem> {
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

    @NonNull
    @Override
    public ScenarioSelectorItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.scenario_view, viewGroup, false)
        ;

        return new ScenarioSelectorItem(v, this.activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ScenarioSelectorItem dataSelectorItem, int i) {
        dataSelectorItem.setData(this.dataEntries.get(i));
    }

    @Override
    public int getItemCount() {
        return this.dataEntries != null ? this.dataEntries.size() : 0;
    }
}
