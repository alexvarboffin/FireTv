package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.walhalla.data.repository.PlaylistManagementPresenterImpl
import com.walhalla.data.repository.SourceType
import com.walhalla.ui.DLog.handleException
import com.walhalla.ui.plugins.Launcher.openBrowser
import tv.hdonlinetv.besttvchannels.movies.watchfree.Const
import tv.hdonlinetv.besttvchannels.movies.watchfree.R
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlaylistManagementBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.LoadingDialogFragment
import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView


class PlaylistManagementActivity : AppCompatActivity(), PlaylistManagementView {
    private var binding: ActivityPlaylistManagementBinding? = null
    private var presenter: PlaylistManagementPresenterImpl? = null

    private val filePickerLauncher: ActivityResultLauncher<String>? = null
    private var uri: Uri? = null
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistManagementBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        val handler = Handler(Looper.getMainLooper())
        presenter = PlaylistManagementPresenterImpl(handler, this, this)


        //
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.playlist_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding!!.spinnerPlaylistType.adapter = adapter
        binding!!.spinnerPlaylistType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    setUI(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val savedPosition = prefs.getInt(KEY_SELECTED_POSITION, 0) // 0 - позиция по умолчанию
        if (savedPosition >= 0 && savedPosition < adapter.count) {
            binding!!.spinnerPlaylistType.setSelection(savedPosition)
        } else {
            // Если сохраненная позиция недопустима, устанавливаем значение по умолчанию
            binding!!.spinnerPlaylistType.setSelection(0)
        }
        setUI(0)

        // Установка слушателей для кнопок
        binding!!.btnSubscribe.setOnClickListener { v: View? ->
            resetUi()
            val var0 = binding!!.spinnerPlaylistType.selectedItemPosition == 0
            //boolean m = sourceType == SourceType.XTREAM_URL;
            if (var0) {
                if (binding!!.switchLocalStorage.isChecked) {
                    val playlistName = profileName
                    presenter!!.onSubscribeFileClick(playlistName, uri)
                } else {
                    val playlistName = profileName
                    val playlistLink = binding!!.etPlaylistLink.text.toString().trim { it <= ' ' }
                    presenter!!.onSubscribeUriClick(playlistName, playlistLink)
                }
            } else {
                val playlistLink = binding!!.etPlaylistLink.text.toString().trim { it <= ' ' }

                val password = binding!!.passwordInput.text.toString().trim { it <= ' ' }
                val userName = binding!!.usernameInput.text.toString().trim { it <= ' ' }

                presenter!!.saveProfile0(
                    profileName, playlistLink, userName, password
                )
            }
        }
        binding!!.cannotFind.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Ваше действие, например, обработка введенного URL
                //String url = etPlaylistLink.getText().toString().trim();
                return@setOnEditorActionListener true
            }
            false
        }
        binding!!.cannotFind.paintFlags =
            binding!!.cannotFind.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding!!.cannotFind.setOnClickListener { v: View? ->
            openBrowser(
                this,
                dec0(v0)
            )
        }
        binding!!.btnParseClipboard.setOnClickListener { v: View? ->
            resetUi()
            presenter!!.parseClipboardM3U()
        }

        //        binding.btnBackup.setOnClickListener(v -> {
//            resetUi();
//            presenter.backup();
//        });
//        binding.btnRestore.setOnClickListener(v -> {
//            resetUi();
//            presenter.restore();
//        });

        //binding.btnParseClipboard.setVisibility(View.GONE);

        //binding.btnBackup.setVisibility(View.GONE);
        //binding.btnRestore.setVisibility(View.GONE);
        //binding.switchLocalStorage.setVisibility(View.GONE);
        binding!!.switchLocalStorage.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            resetUi()
            handleFileSelector(if (isChecked) SourceType.M3U_FILE else SourceType.M3U_URL)
        }

        if (Const.DEBUG) {
            binding!!.etPlaylistLink.setText(
                "https://iptv-org.github.io/iptv/index.m3u" //"https://iptv-org.github.io/iptv/countries/uk.m3u"
                //"https://iptv-org.github.io/iptv/index.country.m3u"//good list
                //"https://iptv-org.github.io/iptv/index.category.m3u"
                //"https://fightmagick.web.app/00.m3u"
            )
        }

        binding!!.fileSelect.setOnClickListener { v: View? ->
            resetUi()
            openFilePicker()
        }
    }

    private val profileName: String
        get() = binding!!.etPlaylistName.text.toString().trim { it <= ' ' }

    private fun setUI(position: Int) {
        if (position == 0) {
            handleFileSelector(if (binding!!.switchLocalStorage.isChecked) SourceType.M3U_FILE else SourceType.M3U_URL)
        } else {
            handleFileSelector(SourceType.XTREAM_URL)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        editor.putInt(KEY_SELECTED_POSITION, position)
        editor.apply()
    }


    fun dec0(intArray: IntArray): String {
        val strArray = CharArray(intArray.size)
        for (i in intArray.indices) {
            strArray[i] = intArray[i].toChar()
        }
        return StringBuilder((String(strArray))).reverse().toString()
    }

    private fun resetUi() {
        binding!!.etPlaylistName.error = null
        binding!!.etPlaylistLink.error = null

        binding!!.passwordInput.error = null
        binding!!.usernameInput.error = null
    }

    override fun showValidationError(
        playlistName: Int?,
        playlistUrl: Int?,
        userName: Int?,
        password: Int?
    ) {
        if (playlistName != null) {
            binding!!.etPlaylistName.error = getString(playlistName)
        }
        if (playlistUrl != null) {
            binding!!.etPlaylistLink.error = getString(playlistUrl)
        }
        if (userName != null) {
            binding!!.usernameInput.error = getString(userName)
        }
        if (password != null) {
            binding!!.passwordInput.error = getString(password)
        }
        //Toast.makeText(this, "@@@@"+playlistName+"@@"+playlistUrl, Toast.LENGTH_SHORT).show();
    }

    override fun showError0(localizedMessage: String) {
        Toast.makeText(this, "" + localizedMessage, Toast.LENGTH_LONG).show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*") // MIME type для m3u файлов
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.data
            if (uri != null) {
                handleFile(uri!!)
            }
        }
    }


    private fun handleFile(uri: Uri) {
        val filePath = getPathFromUri(uri)
        binding!!.fileSelect.text = filePath
    }

    private fun getPathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        var cursor: Cursor? = null
        var filePath: String? = null
        try {
            cursor = contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                filePath = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            handleException(e)
        } finally {
            cursor?.close()
        }
        return filePath ?: uri.toString()
    }

    private fun handleFileSelector(sourceType: SourceType) {
        val m = sourceType == SourceType.XTREAM_URL

        if (sourceType == SourceType.M3U_FILE) {
//            binding.etPlaylistLink.setText("@@@");
//            binding.etPlaylistLink.setHint("");
            binding!!.etPlaylistLink.visibility = View.GONE
            binding!!.etPlaylistFile.visibility = View.VISIBLE
            xtreameGone(true)
        } else if (sourceType == SourceType.M3U_URL) {
            binding!!.etPlaylistLink.visibility = View.VISIBLE
            binding!!.etPlaylistFile.visibility = View.GONE
            xtreameGone(true)
        } else if (m) {
            binding!!.etPlaylistLink.visibility = View.VISIBLE //@@@@
            binding!!.etPlaylistFile.visibility = View.GONE
            xtreameGone(false)
            //Toast.makeText(this, "@@@@", Toast.LENGTH_SHORT).show();
        }

        binding!!.switchLocalStorage.visibility =
            if (m) View.GONE else View.VISIBLE
        binding!!.tvLocalStorage.visibility =
            if (m) View.GONE else View.VISIBLE
        binding!!.btnParseClipboard.visibility =
            if (m) View.GONE else View.VISIBLE
    }

    private fun xtreameGone(b: Boolean) {
        binding!!.xtream.visibility = if (b) View.GONE else View.VISIBLE
        binding!!.cannotFind.visibility =
            if (b) View.GONE else View.VISIBLE
    }


    override fun showPlaylistName(name: String) {
        binding!!.etPlaylistName.setText(name)
    }


    override fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun updatePlaylistContent(content: String) {
    }

    override fun showErrorToast(res: Int) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    }

    override fun onPlaylistUpdated() {
    }

    override fun showProgressBar() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogFragment()
            loadingDialog!!.isCancelable = false
        }
        loadingDialog!!.show(supportFragmentManager, "loading_dialog")
    }

    override fun hideProgressBar() {
        if (loadingDialog != null) {
            loadingDialog!!.dismiss()
        }
    }


    override fun onDestroy() {
        if (loadingDialog != null) {
            loadingDialog!!.dismiss()
        }
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object {
        private const val KEY_SELECTED_POSITION = "selected_position"
        private const val PICK_FILE_REQUEST_CODE = 1334

        // https://github.com/matjava/xtream-playlist
        var v0: IntArray = intArrayOf(
            116,
            115,
            105,
            108,
            121,
            97,
            108,
            112,
            45,
            109,
            97,
            101,
            114,
            116,
            120,
            47,
            97,
            118,
            97,
            106,
            116,
            97,
            109,
            47,
            109,
            111,
            99,
            46,
            98,
            117,
            104,
            116,
            105,
            103,
            47,
            47,
            58,
            115,
            112,
            116,
            116,
            104
        )
    }
}
