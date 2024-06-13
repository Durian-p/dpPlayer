package org.ww.dpplayer.util;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;

public class AnimUtil
{
    public static View.OnTouchListener getTouchAnimListener()
    {
        return getTouchAnimListener(1.05f);
    }

    public static View.OnTouchListener getTouchAnimListener(float scale)
    {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float rawElevation = v.getElevation();
                v.setElevation(10);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Scale up the item
                        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", scale);
                        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", scale);
                        scaleUpX.setDuration(200);
                        scaleUpY.setDuration(200);
                        scaleUpX.start();
                        scaleUpY.start();
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Scale down the item
                        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 1f);
                        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1f);
                        scaleDownX.setDuration(200);
                        scaleDownY.setDuration(200);
                        scaleDownX.start();
                        scaleDownY.start();

//                            if (event.getAction() == MotionEvent.ACTION_UP) {
//                                v.performClick();  // This will trigger the onClick event
//                            }
                        break;
                }
                v.setElevation(rawElevation);
                return false;  // Let other events (like long click) be processed
            }
        };
    }
}
