package com.niebiao.qq;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;

import com.niebiao.qq.drag.DragLayout;
import com.niebiao.qq.drag.MyLinearLayout;
import com.niebiao.qq.drag.DragLayout.onDragStatusChangeListener;
import com.niebiao.qq.drag.utils.Utils;
import com.nineoldandroids.view.ViewHelper;


public class MainActivity extends Activity {
	private DragLayout dLayout;
	private ImageView iv_head;
	private MyLinearLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        iv_head=(ImageView) findViewById(R.id.iv_header);
        main=(MyLinearLayout) findViewById(R.id.mll);
        
        dLayout=(DragLayout) findViewById(R.id.dl);
        dLayout.setDragStatusListener(new onDragStatusChangeListener() {
			
			@Override
			public void onOpen() {
				Utils.showToast(MainActivity.this, "open");
				
			}
			
			@Override
			public void onDraging(float percent) {
				//改变头像透明度
				iv_head.setAlpha(1.0f-percent);
				
			}
			
			@Override
			public void onClose() {
				Utils.showToast(MainActivity.this, "close");
				//主界面头像晃动
				//iv_head.setTranslationX(translationX)
				//iv_head 平滑移动15.0个像素
				ObjectAnimator mAnim = ObjectAnimator.ofFloat(iv_head, "translationX", 15.0f);
				//0.5 秒
				mAnim.setDuration(700);
				//循环移动4圈
				mAnim.setInterpolator(new CycleInterpolator(4));
				mAnim.start();
			}
		});
        //监听主界面 打开时禁止滑动主界面
        main.setDraglayout(dLayout); 
    }
    

}
