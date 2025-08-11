package com.jd.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @param null
 * @author -
 * @date 2025/8/8 12:56
 * @return null
 * @FeignClient(value = "weather-client", url = "http://aliv18.data.moji.com")
 * value默认是微服务名称，第三方服务不知道名称，随便写一个
 */
@FeignClient(value = "weather-client", url = "http://api.yesapi.cn")
public interface WeatherFeignClient {
    @PostMapping("/whapi/json/alicityweather/condition")
    String getWeather(@RequestHeader("Authorization") String auth,
                      @RequestParam("token") String token,
                      @RequestParam("cityId") String cityId);

    @PostMapping("/api/App/Table/Create")
    String create(@RequestParam("app_key") String app_key,
                  @RequestParam("model_name") String model_name);
}