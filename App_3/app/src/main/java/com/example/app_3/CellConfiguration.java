package com.example.app_3;

import android.graphics.drawable.ShapeDrawable;

import java.security.PublicKey;

public class CellConfiguration {
    public String cellNo;
    public ShapeDrawable shapeDrawable;

    public CellConfiguration(String cellNumber, ShapeDrawable shape){
        this.cellNo = cellNumber;
        this.shapeDrawable =  shape;
    }

}
