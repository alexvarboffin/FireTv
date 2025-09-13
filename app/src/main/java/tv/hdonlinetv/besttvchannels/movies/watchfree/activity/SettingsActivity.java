package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import static tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager.TYPE_GRID;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.walhalla.ui.plugins.Launcher;

import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig;
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivitySettingsBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.io.File;
import java.text.DecimalFormat;

public class SettingsActivity extends BaseActivity {


    private ActivitySettingsBinding binding;
    private PrefManager prefManager;
    private String[] sortOptions;
    private String[] modeOptions;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        prefManager = new PrefManager(this);
        sortOptions = new String[]{
                getString(R.string.sort_by_name_asc),
                getString(R.string.sort_by_name_desc),
                getString(R.string.sort_by_id_asc),
                getString(R.string.sort_by_id_desc)
        };

        modeOptions = new String[]{
                getString(R.string.option_details_activity),
                getString(R.string.option_player_activity)
        };

        binding.tvCurrentVersion.setText(BuildConfig.VERSION_NAME);
        binding.tvSaveLocation.setText(getResources().getString(R.string.storagelocation) + getResources().getString(R.string.app_name));
        binding.tvCacheValue.setText(getResources().getString(R.string.label_cache) + readableFileSize(getDirSize(getCacheDir()) + getDirSize(getExternalCacheDir())));
        binding.tvNotificationTag.setText(getResources().getString(R.string.label_notification) + getResources().getString(R.string.app_name));

        if (prefManager.getChannelDisplayItemType().equals("")) {
            binding.tvColumns.setText(R.string.type_grid_layout);
        } else {
            binding.tvColumns.setText(prefManager.getChannelDisplayItemType());
        }

        binding.linearLayoutClearCache.setOnClickListener(v -> clearCache());
        binding.linearLayoutPolicyPrivacy.setOnClickListener(v -> {
            //startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            Launcher.openBrowser(this, getString(R.string.url_privacy_policy));
        });
        binding.linearLayoutColumes.setOnClickListener(v ->
                createAlertDialog(this, prefManager));

        int checkedItem = prefManager.getSortOption();
        binding.tvSort.setText(sortOptions[checkedItem]);
        binding.linearLayoutSort.setOnClickListener(v ->
                createAlertSortDialog(this, prefManager));

        binding.linearLayoutDetailsMode.setOnClickListener(v ->
                createActivityModeDialog(this, prefManager));

        boolean isDetailsMode = prefManager.isDetailsMode();
        binding.tvDetailsMode.setText(modeOptions[isDetailsMode ? 0 : 1]);

        //binding.linearSelectPlayer.setVisibility(View.GONE);
        //@@@@binding.linearSelectPlayer.setOnClickListener(v -> showMediaPlayerDialog(this, prefManager));


        // Night Mode
        binding.switchButtonAnimation.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                prefManager.setNightModeState(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                onResume();
            } else {
                prefManager.setNightModeState(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                onResume();
            }
        });
    }


    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

    private void clearCache() {
        new Handler().postDelayed(() -> {
            binding.tvCacheValue.setText(getResources().getString(R.string.label_cache) + readableFileSize(getDirSize(getCacheDir()) + getDirSize(getExternalCacheDir())));
            Toast.makeText(SettingsActivity.this, getString(R.string.msg_cache_cleared), Toast.LENGTH_SHORT).show();
        }, 3000);
    }

    private AlertDialog dialog;

    private void createAlertSortDialog(Context context, PrefManager prefManager) {

        int checkedItem = prefManager.getSortOption();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_sort_title);
        builder.setSingleChoiceItems(sortOptions, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Сохранение выбранного значения в SharedPreferences
                        prefManager.saveSortOption(which);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applySort(prefManager.getSortOption());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        dialog = builder.create();
        dialog.show();
    }

    private void createActivityModeDialog(Context context, PrefManager prefManager) {
        boolean isDetailsMode = prefManager.isDetailsMode();// Получаем текущее состояние

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_activity_mode_title);
        builder.setSingleChoiceItems(modeOptions, isDetailsMode ? 0 : 1, (dialog, which) -> {
                    // Определяем новое значение на основе выбранного пункта
                    boolean newValue = which == 0; // 0 - DetailsActivity, 1 - PlayerActivity
                    prefManager.setActivityMode(newValue);
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // Можете добавить логику, которая сработает при нажатии "OK"
                })
                .setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void applySort(int sortOption) {

        switch (sortOption) {
            case 0:
                // вызовите selectAllChannelsByAsc()
                break;
            case 1:
                // вызовите selectAllChannelsByDESC()
                break;
            case 2:
                // вызовите selectAllChannelsByIdAsc()
                break;
            case 3:
                // вызовите selectAllChannelsByIdDESC()
                break;
            default:
                // вызовите selectAllChannelsByAsc() по умолчанию
                break;
        }

        onResume();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createAlertDialog(Context context, PrefManager prf) {

        String[] values = getResources().getStringArray(R.array.layout_options);
        int checkeditem = prf.getChannelDisplayItemTypeIndex();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_title_display_channels);
        builder.setSingleChoiceItems(values, checkeditem, (dialog, item) -> {
            switch (item) {
                case 0:
                    prf.setString(Const.KEY_COL_COUNT, TYPE_GRID);
                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, 3);
                    onResume();
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                    break;
                case 1:
                    prf.setString(Const.KEY_COL_COUNT, PrefManager.TYPE_LIST);
                    prf.setInt(Const.KEY_CHANNEL_COLUMNS, 1);
                    onResume();
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                    break;
            }
            this.dialog.dismiss();
        });
        dialog = builder.create();
        dialog.show();
    }

    private void showMediaPlayerDialog(Context context, PrefManager prf) {
        String[] mediaPlayerOptions = {
                getString(R.string.media_player_JZMediaSystem),//default
                getString(R.string.media_player_jz_aliyun),
                getString(R.string.media_player_jz_exo),
                //getString(R.string.media_player_jz_ijk)
        };

        int checkedItem = prf.getMediaPlayerOption();

        new AlertDialog.Builder(this)
                .setTitle(R.string.media_player_title)
                .setSingleChoiceItems(mediaPlayerOptions, checkedItem, (dialog, which) ->
                        prf.saveMediaPlayer(which))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int m = prf.getMediaPlayerOption();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyMediaPlayer(int mediaPlayerOption) {
        switch (mediaPlayerOption) {
            case 0:
                // Инициализация JZMediaAliyun
                //JZMediaAliyun();
                break;
            case 1:
                // Инициализация JZMediaExo
                //JZMediaExo();
                break;
            case 2:
                // Инициализация JZMediaIjk
                //JZMediaIjk();
                break;
            default:
                // Инициализация по умолчанию, если нужно
                //JZMediaAliyun();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (prefManager.getChannelDisplayItemType().equals("")) {
            binding.tvColumns.setText(R.string.type_grid_layout);
        } else {
            binding.tvColumns.setText(prefManager.getChannelDisplayItemType());
        }

        if (prefManager.loadNightModeState()) {
            binding.switchButtonAnimation.setChecked(true);
        } else {
            binding.switchButtonAnimation.setChecked(false);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initCheck();
    }

    private void initCheck() {
        if (prefManager.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);   // set status text dark
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}