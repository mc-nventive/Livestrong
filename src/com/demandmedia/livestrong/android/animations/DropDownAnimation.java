package com.demandmedia.livestrong.android.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class DropDownAnimation extends Animation {
    int targetHeight;
    int initialHeight;
    View view;
    boolean down;

    public DropDownAnimation(View view, int targetHeight) {
        this.view = view;
        this.initialHeight = this.view.getHeight();
        this.targetHeight = targetHeight;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight;
        if (this.initialHeight < targetHeight) {
            newHeight = (int) (targetHeight * interpolatedTime);
        } else {
            newHeight = (int) (targetHeight + initialHeight * (1 - interpolatedTime));
        }
        view.getLayoutParams().height = newHeight;
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
