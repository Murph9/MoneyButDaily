package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.murph9.moneybutdaily.service.CanvasHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static android.graphics.Color.rgb;

public class StackedBarGraphView extends View {
    private Paint paint;
    private List<Bar> bars;
    private float maxValue;

    static class Bar {
        private final List<Pair<String, Float>> values;
        Bar(List<Pair<String, Float>> values) {
            this.values = values;
            Collections.sort(this.values, getComparator());
        }

        public List<Pair<String, Float>> getInOrder() {
            return this.values;
        }

        private static Comparator<Pair<String, Float>> getComparator() {
            return new Comparator<Pair<String, Float>>(){
                public int compare(Pair<String, Float> obj1, Pair<String, Float> obj2) {
                    return obj2.second.compareTo(obj1.second); //desc
                }
            };
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
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 17);
    }

    public void updateBars(List<Bar> bars, float maxValue) {
        this.bars = bars;
        if (bars == null) {
            return;
        }

        this.maxValue = H.ceilWithFactor(maxValue, (int)50);
        this.invalidate();
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

        //draw the entries as vertical rectangles across the canvas (using x% of the space)
        float widthPercent = 0.9f;

        int count = bars.size();
        for (int i = 0; i < count; i++) {
            Bar b = bars.get(i);

            float cur = 0;
            for (Pair<String, Float> p: b.getInOrder()) {
                paint.setColor(getColorForType(p.first));
                float barHeight = height*p.second/this.maxValue;
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * ((float)i) / count, cur,
                        width * ((float)i) / count + width * widthPercent / count, cur+barHeight, paint);
                cur += barHeight;
            }
        }
    }

    //good design i know, TODO make into a service so it at least doesn't change on one load of the application
    private static final HashMap<String, Integer> typeMap = new HashMap<>();
    private final Random colRnd = new Random();
    private Integer getColorForType(String value) {
        if (typeMap.containsKey(value))
            return typeMap.get(value);

        int col = Color.rgb(colRnd.nextInt(256), colRnd.nextInt(256), colRnd.nextInt(256));
        typeMap.put(value, col);
        return col;
    }
}
