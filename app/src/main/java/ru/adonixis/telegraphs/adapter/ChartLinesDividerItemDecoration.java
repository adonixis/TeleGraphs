package ru.adonixis.telegraphs.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.adonixis.telegraphs.R;

import static ru.adonixis.telegraphs.util.UiUtils.dpToPx;

public class ChartLinesDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public ChartLinesDividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.image_divider_gray);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft() + dpToPx(42);
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
