package com.hxh.component.basicore.imageLoader;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hxh.component.basicore.util.Utils;

import java.io.File;

/**
 * 图片加载
 */
public class GliderLoader implements IImageLoader {

    private static volatile GliderLoader singleton = null;

    private GliderLoader() {
    }

    public static GliderLoader getInstance() {
        if (singleton == null) {
            synchronized (GliderLoader.class) {
                if (singleton == null) {
                    singleton = new GliderLoader();
                }
            }
        }
        return singleton;
    }

    @Override
    public void init(Context context) {

    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options) {
        load(getRequestManager(iv.getContext()).load(url), iv, options, null, true, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, CallBack callback) {
        load(getRequestManager(iv.getContext()).load(url), iv, options, callback, true, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, CallBack callback, boolean enableCache) {
        load(getRequestManager(iv.getContext()).load(url), iv, options, callback, enableCache, false);
    }

    private String mCurrentSavePath = "";

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, CallBack callback, String savePath, boolean enableCache) {
        this.mCurrentSavePath = savePath;
        load(getRequestManager(iv.getContext()).load(url), iv, options, callback, enableCache, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, CallBack callback, String savePath) {
        this.mCurrentSavePath = savePath;
        load(getRequestManager(iv.getContext()).load(url), iv, options, callback, true, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, String savePath) {
        this.mCurrentSavePath = savePath;
        load(getRequestManager(iv.getContext()).load(url), iv, options, null, true, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, String savePath, boolean enableCache) {
        this.mCurrentSavePath = savePath;
        load(getRequestManager(iv.getContext()).load(url), iv, options, null, enableCache, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, Options options, boolean enableCache) {
        load(getRequestManager(iv.getContext()).load(url), iv, options, null, false, false);
    }

    @Override
    public void loadFormNet(ImageView iv, String url, boolean asgif) {
        load(getRequestManager(iv.getContext()).load(url), iv, IImageLoader.Options.defaultOptions(), null, true, true);
    }

    @Override
    public void loadResource(ImageView iv, int resId, boolean asgif) {
        load(getRequestManager(iv.getContext()).load(resId), iv, IImageLoader.Options.defaultOptions(), null, true, true);
    }

    @Override
    public void loadResource(ImageView iv, int resId, Options options) {
        getRequestManager(iv.getContext()).load(resId)
                .asBitmap()
                .into(iv);
        //load(getRequestManager(iv.getContext()).load(resId).asBitmap(),iv,options,null,false,false);
    }

    @Override
    public void loadAssest(ImageView iv, String assestName, Options options) {
        load(getRequestManager(iv.getContext()).load("file:///android_asset/" + assestName), iv, options, null, true, false);
    }

    @Override
    public void loadFile(ImageView iv, File file, Options options) {
        load(getRequestManager(iv.getContext()).load(file), iv, options, null, true, false);
    }

    @Override
    public void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    @Override
    public void clearDiskCache(Context context) {
        Glide.getPhotoCacheDir(context).deleteOnExit();
    }

    @Override
    public void resume(Context context) {
        getRequestManager(context).resumeRequests();
    }

    @Override
    public void pause(Context context) {
        getRequestManager(context).pauseRequests();
    }

    /**
     * 获取RequestManager
     *
     * @param context
     * @return
     */
    private RequestManager getRequestManager(Context context) {
        if (context instanceof Activity) {
            return Glide.with(((Activity) context));
        }
        return Glide.with(context);
    }

    private void load(DrawableTypeRequest request, ImageView target, Options options, final CallBack callback, boolean enableCache, boolean isasGif) {
        if (options == null) options = Options.defaultOptions();

        if (-1 != options.loadingResId) {
            request
                    .placeholder(options.loadingResId);
        }
        if (-1 != options.loadErrorResId) {
            request
                    .error(options.loadErrorResId);
        }

        if (!options.isEnableAnimate) {
            request
                    .dontAnimate();
        }

        if (!Utils.Text.isEmpty(mCurrentSavePath)) {
            request.asBitmap();
        }

        DrawableRequestBuilder builder = null;
        if (isasGif) {
            request.asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE);
            builder = setScaleType(request, options);
        } else {
            builder = setScaleType(request, options)
                    .crossFade();
            if (enableCache) {
                builder.diskCacheStrategy(DiskCacheStrategy.SOURCE);
            } else {

                builder.diskCacheStrategy(DiskCacheStrategy.NONE);//禁用磁盘缓存
                builder.skipMemoryCache(true);
            }

        }


        builder
                .listener(new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                        if (null != callback) {
                            callback.onError(e.getMessage());
                        }

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (null != callback) {
                            saveFileToPath(resource);
                            callback.onSuccess(resource);
                        }

                        return false;
                    }
                });

        builder.into(target);


    }

    private void saveFileToPath(Object resource) {
        if (!Utils.Text.isEmpty(mCurrentSavePath)) {
            if (null != resource) {

                Utils.FileUtil.saveFileFromBitMap(((GlideBitmapDrawable) resource).getBitmap(), new File(mCurrentSavePath));
                mCurrentSavePath = null;
            }
        }
    }

    private DrawableTypeRequest setScaleType(DrawableTypeRequest request, Options option) {
        if (option != null && option.scaleType != null) {
            switch (option.scaleType) {
                case MATRIX:
                    break;

                case FIT_XY:
                    break;

                case FIT_START:
                    break;

                case FIT_END:
                    break;

                case CENTER:
                    break;

                case CENTER_INSIDE:
                    break;

                case FIT_CENTER:
                    request.fitCenter();
                    break;

                case CENTER_CROP:
                    request.centerCrop();
                    break;
            }
        }
        return request;
    }


}
