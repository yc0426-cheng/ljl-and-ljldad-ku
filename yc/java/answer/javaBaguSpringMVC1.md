# SpringMVC 八股答案

### Q1

> SpringMVC 的工作流程（请求到响应的完整过程）

1. 请求到达 **DispatcherServlet**（前端控制器）
2. DispatcherServlet 查询 **HandlerMapping**，返回 HandlerExecutionChain（Handler + 拦截器链）
3. 通过 **HandlerAdapter** 调用 Handler（执行参数绑定、调用 Controller 方法）
4. Controller 处理并返回：传统方式返回 ModelAndView；前后端分离返回对象由 @ResponseBody 直接序列化写响应
5. **ViewResolver** 解析 ModelAndView 得到 View
6. View 渲染数据，DispatcherServlet 返回响应

### Q2

> SpringMVC 的核心组件有哪些，各自的作用

- **DispatcherServlet**：前端控制器，统一接收分发请求，协调整个流程
- **HandlerMapping**：处理器映射器，根据 URL 找到能处理的 Handler 及拦截器链
- **HandlerAdapter**：处理器适配器，真正执行 Handler 方法（参数绑定、返回值处理），适配器模式兼容多种 Handler
- **Handler**：处理器（Controller），业务处理
- **ViewResolver**：视图解析器，将逻辑视图名解析为具体 View
- **View**：视图渲染（JSP/Thymeleaf/JSON）

### Q3

> DispatcherServlet 的作用

前端控制器，本质是一个 HttpServlet，是 SpringMVC 的统一入口。负责：接收所有请求 → 协调 HandlerMapping/HandlerAdapter/ViewResolver 等组件 → 统一异常处理 → 返回响应。容器启动时（onRefresh）初始化九大策略组件。

### Q4

> 处理器映射器和处理器适配器的作用

- **HandlerMapping**：URL → Handler 的映射关系（如 RequestMappingHandlerMapping 扫描 @RequestMapping 注解建立映射表）
- **HandlerAdapter**：按 Handler 的类型选择合适的适配器执行（实现类、@RequestMapping 方法、HttpRequestHandler），屏蔽 Handler 差异——典型的适配器模式

### Q5

> @Controller 和 @RestController 的区别

@RestController = @Controller + @ResponseBody。@Controller 返回视图名走视图解析；@RestController 的所有方法返回值直接序列化为 JSON 写入响应体，不再经过视图解析器，是前后端分离的标准选择。

### Q6

> @RequestMapping 及其衍生注解

@RequestMapping 用于将 URL 映射到 Handler，可指定 path、method、params、headers、consumes。衍生注解：@GetMapping、@PostMapping、@PutMapping、@DeleteMapping、@PatchMapping，等价于 method 固定的 @RequestMapping，语义更清晰、更 Restful。

### Q7

> SpringMVC 接收前端参数的方式

- @RequestParam：接收 query/form 的单个参数（required、defaultValue）
- @PathVariable：接收 URL 路径模板变量
- @RequestBody：接收 JSON 请求体绑定到对象
- @RequestHeader / @CookieValue：请求头 / Cookie
- 实体对象：属性名匹配自动绑定（setter 注入）
- 原生对象：HttpServletRequest、HttpServletResponse、HttpSession 直接作参数

### Q8

> @RequestParam 和 @PathVariable 的区别

@RequestParam 取查询字符串或表单中的参数（/user?id=1 的 id）；@PathVariable 取 RESTful URL 路径中的占位符（/user/{id} 的 id）。前者适合过滤、分页参数，后者适合资源唯一标识。

### Q9

> @RequestBody 和 @RequestParam 的区别

- @RequestBody：读取**请求体**（JSON/XML），通过 HttpMessageConverter 反序列化，一个方法只能有一个，POST/PUT 常用
- @RequestParam：读取 **URL query 或表单键值对**，可声明多个
GET 请求没有请求体，一般用 @RequestParam；传 JSON 的 POST 用 @RequestBody。

### Q10

> 转发（forward）和重定向（redirect）的区别

| | forward 转发 | redirect 重定向 |
|---|---|---|
| 请求次数 | 服务器内部 1 次 | 浏览器发起 2 次（302） |
| 地址栏 | 不变 | 变为目标地址 |
| 数据共享 | 共享 request 域 | request 数据丢失 |
| 范围 | 只能站内 | 可跳转任意 URL |
典型场景：POST 后重定向防止表单重复提交（PRG 模式）。

### Q11

> 拦截器和过滤器的区别

- 归属：Filter 是 **Servlet 规范**，Interceptor 是 **SpringMVC** 提供
- 时机：Filter 在 DispatcherServlet **之前**拦截所有请求（含静态资源）；Interceptor 只拦截进入 Handler 的请求
- 能力：Interceptor 可注入 Spring Bean、获取 Handler 方法和注解信息，粒度更细
- 执行：Filter 包裹在最外层，Interceptor 包在 Controller 外层
选择：编码/跨域/全局过滤用 Filter，登录鉴权/权限/日志用 Interceptor。

### Q12

> 拦截器的执行流程（preHandle、postHandle、afterCompletion）

1. **preHandle**：Controller 之前执行，返回 false 则中断（不会执行 Controller 和后续方法）
2. Controller 执行
3. **postHandle**：视图渲染前执行（可修改 ModelAndView）
4. 视图渲染完成
5. **afterCompletion**：无论是否异常都执行，用于清理资源、记录耗时
多个拦截器：preHandle 按注册顺序执行，postHandle/afterCompletion 按注册**逆序**执行。

### Q13

> 如何自定义一个拦截器

① 实现 HandlerInterceptor 接口，重写 preHandle/postHandle/afterCompletion；② 将其注册为 Spring Bean（@Component）；③ 配置类实现 WebMvcConfigurer：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/login", "/doc.html");
}
```

### Q14

> 如何统一处理异常（@ControllerAdvice、@ExceptionHandler）

① @RestControllerAdvice 标注全局异常处理类；② 类内定义多个 @ExceptionHandler(XxxException.class) 方法分别处理业务异常、参数校验异常（MethodArgumentNotValidException）、兜底 Exception；③ 返回统一 Result 结构和对应 HTTP 状态码。Controller 中抛异常即可，无需层层 try-catch。

### Q15

> @ControllerAdvice 的作用

全局 Controller 增强，三大用途：① @ExceptionHandler 统一异常处理；② @ModelAttribute 全局数据填充（每个接口前执行）；③ @InitBinder 全局参数绑定预处理。@RestControllerAdvice = @ControllerAdvice + @ResponseBody。

### Q16

> SpringMVC 返回 JSON 数据的流程，HttpMessageConverter 的作用

方法返回对象且标注 @ResponseBody 时：RequestResponseBodyMethodProcessor 处理返回值 → 按 Content-Type 和可接受的媒体类型**内容协商**选择合适的 **HttpMessageConverter**（默认 classpath 有 Jackson 则用 MappingJackson2HttpMessageConverter）→ 将对象序列化为 JSON 写入响应体。HttpMessageConverter 统一了请求体反序列化（@RequestBody）和响应体序列化（@ResponseBody）的双向转换。

### Q17

> @ResponseBody 的原理

RequestMappingHandlerAdapter 的 HandlerMethodReturnValueHandler 链中，RequestResponseBodyMethodProcessor 识别到 @ResponseBody 注解后，不走 ModelAndView 流程，而是调用 HttpMessageConverter 将返回值写入响应体（writeWithMessageConverters）。

### Q18

> Model、ModelMap、ModelAndView 的区别

- **Model/ModelMap**：只承载视图数据（addAttribute），Model 是接口，ModelMap 是其实现（LinkedHashMap 封装）
- **ModelAndView**：数据 + 视图名一起返回
Handler 参数声明 Model 即可使用；@SessionAttributes 可将 Model 中属性提升到 session。

### Q19

> 如何解决 POST 请求中文乱码问题

配置 CharacterEncodingFilter 强制 request/response 编码为 UTF-8（传统 web.xml 或 SpringBoot 中自动配置，可设 spring.servlet.encoding）。Tomcat8+ 的 GET 请求默认 UTF-8 无需处理；Tomcat7 需设置 server.tomcat.uri-encoding=UTF-8。

### Q20

> SpringMVC 如何处理文件上传（MultipartFile）

① 表单 enctype="multipart/form-data" 或前端 FormData 提交；② Handler 参数用 MultipartFile 接收；③ 配置大小限制：spring.servlet.multipart.max-file-size / max-request-size（SpringBoot 自动启用 StandardServletMultipartResolver）；④ file.transferTo(new File(path)) 保存。多文件用 MultipartFile[] 接收。

### Q21

> Restful 风格的接口设计规范

1. URL 表示**资源**，用名词复数：/users、/users/1
2. 不在 URL 中出现动词，用 HTTP 方法表达操作：GET 查询、POST 新增、PUT 全量更新、DELETE 删除
3. HTTP 状态码表达结果：200/201/204/400/401/403/404/500
4. 过滤分页用 query 参数：/users?page=1&size=10&sort=id,desc
5. 版本管理：/api/v1/users；统一返回结构 {code,msg,data}

### Q22

> 如何解决跨域问题（@CrossOrigin、全局配置）

跨域由浏览器同源策略引起（协议/域名/端口任一不同）。服务端 CORS 方案：
1. @CrossOrigin(originins="...") 标注类或方法
2. 全局：WebMvcConfigurer#addCorsMappings 配置 allowedOriginPatterns、allowedMethods
3. CorsFilter Bean（优先级高于 MVC 配置）
4. 网关/Nginx 统一配置
注意复杂请求先发 OPTIONS 预检，需放行且 allowCredentials=true 时不能配 allowedOrigins="*"。

### Q23

> @SessionAttributes 的作用

将 Model 中指定名称/类型的属性存入 HttpSession，实现多请求间共享。`@SessionAttributes("user")` 后 model.addAttribute("user",u) 自动放入 session，方法参数可用 @SessionAttribute("user") 读取。适用于传统 session 应用，前后端分离中少用（多用 Redis/token）。

### Q24

> SpringMVC 中如何配置视图解析器

实现 WebMvcConfigurer，重写 configureViewResolvers：

```java
@Bean
public ViewResolver viewResolver() {
    InternalResourceViewResolver r = new InternalResourceViewResolver();
    r.setPrefix("/WEB-INF/views/");
    r.setSuffix(".jsp");
    return r;
}
```

Controller 返回 "user" 即解析为 /WEB-INF/views/user.jsp。前后端分离项目不使用视图解析。

### Q25

> 自定义参数类型转换器的方法

实现 `Converter<String, T>` 接口（如字符串转 Date），在 WebMvcConfigurer#addFormatters 中注册 conversionService；简单场景可直接用 @DateTimeFormat(pattern="yyyy-MM-dd") 标注参数/字段，或 @InitBinder 注册 PropertyEditor。SpringMVC 已内置大量转换器（数字、日期等）。

### Q26

> @InitBinder 的作用

初始化 WebDataBinder，用于参数绑定预处理：注册自定义属性编辑器（如日期格式）、设置允许/禁止绑定的字段（防表单注入 setAllowedFields/disallowedFields）。标注在 Controller 方法上对当前类生效，配合 @ControllerAdvice 可全局生效。

### Q27

> SpringMVC 中拦截器如何配置拦截和放行路径

registry.addInterceptor(interceptor) 后链式配置：addPathPatterns("/**") 指定拦截范围，excludePathPatterns("/login", "/css/**", "/doc.html") 指定放行路径；order(n) 或 @Order 控制多个拦截器顺序。

### Q28

> SpringMVC 的全局异常处理和局部异常处理的优先级

局部优先：Controller 内部的 @ExceptionHandler 先匹配（可匹配本类及子类异常），没有匹配到才交给 @ControllerAdvice 全局处理；都没有则走 HandlerExceptionResolver 默认机制（如响应 error 页面或 500）。

### Q29

> GET 和 POST 请求的区别

- 参数位置：GET 在 URL（有长度限制、会暴露在日志和地址栏），POST 在请求体（大小受服务器限制）
- 语义：GET 获取资源、幂等安全、可缓存可收藏；POST 提交数据、非幂等、不缓存
- 编码：GET 只支持 URL 编码，POST 支持多种（form、JSON、multipart）
本质都是 TCP 连接上的 HTTP 报文，区别主要是规范约定。

### Q30

> SpringMVC 如何保证接口的幂等性

幂等 = 同一请求执行多次结果一致。GET/PUT/DELETE 天然幂等，重点是 POST：
1. **Token 机制**：进入页面先取唯一 token 存 Redis，提交时校验并删除（原子操作），重复提交被拒绝
2. **唯一索引/防重表**：数据库层面兜底
3. **分布式锁**：以业务唯一键加锁
4. **状态机**：更新带前置状态条件（where status='待支付'）
5. **乐观锁版本号**：更新时校验 version
