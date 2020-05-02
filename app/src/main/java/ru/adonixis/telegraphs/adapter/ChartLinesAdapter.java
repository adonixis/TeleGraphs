package ru.adonixis.telegraphs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import ru.adonixis.telegraphs.databinding.ItemChartLineBinding;
import ru.adonixis.telegraphs.model.Line;

public class ChartLinesAdapter extends RecyclerView.Adapter<ChartLinesAdapter.ChartLineViewHolder> {

    private final List<Line> lines;
    private final OnChartLineClickListener onChartLineClickListener;

    public ChartLinesAdapter(List<Line> lines, OnChartLineClickListener onChartLineClickListener) {
        this.lines = lines;
        this.onChartLineClickListener = onChartLineClickListener;
    }

    class ChartLineViewHolder extends RecyclerView.ViewHolder {

        private ItemChartLineBinding itemChartLineBinding;

        ChartLineViewHolder(View v) {
            super(v);
            itemChartLineBinding = DataBindingUtil.bind(v);
            if (itemChartLineBinding != null) {
                itemChartLineBinding.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onChartLineClickListener.onItemClick(buttonView, getAdapterPosition(), isChecked);
                    }
                });
            }
        }

        ItemChartLineBinding getBinding() {
            return itemChartLineBinding;
        }
    }

    @NonNull
    @Override
    public ChartLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemChartLineBinding itemChartLineBinding = ItemChartLineBinding.inflate(inflater, parent, false);
        return new ChartLineViewHolder(itemChartLineBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull final ChartLineViewHolder chartLineViewHolder, final int position) {
        Line chartLine = lines.get(position);
        chartLineViewHolder.getBinding().setChartLine(chartLine);
        chartLineViewHolder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return lines == null ? 0 : lines.size();
    }

}