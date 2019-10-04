package com.shadego.gbf.utils;

import com.alibaba.fastjson.JSONObject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
* @author Maclaine E-mail:deathencyclopedia@gmail.com
* 
*/
public interface ApiService {

	@GET
	public Observable<JSONObject> get(@Url String url);
	
	@Streaming
	@GET
	public Flowable<Response<ResponseBody>> download(@Url String url);
	
	@HEAD
    public Flowable<Response<Void>> downloadHeader(@Url String url);
	
//	@POST
//    public Flowable<Response<ResponseBody>> postf(@Url String url,@Body RequestBody body);
}
