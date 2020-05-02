package ru.adonixis.telegraphs.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import ru.adonixis.telegraphs.R;
import ru.adonixis.telegraphs.model.Chart;
import ru.adonixis.telegraphs.model.Line;
import ru.adonixis.telegraphs.util.UiUtils;

public class ChartScrollerView extends View {

    private static final int CARRIAGE_SIDE_WIDTH = UiUtils.dpToPx(6);
    private static final int CARRIAGE_BAR_HEIGHT = UiUtils.dpToPx(2);
    private static final int TOUCH_SPACE = UiUtils.dpToPx(6);
    private static final int CARRIAGE_MIN_WIDTH = UiUtils.dpToPx(30);
    private static final int CHART_LINE_WIDTH = 3;

    private Chart mChart;
    private ChartView mChartView;
    private Paint mPaintChart;
    private Paint mPaintOverlay;
    private Paint mPaintCarriage;
    @ColorInt private int mOverlayColor;
    @ColorInt private int mCarriageColor;
    private int mWidth;
    private int mHeight;
    private RectF mRectCarriageLeft;
    private RectF mRectCarriageRight;
    private RectF mRectCarriageTop;
    private RectF mRectCarriageBottom;
    private RectF mRectOverlayLeft;
    private RectF mRectOverlayRight;
    private float mCarriageLeftX;
    private float mCarriageRightX;
    private float mCarriageLeftPercent;
    private float mCarriageRightPercent;
    private int mBeginIndex;
    private int mEndIndex;
    public boolean isDragCarriage = false;
    public boolean isResizeLeftCarriage = false;
    public boolean isResizeRightCarriage = false;
    private float mLastTouchX;

    private OnChangeCarriagePositionListener mOnChangeCarriagePositionListener;
    public interface OnChangeCarriagePositionListener {
        void onUpdateCarriage(float carriageLeftPercent, float carriageRightPercent);
    }

    public ChartScrollerView(Context context) {
        super(context);
        init(null);
    }

    public ChartScrollerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ChartScrollerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ChartScrollerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mOnChangeCarriagePositionListener = null;
        if (mChart == null) {
            mChart = new Chart();
        }
        mOverlayColor = Color.TRANSPARENT;
        mCarriageColor = Color.GRAY;
        mCarriageLeftPercent = 0.0f;
        mCarriageRightPercent = 100.0f;
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ChartScrollerView, 0, 0);
            mOverlayColor = typedArray.getColor(R.styleable.ChartScrollerView_overlayColor, mOverlayColor);
            mCarriageColor = typedArray.getColor(R.styleable.ChartScrollerView_carriageColor, mCarriageColor);
            mCarriageLeftPercent = Math.max(typedArray.getFloat(R.styleable.ChartScrollerView_carriageLeftPercent, mCarriageLeftPercent), mCarriageLeftPercent);
            mCarriageRightPercent = Math.min(typedArray.getFloat(R.styleable.ChartScrollerView_carriageRightPercent, mCarriageRightPercent), mCarriageRightPercent);
            typedArray.recycle();
        }

        mPaintChart = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintChart.setColor(Color.RED);
        mPaintChart.setStrokeWidth(CHART_LINE_WIDTH);
        mPaintChart.setStyle(Paint.Style.STROKE);
        mPaintChart.setStrokeCap(Paint.Cap.ROUND);

        mPaintOverlay = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOverlay.setColor(mOverlayColor);
        mPaintOverlay.setStyle(Paint.Style.FILL);

        mPaintCarriage = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCarriage.setColor(mCarriageColor);
        mPaintCarriage.setStyle(Paint.Style.FILL);

        mRectCarriageLeft = new RectF();
        mRectCarriageRight = new RectF();
        mRectCarriageTop = new RectF();
        mRectCarriageBottom = new RectF();

        mRectOverlayLeft = new RectF();
        mRectOverlayRight = new RectF();
    }

    public void setOnChangeCarriagePositionListener(OnChangeCarriagePositionListener onChangeCarriagePositionListener) {
        mOnChangeCarriagePositionListener = onChangeCarriagePositionListener;
    }

    public void setChart(Chart chart) {
        mChart = chart;
    }

    public void setChartView(ChartView chartView) {
        mChartView = chartView;
    }

    public void setCarriageInterval(float carriageLeftPercent, float carriageRightPercent) {
        mCarriageLeftPercent = carriageLeftPercent;
        mCarriageRightPercent = carriageRightPercent;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        mCarriageLeftX = mWidth * mCarriageLeftPercent / 100.0f;
        mCarriageRightX = mWidth * mCarriageRightPercent / 100.0f;
        updateCarriage();
    }

    private void updateCarriage() {
        if (mOnChangeCarriagePositionListener != null) {
            mOnChangeCarriagePositionListener.onUpdateCarriage(mCarriageLeftPercent, mCarriageRightPercent);
        }
        mCarriageLeftPercent = mCarriageLeftX * 100.0f / mWidth;
        mCarriageRightPercent = mCarriageRightX * 100.0f / mWidth;
        int oldBeginIndex = mBeginIndex;
        int oldEndIndex = mEndIndex;
        mBeginIndex = Math.max(0, Math.round(mCarriageLeftX * mChart.getXCoords().length / mWidth));
        mEndIndex = Math.min(mChart.getXCoords().length - 1, Math.round(mCarriageRightX * mChart.getXCoords().length / mWidth));
        if ((oldBeginIndex != mBeginIndex || oldEndIndex != mEndIndex) && mChartView != null) {
            mChartView.updateRange(mBeginIndex, mEndIndex);
        }
        mRectCarriageLeft.set(mCarriageLeftX, 0.0f, mCarriageLeftX + CARRIAGE_SIDE_WIDTH, mHeight);
        mRectCarriageRight.set(mCarriageRightX - CARRIAGE_SIDE_WIDTH, 0.0f, mCarriageRightX, mHeight);
        mRectCarriageTop.set(mCarriageLeftX + CARRIAGE_SIDE_WIDTH, 0.0f, mCarriageRightX - CARRIAGE_SIDE_WIDTH, CARRIAGE_BAR_HEIGHT);
        mRectCarriageBottom.set(mCarriageLeftX + CARRIAGE_SIDE_WIDTH, mHeight - CARRIAGE_BAR_HEIGHT, mCarriageRightX - CARRIAGE_SIDE_WIDTH, mHeight);
        mRectOverlayLeft.set(0, 0, mCarriageLeftX, mHeight);
        mRectOverlayRight.set(mCarriageRightX, 0, mWidth, mHeight);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (y > 0 && y < mHeight) {
                    if (x > mCarriageLeftX + CARRIAGE_SIDE_WIDTH + TOUCH_SPACE && x < mCarriageRightX - CARRIAGE_SIDE_WIDTH - TOUCH_SPACE) {
                        isDragCarriage = true;
                    } else if (x > mCarriageLeftX - TOUCH_SPACE && x < mCarriageLeftX + CARRIAGE_SIDE_WIDTH + TOUCH_SPACE) {
                        isResizeLeftCarriage = true;
                    } else if (x > mCarriageRightX - CARRIAGE_SIDE_WIDTH - TOUCH_SPACE && x < mCarriageRightX + TOUCH_SPACE) {
                        isResizeRightCarriage = true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isDragCarriage) {
                    final float dx = x - mLastTouchX;
                    if (mCarriageLeftX + dx > 0 && mCarriageLeftX + dx < mWidth && mCarriageRightX + dx > 0 && mCarriageRightX + dx < mWidth) {
                        mCarriageLeftX += dx;
                        mCarriageRightX += dx;
                        updateCarriage();
                    }
                } else if (isResizeLeftCarriage) {
                    final float dx = x - mLastTouchX;
                    if (mCarriageLeftX + dx > 0 && mCarriageLeftX + dx < mWidth && mCarriageLeftX + dx < mCarriageRightX - CARRIAGE_MIN_WIDTH) {
                        mCarriageLeftX += dx;
                        updateCarriage();
                    }
                } else if (isResizeRightCarriage) {
                    final float dx = x - mLastTouchX;
                    if (mCarriageRightX + dx > 0 && mCarriageRightX + dx < mWidth && mCarriageRightX + dx > mCarriageLeftX + CARRIAGE_MIN_WIDTH) {
                        mCarriageRightX += dx;
                        updateCarriage();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                isDragCarriage = false;
                isResizeLeftCarriage = false;
                isResizeRightCarriage = false;
                break;
            }
        }
        mLastTouchX = x;
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float maxY = getMaxY();
        float graphHeight = mHeight;
        float graphWidth = mWidth;

        if (maxY != 0) {
            int dataLength = mChart.getXCoords().length;
            float colWidth = graphWidth / (dataLength - 1);
            float prevHeight = 0;
            for (int i = 0; i < mChart.getLines().size(); i++) {
                Line line = mChart.getLines().get(i);
                if (line.isEnabled()) {
                    mPaintChart.setColor(line.getColor());
                    for (int j = 0; j < dataLength; j++) {
                        float value = line.getYCoords()[j];
                        float height = graphHeight * value / maxY;
                        if (j > 0) {
                            canvas.drawLine(
                                    (j - 1) * colWidth,
                                    graphHeight - prevHeight,
                                    j * colWidth,
                                    graphHeight - height,
                                    mPaintChart
                            );
                        }
                        prevHeight = height;
                    }
                }
            }
        }

        canvas.drawRect(mRectCarriageLeft, mPaintCarriage);
        canvas.drawRect(mRectCarriageRight, mPaintCarriage);
        canvas.drawRect(mRectCarriageTop, mPaintCarriage);
        canvas.drawRect(mRectCarriageBottom, mPaintCarriage);

        canvas.drawRect(mRectOverlayLeft, mPaintOverlay);
        canvas.drawRect(mRectOverlayRight, mPaintOverlay);
    }

    private float getMaxY() {
        float maxY = mChart.getLines().get(0).getYCoords()[0];
        for (int i = 0; i < mChart.getLines().size(); i++) {
            Line line = mChart.getLines().get(i);
            if (line.isEnabled()) {
                for (int j = 0; j < line.getYCoords().length; j++) {
                    if (line.getYCoords()[j] > maxY) {
                        maxY = line.getYCoords()[j];
                    }
                }
            }
        }
        return maxY;
    }
}