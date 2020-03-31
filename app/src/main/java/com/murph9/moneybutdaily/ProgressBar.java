package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import static android.graphics.Color.rgb;

public class ProgressBar extends View {

    private final Paint paint;
    private float max;
    private float value;

    public ProgressBar(Context context) {
        super(context);
        this.paint = new Paint();
        this.paint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 17);
        this.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setValues(float max, float value) {
        this.max = max;
        this.value = value;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        paint.setColor(rgb(0,0,0));
        canvas.drawRect(0, 0, width*value/max, height, paint);

        drawText(canvas, paint, width, height/2f, H.to2Places(value, true), rgb(180, 180, 180));
    }

    private void drawText(Canvas canvas, Paint paint, float xPos, float yPos, String text, int color) {
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(color);

        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);
        canvas.drawText(text, xPos, yPos + r.height() / 2f, paint);
    }
}
