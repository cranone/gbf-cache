import com.shadego.gbf.Application;
import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes= Application.class)
public class SpringTest {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private OkHttpClient okHttpClient;

    /**
     * restTemplate产生403用例
     */
    @Test
    public void test() throws IOException, InterruptedException {
        String url="https://asset.legend-clover.net/pcr/ui/gacha/production/gachatoppageproduction/material/gacha_bg_pattern%201.png";
        HttpHeaders headers=new HttpHeaders();
//        headers.add("accept","*");
//        headers.add("content-length","-1");
//        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:104.0) Gecko/20100101 Firefox/104.0");
//        headers.add("connection","Upgrade, HTTP2-Settings");
//        headers.add("http2-settings","AAEAAEAAAAIAAAABAAMAAABkAAQBAAAAAAUAAEAA");
//        headers.add("upgrade","h2c");

/*        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            System.out.println(messageConverter+":"+messageConverter.getSupportedMediaTypes());
        }*/

        ResponseEntity<byte[]> exchange = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        System.out.println(exchange.getStatusCode());

        Headers.Builder builder = new Headers.Builder();
        Request request = new Request.Builder().url(url).headers(builder.build()).build();
        Call call = okHttpClient.newCall(request);
        Response response=call.execute();
        System.out.println(response.code());
    }
}
