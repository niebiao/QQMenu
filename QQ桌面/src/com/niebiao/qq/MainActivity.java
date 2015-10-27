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
				//�ı�ͷ��͸����
				iv_head.setAlpha(1.0f-percent);
				
			}
			
			@Override
			public void onClose() {
				Utils.showToast(MainActivity.this, "close");
				//������ͷ��ζ�
				//iv_head.setTranslationX(translationX)
				//iv_head ƽ���ƶ�15.0������
				ObjectAnimator mAnim = ObjectAnimator.ofFloat(iv_head, "translationX", 15.0f);
				//0.5 ��
				mAnim.setDuration(700);
				//ѭ���ƶ�4Ȧ
				mAnim.setInterpolator(new CycleInterpolator(4));
				mAnim.start();
			}
		});
        //���������� ��ʱ��ֹ����������
        main.setDraglayout(dLayout); 
    }
    

}
