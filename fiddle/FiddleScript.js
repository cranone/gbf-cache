if (!oSession.HTTPMethodIs("CONNECT")
    &&(oSession.uriContains('.png')||oSession.uriContains('.jpg')||oSession.uriContains('.mp3')||oSession.uriContains('.mp4')||oSession.uriContains('.css')||oSession.uriContains('.woff')||oSession.uriContains('.json')||url.EndsWith(".js"))
){
    oSession.bypassGateway = true;
    oSession.fullUrl=oSession.fullUrl.Replace("https://","http://");
    oSession["x-overridehost"] = "127.0.0.1:8080";
}