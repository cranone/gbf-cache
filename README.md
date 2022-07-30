# gbf-cache  
GBF local cache  
碧蓝幻想本地缓存.使用本程序还需要配合Chrome插件SwitchyOmega或其他代理插件  

### 原理
Gbf中静态资源均在<https://prd-game-a1-granbluefantasy.akamaized.net>域名下,  
因此可以通过SwitchyOmega将所有静态资源指向本机,gbf-cache判断本地是否已缓存该资源,未缓存则从服务器下载到本机  

### 安装
#### HTTP
1. 配置SwitchyOmega,其中8080为gbf-cache的端口号:  
   *  game-a.granbluefantasy.jp->HTTP 127.0.0.1 8080  
   *  game-a*.granbluefantasy.jp->HTTP 127.0.0.1 8080  
2. 启动gbf-cache,在application.properties中可配置缓存路径  

#### HTTPS(兼容HTTP)
1. 安装HTTPS自签证书  
2. 配置SwitchyOmega,其中8080为gbf-cache的端口号:  
   *  *granbluefantasy.akamaized.net->HTTP 127.0.0.1:8080  
3. 启动gbf-cache,在application.properties中可配置缓存路径和证书路径  
