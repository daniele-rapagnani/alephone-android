package com.marathon.alephone;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DataSelectorAdapter extends RecyclerView.Adapter<DataSelectorItem> {
    private List<DataEntry> dataEntries;
    private final Activity activity;

    DataSelectorAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setDataEntries(List<DataEntry> dataEntries)
    {
        this.dataEntries = dataEntries;
        notifyDataSetChanged();
    }

    public List<DataEntry> getDataEntries()
    {
        return this.dataEntries;
    }

    @NonNull
    @Override
    public DataSelectorItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.data_view, viewGroup, false)
        ;

        return new DataSelectorItem(v, this.activity);
    }

    @Override
    public void onBindViewHolder(@NonNull DataSelectorItem dataSelectorItem, int i) {
        dataSelectorItem.setData(this.dataEntries.get(i));
    }

    @Override
    public int getItemCount() {
        return this.dataEntries != null ? this.dataEntries.size() : 0;
    }
}
