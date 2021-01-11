package com.murph9.moneybutdaily;

import android.annotation.SuppressLint;
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

    private final static String NO_DATA_MESSAGE = "Stacked BarGraphView: No data found";
    private final Paint paint = new Paint();
    private Pair<Float, Float> lastTouched;

    private List<Bar> bars = new LinkedList<>();
    private float maxValue;

    static class Bar {
        private final List<BarSegment> values;
        private final float maxValue;
        Bar(List<BarSegment> values) {
            this.values = values;
            Collections.sort(this.values);

            float maxValue = 0;
            for (BarSegment bs: values) {
                maxValue += bs.value;
            }
            this.maxValue = maxValue;
        }

        public List<BarSegment> getBars() {
            return this.values;
        }
        public float getMax() { return this.maxValue; }
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

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastTouched == null) {
                    return;
                }

                // based on the event, calc which segment was pressed
                final int width = getWidth();
                final int height = getHeight();

                if (bars == null || bars.size() < 1) {
                    return;
                }

                // TODO looks similar to the draw method, but don't slow down the onDraw method!
                // compute rectangles vertically across the canvas (using x% of the space)
                final float widthPercent = 0.9f;
                final int count = bars.size();
                final float x = lastTouched.first;
                final float y = lastTouched.second;
                lastTouched = null;

                for (int i = 0; i < count; i++) {
                    Bar b = bars.get(i);

                    float cur = 0;
                    for (BarSegment bs: b.getBars()) {
                        float barHeight = height*bs.value / maxValue;
                        paint.setColor(CategoryColourService.colourForCategory(bs.label));
                        RectF r = new RectF(width * (1 - widthPercent) / 2 / count + width * ((float)i) / count, cur,
                                width * ((float)i) / count + width * widthPercent / count, cur+barHeight);
                        if (r.contains(x, y)) {
                            barClickedListener.onBarClicked(bs.label + " ("+H.to2Places(bs.value) + ")");
                            return;
                        }
                        cur += barHeight;
                    }
                }
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastTouched = new Pair<>(event.getX(), event.getY());
                }
                return false;
            }
        });
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }


    public void updateBars(List<Bar> bars) {
        this.bars = bars;

        this.invalidate();
        this.maxValue = 20; //min of 20
        if (!this.bars.isEmpty()) {
            for (Bar b: bars) {
                maxValue = Math.max(maxValue, b.getMax());
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStrokeWidth(getResources().getDisplayMetrics().scaledDensity*2);

        final int width = getWidth();
        final int height = getHeight();

        if (bars == null || bars.size() < 1) {
            canvas.drawColor(rgb(200, 200, 200));
            CanvasHelper.drawTextCenteredAt(canvas, paint, width/2f, height/2f, NO_DATA_MESSAGE, rgb(0,0,0));
            return;
        }

        // compute rectangles vertically across the canvas (using x% of the space)
        final float widthPercent = 0.9f;
        final int count = bars.size();

        for (int i = 0; i < count; i++) {
            Bar b = bars.get(i);

            float cur = 0;
            for (BarSegment bs: b.getBars()) {
                float barHeight = height*bs.value / maxValue;
                paint.setColor(CategoryColourService.colourForCategory(bs.label));
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * ((float)i) / count, cur,
                        width * ((float)i) / count + width * widthPercent / count, cur+barHeight, paint);

                cur += barHeight;
            }
        }
    }

    private StackedBarGraphView.StackedBarClickedListener barClickedListener;
    public interface StackedBarClickedListener {
        void onBarClicked(String category);
    }
    public void setOnBarTouchedListener(StackedBarGraphView.StackedBarClickedListener listener) {
        this.barClickedListener = listener;
    }
}
