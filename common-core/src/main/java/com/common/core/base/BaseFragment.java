package com.common.core.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.common.core.R;
import com.common.core.base.action.ActivityAction;
import com.common.core.base.ibase.ILoading;
import com.common.core.base.ibase.IView;

import timber.log.Timber;


/**
 * MVVMFrame 框架基于Google官方的 JetPack 构建，在使用MVVMFrame时，需遵循一些规范：
 * <p>
 * 如果您继承使用了BaseFragment或其子类，你需要参照如下方式添加@AndroidEntryPoint注解
 *
 * @example Fragment
 * //-------------------------
 * @AndroidEntryPoint public class YourFragment extends BaseFragment {
 * <p>
 * }
 * //-------------------------
 */
public abstract class BaseFragment<VDB extends ViewDataBinding> extends Fragment implements IView, ILoading , ActivityAction {


    /**
     * 请通过 {@link #getViewDataBinding()}获取，后续版本 {@link #mBinding}可能会私有化
     */
    private VDB mBinding;

    /**
     * 请通过 {@link #getRootView()} ()}获取，后续版本 {@link #mRootView}可能会私有化
     */
    private View mRootView;

    protected static final float DEFAULT_WIDTH_RATIO = 0.85f;

    private Dialog mDialog;

    private Dialog mProgressDialog;

    private String mJumpTag;
    private long mJumpTime;

    private static final long IGNORE_INTERVAL_TIME = 500;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = createRootView(inflater, container, savedInstanceState);
        initView();
        initViewModel();
        initObserve();
        initViewClick();
        return mRootView;
    }


    public void initView() {
        if (isBinding()) {
            mBinding = DataBindingUtil.bind(getRootView());
        }
    }

    public void initViewModel() {
    }

    public void initObserve() {
    }

    public void initViewClick() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData(savedInstanceState);
    }

    /**
     * 创建 {@link #mRootView}
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    protected View createRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    /**
     * 是否使用DataBinding
     *
     * @return 默认为true 表示使用。如果为false，则不会初始化 {@link #mBinding}。
     */
    @Override
    public boolean isBinding() {
        return true;
    }

    /**
     * 获取 ViewDataBinding
     *
     * @return {@link #mBinding}
     */
    public VDB getViewDataBinding() {
        return mBinding;
    }

    /**
     * 同 {@link #getViewDataBinding()}
     *
     * @return {@link #mBinding}
     */
    public VDB getBinding() {
        return mBinding;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBinding != null) {
            mBinding.unbind();
        }
    }


    /**
     * 获取rootView
     *
     * @return {@link #mRootView}
     */
    protected View getRootView() {
        return mRootView;
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return getRootView().findViewById(id);
    }


    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }



    protected void startActivityForResult(Class<?> cls, int requestCode) {
        startActivityForResult(newIntent(cls), requestCode);
    }

    protected void startActivityForResult(Class<?> cls, int requestCode, @Nullable ActivityOptionsCompat optionsCompat) {
        Intent intent = newIntent(cls);
        if (optionsCompat != null) {
            startActivityForResult(intent, requestCode, optionsCompat.toBundle());
        } else {
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if (isIgnoreJump(intent)) {
            return;
        }
        super.startActivityForResult(intent, requestCode, options);
    }

    protected boolean isIgnoreJump(Intent intent) {
        String jumpTag;
        if (intent.getComponent() != null) {
            jumpTag = intent.getComponent().getClassName();
        } else if (intent.getAction() != null) {
            jumpTag = intent.getAction();
        } else {
            return false;
        }

        if (TextUtils.isEmpty(jumpTag)) {
            return false;
        }

        if (jumpTag.equals(mJumpTag) && mJumpTime > SystemClock.elapsedRealtime() - getIgnoreIntervalTime()) {
            Timber.d("Ignore:" + jumpTag);
            return true;
        }
        mJumpTag = jumpTag;
        mJumpTime = SystemClock.elapsedRealtime();

        return false;
    }

    protected long getIgnoreIntervalTime() {
        return IGNORE_INTERVAL_TIME;
    }
    //---------------------------------------

    protected View inflate(@LayoutRes int id) {
        return inflate(id, null);
    }

    protected View inflate(@LayoutRes int id, @Nullable ViewGroup root) {
        return LayoutInflater.from(getContext()).inflate(id, root);
    }

    protected View inflate(@LayoutRes int id, @Nullable ViewGroup root, boolean attachToRoot) {
        return LayoutInflater.from(getContext()).inflate(id, root, attachToRoot);
    }


}
