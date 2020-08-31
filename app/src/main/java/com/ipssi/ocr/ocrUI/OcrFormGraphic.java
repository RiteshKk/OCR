package com.ipssi.ocr.ocrUI;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.ipssi.ocr.camera.GraphicOverlay;
import com.ipssi.ocr.ocrparser.Document;
import com.ipssi.ocr.ocrparser.FormField;
import com.ipssi.ocr.ocrparser.FormFieldConfig;

import java.util.ArrayList;


/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrFormGraphic extends GraphicOverlay.Graphic {

    private int id;

    private static final int TEXT_COLOR = Color.RED;

    private static Paint rectPaint;
    private static Paint textPaint;
//    private TextBlock textBlock;
//    private Line textLine;
    private ArrayList<FormField> formFields;
    private Document doc;
    Paint docPaint;

    OcrFormGraphic(GraphicOverlay overlay, Document doc) {
        super(overlay);
        this.doc=doc;
        this.formFields = doc.getFormFields();
        if (rectPaint == null) {
            rectPaint = new Paint();
            rectPaint.setColor(TEXT_COLOR);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);
        }
        if (docPaint==null){
            docPaint = new Paint();
            docPaint.setColor(Color.BLUE);
            docPaint.setTextSize(54.0f);

        }

        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setColor(TEXT_COLOR);
            textPaint.setTextSize(54.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        if (formFields == null) {
            return false;
        }
//        RectF rect = this.rect;//Praveen
//
//        rect = translateRect(rect);
        return true;//rect.contains(x, y);
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (formFields == null) {
            return;
        }

        canvas.drawText("Doc : "+doc.getName(), 500, 50, docPaint);
        for (FormField formField : formFields) {
            FormFieldConfig config = formField.getFormconfig();
            RectF rect=config.getFieldRect();
//            canvas.drawRect(rect, rectPaint);
            float left = translateX(rect.left);
            float bottom = translateY(rect.bottom);
            textPaint.setTextSize(50.0f);
            if(formField.isComplete()) {
                textPaint.setColor(Color.GREEN);
                textPaint.setUnderlineText(true);
            }else{
                textPaint.setColor(Color.RED);
                textPaint.setUnderlineText(false);
            }
            canvas.drawText(config.getFormFieldName()+":", rect.left, rect.bottom, textPaint);
            canvas.drawText(formField.getValueBestPossible()==null?"":formField.getValueBestPossible(), rect.left, rect.bottom+50, textPaint);
        }
    }

}
