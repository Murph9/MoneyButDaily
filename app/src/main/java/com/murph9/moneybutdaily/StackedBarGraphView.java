package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.murph9.moneybutdaily.service.CategoryColourService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class StackedBarGraphView extends RectFCanvasView {

    private final static String NO_DATA_MESSAGE = "Stacked BarGraphView: No data found";
    private final HashMap<Rect, BarSegment> cachedRect = new HashMap<>();

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
        noDataFoundMessage = NO_DATA_MESSAGE;
    }
    public StackedBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        noDataFoundMessage = NO_DATA_MESSAGE;
    }
    public StackedBarGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        noDataFoundMessage = NO_DATA_MESSAGE;
    }


    public void updateBars(List<Bar> bars, float maxValue) {
        this.cachedRect.clear();

        if (bars == null) {
            updateRect(null);
            return;
        }

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
                RectF rf = new RectF(width * (1 - widthPercent) / 2 / count + width * ((float)i) / count, cur,
                        width * ((float)i) / count + width * widthPercent / count, cur+barHeight);

                Rect r = new Rect(CategoryColourService.colourForCategory(p.label), rf);
                cachedRect.put(r, p);

                cur += barHeight;
            }
        }

        updateRect(cachedRect.keySet());
    }

    public void rectClicked(Rect r) {
        if (barClickedListener != null && this.cachedRect.containsKey(r)) {
            BarSegment seg = this.cachedRect.get(r);
            barClickedListener.onBarClicked(seg.label + " ("+H.to2Places(seg.value) + ")");
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
