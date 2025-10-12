package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.walhalla.ui.plugins.Launcher.openBrowser
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.MainActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivitySettingsBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var prefManager: PrefManager? = null
    private lateinit var sortOptions: Array<String>
    private lateinit var modeOptions: Array<String>


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        prefManager = PrefManager(this)
        sortOptions = arrayOf(
            getString(R.string.sort_by_name_asc),
            getString(R.string.sort_by_name_desc),
            getString(R.string.sort_by_id_asc),
            getString(R.string.sort_by_id_desc)
        )

        modeOptions = arrayOf(
            getString(R.string.option_details_activity),
            getString(R.string.option_player_activity)
        )

        binding.tvCurrentVersion.text = BuildConfig.VERSION_NAME
        binding.tvSaveLocation.text =
            resources.getString(R.string.storagelocation) + resources.getString(
                R.string.app_name
            )
        binding.tvCacheValue.text = resources.getString(R.string.label_cache) + readableFileSize(
            getDirSize(
                cacheDir
            ) + getDirSize(
                externalCacheDir!!
            )
        )
        binding!!.tvNotificationTag.text =
            resources.getString(R.string.label_notification) + resources.getString(
                R.string.app_name
            )

        if (prefManager!!.channelDisplayItemType == "") {
            binding!!.tvColumns.setText(R.string.type_grid_layout)
        } else {
            binding!!.tvColumns.text = prefManager!!.channelDisplayItemType
        }

        binding!!.linearLayoutClearCache.setOnClickListener { v: View? -> clearCache() }
        binding!!.linearLayoutPolicyPrivacy.setOnClickListener { v: View? ->
            //startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            openBrowser(
                this,
                getString(R.string.url_privacy_policy)
            )
        }
        binding!!.linearLayoutColumes.setOnClickListener { v: View? ->
            createAlertDialog(
                this,
                prefManager!!
            )
        }

        val checkedItem = prefManager!!.sortOption
        binding!!.tvSort.text = sortOptions[checkedItem]
        binding!!.linearLayoutSort.setOnClickListener { v: View? ->
            createAlertSortDialog(
                this,
                prefManager!!
            )
        }

        binding!!.linearLayoutDetailsMode.setOnClickListener { v: View? ->
            createActivityModeDialog(
                this,
                prefManager!!
            )
        }

        val isDetailsMode = prefManager!!.isDetailsMode
        binding!!.tvDetailsMode.text = modeOptions[if (isDetailsMode) 0 else 1]


       binding.linearSelectPlayer.setVisibility(View.VISIBLE);
        binding.linearSelectPlayer.setOnClickListener(View.OnClickListener {
            showMediaPlayerDialog(this, prefManager!!)
        })


        // Night Mode
        binding!!.switchButtonAnimation.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                prefManager!!.setNightModeState(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                onResume()
            } else {
                prefManager!!.setNightModeState(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                onResume()
            }
        }
    }


    fun getDirSize(dir: File): Long {
        var size: Long = 0
        for (file in dir.listFiles()) {
            if (file != null && file.isDirectory) {
                size += getDirSize(file)
            } else if (file != null && file.isFile) {
                size += file.length()
            }
        }
        return size
    }

    private fun clearCache() {
        Handler().postDelayed({
            binding!!.tvCacheValue.text =
                resources.getString(R.string.label_cache) + readableFileSize(
                    getDirSize(
                        cacheDir
                    ) + getDirSize(
                        externalCacheDir!!
                    )
                )
            Toast.makeText(
                this@SettingsActivity,
                getString(R.string.msg_cache_cleared),
                Toast.LENGTH_SHORT
            ).show()
        }, 3000)
    }

    private var dialog: AlertDialog? = null

    private fun createAlertSortDialog(context: Context, prefManager: PrefManager) {
        val checkedItem = prefManager.sortOption

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_sort_title)
        builder.setSingleChoiceItems(
            sortOptions, checkedItem
        ) { dialog, which -> // Сохранение выбранного значения в SharedPreferences
            prefManager.saveSortOption(which)
        }
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                applySort(
                    prefManager.sortOption
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
        dialog = builder.create()
        dialog!!.show()
    }

    private fun createActivityModeDialog(context: Context, prefManager: PrefManager) {
        val isDetailsMode = prefManager.isDetailsMode // Получаем текущее состояние

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_activity_mode_title)
        builder.setSingleChoiceItems(
            modeOptions,
            if (isDetailsMode) 0 else 1
        ) { dialog: DialogInterface?, which: Int ->
            // Определяем новое значение на основе выбранного пункта
            val newValue = which == 0 // 0 - DetailsActivity, 1 - PlayerActivity
            prefManager.setActivityMode(newValue)
        }
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface?, which: Int -> }
            .setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun applySort(sortOption: Int) {
        when (sortOption) {
            0 -> {}
            1 -> {}
            2 -> {}
            3 -> {}
            else -> {}
        }

        onResume()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun createAlertDialog(context: Context, prf: PrefManager) {
        val values = resources.getStringArray(R.array.layout_options)
        val checkeditem = prf.channelDisplayItemTypeIndex

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_title_display_channels)
        builder.setSingleChoiceItems(values, checkeditem) { dialog: DialogInterface?, item: Int ->
            when (item) {
                0 -> {
                    prf.setString(
                        Const.KEY_COL_COUNT,
                        PrefManager.TYPE_GRID
                    )
                    prf.setInt(
                        Const.KEY_CHANNEL_COLUMNS,
                        3
                    )
                    onResume()
                    startActivity(Intent(context, MainActivity::class.java))
                    finish()
                }

                1 -> {
                    prf.setString(
                        Const.KEY_COL_COUNT,
                        PrefManager.TYPE_LIST
                    )
                    prf.setInt(
                        Const.KEY_CHANNEL_COLUMNS,
                        1
                    )
                    onResume()
                    startActivity(Intent(context, MainActivity::class.java))
                    finish()
                }
            }
            this.dialog!!.dismiss()
        }
        dialog = builder.create()
        dialog!!.show()
    }

    private fun showMediaPlayerDialog(context: Context, prf: PrefManager) {
        val mediaPlayerOptions = arrayOf(
            getString(R.string.media_player_JZMediaSystem),  //default
            getString(R.string.media_player_jz_aliyun),
            getString(R.string.media_player_jz_exo),  //getString(R.string.media_player_jz_ijk)
        )

        val checkedItem = prf.mediaPlayerOption

        AlertDialog.Builder(this)
            .setTitle(R.string.media_player_title)
            .setSingleChoiceItems(
                mediaPlayerOptions, checkedItem
            ) { dialog: DialogInterface?, which: Int -> prf.saveMediaPlayer(which) }
            .setPositiveButton(
                android.R.string.ok
            ) { dialog: DialogInterface?, which: Int ->
                val m = prf.mediaPlayerOption
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyMediaPlayer(mediaPlayerOption: Int) {
        when (mediaPlayerOption) {
            0 -> {}
            1 -> {}
            2 -> {}
            else -> {}
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


    public override fun onResume() {
        super.onResume()
        if (prefManager!!.channelDisplayItemType == "") {
            binding.tvColumns.setText(R.string.type_grid_layout)
        } else {
            binding.tvColumns.text = prefManager!!.channelDisplayItemType
        }

        if (prefManager!!.loadNightModeState()) {
            binding.switchButtonAnimation.isChecked = true
        } else {
            binding.switchButtonAnimation.isChecked = false
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        initCheck()
    }

    private fun initCheck() {
        if (prefManager!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // set status text dark
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    companion object {
        fun readableFileSize(size: Long): String {
            if (size <= 0) {
                return "0 Bytes"
            }
            val units = arrayOf("Bytes", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            val stringBuilder = StringBuilder()
            val decimalFormat = DecimalFormat("#,##0.#")
            val d = size.toDouble()
            val pow = 1024.0.pow(digitGroups.toDouble())
            java.lang.Double.isNaN(d)
            stringBuilder.append(decimalFormat.format(d / pow))
            stringBuilder.append(" ")
            stringBuilder.append(units[digitGroups])
            return stringBuilder.toString()
        }
    }
}