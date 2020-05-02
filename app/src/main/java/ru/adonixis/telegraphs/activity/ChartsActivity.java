package ru.adonixis.telegraphs.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import ru.adonixis.telegraphs.R;
import ru.adonixis.telegraphs.adapter.ChartLinesAdapter;
import ru.adonixis.telegraphs.adapter.ChartLinesDividerItemDecoration;
import ru.adonixis.telegraphs.adapter.OnChartLineClickListener;
import ru.adonixis.telegraphs.databinding.ActivityChartsBinding;
import ru.adonixis.telegraphs.model.Chart;
import ru.adonixis.telegraphs.model.Line;
import ru.adonixis.telegraphs.view.ChartScrollerView;
import ru.adonixis.telegraphs.view.ChartView;
import ru.adonixis.telegraphs.viewmodel.ChartsViewModel;

import static ru.adonixis.telegraphs.activity.MainActivity.EXTRA_CHART;

public class ChartsActivity extends AppCompatActivity {

    private static final String KEY_LIGHT_MODE = "LIGHT_MODE";
    private boolean mLightMode;
    private ActivityChartsBinding mActivityChartsBinding;
    private ChartLinesAdapter mChartLinesAdapter;
    private List<Line> mChartLines = new ArrayList<>();
    private Chart mChart;
    private float mCarriageLeftPercent;
    private float mCarriageRightPercent;
    private float mMarkerPercent;
    private boolean isMarkerVisible = false;
    private ChartsViewModel mViewModel;

    private OnChartLineClickListener onChartLineClickListener = new OnChartLineClickListener() {
        @Override
        public void onItemClick(View view, int position, boolean isChecked) {
            mChart.getLines().get(position).setEnabled(isChecked);
            mActivityChartsBinding.viewChart.invalidate();
            mActivityChartsBinding.viewChartScroller.invalidate();
        }
    };

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
        mActivityChartsBinding = DataBindingUtil.setContentView(this, R.layout.activity_charts);
        setSupportActionBar(mActivityChartsBinding.toolbar);
        mViewModel = ViewModelProviders.of(this).get(ChartsViewModel.class);
        mViewModel.getChart().observe(this, new Observer<Chart>() {
            @Override
            public void onChanged(@Nullable final Chart chart) {
                if (chart != null) {
                    mChart = chart;
                }
            }
        });
        mViewModel.getCarriageInterval().observe(this, new Observer<Pair<Float, Float>>() {
            @Override
            public void onChanged(@Nullable final Pair<Float, Float> carriageInterval) {
                if (carriageInterval != null) {
                    mCarriageLeftPercent = carriageInterval.first;
                    mCarriageRightPercent = carriageInterval.second;
                    mActivityChartsBinding.viewChartScroller.setCarriageInterval(mCarriageLeftPercent, mCarriageRightPercent);
                }
            }
        });
        mViewModel.getMarker().observe(this, new Observer<Pair<Boolean, Float>>() {
            @Override
            public void onChanged(@Nullable final Pair<Boolean, Float> marker) {
                if (marker != null) {
                    isMarkerVisible = marker.first;
                    mMarkerPercent = marker.second;
                    mActivityChartsBinding.viewChart.setMarker(isMarkerVisible, mMarkerPercent);
                }
            }
        });

        mActivityChartsBinding.recyclerLines.setHasFixedSize(true);
        mActivityChartsBinding.recyclerLines.setLayoutManager(new LinearLayoutManager(this));
        mChartLinesAdapter = new ChartLinesAdapter(mChartLines, onChartLineClickListener);
        mActivityChartsBinding.recyclerLines.setAdapter(mChartLinesAdapter);
        ChartLinesDividerItemDecoration chartLinesDividerItemDecoration = new ChartLinesDividerItemDecoration(ChartsActivity.this);
        mActivityChartsBinding.recyclerLines.addItemDecoration(chartLinesDividerItemDecoration);
        mActivityChartsBinding.scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mActivityChartsBinding.viewChartScroller.isDragCarriage ||
                        mActivityChartsBinding.viewChartScroller.isResizeLeftCarriage ||
                        mActivityChartsBinding.viewChartScroller.isResizeRightCarriage) {
                    mActivityChartsBinding.viewChartScroller.dispatchTouchEvent(event);
                    return true;
                } else if (mActivityChartsBinding.viewChart.isDragMarker) {
                    mActivityChartsBinding.viewChart.dispatchTouchEvent(event);
                    return true;
                } else {
                        return false;
                }
            }
        });
        mActivityChartsBinding.viewChartScroller.setOnChangeCarriagePositionListener(new ChartScrollerView.OnChangeCarriagePositionListener() {
            @Override
            public void onUpdateCarriage(float carriageLeftPercent, float carriageRightPercent) {
                mViewModel.setCarriageInterval(new Pair<>(carriageLeftPercent, carriageRightPercent));
            }
        });
        mActivityChartsBinding.viewChart.setOnChangeCarriagePositionListener(new ChartView.OnChangeMarkerPositionListener() {
            @Override
            public void onUpdateMarker(boolean isMarkerVisible, float markerPercent) {
                mViewModel.setMarker(new Pair<>(isMarkerVisible, markerPercent));
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            mChart = intent.getParcelableExtra(EXTRA_CHART);
            if (mChart != null) {
                mViewModel.setChart(mChart);
            }
        }
        if (mChart != null) {
            drawChart();
        }
    }

    private void drawChart() {
        List<Line> lines = mChart.getLines();
        mChartLines.clear();
        mChartLines.addAll(lines);
        mChartLinesAdapter.notifyDataSetChanged();
        mActivityChartsBinding.viewChart.setChart(mChart);
        mActivityChartsBinding.viewChart.setRange(0, mChart.getXCoords().length);
        mActivityChartsBinding.viewChartScroller.setChart(mChart);
        mActivityChartsBinding.viewChartScroller.setChartView(mActivityChartsBinding.viewChart);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_charts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_mode) {
            toggleNightMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleNightMode() {
        mLightMode = !mLightMode;
        mActivityChartsBinding.viewChartScroller.invalidate();
        mActivityChartsBinding.viewChart.invalidate();
        SharedPreferences sharedPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_LIGHT_MODE, mLightMode);
        editor.apply();
        recreate();
    }
}
