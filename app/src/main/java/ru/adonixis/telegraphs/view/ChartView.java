package ru.adonixis.telegraphs.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import ru.adonixis.telegraphs.R;
import ru.adonixis.telegraphs.model.Chart;
import ru.adonixis.telegraphs.model.Line;
import ru.adonixis.telegraphs.util.UiUtils;

import static ru.adonixis.telegraphs.util.DateUtils.millisecondsToDateString;

public class ChartView extends View {

    private static final int MARGIN_BETWEEN_VER_LABELS = UiUtils.dpToPx(6);
    private static final int MARGIN_BETWEEN_HOR_LABELS = UiUtils.dpToPx(4);
    private static final int LABEL_FONT_SIZE = UiUtils.spToPx(12);
    private static final int BUBBLE_DATE_FONT_SIZE = UiUtils.spToPx(14);
    private static final int BUBBLE_VALUE_FONT_SIZE = UiUtils.spToPx(16);
    private static final int BUBBLE_LEGEND_FONT_SIZE = UiUtils.spToPx(14);
    private static final int Y_LABELS_COUNT = 6;
    private static final int X_LABELS_COUNT = 6;
    private static final int TOP_MARGIN = UiUtils.dpToPx(12);
    private static final int BOTTOM_MARGIN = UiUtils.dpToPx(28);
    private static final int BUBBLE_RADIUS = UiUtils.dpToPx(10);
    private static final int BUBBLE_TOP_MARGIN = UiUtils.dpToPx(10);
    private static final int BUBBLE_HEIGHT = UiUtils.dpToPx(88);
    private static final int BUBBLE_MIN_WIDTH = UiUtils.dpToPx(110);
    private static final int BUBBLE_TEXT_LEFT_MARGIN = UiUtils.dpToPx(14);
    private static final int BUBBLE_DATE_TOP_MARGIN = UiUtils.dpToPx(22);
    private static final int BUBBLE_VALUE_TOP_MARGIN = UiUtils.dpToPx(28);
    private static final int BUBBLE_LEGEND_TOP_MARGIN = UiUtils.dpToPx(18);
    private static final int MARKER_LINE_WIDTH = 4;
    private static final int MARKER_CIRCLE_WIDTH = 5;
    private static final int GRID_LINE_WIDTH = 2;
    private static final int CHART_LINE_WIDTH = 5;
    private static final int MARKER_CIRCLE_RADIUS = 12;

    private Chart mChart;
    private int mBeginIndex;
    private int mEndIndex;
    @ColorInt private int mLabelColor;
    @ColorInt private int mGridColor;
    @ColorInt private int mMarkerLineColor;
    @ColorInt private int mBubbleBgColor;
    @ColorInt private int mBubbleShadowColor;
    @ColorInt private int mBubbleTextDateColor;
    private Paint mPaintChart;
    private Paint mPaintGrid;
    private Paint mPaintMarkerLine;
    private Paint mPaintMarkerCircleCenter;
    private Paint mPaintMarkerCircle;
    private Paint mPaintBubble;
    private Paint mPaintBubbleTextDate;
    private Paint mPaintBubbleTextValue;
    private Paint mPaintBubbleTextLegend;
    private Paint mPaintLabel;
    private int mWidth;
    private int mHeight;
    private float mMarkerPercent;
    private int mMarkerIndex;
    private boolean isMarkerVisible;
    public boolean isDragMarker = false;
    private float mBubbleWidth = BUBBLE_MIN_WIDTH;

    private ChartView.OnChangeMarkerPositionListener mOnChangeMarkerPositionListener;
    public interface OnChangeMarkerPositionListener {
        void onUpdateMarker(boolean isMarkerVisible, float markerPercent);
    }

    public ChartView(Context context) {
        super(context);
        init(null);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (mChart == null) {
            mChart = new Chart();
        }
        mBeginIndex = 0;
        mEndIndex = 0;
        isMarkerVisible = false;
        mMarkerPercent = 0;
        mMarkerIndex = 0;
        mLabelColor = Color.GRAY;
        mMarkerLineColor = Color.GRAY;
        mGridColor = Color.GRAY;
        mBubbleBgColor = Color.GRAY;
        mBubbleShadowColor = Color.BLACK;
        mBubbleTextDateColor = Color.BLACK;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ChartView, 0, 0);
            mLabelColor = typedArray.getColor(R.styleable.ChartView_labelColor, mLabelColor);
            mMarkerLineColor = typedArray.getColor(R.styleable.ChartView_gridColor, mMarkerLineColor);
            mGridColor = typedArray.getColor(R.styleable.ChartView_markerLineColor, mGridColor);
            mBubbleBgColor = typedArray.getColor(R.styleable.ChartView_bubbleBgColor, mBubbleBgColor);
            mBubbleShadowColor = typedArray.getColor(R.styleable.ChartView_bubbleShadowColor, mBubbleShadowColor);
            mBubbleTextDateColor = typedArray.getColor(R.styleable.ChartView_bubbleTextDateColor, mBubbleTextDateColor);
            typedArray.recycle();
        }

        mPaintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setStrokeWidth(CHART_LINE_WIDTH);
        mPaintChart.setStrokeCap(Paint.Cap.ROUND);

        mPaintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(GRID_LINE_WIDTH);
        mPaintGrid.setColor(mGridColor);

        mPaintMarkerLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMarkerLine.setStyle(Paint.Style.STROKE);
        mPaintMarkerLine.setStrokeWidth(MARKER_LINE_WIDTH);
        mPaintMarkerLine.setColor(mMarkerLineColor);

        mPaintMarkerCircleCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMarkerCircleCenter.setStyle(Paint.Style.FILL);
        mPaintMarkerCircleCenter.setStrokeWidth(MARKER_CIRCLE_WIDTH);
        mPaintMarkerCircleCenter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mPaintMarkerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMarkerCircle.setStyle(Paint.Style.STROKE);
        mPaintMarkerCircle.setStrokeWidth(MARKER_CIRCLE_WIDTH);

        mPaintLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLabel.setColor(mLabelColor);
        mPaintLabel.setTextAlign(Paint.Align.LEFT);
        mPaintLabel.setTextSize(LABEL_FONT_SIZE);

        mPaintBubble = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubble.setStyle(Paint.Style.FILL);
        mPaintBubble.setColor(mBubbleBgColor);
        mPaintBubble.setShadowLayer(BUBBLE_RADIUS, 0, 0, mBubbleShadowColor);

        mPaintBubbleTextDate = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubbleTextDate.setColor(mBubbleTextDateColor);
        mPaintBubbleTextDate.setTextAlign(Paint.Align.LEFT);
        mPaintBubbleTextDate.setTextSize(BUBBLE_DATE_FONT_SIZE);
        mPaintBubbleTextDate.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mPaintBubbleTextValue = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubbleTextValue.setTextAlign(Paint.Align.LEFT);
        mPaintBubbleTextValue.setTextSize(BUBBLE_VALUE_FONT_SIZE);
        mPaintBubbleTextValue.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mPaintBubbleTextLegend = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBubbleTextLegend.setTextAlign(Paint.Align.LEFT);
        mPaintBubbleTextLegend.setTextSize(BUBBLE_LEGEND_FONT_SIZE);
    }

    public void setOnChangeCarriagePositionListener(OnChangeMarkerPositionListener onChangeMarkerPositionListener) {
        mOnChangeMarkerPositionListener = onChangeMarkerPositionListener;
    }

    public void setChart(Chart chart) {
        mChart = chart;
    }

    public void setRange(int beginIndex, int endIndex) {
        mBeginIndex = beginIndex;
        mEndIndex = endIndex;
    }

    public void updateRange(int beginIndex, int endIndex) {
        setRange(beginIndex, endIndex);
        invalidate();
    }

    public void setMarker(boolean isMarkerVisible, float markerPercent) {
        this.isMarkerVisible = isMarkerVisible;
        mMarkerPercent = markerPercent;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (y > 0 && y < mHeight && x > 0 && x < mWidth) {
                    isDragMarker = true;
                    isMarkerVisible = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isDragMarker) {
                    if (x > 0 && x < mWidth) {
                        updateMarkerPosition(x);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                isDragMarker = false;
                break;
            }
        }
        return true;
    }

    private void updateMarkerPosition(float x) {
        int oldMarkerIndex = mMarkerIndex;
        int newMarkerIndex = Math.round(mBeginIndex + x * (mEndIndex - mBeginIndex) / (float) mWidth);
        if (newMarkerIndex >= mBeginIndex && newMarkerIndex <= mEndIndex && oldMarkerIndex != newMarkerIndex) {
            mMarkerIndex = newMarkerIndex;
            mMarkerPercent = (mMarkerIndex - mBeginIndex) / ((float) mEndIndex - mBeginIndex) * 100.0f;
            if (mOnChangeMarkerPositionListener != null) {
                mOnChangeMarkerPositionListener.onUpdateMarker(isMarkerVisible, mMarkerPercent);
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float maxYValue = getMaxY();
        float graphHeight = mHeight - (TOP_MARGIN + BOTTOM_MARGIN);
        float graphWidth = mWidth;

        String yLabel;
        String xLabel;

        for (int i = 0; i < Y_LABELS_COUNT; i++) {
            float y = graphHeight / (Y_LABELS_COUNT - 1) * i + TOP_MARGIN;
            canvas.drawLine(
                    0,
                    y + MARGIN_BETWEEN_VER_LABELS,
                    graphWidth,
                    y + MARGIN_BETWEEN_VER_LABELS,
                    mPaintGrid
            );
        }

        for (int i = 0; i < X_LABELS_COUNT; i++) {
            float x = (graphWidth / X_LABELS_COUNT + MARGIN_BETWEEN_HOR_LABELS) * i;
            int index = Math.round(mBeginIndex + ((mEndIndex - mBeginIndex) / (float) (X_LABELS_COUNT - 1)) * i);
            xLabel = millisecondsToDateString(mChart.getXCoords()[index], "MMM d");
            canvas.drawText(xLabel, x, mHeight + (mPaintLabel.descent() + mPaintLabel.ascent()) / 2.0f, mPaintLabel);
        }

        if (maxYValue != 0) {
            int dataLength = mEndIndex - mBeginIndex + 1;
            float colWidth = graphWidth / (dataLength - 1);
            float prevHeight = 0;
            for (int i = 0; i < mChart.getLines().size(); i++) {
                Line line = mChart.getLines().get(i);
                if (line.isEnabled()) {
                    mPaintChart.setColor(line.getColor());
                    for (int j = 0; j < dataLength; j++) {
                        float value = line.getYCoords()[j + mBeginIndex];
                        float height = graphHeight * value / maxYValue;
                        if (j > 0) {
                            canvas.drawLine(
                                    (j - 1) * colWidth,
                                    TOP_MARGIN - prevHeight + MARGIN_BETWEEN_VER_LABELS + graphHeight,
                                    j * colWidth,
                                    TOP_MARGIN - height + MARGIN_BETWEEN_VER_LABELS + graphHeight,
                                    mPaintChart
                            );
                        }
                        prevHeight = height;
                    }
                }
            }
        }

        for (int i = 0; i < Y_LABELS_COUNT; i++) {
            float y = graphHeight / (float) (Y_LABELS_COUNT - 1) * i + TOP_MARGIN;
            yLabel = "" + Math.round(maxYValue - (maxYValue / (float) (Y_LABELS_COUNT - 1) * i));
            canvas.drawText(yLabel, 0, y, mPaintLabel);
        }
        if (isMarkerVisible) {
            int markerIndex = Math.round(mBeginIndex + (mEndIndex - mBeginIndex) * mMarkerPercent / 100.0f);
            float markerX = (markerIndex  - mBeginIndex) * mWidth / (float) (mEndIndex - mBeginIndex);
            canvas.drawLine(
                    markerX,
                    TOP_MARGIN + MARGIN_BETWEEN_VER_LABELS,
                    markerX,
                    mHeight - BOTTOM_MARGIN + MARGIN_BETWEEN_VER_LABELS,
                    mPaintMarkerLine
            );

            for (int i = 0; i < mChart.getLines().size(); i++) {
                Line line = mChart.getLines().get(i);
                if (line.isEnabled()) {
                    mPaintMarkerCircle.setColor(line.getColor());
                    float val = line.getYCoords()[markerIndex];
                    float rat = val / maxYValue;
                    float h = graphHeight * rat;
                    canvas.drawCircle(
                            markerX,
                            TOP_MARGIN - h + MARGIN_BETWEEN_VER_LABELS + graphHeight,
                            MARKER_CIRCLE_RADIUS,
                            mPaintMarkerCircleCenter
                    );
                    canvas.drawCircle(
                            markerX,
                            TOP_MARGIN - h + MARGIN_BETWEEN_VER_LABELS + graphHeight,
                            MARKER_CIRCLE_RADIUS,
                            mPaintMarkerCircle
                    );
                }
            }

            String bubbleValue;
            String bubbleLegend;
            float prevBubbleTextValueWidth = 0;
            float prevBubbleTextLegendWidth = 0;
            float maxTextWidth;
            float bubbleWidth = 0;
            String bubbleDate = millisecondsToDateString(mChart.getXCoords()[markerIndex], "EEE, MMM d");
            for (int i = 0; i < mChart.getLines().size(); i++) {
                Line line = mChart.getLines().get(i);
                if (line.isEnabled()) {
                    maxTextWidth = Math.max(prevBubbleTextValueWidth, prevBubbleTextLegendWidth);
                    bubbleValue = "" + Math.round(line.getYCoords()[markerIndex]);
                    prevBubbleTextValueWidth = mPaintBubbleTextValue.measureText(bubbleValue);
                    bubbleLegend = line.getName();
                    prevBubbleTextLegendWidth = mPaintBubbleTextLegend.measureText(bubbleLegend);
                    bubbleWidth += maxTextWidth + BUBBLE_TEXT_LEFT_MARGIN;
                }
            }
            bubbleWidth += Math.max(prevBubbleTextValueWidth, prevBubbleTextLegendWidth) + BUBBLE_TEXT_LEFT_MARGIN;
            bubbleWidth = Math.max(bubbleWidth, mPaintBubbleTextValue.measureText(bubbleDate) + BUBBLE_TEXT_LEFT_MARGIN);
            mBubbleWidth = bubbleWidth;

            float bubbleMargin = BUBBLE_TOP_MARGIN - (mMarkerPercent / 100.0f) * (2.0f * BUBBLE_TOP_MARGIN);
            float bubbleX = (mMarkerPercent / 100.0f) * (mWidth - mBubbleWidth) + bubbleMargin;

            canvas.drawRoundRect(
                    bubbleX,
                    BUBBLE_TOP_MARGIN,
                    bubbleX + mBubbleWidth,
                    BUBBLE_HEIGHT,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    mPaintBubble
            );

            float bubbleDateX = bubbleX + BUBBLE_TEXT_LEFT_MARGIN;
            float bubbleDateY = BUBBLE_TOP_MARGIN + BUBBLE_DATE_TOP_MARGIN;

            canvas.drawText(bubbleDate, bubbleDateX, bubbleDateY, mPaintBubbleTextDate);

            float bubbleValueX = bubbleX;
            float bubbleValueY = bubbleDateY + BUBBLE_VALUE_TOP_MARGIN;
            float bubbleLegendY = bubbleValueY + BUBBLE_LEGEND_TOP_MARGIN;

            prevBubbleTextValueWidth = 0;
            prevBubbleTextLegendWidth = 0;
            for (int i = 0; i < mChart.getLines().size(); i++) {
                Line line = mChart.getLines().get(i);
                if (line.isEnabled()) {
                    maxTextWidth = Math.max(prevBubbleTextValueWidth, prevBubbleTextLegendWidth);
                    bubbleValueX += maxTextWidth + BUBBLE_TEXT_LEFT_MARGIN;
                    bubbleValue = "" + Math.round(line.getYCoords()[markerIndex]);
                    prevBubbleTextValueWidth = mPaintBubbleTextValue.measureText(bubbleValue);
                    bubbleLegend = line.getName();
                    prevBubbleTextLegendWidth = mPaintBubbleTextLegend.measureText(bubbleLegend);
                    mPaintBubbleTextValue.setColor(line.getColor());
                    canvas.drawText(bubbleValue, bubbleValueX, bubbleValueY, mPaintBubbleTextValue);
                    mPaintBubbleTextLegend.setColor(line.getColor());
                    canvas.drawText(bubbleLegend, bubbleValueX, bubbleLegendY, mPaintBubbleTextLegend);
                }
            }
        }
    }

    private float getMaxY() {
        float maxY = mChart.getLines().get(0).getYCoords()[0];
        for (int i = 0; i < mChart.getLines().size(); i++) {
            Line line = mChart.getLines().get(i);
            if (line.isEnabled()) {
                for (int j = mBeginIndex; j <= mEndIndex; j++) {
                    if (line.getYCoords()[j] > maxY) {
                        maxY = line.getYCoords()[j];
                    }
                }
            }
        }
        return maxY;
    }

}