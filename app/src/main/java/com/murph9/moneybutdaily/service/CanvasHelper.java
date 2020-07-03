package com.murph9.moneybutdaily.service;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class CanvasHelper {

    //https://stackoverflow.com/a/32081250/9353639
    public static void drawTextCenteredAt(Canvas canvas, Paint paint, float xPos, float yPos, String text, int color) {
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
