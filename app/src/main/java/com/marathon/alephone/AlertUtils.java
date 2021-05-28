package com.marathon.alephone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    private final static String DIALOGS_PREFS = "dialogsprefs";

    public static void showError(final Activity activity, final String title, final String text) {
        showWithIcon(activity, title, text, android.R.drawable.ic_dialog_alert);
    }

    public static void showInfo(final Activity activity, final String title, final String text) {
        showWithIcon(activity, title, text, android.R.drawable.ic_dialog_info);
    }

    public static boolean showOnetimeInfo(
            final String id,
            final Activity activity,
            final String title,
            final String text
    ) {
        if (!getPrefBool(activity, id, false)) {
            showWithIcon(activity, title, text, android.R.drawable.ic_dialog_info);
            setPrefBool(activity, id, true);
            return true;
        }

        return false;
    }

    private static void showWithIcon(final Activity activity, final String title, final String text, final int icon) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(activity)
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
                new MaterialAlertDialogBuilder(activity)
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

                new MaterialAlertDialogBuilder(activity)
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

    public static void showToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected static boolean getPrefBool(Context c, String key, boolean def) {
        SharedPreferences pref = c.getSharedPreferences(DIALOGS_PREFS, Context.MODE_PRIVATE);
        return pref.getBoolean(key, def);
    }

    protected static void setPrefBool(Context c, String key, boolean val) {
        SharedPreferences pref = c.getSharedPreferences(DIALOGS_PREFS, Context.MODE_PRIVATE);

        SharedPreferences.Editor e = pref.edit();
        e.putBoolean(key, val);
        e.commit();
    }
}
