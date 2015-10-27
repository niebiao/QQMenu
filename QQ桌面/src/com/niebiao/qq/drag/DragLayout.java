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
	 * 状态枚举
	 */
	public static enum Status{
		Close,Open,Draging;
	}
	public interface onDragStatusChangeListener{
		void onClose();
		void onOpen();
		void onDraging(float percent);  //拖动
	}
	public void setDragStatusListener(onDragStatusChangeListener mListener){
		this.mListener=mListener;
	}
	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// a 初始化操作
		mDragHelper=ViewDragHelper.create(this, mCallback);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public DragLayout(Context context) {
		this(context,null);
	} 
	//获取状态
	public Status getStatus() {
		return mStatus;
	}
	public void setStatus(Status mStatus) {
		this.mStatus = mStatus;
	}

	ViewDragHelper.Callback mCallback=new ViewDragHelper.Callback() {
		//c 重写事件
		
		// 1 根据返回结果决定但前的child是否可以拖拽
		// pointerId 多点触控 时各个id
		//尝试捕获的view 
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return true;               //直接返回true 可以调用更多的回调方法 否则可能有的方法不能用
		}
		//view 被捕获的时候调用  但前view是可以拖拽
		@Override
		public void onViewCaptured(View capturedChild, int activePointerId) {
			super.onViewCaptured(capturedChild, activePointerId);
		}
		//可以拖拽的范围  不对拖拽进行真正的限制 仅仅决定里执行动画的数度
		@Override
		public int getViewHorizontalDragRange(View child) {
			return mRange;
		}
		// 2 根据建议值修正将要移动的（横向）位置
		//此时没用发生真正的移动
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			// child 拖拽的view
			// left 新的位置 拖动到那了哪个位置 dx 位置的变化量
			if (child==mMainContent) {
				left = fixLeft(left);
			}
			  return left;                    //横向拖拽
		}
		
		//当view位置改变的时候，处理要做的事情（跟新状态，伴随动画，重新绘制）
		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			int newLeft=left;
			//为里兼容低版本，每次修改之后，进行重绘
			if (changedView==mLeftContent) {
				//当移动左面板时 再强行放回去
				mLeftContent.layout(0, 0, 0+mWidth, 0+mHeight);
				//把当前的变化量传递给mMainContent
				
				newLeft=mMainContent.getLeft()+dx;
				//修正left
				newLeft=fixLeft(newLeft);
				mMainContent.layout(newLeft, 0,newLeft+mWidth , 0+mHeight);
			}
			//跟新状态，执行动画
			dispatchDragEvent(newLeft);
			invalidate();
		}
		
		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			//上下拖动
			return super.clampViewPositionVertical(child, top, dy);
		}
	//当view释放的时候，处理的事情（执行动画） 松手了
		//水平方向的速度 松手的数度
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
		//跟新状态 执行回调
		Status lastStatus=mStatus;
		mStatus=UpdateStatus(percent);
		if (mStatus!=lastStatus) {
			if(mStatus==Status.Close){
				//关闭状态
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
		//         伴随动画 
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
		//              1 左  缩放动画 平移动画 透明度
					// 0.0->1.0 >>> 0.5->1.0 >>> 0.5*percent+0.5
		//			mLeftContent.setScaleX(0.5f+0.5f*percent);
		//			mLeftContent.setScaleY(0.5f+0.5f*percent);
					//兼容版本 nineoldandroids-2.4.0.jar android9个属性不兼容低版本
					ViewHelper.setScaleX(mLeftContent, 0.5f+0.5f*percent);
					ViewHelper.setScaleY(mLeftContent, 0.5f+0.5f*percent);
					//平移 -width/2->0.0  
					ViewHelper.setTranslationX(mLeftContent, evaluate(percent,-mWidth/2.0f,0));
					//透明度 0.5->1.0f
					ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.5f, 1.0f));
		//             2 主 缩放动画 
					//1.0->0.8
					ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.8f));
					ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.8f));
		//             3 背景 亮度
					getBackground().setColorFilter((Integer) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), Mode.SRC_OVER);
	}

	//估值器
	public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
	//颜色变化过度 重一种颜色逐渐变为另一种颜色
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
		
		// d 2 持续平滑动画
		if (mDragHelper.continueSettling(true)) {//动画是否继续
			//如果返回true 动画继续执行
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	public  void  open() {
		open(true);
	}
	public void open(boolean isSoom) {
		if (isSoom) {
			// d 1 触发一个动画 平滑动画
			if (mDragHelper.smoothSlideViewTo(mMainContent, mRange, 0)) {
				//返回true 代表还没有移动到指定位置需要刷新界面
				// 将所有动画排列 执行
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
			// d 1 触发一个动画 平滑动画
			if (mDragHelper.smoothSlideViewTo(mMainContent, 0, 0)) {
				//返回true 代表还没有移动到指定位置需要刷新界面
				// 将所有动画排列 执行
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
	
	
	
	// b 传递触发事件
		//分发事件
//		public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
//			return false;
//			};
		//拦截事件
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			//让view 判断是否拦截
			return mDragHelper.shouldInterceptTouchEvent(ev);
		}
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			//处理事件
			try {
				mDragHelper.processTouchEvent(event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//返回true 持续接收事件
			return true;
	}
		//1 加载布局
		//  当加载完成xml后，就会执行这个方法
		@Override
		protected void onFinishInflate() {
			super.onFinishInflate();
			// 至少两个子iew 子view是iewgroup的子类
			if (getChildCount()<2) {
				throw new IllegalStateException("至少两个子view ");
			}
			if (!(getChildAt(0) instanceof ViewGroup) && !(getChildAt(1) instanceof ViewGroup)) {
				throw new IllegalArgumentException("子view是iewgroup的子类");
			}
			mLeftContent = (ViewGroup) getChildAt(0);
			mMainContent = (ViewGroup) getChildAt(1);
		}
		// 2 帐量尺寸
		//当尺寸变化的时候调用
		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			
			mHeight = getMeasuredHeight();
			mWidth = getMeasuredWidth();
			mRange = (int) (mWidth*0.6);
		}
}
