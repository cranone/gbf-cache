package com.shadego.gbf.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitFactory {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitFactory.class);
	private static RetrofitFactory mInstance;
	private ApiService apiService;

	public RetrofitFactory() {
		OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
			Request.Builder builder = chain.request().newBuilder();
			builder.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
				.addHeader("accept", "*/*")
				.addHeader("connection", "Keep-Alive")
				.addHeader("contentType", "text/html;charset=uft-8")
			;
            return chain.proceed(builder.build());
		}).connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
		Retrofit retrofit = new Retrofit.Builder().client(httpClient).baseUrl("http://127.0.0.1/")
		         .addConverterFactory(new Retrofit2ConverterFactory())
		        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
		this.apiService = retrofit.create(ApiService.class);
		logger.info("Retrofit init ok");
	}

	public static RetrofitFactory getInstance() {
		if (mInstance == null) {
			synchronized (RetrofitFactory.class) {
				mInstance = new RetrofitFactory();
			}
		}
		return mInstance;
	}

	public ApiService getApiService() {
		return apiService;
	}
}
