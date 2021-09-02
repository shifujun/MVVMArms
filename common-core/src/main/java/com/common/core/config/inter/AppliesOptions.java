package com.common.core.config.inter;

import android.content.Context;

import com.common.core.http.InterceptorConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.common.core.di.module.ConfigModule;

import androidx.room.RoomDatabase;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


/**
 * 为框架提供一些配置参数入口
 *
 * @see <a href="https://github.com/bumptech/glide/blob/f7d860412f061e059aa84a42f2563a01ac8c303b/library/src/main/java/com/bumptech/glide/module/AppliesOptions.java">Glide</a>
 */
public interface AppliesOptions {

    /**
     * 为框架提供一些配置参数入口
     *
     * @param context
     * @param builder
     */
    void applyOptions(Context context, ConfigModule.Builder builder);


    /**
     * 为框架中的{@link Retrofit}提供配置参数入口
     */
    interface RetrofitOptions {
        void applyOptions(Retrofit.Builder builder);
    }

    /**
     * 为框架中的{@link OkHttpClient}提供配置参数入口
     */
    interface OkHttpClientOptions {
        void applyOptions(OkHttpClient.Builder builder);
    }

    /**
     * 为框架中的{@link Gson}提供配置参数入口
     */
    interface GsonOptions {
        void applyOptions(GsonBuilder builder);
    }

    /**
     * 为框架中的{@link InterceptorConfig}提供配置参数入口
     */
    interface InterceptorConfigOptions {
        void applyOptions(InterceptorConfig.Builder builder);
    }

    /**
     * 为框架中的{@link Gson}提供配置参数入口
     */
    interface RoomDatabaseOptions<T extends RoomDatabase> {
        void applyOptions(RoomDatabase.Builder<T> builder);
    }

}
