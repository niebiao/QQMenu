package com.niebiao.qq.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

public class DragLayout extends FrameLayout {

	private ViewDragHelper mDragHelper;
	private onDragStatusChangeListener mListener;
	private Status mStatus=Status.Close;
	
	/*
	 * ״̬ö��
	 */
	public static enum Status{
		Close,Open,Draging;
	}
	public interface onDragStatusChangeListener{
		void onClose();
		void onOpen();
		void onDraging(float percent);  //�϶�
	}
	public void setDragStatusListener(onDragStatusChangeListener mListener){
		this.mListener=mListener;
	}
	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// a ��ʼ������
		mDragHelper=ViewDragHelper.create(this, mCallback);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public DragLayout(Context context) {
		this(context,null);
	} 
	//��ȡ״̬
	public Status getStatus() {
		return mStatus;
	}
	public void setStatus(Status mStatus) {
		this.mStatus = mStatus;
	}

	ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
		//c ��д�¼�
		
		// 1 ���ݷ��ؽ��������ǰ��child�Ƿ������ק
		// pointerId ��㴥�� ʱ����id
		//���Բ����view 
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return true;               //ֱ�ӷ���true ���Ե��ø���Ļص����� ��������еķ���������
		}
		//view �������ʱ�����  ��ǰview�ǿ�����ק
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
		}
		//������ק�ķ�Χ  ������ק�������������� ����������ִ�ж���������
		@Override
		public int getViewHorizontalDragRange(View child) {
			return mRange;
		}
		// 2 ���ݽ���ֵ������Ҫ�ƶ��ģ�����λ��
		//��ʱû�÷����������ƶ�
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// child ��ק��view
			// left �µ�λ�� �϶��������ĸ�λ�� dx λ�õı仯��
			if (child==mMainContent) {
				left = fixLeft(left);
			}
			  return left;                    //������ק
		}
		
		//��viewλ�øı��ʱ�򣬴���Ҫ�������飨����״̬�����涯�������»��ƣ�
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			int newLeft=left;
			//Ϊ����ݵͰ汾��ÿ���޸�֮�󣬽����ػ�
			if (changedView==mLeftContent) {
				//���ƶ������ʱ ��ǿ�зŻ�ȥ
				mLeftContent.layout(0, 0, 0+mWidth, 0+mHeight);
				//�ѵ�ǰ�ı仯�����ݸ�mMainContent
				
				newLeft=mMainContent.getLeft()+dx;
				//����left
				newLeft=fixLeft(newLeft);
				mMainContent.layout(newLeft, 0,newLeft+mWidth , 0+mHeight);
			}
			//����״̬��ִ�ж���
			dispatchDragEvent(newLeft);
			invalidate();
		}
		
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			//�����϶�
			return super.clampViewPositionVertical(child, top, dy);
		}
	//��view�ͷŵ�ʱ�򣬴�������飨ִ�ж����� ������
		//ˮƽ������ٶ� ���ֵ�����
		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			if (xvel==0&&mMainContent.getLeft()>mRange/2.0f) {
				open();
			}else if (xvel>0) {
				open();
			} else {
				close();
			}
		}
		@Override
		public void onViewDragStateChanged(int state) {
			super.onViewDragStateChanged(state);
		}
		
	};
	private int fixLeft(int left) {
		if (left<0) {
			return 0;
		}else if (left>mRange) {
			return mRange;
		}
		return left;
	}
	
	
	private void dispatchDragEvent(int newLeft) {
		float percent=(float) (newLeft*1.0/mRange);       //0->1.0
		if (mListener!=null) {
			mListener.onDraging(percent);
		}
		//����״̬ ִ�лص�
		Status lastStatus=mStatus;
		mStatus=UpdateStatus(percent);
		if (mStatus!=lastStatus) {
			if(mStatus==Status.Close){
				//�ر�״̬
				if (mListener!=null) {
					mListener.onClose();
				}
			}else if (mStatus==Status.Open) {
				if (mListener!=null) {
					mListener.onOpen();
				}
			}else {
				if (mListener!=null) {
					mListener.onDraging(percent);
				}
			}
		}
		//         ���涯�� 
		animViews(percent);
		
	}
	
	private Status UpdateStatus(float percent) {
		if (percent==0f) {
			return Status.Close;
		}else if (percent==1.0f) {
			return Status.Open;
		}
		return Status.Draging;
	}
	private void animViews(float percent) {
		//              1 ��  ���Ŷ��� ƽ�ƶ��� ͸����
					// 0.0->1.0 >>> 0.5->1.0 >>> 0.5*percent+0.5
		//			mLeftContent.setScaleX(0.5f+0.5f*percent);
		//			mLeftContent.setScaleY(0.5f+0.5f*percent);
					//���ݰ汾 nineoldandroids-2.4.0.jar android9�����Բ����ݵͰ汾
					ViewHelper.setScaleX(mLeftContent, 0.5f+0.5f*percent);
					ViewHelper.setScaleY(mLeftContent, 0.5f+0.5f*percent);
					//ƽ�� -width/2->0.0  
					ViewHelper.setTranslationX(mLeftContent, evaluate(percent,-mWidth/2.0f,0));
					//͸���� 0.5->1.0f
					ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.5f, 1.0f));
		//             2 �� ���Ŷ��� 
					//1.0->0.8
					ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.8f));
					ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.8f));
		//             3 ���� ����
					getBackground().setColorFilter((Integer) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
	}

	//��ֵ��
	public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
	//��ɫ�仯���� ��һ����ɫ�𽥱�Ϊ��һ����ɫ
	public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }
	
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		
		// d 2 ����ƽ������
		if (mDragHelper.continueSettling(true)) {//�����Ƿ����
			//�������true ��������ִ��
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	public  void  open() {
		open(true);
	}
	public void open(boolean isSoom) {
		if (isSoom) {
			// d 1 ����һ������ ƽ������
			if (mDragHelper.smoothSlideViewTo(mMainContent, mRange, 0)) {
				//����true ����û���ƶ���ָ��λ����Ҫˢ�½���
				// �����ж������� ִ��
				ViewCompat.postInvalidateOnAnimation(this);
			}
		}else {
			mMainContent.layout(0, 0, 0+mWidth, 0+mHeight);
		}
	}
	public void close() {
		close(true);
	}
	
	public void close(boolean isSoom) {
		if (isSoom) {
			// d 1 ����һ������ ƽ������
			if (mDragHelper.smoothSlideViewTo(mMainContent, 0, 0)) {
				//����true ����û���ƶ���ָ��λ����Ҫˢ�½���
				// �����ж������� ִ��
				ViewCompat.postInvalidateOnAnimation(this);
			}
		}else {
			mMainContent.layout(0, 0, 0+mWidth, 0+mHeight);
		}
		
	}
	
	private ViewGroup mLeftContent;
	private ViewGroup mMainContent;
	private int mHeight;
	private int mWidth;
	private int mRange;
	
	
	
	// b ���ݴ����¼�
		//�ַ��¼�
//		public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
//			return false;
//			};
		//�����¼�
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			//��view �ж��Ƿ�����
			return mDragHelper.shouldInterceptTouchEvent(ev);
		}
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			//�����¼�
			try {
				mDragHelper.processTouchEvent(event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//����true ���������¼�
			return true;
	}
		//1 ���ز���
		//  ���������xml�󣬾ͻ�ִ���������
		@Override
		protected void onFinishInflate() {
			super.onFinishInflate();
			// ����������iew ��view��iewgroup������
			if (getChildCount()<2) {
				throw new IllegalStateException("����������view ");
			}
			if (!(getChildAt(0) instanceof ViewGroup) && !(getChildAt(1) instanceof ViewGroup)) {
				throw new IllegalArgumentException("��view��iewgroup������");
			}
			mLeftContent = (ViewGroup) getChildAt(0);
			mMainContent = (ViewGroup) getChildAt(1);
		}
		// 2 �����ߴ�
		//���ߴ�仯��ʱ�����
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			
			mHeight = getMeasuredHeight();
			mWidth = getMeasuredWidth();
			mRange = (int) (mWidth*0.6);
		}
}
