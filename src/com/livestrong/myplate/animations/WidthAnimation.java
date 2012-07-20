package com.livestrong.myplate.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class WidthAnimation extends Animation {
	int targetWidth;
    int initialWidth;
    int delta;
    View view;
    boolean isGrowing;

    public WidthAnimation(View view, int initialWidth, int targetWidth) {
        this.view = view;
        this.initialWidth = initialWidth;
        this.targetWidth = targetWidth;
        
        view.getLayoutParams().width = this.initialWidth;
        view.requestLayout();
        
        this.isGrowing = (this.targetWidth > this.initialWidth);
        
        if (isGrowing){
        	delta = targetWidth - initialWidth;
        } else {
        	delta = initialWidth - targetWidth;
        }
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newWidth;
        
        if (isGrowing){
        	newWidth = (int) (initialWidth + (interpolatedTime * delta));
        } else {
        	newWidth = (int) (targetWidth + delta * (1 - interpolatedTime));
        }

        view.getLayoutParams().width = newWidth;
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
