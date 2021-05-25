package com.marathon.alephone.scenario;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.marathon.alephone.AlephOneActivity;
import com.marathon.alephone.AlertUtils;
import com.marathon.alephone.NotificationsManager;
import com.marathon.alephone.R;
import com.marathon.alephone.ScenarioSelectorActivity;

import org.apache.commons.io.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ScenarioSelectorItem extends RecyclerView.ViewHolder {
    private final static HashMap<String, Integer> scenarioIcons =
        new HashMap<String, Integer>()
    ;

    private final TextView titleView;
    private final TextView sizeView;
    private final TextView lastPlayedView;
    private final Button runButton;
    private final Button menuButton;
    private final ImageView imageView;
    private final ScenarioSelectorActivity activity;

    static {
        scenarioIcons.put("Marathon EVIL", R.drawable.evil);
        scenarioIcons.put("Marathon RED", R.drawable.red);
        scenarioIcons.put("Marathon", R.drawable.m1);
        scenarioIcons.put("Marathon 2", R.drawable.m2);
        scenarioIcons.put("Marathon Infinity", R.drawable.mi);
        scenarioIcons.put("Eternal", R.drawable.eternal);
        scenarioIcons.put("Eternal X", R.drawable.eternal);
        scenarioIcons.put("Tempus Irae", R.drawable.tempusirae);
        scenarioIcons.put("Rubicon X", R.drawable.rubiconx);
        scenarioIcons.put("Marathon Phoenix", R.drawable.phoenix);
        scenarioIcons.put("Marathon Phoenix SE", R.drawable.phoenix);
        scenarioIcons.put("", R.drawable.unknown);
    }

    public ScenarioSelectorItem(
        View itemView,
        ScenarioSelectorActivity activity
    ) {
        super(itemView);

        this.activity = activity;
        this.titleView = (TextView)itemView.findViewById(R.id.scenario_name);
        this.sizeView = (TextView)itemView.findViewById(R.id.scenario_size);
        this.lastPlayedView = (TextView)itemView.findViewById(R.id.last_played);
        this.runButton = (Button)itemView.findViewById(R.id.run_scenario);
        this.menuButton = (Button)itemView.findViewById(R.id.scenario_menu_button);
        this.imageView = (ImageView)itemView.findViewById(R.id.scenario_image);
    }

    public void setData(final ScenarioEntry de) {
        this.titleView.setText(de.scenarioName);
        this.sizeView.setText(FileUtils.byteCountToDisplaySize(de.size));

        SimpleDateFormat f = new SimpleDateFormat("dd/mm/yyyy hh:mm");

        this.lastPlayedView.setText(
            de.lastPlayed == null ? "Never" : f.format(de.lastPlayed)
        );

        this.runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.onPlayScenario(de);
            }
        });

        this.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(activity, menuButton);
                popup.inflate(R.menu.scenario_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.uninstall_scenario:
                                activity.onUninstall(de);
                                return true;

                            case R.id.export_data:
                                activity.onExportScenarioData(de);
                                return true;

                            case R.id.import_data:
                                activity.onImportScenarioData(de);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popup.show();
            }
        });

        int icon = scenarioIcons.get("");

        if (scenarioIcons.containsKey(de.scenarioName)) {
            icon = scenarioIcons.get(de.scenarioName);
        }

        this.imageView.setImageDrawable(this.activity.getDrawable(icon));
    }
}
