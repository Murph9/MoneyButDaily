package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.List;

import static android.graphics.Color.rgb;

public class BarGraphView extends View {

    private final DecimalFormat valueFormat = new DecimalFormat("#.##");

    private Paint paint;
    private float displayDensity;

    private List<Bar> bars;

    public static class Bar {
        final float value;
        final String label;

        public Bar (float value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    public BarGraphView(Context context) {
        super(context);
    }
    public BarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public BarGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(List<Bar> bars) {
        this.displayDensity = getResources().getDisplayMetrics().density;

        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 17);

        updateBars(bars);
    }
    public void updateBars(List<Bar> bars) {
        this.bars = bars;

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

        float maxValue = Float.MIN_VALUE;
        float minValue = Float.MAX_VALUE;
        for (Bar b: this.bars) {
            maxValue = Math.max(maxValue, b.value);
            minValue = Math.min(minValue, b.value);
        }
        //0 must be included
        maxValue = Math.max(maxValue, 0);
        minValue = Math.min(minValue, 0);


        //draw 7 vertical rectangles across the canvas (using 80% of the space)
        //TODO use this.density
        float count = 7;
        float widthPercent = 0.9f;

        float zeroPos = height*calcHeightPercentage(0, minValue, maxValue);

        for (int i = 0; i < count; i++) {
            Bar b = bars.get(i); //TODO hack, please actually work out how many to display
            paint.setColor(getColor(b.value));
            float barHeight = height*calcHeightPercentage(b.value, minValue, maxValue);
            if (b.value > 0) {
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * (i) / count, barHeight, width * i / count + width * widthPercent / count, zeroPos, paint);
            } else {
                canvas.drawRect(width * (1 - widthPercent) / 2 / count + width * (i) / count, zeroPos, width * i / count + width * widthPercent / count, barHeight, paint);
            }
            //value
            drawTextCenteredAt(canvas, paint, width*i/count + width*widthPercent/2/count, Math.max(scaledDensity * 17 * 2.5f, barHeight), valueFormat.format(bars.get(i).value), rgb(0,0,0));

            //label
            drawTextCenteredAt(canvas, paint, width*i/count + width*widthPercent/2/count, scaledDensity * 17 * 1.5f, bars.get(i).label, rgb(0,0,0)); //TODO if negative move
        }

        //draw the cross center line
        paint.setColor(rgb(0,0,0));
        paint.setStrokeWidth(2*scaledDensity);
        canvas.drawLine(0, zeroPos, width, zeroPos, paint);

        //TODO draw text for the y axis labels
        //canvas.drawText("0", zeroPos, 0, paint);
    }

    private float calcHeightPercentage(float value, float minValue, float maxValue) {
        if (maxValue - minValue == 0)
            return 0.5f;

        return (maxValue - value)/(maxValue - minValue); //TODO only works if max is always pos, and min is always negative (or either is 0)
    }

    private int getColor(float val) {
        //very negative should be red (255,0,0)
        //very positive should be green (0,255,0)
        //yellow in the middle (255,255,0)
        if (val >= 0) {
            return Color.rgb((int)((Math.atan(-val/30)/Math.PI*2 + 1)*255), 255, 0);
        } else {
            return Color.rgb(255, (int)((Math.atan(val/10)/Math.PI*2 + 1)*255), 0);
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
