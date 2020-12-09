package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.murph9.moneybutdaily.service.CanvasHelper;
import com.murph9.moneybutdaily.service.CategoryColourService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.graphics.Color.rgb;

public class StackedBarGraphView extends View {
    private final Paint paint = new Paint();
    private List<Bar> bars;
    private final List<Pair<BarSegment, RectF>> cachedRect = new LinkedList<>();

    static class Bar {
        private final List<BarSegment> values;
        Bar(List<BarSegment> values) {
            this.values = values;
            Collections.sort(this.values);
        }

        public List<BarSegment> getBars() {
            return this.values;
        }
    }

    static class BarSegment implements Comparable<BarSegment> {
        public final String label;
        public final Float value;
        public BarSegment(String label, float value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public int compareTo(@NonNull BarSegment barSegment) {
            return barSegment.value.compareTo(this.value); // desc
        }
    }

    public StackedBarGraphView(Context context) {
        super(context);
        init();
    }
    public StackedBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public StackedBarGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 17);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    performClick();
                    return false;
                }

                //based on the event, calc which rectangle was pressed
                for (Pair<BarSegment, RectF> r: StackedBarGraphView.this.cachedRect) {
                    if (r.second.contains(event.getX(), event.getY())) {
                        StackedBarGraphView.this.barClickedListener.onBarClicked(r.first.label + " ("+H.to2Places(r.first.value) + ")");
                        break;
                    }
                }
                return false;
            }
        });
    }

    public void updateBars(List<Bar> bars, float maxValue) {
        this.bars = bars;
        if (bars == null) {
            return;
        }

        this.cachedRect.clear();
        maxValue = H.ceilWithFactor(maxValue, (int)50);
        this.invalidate();


        // compute rectangles vertically across the canvas (using x% of the space)
        final float widthPercent = 0.9f;
        final int width = getWidth();
        final int height = getHeight();
        final int count = bars.size();
        for (int i = 0; i < count; i++) {
            Bar b = bars.get(i);

            float cur = 0;
            for (BarSegment p: b.getBars()) {
                float barHeight = height*p.value/maxValue;
                RectF r = new RectF(width * (1 - widthPercent) / 2 / count + width * ((float)i) / count, cur,
                        width * ((float)i) / count + width * widthPercent / count, cur+barHeight);
                cachedRect.add(new Pair<>(p, r));
                cur += barHeight;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        paint.setStrokeWidth(getResources().getDisplayMetrics().scaledDensity*2);

        if (bars == null || bars.size() < 1) {
            canvas.drawColor(rgb(200, 200, 200));
            CanvasHelper.drawTextCenteredAt(canvas, paint, width/2f, height/2f, "Stacked BarGraphView: No data found", rgb(0,0,0));
            return;
        }

        for (Pair<BarSegment, RectF> p: this.cachedRect) {
            paint.setColor(CategoryColourService.colourForCategory(p.first.label));
            canvas.drawRect(p.second, paint);
        }
    }

    //region listener related things
    private StackedBarGraphView.StackedBarClickedListener barClickedListener;
    public interface StackedBarClickedListener {
        void onBarClicked(String category);
    }
    public void setOnBarTouchedListener(StackedBarGraphView.StackedBarClickedListener listener) {
        this.barClickedListener = listener;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    //end region
}
