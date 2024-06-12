//package org.ww.dpplayer.ui.widget;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.viewpager2.widget.ViewPager2;
//import org.jetbrains.annotations.NotNull;
//
//public class MyViewPager extends ViewPager2 {
//
//    private float initialX, initialY;
//
//    public MyViewPager(@NonNull @NotNull Context context) {
//        super(context);
//    }
//
//    public MyViewPager(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public MyViewPager(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public MyViewPager(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                initialX = event.getX();
//                initialY = event.getY();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                float diffX = event.getX() - initialX;
//                float diffY = event.getY() - initialY;
//
//                if (Math.abs(diffY) > Math.abs(diffX)) {
//                    // 垂直滑动，不拦截，让子视图处理
//                    return false;
//                }
//                break;
//        }
//        return super.onInterceptTouchEvent(event);
//    }
//}
