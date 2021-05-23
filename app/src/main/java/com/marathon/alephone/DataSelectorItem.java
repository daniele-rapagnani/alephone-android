package com.marathon.alephone;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DataSelectorItem extends RecyclerView.ViewHolder {
    private final TextView titleView;
    private final Button runButton;
    private final Activity activity;

    public DataSelectorItem(@NonNull View itemView, Activity activity) {
        super(itemView);

        this.activity = activity;

        this.titleView = (TextView)itemView.findViewById(R.id.scenario_name);
        this.runButton = (Button)itemView.findViewById(R.id.run_scenario);
    }

    public void setData(final DataEntry de) {
        this.titleView.setText(de.scenarioName);
        this.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DataSelectorItem.this.activity, MainActivity.class);
                i.putExtra("scenarioPath", de.path);
                DataSelectorItem.this.activity.startActivity(i);
            }
        });
    }
}
