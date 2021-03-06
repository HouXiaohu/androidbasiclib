package com.hxh.component.basicore.Base.delegate;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hxh.component.basicore.Base.delegate.interfaces.IToolBarRelated;
import com.hxh.component.basicore.Base.topbar.ActionBarConfig;
import com.hxh.component.basicore.Base.topbar.ActionBarProvider;
import com.hxh.component.basicore.CommonConponet.DefaultBackButton;
import com.hxh.component.basicore.CoreLib;
import com.hxh.component.basicore.R;
import com.hxh.component.basicore.util.AppManager;
import com.hxh.component.basicore.util.Utils;
import com.hxh.component.basicore.util.aspj.annotation.Safe;
import com.zhy.autolayout.AutoRelativeLayout;

/**
 * ToolBar相关功能
 * 两种方法开启：
 * 1. new ToolBarDelegate(传入ActionBarConfig)
 * 2. instance = new ToolBarDelegate()  -> instance.init(传入ActionBarConfig)
 */
public class ToolBarDelegate implements IToolBarRelated {

    public ToolBarDelegate() {
    }

    /**
     * 你可以new的时候不用传入ActionBarConfig,
     *
     * @param config
     */
    public ToolBarDelegate(ActionBarConfig config) {
        //        if (null == config) {
        //            throw new IllegalStateException("if you enable toolbar,Then you must provide ActionBarConfig!");
        //        }
        this.mActionbarconfig = config;
    }


    private ActionBarConfig mActionbarconfig;
    private View layoutrootView;
    private AutoRelativeLayout rootView;
    private TextView tv_title;//Toolbar的Title

    private Button btn_rightButton;//系统默认的右边布局
    private LinearLayout linear_actionbar_right;
    private ImageView actionbar_rightview_img;
    private ActionBarConfig.BackViewConfig mBackViewConfig;
    private OnToolbarClickListener clicklistener;
    private DefaultBackButton btn_defaultBackView;
    private ImageButton btn_defaultBackView_actionbar_leftview;
    private int mCurrentClickRightButtonsPosition = 0;
    private View view_splitline;
    //region 关联ToolBar到View上
    /**
     * 调用步骤：
     * 1. new 此对象
     * 2. init
     * 3. 调用此方法
     * 将toolbar关联到某个view上,必须在onCreateView中调用
     *
     * @param rootView
     */
    private Toolbar toolbar;

    public void fetchToView(View rootView, Fragment fragment) {
        fetchToolBar(rootView);
        ((AppCompatActivity) fragment.getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) fragment.getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void fetchToView(View rootView, AppCompatActivity activity) {
        fetchToolBar(rootView);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void fetchToolBar(View rootView) {

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (null == toolbar) {
            throw new IllegalStateException("There is no id is 'toolbar' view in your layout");
        }
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.addView(layoutrootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        toolbar.setVisibility(View.VISIBLE);
    }
    //endregion

    public void init(ActionBarConfig config) {
        this.mActionbarconfig = config;
        initRootView();
        initToolBar();
    }

    public void init() {
        initRootView();
        initToolBar();
    }


    private void initRootView() {
        layoutrootView = LayoutInflater.from(Utils.getApplicationContext()).inflate(R.layout.layout_default_toolbar, null);
        rootView = (AutoRelativeLayout) layoutrootView.findViewById(R.id.frame_toolbar);

        btn_defaultBackView = (DefaultBackButton) rootView.findViewById(R.id.btn_defaultback);
        btn_defaultBackView_actionbar_leftview = (ImageButton) btn_defaultBackView.findViewById(R.id.actionbar_leftview);
        actionbar_rightview_img = (ImageView) rootView.findViewById(R.id.actionbar_rightview_img);
        linear_actionbar_right = (LinearLayout) rootView.findViewById(R.id.linear_actionbar_right);
        tv_title = (TextView) rootView.findViewById(R.id.tv_title);
        btn_rightButton = (Button) rootView.findViewById(R.id.btn_rightview);
        view_splitline = rootView.findViewById(R.id.view_splitline);
    }



    private void initToolBar() {
        if (null == mActionbarconfig) {
            return;
        }
        clicklistener = new OnToolbarClickListener();
        ActionBarProvider actionBarProvider = CoreLib.getInstance().getAppComponent().globalActionBarProvider();
        //region 左边布局
        if (mActionbarconfig.isEnable_backview()) {
            //region 使用用户自定义的Backview
            if (null != mActionbarconfig.getBackViewConfig()) {
                mBackViewConfig = mActionbarconfig.getBackViewConfig();
                View view = mBackViewConfig.getBackView();
                if (null != view) {
                    view.setLayoutParams(getBackViewDefaultLayoutParam());
                    settingBackViewGravity(view);
                    rootView.addView(view);
                } else {
                    boolean isShowBackImg = false, isShowBackTitle = false;

                    Bitmap bacimg = null;
                    String bactitle = null;
                    int backviewColor;
                    if (null != mBackViewConfig.getBackBitmap()) {
                        isShowBackImg = true;
                        bacimg = mBackViewConfig.getBackBitmap();
                    }
                    if (null != mBackViewConfig.getBackTitle() && !"".equals(mBackViewConfig.getBackTitle())) {
                        isShowBackTitle = true;
                        bactitle = mBackViewConfig.getBackTitle();
                    }
                    try {
                        backviewColor = ContextCompat.getColor(rootView.getContext(), mBackViewConfig.getColor());
                    } catch (Exception e) {
                        backviewColor = mBackViewConfig.getColor();
                    }


                    btn_defaultBackView.setVisibility(View.VISIBLE);
                    btn_defaultBackView.setImageAndTextVisible(isShowBackTitle, isShowBackImg);
                    if (null == mActionbarconfig.getTitleText() || "".equals(mActionbarconfig.getTitleText())) {
                        btn_defaultBackView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    } else {
                        btn_defaultBackView.setBacViewTitleMaxNumber();
                    }
                    btn_defaultBackView.setBackView(bacimg, bactitle, backviewColor);
                    btn_defaultBackView_actionbar_leftview.setOnTouchListener(clicklistener);
                    btn_defaultBackView.setOnTouchListener(clicklistener);
                }


            }
            //endregion

            //region 使用全局BackView（由GlobalActionBarConfig 提供）,如果用户没有提供，那么将会不显示返回
            else {
                btn_defaultBackView_actionbar_leftview.setOnTouchListener(clicklistener);
                btn_defaultBackView.setOnTouchListener(clicklistener);
                //region 如果用户指定了BackColor，那么改BackImg和BackTitle
                if (1234 != mActionbarconfig.getBackViewColor()) {
                    //使用全局
                    btn_defaultBackView.setVisibility(View.VISIBLE);

                    int backviewColor = -1;
                    try {
                        backviewColor = ContextCompat.getColor(rootView.getContext(), mActionbarconfig.getBackViewColor());
                    } catch (Exception e) {
                        backviewColor = mActionbarconfig.getBackViewColor();
                    }

                    if (null != actionBarProvider) {
                        btn_defaultBackView.setBackView(actionBarProvider.getM_backImg(), actionBarProvider.getM_backTitle(), actionBarProvider.getBackColor(), backviewColor);
                    }
                }
                //endregion
                //region 如果用户没有指定BackColor，只是想用默认的，不进行任何修改
                else if (null != actionBarProvider) {

                    btn_defaultBackView.setVisibility(View.VISIBLE);
                    //使用全局，包含图标，文字，颜色风格
                    int color = 0;
                    try {
                        color = ContextCompat.getColor(rootView.getContext(), actionBarProvider.getBackColor());

                    } catch (Exception e) {
                        color = actionBarProvider.getBackColor();
                    }

                    //如果Color值是非颜色值，那么去返回图标中取主色
                    if (null != actionBarProvider.getM_backImg()) {
                        btn_defaultBackView.setBackView_noReplace(actionBarProvider.getM_backImg(), actionBarProvider.getM_backTitle(), color);
                    } else {
                        btn_defaultBackView.setBackView_noReplace(null, actionBarProvider.getM_backTitle(), color);
                    }

                }
                //endregion

            }
            //endregion
        } else {
            //关闭BackView
            btn_defaultBackView.setVisibility(View.GONE);

        }
        //endregion


        //region 右边
        if (mActionbarconfig.isEnable_rightview()) {
            if (1234 != mActionbarconfig.getRight_buttonImgResId()) {
                setActionBarRight(mActionbarconfig.getRight_buttonImgResId());
            } else if (null != mActionbarconfig.getRight_buttontitle()) {
                setActionBarRight(mActionbarconfig.getRight_buttontitle(), mActionbarconfig.getRight_buttontitleColor());
            } else if (null != mActionbarconfig.getRight_imgResIds()) {
                setActionBarRight(mActionbarconfig.getRight_imgResIds());
            }
        }
        //endregion

        //region 背景，标题
        if (null != mActionbarconfig.getTitleText()) {
            setActionBar_Title(mActionbarconfig.getTitleText());
            if (1234 != mActionbarconfig.getTitleColor()) {
                try {
                    tv_title.setTextColor(ContextCompat.getColor(rootView.getContext(), mActionbarconfig.getTitleColor()));
                } catch (Exception e) {
                    tv_title.setTextColor(mActionbarconfig.getTitleColor());
                }
            } else if (null != actionBarProvider) {
                try {
                    tv_title.setTextColor(ContextCompat.getColor(rootView.getContext(), actionBarProvider.getTitleColor()));
                } catch (Exception e) {
                    tv_title.setTextColor(actionBarProvider.getTitleColor());
                }
            }
        }

        // 背景
        if (null == mActionbarconfig.getBackgroundDrawable() && 1234 == mActionbarconfig.getBackgroundColor()) {
            if (null != actionBarProvider) {
                if (null == mActionbarconfig.getBackgroundDrawable()) {
                    try {
                        rootView.setBackgroundColor(ContextCompat.getColor(rootView.getContext(), actionBarProvider.getBackgroundColor()));
                    } catch (Exception e) {
                        rootView.setBackgroundColor(actionBarProvider.getBackgroundColor());
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 16) {
                        rootView.setBackground(actionBarProvider.getBackgroundDrawable());
                    } else
                        rootView.setBackgroundDrawable(actionBarProvider.getBackgroundDrawable());
                }
            }
        } else {
            if (null == mActionbarconfig.getBackgroundDrawable()) {
                if (mActionbarconfig.getBackgroundColor() != -1) {
                    int color = -1;
                    if (mActionbarconfig.getBackgroundColor() == 0) {
                        color = Color.WHITE;
                    } else {
                        try {
                            color = ContextCompat.getColor(rootView.getContext(), mActionbarconfig.getBackgroundColor());
                        } catch (Exception e) {
                            color = mActionbarconfig.getBackgroundColor();
                        }
                    }
                    rootView.setBackgroundColor(color);
                } else {
                    rootView.setBackgroundColor(Color.WHITE);
                }
            } else {
                if (Build.VERSION.SDK_INT >= 16) {
                    rootView.setBackground(mActionbarconfig.getBackgroundDrawable());
                } else
                    rootView.setBackgroundDrawable(mActionbarconfig.getBackgroundDrawable());
            }
        }

        //endregion

        //region 是否开启splitLine
        if (mActionbarconfig.isEnable_splitline()) {
            view_splitline.setVisibility(View.VISIBLE);
        } else {
            view_splitline.setVisibility(View.GONE);
        }

        //endregion
    }

    private class OnRightImgesClicListener implements View.OnClickListener {
        private int position;

        public OnRightImgesClicListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (null != mActionbarconfig.getListener()) {
                mActionbarconfig.getListener().onRightButtonsClick(this.position);
            }
        }
    }


    private void setActionBarRight(int resid) {
        linear_actionbar_right.setVisibility(View.VISIBLE);
        actionbar_rightview_img.setVisibility(View.VISIBLE);
        actionbar_rightview_img.setImageBitmap(Utils.BitmapUtils.getBitmap(resid, 38, 38));
        btn_rightButton.setVisibility(View.GONE);
        linear_actionbar_right.setOnClickListener(clicklistener);
        //  btn_rightButton = null;
    }

    private void setActionBarRight(Drawable resid) {
        linear_actionbar_right.setVisibility(View.VISIBLE);
        actionbar_rightview_img.setVisibility(View.VISIBLE);
        actionbar_rightview_img.setImageDrawable(resid);
        btn_rightButton.setVisibility(View.GONE);
        linear_actionbar_right.setOnClickListener(clicklistener);
        //    btn_rightButton = null;
    }

    private void setActionBarRight(int[] resids) {
        linear_actionbar_right.setVisibility(View.VISIBLE);
        actionbar_rightview_img.setVisibility(View.GONE);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(48, 48);
        param.rightMargin = 20;
        param.gravity = Gravity.CENTER_VERTICAL;
        for (int i = 0; i < resids.length; i++) {

            int resid = resids[i];
            ImageView imageView = new ImageView(rootView.getContext());
            imageView.setLayoutParams(param);

            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(resid);


            imageView.setOnClickListener(new OnRightImgesClicListener(i));
            linear_actionbar_right.addView(imageView);
        }
        //        actionbar_rightview_img = null;
        //        btn_rightButton = null;
    }

    private void setActionBarRight(String title, int titlecolor) {
        linear_actionbar_right.setVisibility(View.GONE);
        actionbar_rightview_img.setVisibility(View.GONE);
        btn_rightButton.setVisibility(View.VISIBLE);
        btn_rightButton.setText(title);
        int titleColor = 0;
        try {
            titleColor = ContextCompat.getColor(rootView.getContext(), titlecolor);
        } catch (Exception e) {
            titleColor = titlecolor;
        }
        btn_rightButton.setTextColor(titleColor);
        btn_rightButton.setOnClickListener(clicklistener);
        //        linear_actionbar_right = null;
        //        actionbar_rightview_img = null;
    }

    public void settingBackViewGravity(View view) {
        if (null != view) {
            if (view instanceof TextView) {
                ((TextView) view).setGravity(Gravity.CENTER);
            } else if (view instanceof Button) {
                ((Button) view).setGravity(Gravity.CENTER);
            }
        }
    }

    public FrameLayout.LayoutParams getBackViewDefaultLayoutParam() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = 20;
        return params;
    }


    class OnToolbarClickListener implements View.OnClickListener, View.OnTouchListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.actionbar_leftview) {

            } else if (id == R.id.btn_rightview || id == R.id.linear_actionbar_right) {
                if (null != mActionbarconfig.getListener()) {
                    mActionbarconfig.getListener().onRightClick();
                }
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if (v.getId() == R.id.btn_defaultback || v.getId() == R.id.actionbar_leftview) {
                    v.setAlpha(1f);
                    if (null != mActionbarconfig.getListener()) {
                        mActionbarconfig.getListener().onLeftClick();
                    } else {
                        AppManager.back();
                    }
                }

            }
            if (action == MotionEvent.ACTION_DOWN) {
                if (v.getId() == R.id.btn_defaultback || v.getId() == R.id.actionbar_leftview) {
                    v.setAlpha(0.5f);
                }
            }
            return true;
        }
    }


    //region 以下是你可以获得的
    public View getToolBarRootView() {
        return layoutrootView;
    }

    /**
     * 设置ActionbarTitle
     *
     * @param title
     */
    public void setActionBar_Title(String title) {
        if (!Utils.Text.isEmpty(title)) {
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText(title);
        }
    }


    public View getActionbar_rightView() {
        if (null != actionbar_rightview_img && View.VISIBLE == actionbar_rightview_img.getVisibility()) {
            return actionbar_rightview_img;
        } else if (null != btn_rightButton && View.VISIBLE == btn_rightButton.getVisibility() ) {
            return btn_rightButton;
        } else if (null != linear_actionbar_right && View.VISIBLE == linear_actionbar_right.getVisibility()) {
            return linear_actionbar_right;
        }
        return null;
    }

    public ImageView getActionbar_rightImageView() {
        return actionbar_rightview_img;
    }

    public LinearLayout getActionbar_rightImageViewButtons() {
        return linear_actionbar_right;
    }


    public TextView getActionbar_title() {
        return tv_title;
    }


    public void setActionBar_Title(TextView tv_title) {
        this.tv_title = tv_title;
    }


    public ActionBarConfig getActionBarConfig() {
        return mActionbarconfig;
    }


    @Safe
    public void setBackViewTitle(String title) {
        btn_defaultBackView.setBackText(title);
    }

    @Safe
    public void setActionbar_rightText(String text) {
        setActionBarRight(text, mActionbarconfig.getRight_buttontitleColor());
        //  btn_rightButton.setText(text);
    }

    @Safe
    public void setActionbar_rightImg(int resId) {
        setActionBarRight(resId);
        //actionbar_rightview_img.setImageResource(resId);
    }

    @Safe
    public void setActionbar_rightImg(Drawable drawable) {
        setActionBarRight(drawable);
        //actionbar_rightview_img.setImageDrawable(drawable);
    }

    public void setActionBarConfig(ActionBarConfig config) {
        this.mActionbarconfig = config;
        if (null == rootView || null == btn_rightButton || null == linear_actionbar_right) {
            init();
        } else {
            initToolBar();
        }
    }

    public void setBackViewConfig_title(String title) {
        btn_defaultBackView.setBackText(title);
    }


    //endregion
}
