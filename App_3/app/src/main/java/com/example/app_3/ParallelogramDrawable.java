package com.example.app_3;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

public class ParallelogramDrawable {
    public ShapeDrawable shapeDrawable;
    public Region region;
    public Paint paint;
    public Path path;

    public ParallelogramDrawable(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int color) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        path = new Path();
        // Define the path of the parallelogram
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.close();

        PathShape pathShape = new PathShape(path, Math.max(x1, Math.max(x2, Math.max(x3, x4))) - Math.min(x1, Math.min(x2, Math.min(x3, x4))),
                Math.max(y1, Math.max(y2, Math.max(y3, y4))) - Math.min(y1, Math.min(y2, Math.min(y3, y4))));
        shapeDrawable = new ShapeDrawable(pathShape);
        shapeDrawable.getPaint().set(paint);

        // Initialize region
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
    }

    public Drawable getDrawable() {
        return shapeDrawable;
    }

    public boolean contains(int x, int y) {
        return region.contains(x, y);
    }
}
