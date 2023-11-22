package polytech;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import polytech.ratelim.GuavaRateLimiter;
import polytech.ratelim.RateLimiter;
import polytech.ratelim.RateLimiterBasedThrottler;
import polytech.ratelim.Throttler;
import polytech.vk.api.VkClient;
import polytech.vk.api.VkClientImpl;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class AppConfig {
    private static final int CLIENT_RESPONSE_TIMEOUT_SECONDS = 5;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public CloseableHttpClient apacheHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public OkAuthorizator okAuthorizator(@Autowired HttpClient httpClient) {
        return new OkAuthorizator(httpClient);
    }

    @Bean
    public OKClient okClient(
            @Autowired CloseableHttpClient apacheHttpClient,
            @Autowired HttpClient httpClient
    ) {
        return new OkClientImpl(apacheHttpClient, httpClient);
    }

    @Bean
    public VkClient vkClient() {
        return new VkClientImpl();
    }

    @Bean
    public RateLimiter rateLimiter(
            @Value("${api.ratelimiter.permits-per-second}") double permitsPerSeconds,
            @Value("${api.ratelimiter.records-maxsize}") int recordsMaxSize,
            @Value("${api.ratelimiter.records-ttl-minutes}") int recordsTtlMinutes
    ) {
        return new GuavaRateLimiter(permitsPerSeconds, recordsMaxSize,
                Duration.of(recordsTtlMinutes, ChronoUnit.MINUTES)
        );
    }

    @Bean
    public Throttler throttler(
            @Value("${bot.replythrottler.replies-per-second}") double repliesPerSecond,
            @Value("${api.ratelimiter.records-maxsize}") int recordsMaxSize,
            @Value("${api.ratelimiter.records-ttl-minutes}") int recordsTtlMinutes
    ) {
        RateLimiter rateLimiter = new GuavaRateLimiter(repliesPerSecond, recordsMaxSize,
                Duration.of(recordsTtlMinutes, ChronoUnit.MINUTES)
        );
        return new RateLimiterBasedThrottler(rateLimiter);
    }
}
