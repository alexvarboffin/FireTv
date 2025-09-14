package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.walhalla.ui.DLog;

public class BaseActivity extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DLog.d("@@@@" + getClass().getSimpleName());
    }



}
