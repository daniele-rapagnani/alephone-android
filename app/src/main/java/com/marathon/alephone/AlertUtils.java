package com.marathon.alephone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.marathon.alephone.scenario.IScenarioInstallListener;

import java.util.ArrayList;
import java.util.List;

public class AlertUtils {
    public interface YesNoListener {
        void yes();
        void no();
    }

    public interface ChoiceListener<T> {
        void success(T choice);
        void canceled();
    }

    public static void showError(final Activity activity, final String title, final String text) {
        showWithIcon(activity, title, text, android.R.drawable.ic_dialog_alert);
    }

    public static void showInfo(final Activity activity, final String title, final String text) {
        showWithIcon(activity, title, text, android.R.drawable.ic_dialog_info);
    }

    private static void showWithIcon(final Activity activity, final String title, final String text, final int icon) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(text)
                        .setCancelable(true)
                        .setIcon(icon)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show()
                ;
            }
        });
    }

    public static void showYesNo(
            final Activity activity,
            final String title,
            final String text,
            final YesNoListener listener
    ) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(text)
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listener.yes();
                                dialogInterface.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listener.no();
                                dialogInterface.cancel();
                            }
                        })
                        .show()
                ;
            }
        });
    }

    public static <T> void showChoice(
        final Activity activity,
        final String title,
        final List<T> choices,
        final ChoiceListener<T> listener
    ) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence[] strChoices = new CharSequence[choices.size()];

                for (int i = 0; i < choices.size(); i++) {
                    strChoices[i] = choices.get(i).toString();
                }

                final List<T> selected = new ArrayList<>();
                selected.add(choices.get(0));

                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setSingleChoiceItems(
                            strChoices,
                            0,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selected.clear();
                                    selected.add(choices.get(i));
                                }
                            }
                        )
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listener.success(selected.get(0));
                                dialogInterface.cancel();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listener.canceled();
                                dialogInterface.cancel();
                            }
                        })
                        .show()
                ;
            }
        });
    }
}
