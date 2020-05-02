package ru.adonixis.telegraphs.viewmodel;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ru.adonixis.telegraphs.model.Chart;
import ru.adonixis.telegraphs.model.Line;

public class MainViewModel extends ViewModel {

    private static final String JSON_COLUMNS = "columns";
    private static final String JSON_TYPES = "types";
    private static final String JSON_NAMES = "names";
    private static final String JSON_COLORS = "colors";
    private static final String JSON_TYPE_X = "x";
    private static final String JSON_TYPE_LINE = "line";

    private MutableLiveData<List<Chart>> chartsLiveData;
    private MutableLiveData<String> errorMessageChartsLiveData;

    public LiveData<List<Chart>> getChartsLiveData() {
        if (chartsLiveData == null) {
            chartsLiveData = new MutableLiveData<>();
        }
        return chartsLiveData;
    }

    public LiveData<String> getErrorMessageChatHistoryLiveData() {
        errorMessageChartsLiveData = new MutableLiveData<>();
        return errorMessageChartsLiveData;
    }

    public void parseJsonCharts(InputStream inputStream) {
        List<Chart> charts = new ArrayList<>();
        String json;
        JSONArray jsonCharts;
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);

            try {
                jsonCharts = new JSONArray(json);
                for (int i = 0; i < jsonCharts.length(); i++) {
                    Chart chart = new Chart();
                    List<String> labels = new ArrayList<>();
                    List<long[]> coordsList = new ArrayList<>();
                    JSONObject jsonChart = jsonCharts.getJSONObject(i);
                    JSONArray jsonColumns = jsonChart.getJSONArray(JSON_COLUMNS);
                    for (int j = 0; j < jsonColumns.length(); j++){
                        JSONArray jsonColumn = jsonColumns.getJSONArray(j);
                        String label = jsonColumn.getString(0);
                        labels.add(label);
                        long[] coords = new long[jsonColumn.length() - 1];
                        for (int k = 1; k < jsonColumn.length(); k++){
                            long coord = jsonColumn.getLong(k);
                            coords[k - 1] = coord;
                        }
                        coordsList.add(coords);
                    }
                    JSONObject jsonTypes = jsonChart.getJSONObject(JSON_TYPES);
                    List<Line> lines = new ArrayList<>();
                    for (int j = 0; j < labels.size(); j++) {
                        String type = jsonTypes.getString(labels.get(j));
                        if (JSON_TYPE_X.equals(type)) {
                            chart.setXCoords(coordsList.get(j));
                        } else if (JSON_TYPE_LINE.equals(type)) {
                            Line line = new Line();
                            line.setLabel(labels.get(j));
                            line.setYCoords(coordsList.get(j));
                            lines.add(line);
                        }
                    }
                    JSONObject jsonNames = jsonChart.getJSONObject(JSON_NAMES);
                    JSONObject jsonColors = jsonChart.getJSONObject(JSON_COLORS);
                    for (int j = 0; j < lines.size(); j++) {
                        String name = jsonNames.getString(lines.get(j).getLabel());
                        lines.get(j).setName(name);
                        String color = jsonColors.getString(lines.get(j).getLabel());
                        lines.get(j).setColor(Color.parseColor(color));
                        lines.get(j).setEnabled(true);
                    }
                    chart.setLines(lines);
                    charts.add(chart);
                }
                chartsLiveData.setValue(charts);
            } catch (JSONException e) {
                errorMessageChartsLiveData.setValue(e.getMessage());
            }
        } catch (IOException e) {
            errorMessageChartsLiveData.setValue(e.getMessage());
        }
    }

}
