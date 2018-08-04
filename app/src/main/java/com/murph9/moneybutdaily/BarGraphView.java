package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.List;

import static android.graphics.Color.rgb;

public class BarGraphView extends View {

    //TODO grouping month/week labels?

    private static final DashPathEffect currentDash = new DashPathEffect(new float[] {10,10}, 5);

    private float scale;
    private HashMap<Bar, SpecialBar> specialBars;
    private List<Bar> bars;

    private Paint paint;
    private float minValue;
    private float maxValue;

    public static class Bar {
        final float value;
        final String label;

        public Bar (float value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    public enum SpecialBar {
        Future,
        Current
    }

    public BarGraphView(Context context) {
        super(context);

        this.paint = new Paint();
    }
    public BarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.paint = new Paint();
    }
    public BarGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.paint = new Paint();
    }

    public void init(float scale) {
        setColourScale(scale);

        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 17);
    }
    public void setColourScale(float scale) {
        if (this.scale <= 0)
            scale = 1; //my sanity is important
        this.scale = scale;
    }

    public void updateBars(List<Bar> bars, HashMap<Bar, SpecialBar> specials) {
        this.specialBars = specials;

        this.bars = bars;
        this.maxValue = 0; //0 must be included
        this.minValue = 0;

        if (bars == null) {
            return;
        }

        for (Bar b: this.bars) {
            maxValue = Math.max(maxValue, b.value);
            minValue = Math.min(minValue, b.value);
        }

        //round max and min to nearest 'nice' value so the bars aren't at the top of the graph
        //NOTE: isn't really noticeable
        maxValue = H.ceilWithFactor(maxValue, (int)scale);
        minValue = H.floorWithFactor(minValue, (int)scale);

        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (bars == null || bars.size() < 1) {
            canvas.drawColor(rgb(200, 200, 200));
            drawTextCenteredAt(canvas, paint, width/2, height/2, "BarGraphView: No data found", rgb(0,0,0));
            return;
        }

        //draw the entries as vertical rectangles across the canvas (using 80% of the space)
        //TODO use this.density
        float widthPercent = 0.9f;

        float zeroPos = calcHeightPercentage(0, height);

        int count = bars.size();
        for (int i = 0; i < count; i++) {
            Bar b = bars.get(i); //TODO hack, please actually work out how many to display

            SpecialBar special = null;
            if (specialBars != null && specialBars.containsKey(b))
                special = specialBars.get(b);

            paint.setColor(getColor(b.value, special));
            float barHeight = calcHeightPercentage(b.value, height);
            if (b.value > 0) {
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * (i) / count, barHeight, width * i / count + width * widthPercent / count, zeroPos, paint);
            } else {
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * (i) / count, zeroPos, width * i / count + width * widthPercent / count, barHeight, paint);
            }

            if (special == SpecialBar.Current) {
                //draw today dashed as a highlight
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4); //TODO based on density please
                paint.setColor(rgb(0,0,0));
                paint.setPathEffect(currentDash);
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * (i) / count, 0, width * i / count + width * widthPercent / count, height, paint);

                paint.setStyle(Paint.Style.FILL);
            }

            //value
            drawTextCenteredAt(canvas, paint, width*i/count + width*widthPercent/2/count, Math.max(scaledDensity * 17 * 2.5f, barHeight), H.to2Places(bars.get(i).value), rgb(0,0,0));

            //label
            drawTextCenteredAt(canvas, paint, width*i/count + width*widthPercent/2/count, scaledDensity * 17 * 1.5f, bars.get(i).label, rgb(0,0,0));
        }

        //draw the 0 cross center line
        paint.setColor(rgb(0,0,0));
        paint.setStrokeWidth(2*scaledDensity);
        canvas.drawLine(0, zeroPos, width, zeroPos, paint);
    }


    //NOTE: this only works if max is always pos, and min is always negative (or either is 0)
    private float calcHeightPercentage(float value, float height) {
        if (maxValue - minValue == 0) //no x/0 please
            return height; //bottom by default

        return height*(maxValue - value)/(maxValue - minValue);
    }

    private int getColor(float val, SpecialBar special) {
        //very negative should be red (255,0,0)
        //very positive should be green (0,255,0)
        //yellow in the middle (255,255,0)
        //see through when future

        int alpha = 255;
        if (special == SpecialBar.Current) {
            //nothing special anymore
        } else if (special == SpecialBar.Future) {
            alpha = 80;
        }

        if (val >= 0) {
            return Color.argb(alpha, (int)((Math.atan(-val/ scale)/Math.PI*2 + 1)*255), 255, 0);
        } else {
            return Color.argb(alpha, 255, (int)((Math.atan(val/(scale /3))/Math.PI*2 + 1)*255), 0);
        }
    }

    //https://stackoverflow.com/a/32081250/9353639
    private void drawTextCenteredAt(Canvas canvas, Paint paint, float xPos, float yPos, String text, int color) {
        if (text == null || text.isEmpty())
            return; //the most beautiful painting job is now done

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(color);

        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);
        float x = xPos - r.width() / 2f - r.left;
        float y = yPos - r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }
}
