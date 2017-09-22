## 基本网络请求框架

参考
 * Retrofit
 * https://github.com/square/retrofit
 * 实现多个baseUrl 参考
 * http://www.jianshu.com/p/2919bdb8d09a

全局配置
```
        EasyOKHttp.Builder builder = new EasyOKHttp.Builder();
        EasyOKHttp easyOKHttp = builder.baseUrl("http://op.juhe.cn/").build();
        // 根据需求配置
        easyOKHttp.getOkHttpClientBuilder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        easyOKHttp.initHttpClient();
```

单个请求配置
        EasyOptions
        通过
        easyOKHttp.createCall 得到 EasyCall 对象

执行请求
        execute 同步
        enqueue 异步（回调UI线程）

1.1 添加上传文件和下载文件操作<br>
1.2 添加Cahe<br>
1.3 添加cookie


