package com.murph9.moneybutdaily;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.murph9.moneybutdaily.service.CanvasHelper;

import java.util.Collection;

import static android.graphics.Color.rgb;

public abstract class RectFCanvasView extends View {

    private final Paint paint = new Paint();
    protected String noDataFoundMessage;
    private Collection<Rect> bars;
    private Collection<Text> text;

    static class Rect {
        public final int colour;
        public final RectF rect;
        // TODO style like dashed
        public Rect(int colour, RectF rect) {
            this.colour = colour;
            this.rect = rect;
        }
    }

    static class Text {
        public final String text;
        public final int colour;
        public final float xPos;
        public final float yPos;

        public Text(float xPos, float yPos, String text, int colour) {
            this.text = text;
            this.colour = colour;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }

    public RectFCanvasView(Context context) {
        super(context);
        init();
    }
    public RectFCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public RectFCanvasView(Context context, AttributeSet attrs, int defStyle) {
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
                for (Rect r: RectFCanvasView.this.bars) {
                    if (r.rect.contains(event.getX(), event.getY())) {
                        rectClicked(r);
                        break;
                    }
                }
                return false;
            }
        });
    }

    protected abstract void rectClicked(Rect r);

    protected void setNoDataFoundMessage(String message) {
        this.noDataFoundMessage = message;
    }

    protected void updateRect(Collection<Rect> bars) {
        this.bars = bars;
        if (bars == null) {
            return;
        }
        this.invalidate();
    }

    protected void updateText(Collection<Text> texts) {
        this.text = texts;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        paint.setStrokeWidth(getResources().getDisplayMetrics().scaledDensity*2);

        if (bars == null || bars.size() < 1) {
            canvas.drawColor(rgb(200, 200, 200));
            CanvasHelper.drawTextCenteredAt(canvas, paint, width/2f, height/2f, noDataFoundMessage, rgb(0,0,0));
            return;
        }

        for (Rect p: this.bars) {
            paint.setColor(p.colour);
            canvas.drawRect(p.rect, paint);
        }
    }
}
