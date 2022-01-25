package com.arms.media.mvvm.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.arms.core.base.BaseActivity;
import com.arms.export.arouter.RouterHub;
import com.arms.export.callback.OnCameraListener;
import com.arms.export.callback.OnPhotoSelectListener;
import com.arms.media.BR;
import com.arms.media.R;
import com.arms.media.databinding.MediaImageSelectActivityBinding;
import com.arms.media.mvvm.dialog.AlbumDialog;
import com.arms.common.action.StatusAction;
import com.arms.common.adapter.BaseAdapter;
import com.arms.common.aop.Log;
import com.arms.common.aop.Permissions;
import com.arms.common.aop.SingleClick;
import com.arms.common.layout.StatusLayout;
import com.arms.common.manager.ThreadPoolManager;
import com.arms.common.view.FloatActionButton;
import com.arms.common.view.GridSpaceDecoration;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/07/24
 * desc   : 选择图片
 */
@Route(path = RouterHub.PUBLIC_MEDIA_IMAGESELECTACTIVITY)
public final class ImageSelectActivity extends BaseActivity<MediaImageSelectActivityBinding>
        implements StatusAction, Runnable,
        OnItemClickListener,
        OnItemLongClickListener,
        OnItemChildClickListener {

    private static final String INTENT_KEY_IN_MAX_SELECT = "maxSelect";

    private static final String INTENT_KEY_OUT_IMAGE_LIST = "imageList";

    @Log
    @Permissions({Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE})
    public static void start(BaseActivity activity, int maxSelect, OnPhotoSelectListener listener) {
        if (maxSelect < 1) {
            // 最少要选择一个图片
            throw new IllegalArgumentException("are you ok?");
        }
        Intent intent = new Intent(activity, ImageSelectActivity.class);
        intent.putExtra(INTENT_KEY_IN_MAX_SELECT, maxSelect);
        activity.startActivityForResult(intent, (resultCode, data) -> {

            if (listener == null) {
                return;
            }

            if (data == null) {
                listener.onCancel();
                return;
            }

            ArrayList<String> list = data.getStringArrayListExtra(INTENT_KEY_OUT_IMAGE_LIST);
            if (list == null || list.isEmpty()) {
                listener.onCancel();
                return;
            }

            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (!new File(iterator.next()).isFile()) {
                    iterator.remove();
                }
            }

            if (resultCode == RESULT_OK && !list.isEmpty()) {
                listener.onSelected(list);
                return;
            }
            listener.onCancel();
        });
    }

    private StatusLayout mStatusLayout;
    private RecyclerView mRecyclerView;
    private FloatActionButton mFloatingView;

    private BaseAdapter<String> mAdapter = new BaseAdapter<String>(R.layout.media_image_select_item, BR.item) {
        @Override
        protected void convert(@NonNull BaseDataBindingHolder<?> holder, String item) {
            super.convert(holder, item);
            ((CheckBox) holder.findView(R.id.iv_image_select_check)).setChecked(mSelectImage.contains(item));

        }
    };

    /**
     * 最大选中
     */
    private int mMaxSelect = 1;
    /**
     * 选中列表
     */
    private final ArrayList<String> mSelectImage = new ArrayList<>();

    /**
     * 全部图片
     */
    private final ArrayList<String> mAllImage = new ArrayList<>();
    /**
     * 图片专辑
     */
    private final HashMap<String, List<String>> mAllAlbum = new HashMap<>();

    /**
     * 专辑选择对话框
     */
    private AlbumDialog.Builder mAlbumDialog;

    @Override
    public int getLayoutId() {
        return R.layout.media_image_select_activity;
    }

    @Override
    public void initView() {
        if (getTitleBar() != null) {
            getTitleBar().setOnTitleBarListener(this);
        }
        mStatusLayout = findViewById(R.id.hl_image_select_hint);
        mRecyclerView = findViewById(R.id.rv_image_select_list);
        mFloatingView = findViewById(R.id.fab_image_select_floating);
        setOnClickListener(mFloatingView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mAdapter.addChildClickViewIds(R.id.fl_image_select_check);
        mAdapter.setOnItemChildClickListener(this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        // 禁用动画效果
        mRecyclerView.setItemAnimator(null);
        // 添加分割线
        mRecyclerView.addItemDecoration(new GridSpaceDecoration((int) getResources().getDimension(R.dimen.res_dp_3)));
        // 设置滚动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mFloatingView.hide();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mFloatingView.show();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void initData() {
        // 获取最大的选择数
        mMaxSelect = getInt(INTENT_KEY_IN_MAX_SELECT, mMaxSelect);

        // 显示加载进度条
        showLoading();
        // 加载图片列表
        ThreadPoolManager.getInstance().execute(this);
    }

    @Override
    public StatusLayout getStatusLayout() {
        return mStatusLayout;
    }


    @SingleClick
    @Override
    public void onRightClick(View view) {
        if (mAllImage.isEmpty()) {
            return;
        }

        ArrayList<AlbumDialog.AlbumInfo> data = new ArrayList<>(mAllAlbum.size() + 1);

        int count = 0;
        Set<String> keys = mAllAlbum.keySet();
        for (String key : keys) {
            List<String> list = mAllAlbum.get(key);
            if (list == null || list.isEmpty()) {
                continue;
            }
            count += list.size();
            data.add(new AlbumDialog.AlbumInfo(list.get(0), key, String.format(getString(R.string.res_image_select_total), list.size()), mAdapter.getData() == list));
        }
        data.add(0, new AlbumDialog.AlbumInfo(mAllImage.get(0), getString(R.string.res_image_select_all), String.format(getString(R.string.res_image_select_total), count), mAdapter.getData() == mAllImage));

        if (mAlbumDialog == null) {
            mAlbumDialog = new AlbumDialog.Builder(this)
                    .setListener((dialog, position, bean) -> {

                        setRightTitle(bean.getName());
                        // 滚动回第一个位置
                        mRecyclerView.scrollToPosition(0);
                        if (position == 0) {
                            mAdapter.setList(mAllImage);
                        } else {
                            mAdapter.setList(mAllAlbum.get(bean.getName()));
                        }
                        // 执行列表动画
                        mRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.res_layout_from_right));
                        mRecyclerView.scheduleLayoutAnimation();
                    });
        }
        mAlbumDialog.setData(data)
                .show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Iterator<String> iterator = mSelectImage.iterator();
        // 遍历判断选择了的图片是否被删除了
        while (iterator.hasNext()) {
            String path = iterator.next();
            File file = new File(path);
            if (file.isFile()) {
                continue;
            }

            iterator.remove();
            mAllImage.remove(path);

            File parentFile = file.getParentFile();
            if (parentFile == null) {
                continue;
            }

            List<String> data = mAllAlbum.get(parentFile.getName());
            if (data != null) {
                data.remove(path);
            }
            mAdapter.notifyDataSetChanged();

            if (mSelectImage.isEmpty()) {
                mFloatingView.setImageResource(R.drawable.res_camera_ic);
            } else {
                mFloatingView.setImageResource(R.drawable.res_succeed_ic);
            }
        }
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_image_select_floating) {
            if (mSelectImage.isEmpty()) {
                // 点击拍照
                CameraActivity.start(this, false, new OnCameraListener() {
                    @Override
                    public void onSelected(File file) {
                        // 当前选中图片的数量必须小于最大选中数
                        if (mSelectImage.size() < mMaxSelect) {
                            mSelectImage.add(file.getPath());
                        }

                        // 这里需要延迟刷新，否则可能会找不到拍照的图片
                        postDelayed(() -> {
                            // 重新加载图片列表
                            ThreadPoolManager.getInstance().execute(ImageSelectActivity.this);
                        }, 1000);
                    }

                    @Override
                    public void onError(String details) {
                        toast(details);
                    }
                });
                return;
            }

            // 完成选择
            setResult(RESULT_OK, new Intent().putStringArrayListExtra(INTENT_KEY_OUT_IMAGE_LIST, mSelectImage));
            finish();
        }
    }


    @Override
    public void onItemChildClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        if (view.getId() == R.id.fl_image_select_check) {

            String path = mAdapter.getItem(position);
            File file = new File(path);
            if (!file.isFile()) {
                mAdapter.removeAt(position);
                toast(R.string.res_image_select_error);
                return;
            }

            if (mSelectImage.contains(path)) {
                mSelectImage.remove(path);

                if (mSelectImage.isEmpty()) {
                    mFloatingView.setImageResource(R.drawable.res_camera_ic);
                }

                mAdapter.notifyItemChanged(position);
                return;
            }

            if (mMaxSelect == 1 && mSelectImage.size() == 1) {

                List<String> data = mAdapter.getData();
                int index = data.indexOf(mSelectImage.remove(0));
                if (index != -1) {
                    mAdapter.notifyItemChanged(index);
                }
                mSelectImage.add(path);

            } else if (mSelectImage.size() < mMaxSelect) {

                mSelectImage.add(path);

                if (mSelectImage.size() == 1) {
                    mFloatingView.setImageResource(R.drawable.res_succeed_ic);
                }
            } else {
                toast(String.format(getString(R.string.res_image_select_max_hint), mMaxSelect));
            }
            mAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        ImagePreviewActivity.start(getActivity(), mAdapter.getData(), position);
    }

    @Override
    public boolean onItemLongClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
        if (mSelectImage.size() < mMaxSelect) {
            // 长按的时候模拟选中
            return view.findViewById(R.id.fl_image_select_check).performClick();
        }
        return false;
    }


    @Override
    public void run() {
        mAllAlbum.clear();
        mAllImage.clear();

        final Uri contentUri = MediaStore.Files.getContentUri("external");
        final String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        final String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" + " AND " + MediaStore.MediaColumns.SIZE + ">0";

        ContentResolver contentResolver = getContentResolver();
        String[] projections = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.SIZE};

        Cursor cursor = null;
        if (XXPermissions.isGranted(this, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)) {
            cursor = contentResolver.query(contentUri, projections, selection, new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)}, sortOrder);
        }
        if (cursor != null && cursor.moveToFirst()) {

            int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            int mimeTypeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);

            do {
                long size = cursor.getLong(sizeIndex);
                // 图片大小不得小于 1 KB
                if (size < 1024) {
                    continue;
                }

                String type = cursor.getString(mimeTypeIndex);
                String path = cursor.getString(pathIndex);
                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(type)) {
                    continue;
                }

                File file = new File(path);
                if (!file.exists() || !file.isFile()) {
                    continue;
                }

                File parentFile = file.getParentFile();
                if (parentFile == null) {
                    continue;
                }

                // 获取目录名作为专辑名称
                String albumName = parentFile.getName();
                List<String> data = mAllAlbum.get(albumName);
                if (data == null) {
                    data = new ArrayList<>();
                    mAllAlbum.put(albumName, data);
                }
                data.add(path);
                mAllImage.add(path);

            } while (cursor.moveToNext());

            cursor.close();
        }

        postDelayed(() -> {
            // 滚动回第一个位置
            mRecyclerView.scrollToPosition(0);
            // 设置新的列表数据
            mAdapter.setList(mAllImage);

            if (mSelectImage.isEmpty()) {
                mFloatingView.setImageResource(R.drawable.res_camera_ic);
            } else {
                mFloatingView.setImageResource(R.drawable.res_succeed_ic);
            }

            // 执行列表动画
            mRecyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.res_layout_fall_down));
            mRecyclerView.scheduleLayoutAnimation();

            if (mAllImage.isEmpty()) {
                // 显示空布局
                showEmpty();
                // 设置右标题
                setRightTitle(null);
            } else {
                // 显示加载完成
                showComplete();
                // 设置右标题
                setRightTitle(R.string.res_image_select_all);
            }
        }, 500);
    }
}