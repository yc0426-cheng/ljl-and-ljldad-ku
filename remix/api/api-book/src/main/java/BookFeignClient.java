import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p><b>远程调用-书籍服务</b></p>
 *
 * @author yangcheng
 * @since 2026/9/17 15:50
 */
@FeignClient(name = "book-server", contextId = "BookFeignClient", path = "/book")
public interface BookFeignClient {
}
