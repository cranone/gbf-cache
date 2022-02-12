package com.shadego.gbf.utils;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitFactory {
    private static final Logger logger = LoggerFactory.getLogger(RetrofitFactory.class);
	private final ApiService apiService;

	public RetrofitFactory() {
		OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
			Request.Builder builder = chain.request().newBuilder();
			builder.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
				//.addHeader("accept", "*/*")
				//.addHeader("accept-charset", "UTF-8")
				//.addHeader("connection", "Keep-Alive")
				//.addHeader("Content-Type", "text/html;charset=uft-8")
			;
            return chain.proceed(builder.build());
		}).connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
		Retrofit retrofit = new Retrofit.Builder().client(httpClient).baseUrl("http://127.0.0.1/")
		         .addConverterFactory(new Retrofit2ConverterFactory())
		        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
		this.apiService = retrofit.create(ApiService.class);
		logger.info("Retrofit init ok");
	}

	private static final class MInstanceHolder {
		private static final RetrofitFactory mInstance = new RetrofitFactory();
	}

	public static RetrofitFactory getInstance() {
		return MInstanceHolder.mInstance;
	}

	public ApiService getApiService() {
		return apiService;
	}
}
