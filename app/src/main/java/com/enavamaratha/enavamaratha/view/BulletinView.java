package com.enavamaratha.enavamaratha.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import java.util.List;

/**
 * Created by win7 on 14-07-2016.
 */

public class BulletinView extends TextView implements Runnable{
    private int currentScrollX;
    private boolean isStop = false;
    private int textWidth;
    private List<String> mList= null;
    private final int REPEAT = 1;
    private int repeatCount = 0;
    private int currentNews = 0;

    public BulletinView(Context context) {
        super(context);
        init();
        // TODO Auto-generated constructor stub
    }
    public BulletinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public BulletinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init(){
        setClickable(true);
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
    }

    public void setData(List<String> mList){
    /*    if(mList == null || mList.size()==0){
            return;
        }*/
        if( mList.size()==0)
        {
            return;
        }
        this.mList = mList;
        currentNews = 0;
        String n = mList.get(currentNews);
        setText(n);
        setTag(n);
        startScroll();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        MeasureTextWidth();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if(screenState == SCREEN_STATE_ON){
            startScroll();
        }else{
            stopScroll();
        }
    }

    /**
     * 获取文字宽度
     */
    private void MeasureTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
    }

    @Override
    public void run() {


        if(textWidth < 1)
        {
            //title null api error.
            if(mList != null && mList.size() > 0)
            {
                nextNews();
            }
            else
            {
                return;
            }
        }
        currentScrollX += 1;
        scrollTo(currentScrollX, 0);
        if (isStop)
        {
            return;
        }

//					return;
                //not full a line
//		if(textWidth <= getWidth()){
                if (getScrollX() >= textWidth)
                {
                    Log.i("","getScrollX:"+getScrollX());
                    currentScrollX = -getWidth();
                    scrollTo(currentScrollX, 0);
                    if(repeatCount >= REPEAT)
                    {
                        Log.i("Bulletin Repaeat Count",""+repeatCount);
                        Log.i("REPEAT",""+REPEAT);
                        //reach max times
                        nextNews();
            }
            else
            {
                repeatCount ++;
            }

            // return;
        }
//		}else{
//			if(getScrollX() >= textWidth-getWidth()+50)
//			currentScrollX = -getWidth();
//			scrollTo(currentScrollX, 0);
//		}

        postDelayed(this, 10);
    }

    private void nextNews(){
        repeatCount = 0;
        currentNews ++;
        currentNews = currentNews%mList.size();//cycle index
        String n = mList.get(currentNews);
        Log.i("String n in Marquee",""+currentNews);
        setText(n);
        setTag(n);
//		startScroll();
    }

    // 开始滚动
    public void startScroll() {
        isStop = false;
        this.removeCallbacks(this);
        post(this);
    }
    // 停止滚动
    public void stopScroll() {
        isStop = true;
    }
}