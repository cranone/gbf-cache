package com.shadego.gbf.utils;

import com.alibaba.fastjson.JSONObject;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.Map;

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

	@Streaming
	@GET
	public Flowable<Response<ResponseBody>> download(@Url String url,@HeaderMap Map<String, String> headers);
	
	@HEAD
    public Flowable<Response<Void>> downloadHeader(@Url String url);
	
	@POST
    public Flowable<Response<ResponseBody>> post(@Url String url,@HeaderMap Map<String, String> headers,@Body RequestBody body);
}
