package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.walhalla.data.repository.SourceType;
import com.walhalla.ui.DLog;

import tv.hdonlinetv.besttvchannels.movies.watchfree.Const;
import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivityPlaylistManagementBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.fragment.LoadingDialogFragment;

import com.walhalla.data.repository.PlaylistManagementPresenterImpl;
import com.walhalla.ui.plugins.Launcher;

import tv.hdonlinetv.besttvchannels.movies.watchfree.presenter.PlaylistManagementView;


public class PlaylistManagementActivity extends AppCompatActivity implements PlaylistManagementView {

    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final int PICK_FILE_REQUEST_CODE = 1334;

    private ActivityPlaylistManagementBinding binding;
    private PlaylistManagementPresenterImpl presenter;

    private ActivityResultLauncher<String> filePickerLauncher;
    private Uri uri;
    private LoadingDialogFragment loadingDialog;

    // https://github.com/matjava/xtream-playlist
    public static int[] v0 = new int[]{116, 115, 105, 108, 121, 97, 108, 112, 45, 109, 97, 101, 114, 116, 120, 47, 97, 118, 97, 106, 116, 97, 109, 47, 109, 111, 99, 46, 98, 117, 104, 116, 105, 103, 47, 47, 58, 115, 112, 116, 116, 104};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Handler handler = new Handler(Looper.getMainLooper());
        presenter = new PlaylistManagementPresenterImpl(handler, this, this);


        //
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.playlist_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinnerPlaylistType.setAdapter(adapter);
        binding.spinnerPlaylistType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setUI(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedPosition = prefs.getInt(KEY_SELECTED_POSITION, 0); // 0 - позиция по умолчанию
        if (savedPosition >= 0 && savedPosition < adapter.getCount()) {
            binding.spinnerPlaylistType.setSelection(savedPosition);
        } else {
            // Если сохраненная позиция недопустима, устанавливаем значение по умолчанию
            binding.spinnerPlaylistType.setSelection(0);
        }
        setUI(0);

        // Установка слушателей для кнопок
        binding.btnSubscribe.setOnClickListener(v -> {

            resetUi();

            boolean var0 = binding.spinnerPlaylistType.getSelectedItemPosition() == 0;
            //boolean m = sourceType == SourceType.XTREAM_URL;
            if (var0) {
                if (binding.switchLocalStorage.isChecked()) {
                    String playlistName = getProfileName();
                    presenter.onSubscribeFileClick(playlistName, uri);
                } else {
                    String playlistName = getProfileName();
                    String playlistLink = binding.etPlaylistLink.getText().toString().trim();
                    presenter.onSubscribeUriClick(playlistName, playlistLink);
                }
            } else {
                String playlistLink = binding.etPlaylistLink.getText().toString().trim();

                String password = binding.passwordInput.getText().toString().trim();
                String userName = binding.usernameInput.getText().toString().trim();

                presenter.saveProfile0(
                        getProfileName(), playlistLink, userName, password);
            }

        });
        binding.cannotFind.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // Ваше действие, например, обработка введенного URL
                //String url = etPlaylistLink.getText().toString().trim();
                return true;
            }
            return false;
        });
        binding.cannotFind.setPaintFlags(binding.cannotFind.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.cannotFind.setOnClickListener(v -> {
            Launcher.openBrowser(this, dec0(v0));
        });
        binding.btnParseClipboard.setOnClickListener(v -> {
            resetUi();
            presenter.parseClipboardM3U();
        });
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

        binding.switchLocalStorage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            resetUi();
            handleFileSelector(isChecked ? SourceType.M3U_FILE : SourceType.M3U_URL);
        });

        if (Const.DEBUG) {
            binding.etPlaylistLink.setText(
                    "https://iptv-org.github.io/iptv/index.m3u"
                    //"https://iptv-org.github.io/iptv/countries/uk.m3u"
                    //"https://iptv-org.github.io/iptv/index.country.m3u"//good list
                    //"https://iptv-org.github.io/iptv/index.category.m3u"
                    //"https://fightmagick.web.app/00.m3u"
            );
        }

        binding.fileSelect.setOnClickListener(v -> {
            resetUi();
            openFilePicker();
        });
    }

    private String getProfileName() {
        return binding.etPlaylistName.getText().toString().trim();
    }

    private void setUI(int position) {
        if (position == 0) {
            handleFileSelector(binding.switchLocalStorage.isChecked() ? SourceType.M3U_FILE : SourceType.M3U_URL);
        } else {
            handleFileSelector(SourceType.XTREAM_URL);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SELECTED_POSITION, position);
        editor.apply();
    }


    public String dec0(int[] intArray) {
        char[] strArray = new char[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            strArray[i] = (char) intArray[i];
        }
        return new StringBuilder((String.valueOf(strArray))).reverse().toString();
    }

    private void resetUi() {
        binding.etPlaylistName.setError(null);
        binding.etPlaylistLink.setError(null);

        binding.passwordInput.setError(null);
        binding.usernameInput.setError(null);


    }

    @Override
    public void showValidationError(Integer playlistName, Integer playlistUrl, Integer userName, Integer password) {
        if (playlistName != null) {
            binding.etPlaylistName.setError(getString(playlistName));
        }
        if (playlistUrl != null) {
            binding.etPlaylistLink.setError(getString(playlistUrl));
        }
        if (userName != null) {
            binding.usernameInput.setError(getString(userName));
        }
        if (password != null) {
            binding.passwordInput.setError(getString(password));
        }
        //Toast.makeText(this, "@@@@"+playlistName+"@@"+playlistUrl, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError0(String localizedMessage) {
        Toast.makeText(this, "" + localizedMessage, Toast.LENGTH_LONG).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // MIME type для m3u файлов
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            if (uri != null) {
                handleFile(uri);
            }
        }
    }


    private void handleFile(Uri uri) {
        String filePath = getPathFromUri(uri);
        binding.fileSelect.setText(filePath);
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = null;
        String filePath = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            DLog.handleException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return (filePath == null) ? String.valueOf(uri) : filePath;
    }

    private void handleFileSelector(SourceType sourceType) {
        boolean m = sourceType == SourceType.XTREAM_URL;

        if (sourceType == SourceType.M3U_FILE) {
//            binding.etPlaylistLink.setText("@@@");
//            binding.etPlaylistLink.setHint("");
            binding.etPlaylistLink.setVisibility(View.GONE);
            binding.etPlaylistFile.setVisibility(View.VISIBLE);
            xtreameGone(true);
        } else if (sourceType == SourceType.M3U_URL) {
            binding.etPlaylistLink.setVisibility(View.VISIBLE);
            binding.etPlaylistFile.setVisibility(View.GONE);
            xtreameGone(true);
        } else if (m) {
            binding.etPlaylistLink.setVisibility(View.VISIBLE);//@@@@
            binding.etPlaylistFile.setVisibility(View.GONE);
            xtreameGone(false);
            //Toast.makeText(this, "@@@@", Toast.LENGTH_SHORT).show();
        }

        binding.switchLocalStorage.setVisibility(m ? View.GONE : View.VISIBLE);
        binding.tvLocalStorage.setVisibility(m ? View.GONE : View.VISIBLE);
        binding.btnParseClipboard.setVisibility(m ? View.GONE : View.VISIBLE);

    }

    private void xtreameGone(boolean b) {
        binding.xtream.setVisibility(b ? View.GONE : View.VISIBLE);
        binding.cannotFind.setVisibility(b ? View.GONE : View.VISIBLE);
    }


    @Override
    public void showPlaylistName(String name) {
        binding.etPlaylistName.setText(name);
    }


    @Override
    public void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updatePlaylistContent(String content) {

    }

    @Override
    public void showErrorToast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaylistUpdated() {

    }

    @Override
    public void showProgressBar() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialogFragment();
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
    }

    @Override
    public void hideProgressBar() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
