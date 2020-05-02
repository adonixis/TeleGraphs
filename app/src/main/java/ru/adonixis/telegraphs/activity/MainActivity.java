package ru.adonixis.telegraphs.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import ru.adonixis.telegraphs.R;
import ru.adonixis.telegraphs.databinding.ActivityMainBinding;
import ru.adonixis.telegraphs.model.Chart;
import ru.adonixis.telegraphs.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static final String KEY_LIGHT_MODE = "LIGHT_MODE";
    public static final String EXTRA_CHART = "CHART";
    private boolean mLightMode;
    private ActivityMainBinding mActivityMainBinding;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLightMode = sharedPrefs.getBoolean(KEY_LIGHT_MODE, true);
        if (mLightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);
        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mActivityMainBinding.toolbar);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mViewModel.getChartsLiveData().observe(this, new Observer<List<Chart>>() {
            @Override
            public void onChanged(@Nullable final List<Chart> charts) {
                if (charts != null) {
                    String[] chartLabels = new String[charts.size()];
                    for (int i = 0; i < charts.size(); i++) {
                        chartLabels[i] = "Chart " + (i + 1) + " (" + charts.get(i).getLines().size() + " lines)";
                    }
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                    mBuilder.setTitle(getString(R.string.title_choose_chart));
                    mBuilder.setSingleChoiceItems(chartLabels, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Chart chart = charts.get(which);
                            Intent intent = new Intent(MainActivity.this, ChartsActivity.class);
                            intent.putExtra(EXTRA_CHART, chart);
                            startActivity(intent);
                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
            }
        });
        mViewModel.getErrorMessageChatHistoryLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Toast.makeText(MainActivity.this, "Error: " + s, Toast.LENGTH_SHORT).show();
            }
        });
        mActivityMainBinding.btnOpenDefaultJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputStream inputStream = getResources().openRawResource(R.raw.chart_data);
                mViewModel.parseJsonCharts(inputStream);
            }
        });
        mActivityMainBinding.btnOpenJsonFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.title_select_file)), READ_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedFileUri;
            if (data != null) {
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);
                        mViewModel.parseJsonCharts(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
