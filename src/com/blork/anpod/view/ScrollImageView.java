package com.blork.anpod.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

// TODO: Auto-generated Javadoc
/**
 * The Class ScrollImageView.
 */
public class ScrollImageView extends View {
	
	/** The DEFAUL t_ padding. */
	private final int DEFAULT_PADDING = 10;
	
	/** The m display. */
	private Display mDisplay;
	
	/** The m image. */
	private Bitmap mImage;

	/* Current x and y of the touch */
	/** The m current x. */
	private float mCurrentX = 0;
	
	/** The m current y. */
	private float mCurrentY = 0;

	/** The m total x. */
	private float mTotalX = 0;
	
	/** The m total y. */
	private float mTotalY = 0;

	/* The touch distance change from the current touch */
	/** The m delta x. */
	private float mDeltaX = 0;
	
	/** The m delta y. */
	private float mDeltaY = 0;

	/** The m display width. */
	int mDisplayWidth;
	
	/** The m display height. */
	int mDisplayHeight;
	
	/** The m padding. */
	int mPadding;

	/**
	 * Instantiates a new scroll image view.
	 *
	 * @param context the context
	 */
	public ScrollImageView(Context context) {
		super(context);
		initScrollImageView(context);
	}
	
	/**
	 * Instantiates a new scroll image view.
	 *
	 * @param context the context
	 * @param attributeSet the attribute set
	 */
	public ScrollImageView(Context context, AttributeSet attributeSet) {
		super(context);
		initScrollImageView(context);
	}

	/**
	 * Inits the scroll image view.
	 *
	 * @param context the context
	 */
	private void initScrollImageView(Context context) {
		mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mPadding = DEFAULT_PADDING;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureDim(widthMeasureSpec, mDisplay.getWidth());
		int height = measureDim(heightMeasureSpec, mDisplay.getHeight());
		setMeasuredDimension(width, height);
	}

	/**
	 * Measure dim.
	 *
	 * @param measureSpec the measure spec
	 * @param size the size
	 * @return the int
	 */
	private int measureDim(int measureSpec, int size) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			result = size;
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public Bitmap getImage() {
		return mImage;
	}

	/**
	 * Sets the image.
	 *
	 * @param image the new image
	 */
	public void setImage(Bitmap image) {
		mImage = image;
	}

	/**
	 * Gets the padding.
	 *
	 * @return the padding
	 */
	public int getPadding() {
		return mPadding;
	}

	/**
	 * Sets the padding.
	 *
	 * @param padding the new padding
	 */
	public void setPadding(int padding) {
		this.mPadding = padding;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mCurrentX = event.getRawX();
			mCurrentY = event.getRawY();
		} 
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float x = event.getRawX();
			float y = event.getRawY();

			// Update how much the touch moved
			mDeltaX = x - mCurrentX;
			mDeltaY = y - mCurrentY;

			mCurrentX = x;
			mCurrentY = y;

			invalidate();
		}
		// Consume event
		return true;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		if (mImage == null) {
			return;
		}

		float newTotalX = mTotalX + mDeltaX;
		// Don't scroll off the left or right edges of the bitmap.
		if (mPadding > newTotalX && newTotalX > getMeasuredWidth() - mImage.getWidth() - mPadding)
			mTotalX += mDeltaX;

		float newTotalY = mTotalY + mDeltaY;
		// Don't scroll off the top or bottom edges of the bitmap.
		if (mPadding > newTotalY && newTotalY > getMeasuredHeight() - mImage.getHeight() - mPadding)
			mTotalY += mDeltaY;

		Paint paint = new Paint();
		canvas.drawBitmap(mImage, mTotalX, mTotalY, paint);
	}
}