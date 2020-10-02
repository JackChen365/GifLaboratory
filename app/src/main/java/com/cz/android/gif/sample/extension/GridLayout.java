package com.cz.android.gif.sample.extension;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.cz.android.gif.sample.R;


/**
 * Created by cz on 15/11/30.
 *
 * A very very old view supports you display view as a grid.
 */
public class GridLayout extends ViewGroup {
    public static final int AUTO_HEIGHT=-1;
    public static final int ITEM_WIDTH = 0x00;
    public static final int HORIZONTAL_PADDING = 0x01;
    private int fixRaw;//固定列表
    private int raw;//计算列数
    private int itemWidth;//指定条目宽
    private int fixItemWidth;//保留的横向间距,不会用作计算
    private int itemHeight;//指定条目高
    private int horizontalPadding;//横向间距
    private int fixHorizontalPadding;//保留的横向间距,不会用作计算修改
    private int verticalPadding;//纵向间距
    private int itemSizeMode;
    private OnItemClickListener listener;

    public GridLayout(Context context) {
        this(context, null, 0);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridLayout);
        setFixRaw(a.getInteger(R.styleable.GridLayout_grid_fixRaw, 0));
        setItemWidth((int) a.getDimension(R.styleable.GridLayout_grid_itemWidth, 0));
        setItemHeight(a.getLayoutDimension(R.styleable.GridLayout_grid_itemHeight, 0));
        setItemHorizontalPadding((int) a.getDimension(R.styleable.GridLayout_grid_itemHorizontalPadding, 0));
        setItemVerticalPadding((int) a.getDimension(R.styleable.GridLayout_grid_itemVerticalPadding, 0));
        setItemSizeMode(a.getInt(R.styleable.GridLayout_grid_itemSizeMode, ITEM_WIDTH));
        a.recycle();
    }

    /**
     * 设置条目尺寸适应模式
     * item_width:宽度为主
     * horizontal_padding:横向边矩为主
     *
     * @param mode
     */
    public void setItemSizeMode(int mode) {
        itemSizeMode = mode;
        requestLayout();
    }


    public void setFixRaw(int fixRaw) {
        this.fixRaw = fixRaw;
        requestLayout();
    }


    public void setItemVerticalPadding(int verticalPadding) {
        this.verticalPadding = verticalPadding;
        requestLayout();
    }

    public void setItemHorizontalPadding(int horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        this.fixHorizontalPadding = horizontalPadding;
        requestLayout();
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
        this.fixItemWidth = itemWidth;
        requestLayout();
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        int childWidth, childHeight=0;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
        if (0 < fixRaw) {
            //设定raw,不设定width,则动态计算,否则,而分个数内边距
            if (0 < itemWidth) {
                //动态改变横向间距
                switch (itemSizeMode) {
                    case ITEM_WIDTH:
                        if (width < fixItemWidth * fixRaw) {
                            //缩小width宽度,以适应fixRaw
                            itemWidth = (width - (fixRaw + 1) * fixHorizontalPadding) / fixRaw;
                        } else {
                            itemWidth = fixItemWidth;
                        }
                        horizontalPadding = (width - fixRaw * itemWidth) / (fixRaw + 1);
                        break;
                    case HORIZONTAL_PADDING:
                        //当固定尺寸大小,以及固定边距超出宽时
                        //缩小width宽度,以适应fixRaw
                        horizontalPadding = fixHorizontalPadding;
                        itemWidth = (width - (fixRaw + 1) * fixHorizontalPadding) / fixRaw;
                        break;
                }
                childWidth = itemWidth;
            } else {
                childWidth = (width - (fixRaw + 1) * fixHorizontalPadding) / fixRaw;
            }
            raw = fixRaw;
        } else {
            childWidth = fixItemWidth;
            switch (itemSizeMode) {
                case ITEM_WIDTH:
                    //不设定raw,则取width
                    raw = width / childWidth;
                    if (0 < childCount && childCount < raw) {
                        raw = childCount;
                    }
                    horizontalPadding = (width - childWidth * raw) / (raw + 1);
                    break;
                case HORIZONTAL_PADDING:
                    //边距为主,自适应
                    horizontalPadding = fixHorizontalPadding;
                    raw = width / (childWidth + horizontalPadding);
                    if (width < (width - raw * fixItemWidth) - (raw + 1) * fixHorizontalPadding) {
                        //超出,列减
                        raw--;
                    }
                    itemWidth = (width - (raw + 1) * fixHorizontalPadding) / raw;
                    childWidth = itemWidth;
                    break;
            }
        }
        int row = (0 == childCount % raw) ? childCount / raw : childCount / raw + 1;//fixRaw
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int cellHeightSpec =heightMeasureSpec;
        if(AUTO_HEIGHT!=itemHeight){
            childHeight = itemHeight;
            cellHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
        }
        for (int index = 0; index < childCount; index++) {
            final View child = getChildAt(index);
            child.measure(cellWidthSpec, cellHeightSpec);
            if(0>itemHeight) {
                itemHeight=child.getMeasuredHeight();
                childHeight = itemHeight;
            }
        }
        //计算总高度
        int totalHeight = 0;
        if (0 < childCount) {
            totalHeight = paddingTop + paddingBottom + childHeight * row + verticalPadding * (row + 1);
        }
        int minHeight = getSuggestedMinimumHeight();
        if (totalHeight < minHeight) {
            totalHeight = minHeight;
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int x = paddingLeft + horizontalPadding, y = paddingTop + verticalPadding, index = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            //三种状态,靠左==leftPadding/中间horizontalPadding/靠右==rightPadding
            int itemWidth = child.getMeasuredWidth();
            int itemHeight = child.getMeasuredHeight();
//             单个条目居中
            child.layout(x, y, x + itemWidth, y + itemHeight);
            //当设置了raw时,让条目居中,否则,条目靠左
            if (index >= (raw - 1)) {
                index = 0;
                x = paddingLeft + horizontalPadding;
                y += (itemHeight + verticalPadding);
            } else {
                index++;
                x += (itemWidth + horizontalPadding);
            }
        }
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onItemClick(v, indexOfChild(v));
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }


}
