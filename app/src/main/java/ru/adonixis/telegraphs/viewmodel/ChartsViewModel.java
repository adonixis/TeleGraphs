package ru.adonixis.telegraphs.viewmodel;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ru.adonixis.telegraphs.model.Chart;

public class ChartsViewModel extends ViewModel {

    private MutableLiveData<Chart> chartLiveData = new MutableLiveData<>();
    private MutableLiveData<Pair<Float, Float>> carriageIntervalLiveData = new MutableLiveData<>();
    private MutableLiveData<Pair<Boolean, Float>> markerLiveData = new MutableLiveData<>();

    public void setChart(Chart chart) {
        chartLiveData.setValue(chart);
    }

    public LiveData<Chart> getChart() {
        return chartLiveData;
    }

    public void setCarriageInterval(Pair<Float, Float> carriageInterval) {
        carriageIntervalLiveData.setValue(carriageInterval);
    }

    public LiveData<Pair<Float, Float>> getCarriageInterval() {
        return carriageIntervalLiveData;
    }

    public void setMarker(Pair<Boolean, Float> marker) {
        markerLiveData.setValue(marker);
    }

    public LiveData<Pair<Boolean, Float>> getMarker() {
        return markerLiveData;
    }

}
