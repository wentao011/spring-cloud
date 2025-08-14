需要详细了解具体的框架或者功能，可以参照官网介绍
说明：本文是基于【雷神：SpringCloud - 快速通关】进行实践操作，并对雷神的笔记做一个更详细的补充，供大家学习参考，一起加油！

视频地址：https://b23.tv/DnZRdua
雷神笔记链接：
雷神资料：（代码+课件+逻辑图）
本人代码：

用于测试API接口的工具：Apipost
IDEA自动提示代码插件：通义灵码

涉及到的idea插件：
1.generateAllSetter - 使用alt + enter 为实体类一件生成所有的setter方法以及默认值
2.FeignClient Assistant - 一键跳跃feignclient客户端和controller
springcloud简介
springcloud是分布式系统一站式解决方案。
什么是分布式系统？
架构分：单体和分布式。集群只是一种物理形态，分布式是工作方式。 
架构演进	单体架构	集群架构	分布式架构
定义	所有功能模块都在一个项目里	单体的多服务器版本	一个大型应用被拆分成很多小应用分布部署在各个机器；
优点	开发部署简单	解决大并发	解决了单体+集群的问题
缺点	无法应对高并发	问题1：模块块升级 麻烦
问题2：多语言团队 交互不通	
基于自己的理解：
分布式架构（模拟用户访问）
1. 通过网关来发送各个微服务的请求（请求路由）。用gateway。网关需要对请求进行分发，所以要注册到注册中心。
2. 将各微服务布置到各服务器，即微服务（自治） 独立部署、数据隔离、语言无关，将不同模块部署到多个服务器，每个模块都要有副本服务器。不能让每个模块只部署到一个服务器，会出现单点故障问题：如果这个服务器崩了，那应用就不能提供完整服务了
3. 如果模块跨服务器之间调用会遇到什么问题？远程调用RPC。如果远程调用怎么让应用知道调用哪个服务器的微服务。此时就需要用到nacos注册中心和配置中心，注册中心有两个功能：服务注册（监控服务上下线）和服务发现（远程调用之前要发现对方在哪）。配置中心：统一管理配置文件+推送配置的变更。Nacos+OpenFeign
4. 如果模块之间调用失败导致服务调用链整体阻塞甚至雪崩，怎么办？服务熔断（快速失败机制），及时释放资源，防止资源耗尽。Sentinal
5. 如果有一个操作需要多个数据库合作，而不同数据库部署在不同服务器，这就需要用到分布式事务。Seata
前期准备
建springcloud-demo项目
先用手脚架快速搭建框架


导依赖
pom父模块
注意：springboot, springcloud, springcloud-alibaba的版本
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.atguigu</groupId>
    <artifactId>spring-cloud-demo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <spring-cloud-alibaba.version>2023.0.3.2</spring-cloud-alibaba.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>

建services模块
services模块作为管理所有service-xxx 模块的父模块

导入依赖
在service中导入nacos-discovery依赖
<dependencies>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>

建service-order/product模块
注意父模块是services


nacos - 注册/配置中心
基础入门
官网：https://nacos.io/zh-cn/docs/v2/quickstart/quick-start.html
这里要下载nacos的服务端：nacos server  账号密码都是nacos
暂时版本用图中所示，用docker也行，参考雷神文档

● 启动：startup.cmd -m standalone


启动成功


注册中心
服务注册
作用：将微服务注册到nacos中进行统一管理

service-order, service-product都加依赖
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
service-order模块
application.properties
server.port=8000
spring.application.name=service-order
spring.cloud.nacos.server-addr=127.0.0.1:8848
启动类
@SpringBootApplication
public class OrderMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class, args);
    }
}
service-product模块
application.properties
server.port=9000
spring.application.name=service-product
spring.cloud.nacos.server-addr=127.0.0.1:8848
启动类
@SpringBootApplication
public class ProductMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductMainApplication.class, args);
    }
}
效果


查看效果
访问：http://localhost:8848/nacos 可以看到服务已经注册上来；

启动集群
例如：service-order启动两个，service-product启动3个
order端口:8000/8001 
product端口：9000/9001/9002 



服务发现
服务发现的作用是：服务间的远程调用通过nacos发现对方的服务，然后进行调用，后续不用手动调，这里只要加上注解，两个API作为了解。

启动类加注解
@EnableDiscoveryClient
@EnableDiscoveryClient
@SpringBootApplication
public class ProductMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductMainApplication.class, args);
    }
}
测试类
测试类的包和启动类的包保持一致
@SpringBootTest
public class ProductApplicationTest {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    NacosDiscoveryClient nacosDiscoveryClient;//二者效果一样，这个依赖nacos

    @Test
    public void discoveryClientTest(){
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            System.out.println("service = " + service);
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("instance.getHost() = " + instance.getHost());
                System.out.println("instance.getPort() = " + instance.getPort());
            }
        }
    }

    @Test
    public void nacosDiscoveryClientTest(){
        List<String> services = nacosDiscoveryClient.getServices();
        for (String service : services) {
            System.out.println("service = " + service);
            List<ServiceInstance> instances = nacosDiscoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("instance.getHost() = " + instance.getHost());
                System.out.println("instance.getPort() = " + instance.getPort());
            }
        }
    }
}

远程调用



新建model模块
和services模块平级，实体类的统一管理

导入依赖
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>annotationProcessor</scope>
    </dependency>
</dependencies>
实体类
com.atguigu.bean
@Data
public class Order {
    private Long id;
    private BigDecimal totalAmount;
    private Long userId;
    private String nickName;
    private String address;
    private List<Product> productList;
}
@Data
public class Product {
    private Long id;
    private BigDecimal price;
    private String productName;
    private int num;
}
在services的pom文件中导入model，就可以用了
<dependency>
    <groupId>com.atguigu</groupId>
    <artifactId>model</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

回到service业务类
service-product
controller
@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/productId/{id}")
    public Product getProductById(@PathVariable("id") Long productId) {
        Product product = productService.getProductById(productId);
        return product;
    }
}
service
public interface ProductService {
    Product getProductById(Long productId);
}
@Service
public class ProductServiceImpl implements ProductService {
    @Override
    public Product getProductById(Long productId) {
        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("99"));
        product.setProductName("苹果-" + productId);
        product.setNum(11);
        return product;
    }
}
测试：http://localhost:9000/product/2


service-order
controller
@RestController
public class OrderController {
    @Autowired
    OrderService orderService;

    @GetMapping(value = "/create")
    public Order createOrder(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {
        Order order = orderService.createOrder(userId, productId);
        return order;
    }
}
service
public interface OrderService {
    Order createOrder(Long userId, Long productId);
}
此处需要对service-product服务进行远程调用，稍后处理，先测试
@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(Long userId, Long productId) {
        Order order = new Order();
        order.setId(1L);
        //TODO 总金额
        order.setTotalAmount(new BigDecimal("0"));
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        //TODO 远程查询商品列表
        order.setProductList(null);
        return order;
    }
}
可以自动生成getter/setter方法的IDEA插件


测试：http://localhost:8000/create?userId=2&productId=23


完善业务类中远程调用
service-order
config
将RestTemplate加入到spring容器
@Configuration
public class OrderConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
service
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    RestTemplate restTemplate;
    @Override
    public Order createOrder(Long userId, Long productId) {
        Product product = getProductFromRemote(productId);
        
        Order order = new Order();
        order.setId(1L);
        // 总金额=价格*数量
        BigDecimal price = product.getPrice();//价格
        int num = product.getNum();//数量
        order.setTotalAmount(price.multiply(new BigDecimal(num)));//总价
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        // 远程查询商品列表
        order.setProductList(Arrays.asList(product));
        return order;
    }

    //远程调用获取商品信息
    public Product getProductFromRemote(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.get(0);
        //远程URL
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/productId/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
}

测试：http://localhost:8000/create?userId=2&productId=23



负载均衡
1.使用loadBalancerClient

在services模块加入依赖
<!--负载均衡-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
测试
前提：只能负载均衡注册到nacos的服务
@SpringBootTest
public class OrderApplicationTest {
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Test
    public void test() {
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
    }
}
效果


改造service-order 的OrderServiceImpl的远程调用product服务的方法

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Override
    public Order createOrder(Long userId, Long productId) {
        //        Product product = this.getProductFromRemote(productId);
        Product product = this.getProductFromRemoteWithLoadBalance(productId);
        Order order = new Order();
        order.setId(1L);
        // 总金额=价格*数量
        BigDecimal price = product.getPrice();//价格
        int num = product.getNum();//数量
        order.setTotalAmount(price.multiply(new BigDecimal(num)));//总价
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        // 远程查询商品列表
        order.setProductList(Arrays.asList(product));
        return order;
    }

    //阶段二：加入负载均衡
    public Product getProductFromRemoteWithLoadBalance(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        //远程URL
        String url = "http://" + choose.getHost() + ":" + choose.getPort() + "/productId/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }

    //远程调用获取商品信息
    public Product getProductFromRemote(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.get(0);
        //远程URL
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/productId/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
}
效果

2.使用@LoadBalanced注解
config
@Configuration
public class OrderConfig {
    @LoadBalanced //基于注解式的负载均衡
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
ProductController
@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/productId/{id}")
    public Product getProductById(@PathVariable("id") Long productId) {
        System.out.println("正在远程调用service-product...");
        Product product = productService.getProductById(productId);
        return product;
    }
}
OrderServiceImpl
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Override
    public Order createOrder(Long userId, Long productId) {
        //        Product product = this.getProductFromRemote(productId);
        //        Product product = this.getProductFromRemoteWithLoadBalance(productId);
        Product product = this.getProductFromRemoteWithLoadBalanceAnnotation(productId);
        Order order = new Order();
        order.setId(1L);
        // 总金额=价格*数量
        BigDecimal price = product.getPrice();//价格
        int num = product.getNum();//数量
        order.setTotalAmount(price.multiply(new BigDecimal(num)));//总价
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        // 远程查询商品列表
        order.setProductList(Arrays.asList(product));
        return order;
    }

    //阶段三：于注解的负载均衡
    public Product getProductFromRemoteWithLoadBalanceAnnotation(Long productId) {
        //给远程发送请求；；service-product会被动态替换
        String url = "http://service-product/productId/" + productId;
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }

    //阶段二：加入负载均衡
    public Product getProductFromRemoteWithLoadBalance(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        //远程URL
        String url = "http://" + choose.getHost() + ":" + choose.getPort() + "/productId/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }

    //远程调用获取商品信息
    public Product getProductFromRemote(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.get(0);
        //远程URL
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/productId/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
}
效果：http://localhost:8000/create?userId=2&productId=23
分别在各个service-product打印

 思考：注册中心宕机，远程调用还能成功吗？  

可以进行实验
实验一：五个服务启动，在nacos中有注册，但是没有执行http://localhost:8000/create?userId=2&productId=23测试，也就是没有经过远程调用，然后将nacos关闭，再调用请求测试
结论：不能调用，因为远程调用由于nacos宕机找不到地址
实验一：五个服务启动，在nacos中有注册，但是执行http://localhost:8000/create?userId=2&productId=23测试，已经经过远程调用，然后将nacos关闭，再调用请求测试
结论：可以调用，因为缓存中有地址。但如果对方服务宕机则也调不通。
原理
第一次远程调用要经过两个步骤：1.拿到nacos服务地址列表  2.给对方服务的某个地址发送请求。
第二次及后续：就会将步骤1省略，已经将地址列表放到缓存中了，即使nacos宕机也能远程调用，并且能负载均衡。
小结
1. 使用 RestTemplate 可以获取到远程数据
2. 必须精确指定地址和端口
3. 如果远程宕机将不可用
期望：可以负载均衡调用，不用担心远程宕机
配置中心

整合配置
services导入依赖
<!--配置中心-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
service-order的配置文件application.properties
导入了配置中心的依赖但是没设置（用到）配置中心就会报错，解决办法，关闭自动检查

server.port=8000
spring.application.name=service-order
spring.cloud.nacos.server-addr=127.0.0.1:8848
spring.config.import=nacos:service-order.properties
#暂未用到配置中心功能，需要关闭配置检查
spring.cloud.nacos.config.import-check.enabled=false
service-product
spring.application.name=service-product
spring.cloud.nacos.server-addr=127.0.0.1:8848
#暂未用到配置中心功能，需要关闭配置检查
spring.cloud.nacos.config.import-check.enabled=false
在nacos服务端进行配置


代码测试
order: controller
@RestController
public class OrderController {
    @Autowired
    OrderService orderService;
    @Value("${order.timeout}")
    String orderTimeout;
    @Value("${order.auto-confirm}")
    String orderAutoConfirm;

    @GetMapping("/config")
    public String getConfig() {
        return "OrderTimeout+OrderAutoConfirm = " + orderTimeout+" : " + orderAutoConfirm;
    }
    @GetMapping(value = "/create")
    public Order createOrder(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {
        Order order = orderService.createOrder(userId, productId);
        return order;
    }
}

自动刷新
1. @Value(“${xx}”) 获取配置 + @RefreshScope 实现自动刷新  

但是会产生一个问题：nacos配置中修改后，不重启服务，发请求不能自动更新修改后的数据


实时更新配置显示的办法：在@RestController上加@RefreshScope即可

2. @ConfigurationProperties 无感自动刷新  
加com.atguigu.order.properties.OrderProperties
@Component
@ConfigurationProperties(value = "order")//配置批量绑定在nacos下，可以无需@RefreshScope就能实现自动刷新
@Data
public class OrderProperties {
    String timeout;
    // 这里nacos配置的-分割，会被javabean以驼峰命名方式映射到此处
    String autoConfirm;
}
service-order: controller
//@RefreshScope
@RestController
public class OrderController {
    @Autowired
    OrderService orderService;
    //    @Value("${order.timeout}")
    //    String orderTimeout;
    //    @Value("${order.auto-confirm}")
    //    String orderAutoConfirm;

    @Autowired
    OrderProperties orderProperties;

    @GetMapping("/config")
    public String getConfig() {
        return "OrderTimeout+OrderAutoConfirm = " + orderProperties.getTimeout() + " : " + orderProperties.getAutoConfirm();
    }
    @GetMapping(value = "/create")
    public Order createOrder(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {
        Order order = orderService.createOrder(userId, productId);
        return order;
    }
}
3.NacosConfigManager 监听配置变化  
在启动类中添加ApplicationRunner实例，是一个一次性任务，项目启动其他他就会执行
@SpringBootApplication
public class OrderMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMainApplication.class, args);
    }

    //1、项目启动就监听配置文件变化
    //2、发生变化后拿到变化值
    //3、发送邮件
    @Bean
    ApplicationRunner applicationRunner(NacosConfigManager nacosConfigManager) {
//        return new ApplicationRunner() {
//            @Override
//            public void run(ApplicationArguments args) throws Exception {
//
//            }
//        }
        return args -> {
            //这个监听的服务和application.yml中naocs的配置中心有关
            ConfigService configService = nacosConfigManager.getConfigService();
            configService.addListener("service-order.properties", "DEFAULT_GROUP", new Listener() {
                @Override
                public Executor getExecutor() {
                    return Executors.newFixedThreadPool(4);
                }
                @Override
                public void receiveConfigInfo(String s) {
                    System.out.println("变化的配置信息：" + s);
                    System.out.println("邮件通知....");
                }
            });
            System.out.println("=========");
        };
    }
}

测试效果


 思考： Nacos中的数据集 和 application.properties 有相同的 配置项，哪个生效？  

以配置中心为准，不然要配置中心干什么
测试：





数据隔离
作用：配置中心基于项目激活哪个环境标识，动态指定名称空间，动态加载指定文件和配置


创建几个namespace和Group等方便测试

设置application.yml
按需加载，设置application.yml配置文件，将application.properties注释掉
server:
  port: 8000
spring:
  profiles:
    active: prod
  application:
    name: service-order
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      config:
        import-check:
          enabled: false
        namespace: ${spring.profiles.active:public}

---
spring:
  config:
    import:
      - nacos:common.properties?group=order
      - nacos:database.properties?group=order
    activate:
      on-profile: dev

---
spring:
  config:
    import:
      - nacos:common.properties?group=order
      - nacos:database.properties?group=order
      - nacos:haha.properties?group=order
    activate:
      on-profile: test

---
spring:
  config:
    import:
      - nacos:common.properties?group=order
      - nacos:database.properties?group=order
      - nacos:hehe.properties?group=order
    activate:
      on-profile: prod
代码层面
OrderProperties
@Component
@ConfigurationProperties(value = "order")//配置批量绑定在nacos下，可以无需@RefreshScope就能实现自动刷新
@Data
public class OrderProperties {
    String timeout;
    String autoConfirm;
    String dbUrl;
}
OrderController
//@RefreshScope
@RestController
public class OrderController {
    @Autowired
    OrderService orderService;
//    @Value("${order.timeout}")
//    String orderTimeout;
//    @Value("${order.auto-confirm}")
//    String orderAutoConfirm;

    @Autowired
    OrderProperties orderProperties;

    @GetMapping("/config")
    public String getConfig() {
        return orderProperties.getTimeout() + " : " + orderProperties.getAutoConfirm() + " : " + orderProperties.getDbUrl();
    }
    @GetMapping(value = "/create")
    public Order createOrder(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {
        Order order = orderService.createOrder(userId, productId);
        return order;
    }
}

测试效果







小节

雷：
nacos怎么使用：
1. 注册中心
  a. 服务注册
    ⅰ. 导nacos-discovery依赖
    ⅱ. 配置naocs地址
    ⅲ. 启动nacos
    ⅳ. 启动微服务
  b. 服务发现
    ⅰ. 启动类加@EnableDiscoveryClient注解
    ⅱ. DiscoveryClient用于发现nacos列表所有服务
    ⅲ. 配置类加@Bean （RestTemplate），与DiscoveryClient搭配即可远程调用
    ⅳ. 导loadbalancer依赖
      1. 方案一：用loadBalancerClient.choose(服务名)实习负载均衡
      2. 方案二：直接在@Bean （RestTemplate）加注解@LoadBalanced 
2. 配置中心
  a. 整合配置
    ⅰ. 导入依赖nacos-config
    ⅱ. 配置nacos配置中心地址
spring.config.import=nacos:service-order.properties,nacos:common.properties
#暂未用到配置中心功能，需要关闭配置检查
spring.cloud.nacos.config.import-check.enabled=false
    ⅲ. 在nacos服务端页面编写配置文件
    ⅳ. 用 @Value("${order.timeout}")代码中拿到配置信息
  b. 自动刷新
    ⅰ. 方案一：在有@Value的类上加@RefreshScope实现自动刷新
    ⅱ. 方案二：写一个properties配置类，加注解@ConfigurationProperties(value = "order")+@Component+@Data，用到配置文件的地方@Autowired即可使用
  c. 数据隔离
    ⅰ. 这里主要编写yml配置文件，清楚怎么配置即可
server:
  port: 8000
spring:
  profiles:
    active: dev
    include: feign
  application:
    name: service-order
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      config:
        import-check:
          enabled: false
        namespace: ${spring.profiles.active:public}
---
spring:
  config:
    import:
      - nacos:common.properties?group=order
      - nacos:database.properties?group=order
    activate:
      on-profile: dev

    ⅱ. 多环境（public, dev, test, prod）=》用namespace管理
    ⅲ. 多服务（order, product）=》group
    ⅳ. 多配置（xxx.properties）=>具体xxx.properties
    ⅴ. 多配置项

openFeign
OpenFeign 是一个声明式远程调用客户端；
区别于：restTemplate是编程式远程调用

基础使用

services中导依赖
<!--openfeign-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
启动类加注解
@EnableFeignClients(value = "com.atguigu.order.feign")
编写FeignClient接口
远程调用方法直接将调用地方的controller方法粘贴过来即可
@FeignClient(value = "service-product")
public interface ProductFeignClient {
    @GetMapping(value = "/productId/{id}")
    public Product getProductById(@PathVariable("id") Long productId);
}
改造service-order  OrderServiceImpl
@Autowired
ProductFeignClient productFeignClient;

Product product = productFeignClient.getProductById(productId);


远程调用外部API

feign接口
@FeignClient(value = "weather-client", url = "http://aliv18.data.moji.com")
public interface WeatherFeignClient {
    @PostMapping("/whapi/json/alicityweather/condition")
    String getWeather(@RequestHeader("Authorization") String auth,
                      @RequestParam("token") String token,
                      @RequestParam("cityId") String cityId);
}
测试方法
@SpringBootTest
public class WeatherTest {
    @Autowired
    WeatherFeignClient weatherFeignClient;
    @Test
    public void test() {
        String weather = weatherFeignClient.getWeather("自己的AppCode",
                                                       "50b53ff8dd7d9fa320d3d3ca32cf8ed1","2182");
        System.out.println("weather = " + weather);
    }
}
测试效果





客户端负载均衡：客户端在选择服务地址进行调用，例如service-order调用service-product
服务端负载均衡：服务端进行负载均衡，客户端只要发请求即可，例如调用墨迹天气API

进阶配置
开启日志

application.yml中开启日志
说明： 这行配置是 Spring Boot 的日志配置，用于设置 com.atguigu.order.feign 这个包下的 日志级别 为 DEBUG。  
logging:
  level:
    com.atguigu.order.feign: debug
在OrderConfig中设置日志信息
说明：这是 Feign 提供的日志级别配置，它控制 Feign 请求和响应的详细日志，Logger.Level.FULL 代表 打印所有请求和响应的详细信息
● 请求方法（GET、POST等）
● 请求URL
● 请求头
● 请求体
● 响应状态码
● 响应头
● 响应体
● 请求的执行时间
@Bean
Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
}
超时控制


在application.yml引入application-feign.yml

添加application-feign.yml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            logger-level: full
            connect-timeout: 1000
            read-timeout: 2000
          service-product:
            logger-level: full
            connect-timeout: 3000
            read-timeout: 5000
源码


在service-product中模拟超时，后续记得注销
try {
    TimeUnit.SECONDS.sleep(100);
} catch (InterruptedException e) {
    e.printStackTrace();
}


效果
	
重试机制

在service-order 的OrderConfig中加入重试机制
@Bean
Retryer retryer() {
    return new Retryer.Default();
}

源码：默认重试5次，初始间隔100毫秒，后续每次乘1.5，最多间隔1秒


拦截器

全局拦截器——拦截所有远程调用请求
响应拦截器用的不多，请求拦截器多
 在service-order 的包下 com.atguigu.order.interceptor
@Component
public class XTokenRequestIntercepter implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Token", UUID.randomUUID().toString());
    }
}
改造service-product的ProductController
@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/productId/{id}")
    public Product getProductById(@PathVariable("id") Long productId, HttpServletRequest httpServletRequest) {
        String XToken = httpServletRequest.getHeader("X-Token");
        System.out.println("XToken = " + XToken);
        System.out.println("正在远程调用service-product...");
        //        int i = 10 / 0;
        Product product = productService.getProductById(productId);
        return product;
    }
}
效果


局部拦截器


测试
实验一：拦截器不放容器+配置定制拦截器

效果：有效


实验二：拦截器不放容器+不定制拦截器

效果：失败


fallback - 兜底返回

引入sentinel依赖
<!--sentinel-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
开启熔断
在application-feign.yml加配置
feign:
  sentinel:
    enabled: true
ProductFeignClient加上回调实现	
@FeignClient(value = "service-product", fallback = ProductFeignClientFallback.class)
public interface ProductFeignClient {
    @GetMapping(value = "/productId/{id}")
    public Product getProductById(@PathVariable("id") Long productId);
}
ProductFeignClientFallback
@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long productId) {
        System.out.println("兜底回调....");
        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("0"));
        product.setProductName("未知商品");
        product.setNum(0);

        return product;
    }
}

测试前先将重试机制关了，不然会一直重试，无法快速看到兜底结果。

测试：模拟远程调用失败
在调用service-product的controller中加入一个错误。或者将service-order服务给停掉


小节

雷：
openfeign使用
1. 基础使用
  a. 导依赖openfeign
  b. 启动类加注解@EnableFeignClients(value = "com.atguigu.order.feign")
  c. 写接口@FeignClient(value = "service-product")，再注入使用即可
2. 进阶配置
  a. 开启日志
    ⅰ. 修改yml + 配置类Logger.Level feignLoggerLevel()
  b. 超时控制
    ⅰ. 加一个关于feign的配置，设置服务的连接时间+超时时间
  c. 重试机制
    ⅰ. 在配置类加Bean: new Retryer.Default()
  d. 拦截器
    ⅰ. 全局拦截：写一个拦截类实现RequestIntecepter接口
    ⅱ. 局部拦截：在feign配置文件的某服务下加request-interceptors配置
  e. fallback回调
    ⅰ. 加sentinel依赖
    ⅱ. yml文件加sentinel配置
    ⅲ. @FeignClent(中加入fallback=XXX.class)
    ⅳ. 写@FeignClent的实现类
sentinel

环境搭建
下载sentinel dashboard

启动：
cmd输入：java -jar sentinel.jar
账号密码都是sentinel
配置连接+热加载（服务启动就加载）
spring.cloud.sentinel.transport.dashboard=localhost:8080
spring.cloud.sentinel.eager=true
说明：
簇点链路中的链路来自于几种资源：
1. 主流框架自动适配（例如：web请求）
2. 声明式 Sphu API(不常用)
3.  声明式：@SentinelResource  

异常处理
什么样的异常对应什么样的异常处理

PS:blockHandler只能处理BlockException，而fallback还可以处理业务异常，比如int i = 10/0.
 Web接口 出现异常
model模块写异常类
模拟异常处理
@Data
public class R {
    private Integer code;
    private String msg;
    private Object data;

    public static R ok() {
        R r = new R();
        r.setCode(200);
        return r;
    }
    public static R ok(String msg,Object data) {
        R r = new R();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    public static R error() {
        R r = new R();
        r.setCode(500);
        return r;
    }
    public static R error(Integer code,String msg) {
        R r = new R();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
实现BlockExceptionHandler接口
在service-order的 com.atguigu.order.exception包中写一个BlockExceptionHandler的实现类
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       String resourceName, BlockException e) throws Exception {
        response.setStatus(429); //too many requests
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        R error = R.error(500, resourceName + " 被Sentinel限制了，原因：" + e.getClass());
        String json = objectMapper.writeValueAsString(error);
        writer.write(json);
        writer.flush();
        writer.close();
    }
}

配置流控规则
QPS设置每秒只允许1个请求


效果

违反流控规则就走MyBlockExceptionHandler异常


 @SentinelResource  出现异常
在目标方法加@SentinelResource注解

编写blockHandler方法或者fallback方法处理异常
blockHandler="异常处理方法"，这个方法中加参数: BlockException exception
fallback="异常处理方法"，这个方法中加参数：Throwable exception
二者作用一样
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    ProductFeignClient productFeignClient;

    @SentinelResource(value = "createOrder",blockHandler = "createOrderFallback")
    @Override
    public Order createOrder(Long userId, Long productId) {
        //        Product product = this.getProductFromRemote(productId);
        //        Product product = this.getProductFromRemoteWithLoadBalance(productId);
        //        Product product = this.getProductFromRemoteWithLoadBalanceAnnotation(productId);
        Product product = productFeignClient.getProductById(productId);
        Order order = new Order();
        order.setId(1L);
        // 总金额=价格*数量
        BigDecimal price = product.getPrice();//价格
        int num = product.getNum();//数量
        order.setTotalAmount(price.multiply(new BigDecimal(num)));//总价
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        // 远程查询商品列表
        order.setProductList(Arrays.asList(product));
        return order;
    }

    public Order createOrderFallback(Long userId, Long productId, BlockException e) {
        Order order = new Order();
        order.setId(0L);
        order.setTotalAmount(new BigDecimal("0"));
        order.setUserId(userId);
        order.setNickName("未知用户");
        order.setAddress("异常信息" + e.getClass());
        return order;
    }


    //阶段三：于注解的负载均衡
    public Product getProductFromRemoteWithLoadBalanceAnnotation(Long productId) {
        //给远程发送请求；；service-product会被动态替换
        String url = "http://service-product/productId/" + productId;
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }

}
测试



 OpenFeign调用   出现异常
走openfeign的fallback异常




 SphU 硬编码  出现异常
暂时略...

规则 - 流量控制
在application-feign.yml中加配置
web-context-unify: false #在sentinel中不共用同一个上下文


阈值类型
QPS：统计每秒请求数
并发线程数：统计并发线程数

集群
集群和流控模式是相背离的，二选一

流控模式
流控模式——直接策略：

只对一个资源进行控制，之前的测试一直用这个

流控模式——关联策略：
在OrderController加方法，方便测试
@GetMapping("/writeDb")
public String writeDb() {
    return "writeDb success...";
}
@GetMapping("/readDb")
public String readDb() {
    log.info("readDb success...");
    return "readDb success...";
}

	
情况有两种
1. 单独访问/readDb或者/writeDb，多少请求都能成功
2. 当/writeDb请求量非常大的时候，突然访问/readDb，则会崩溃，走自定义BlockExceptionHandler的实现类
使用场景：当系统里出现资源竞争的时候，使用关联策略进行限制。例如这里只有写量特别大的时候才会限制读，其他不限制。
注意：这里可能需要手速快点！
流控模式——链路策略：
@GetMapping("/seckill")
public Order seckill(@RequestParam("userId") Long userId,
                     @RequestParam("productId") Long productId) {
    Order order = orderService.createOrder(userId, productId);
    order.setId(Long.MAX_VALUE);
    return order;
}

特点：两个请求访问一个资源（随便选一个creatOrder资源，即加了@SentinelResource的资源），但只限制seckill这个请求





流控效果
流控效果——快速失败
作用：处理不了的请求直接丢弃。交给Web接口异常处理MyBlockExceptionHandler。





流控效果——Warm Up
作用：慢慢将每秒的请求达到峰值，有个预热机制

例如我这里QPS是10，需要经过3秒才能达到10，前三秒是慢慢将能接收请求数量提上来。

流控效果——排队等待



注意！


规则 - 熔断降级
熔断降级是保护自身的手段，通常配置在调用端



熔断策略——慢调用比例
举例：统计时长5秒，比例阈值70%的请求在最大RT（最大反应时间）1秒以上，则触发熔断降级。
熔断时长：断开时间，时间到了则会发一个请求试探，有用则通，没用则再继续熔断。
最小请求数：指统计时长内最少发的请求数量。
RT：response time
此处：由于是在远程调用处设置了熔断策略，所以熔断之后走openFeign的fallback处理
此处发生熔断后直接回调，不发远程请求


实验


熔断策略——异常比例
统计时长内发生异常的比例达到比例阈值，则发生熔断.
此处发生熔断后直接回调，不发远程请求。




熔断策略——异常数
统计时长内发生异常的数量达到设置的异常数，则发生熔断.
此处发送了10个远程调用的请求发现都是异常的，直接熔断，30秒内不会再进行远程调用





规则-热点参数
案例


@GetMapping("/seckill")
@SentinelResource(value = "seckill-order",fallback = "seckillFallback") //此处用于测试热点规则
//    public Order seckill(@RequestParam("userId") Long userId,
//                         @RequestParam("productId") Long productId) {
//此处模拟不传参数，给默认参数
//    public Order seckill(@RequestParam(value = "userId",defaultValue = "888") Long userId,
//                         @RequestParam(value = "productId",defaultValue = "1000") Long productId) {
public Order seckill(@RequestParam(value = "userId",required = false) Long userId,
                     @RequestParam(value = "productId",required = false) Long productId) {
    Order order = orderService.createOrder(userId, productId);
    order.setId(Long.MAX_VALUE);
    return order;
}

public Order seckillFallback(Long userId, Long productId, Throwable exception) {
    System.out.println("seckillFallback...");
    Order order = new Order();
    order.setId(productId);
    order.setUserId(userId);
    order.setAddress("seckillFallback异常信息" + exception.getClass());
    return order;
}
需求一
携带流控参数的参与流控，不携带的不参与。
eg: 带了userId流空规则生效，不带不生效。
参数索引标注了参数位置


需求二
这里在参数索引位置为0的参数中，设置了参数值=6的特殊情况，它的限流阈值是10000.
意味着，除了userId=6的请求，其他请求还是每秒不能超过1


需求三
意味着在索引参数为1的参数上，其他请求阈值是100000，但是参数值=666的情况，则限流阈值=0，意味着参数值为666的商品，不让访问。


为什么这里没走fallback回调
因为用错了异常，不能用BlockException，应该用Throwable

小节
雷：
对于sentinel，掌握
1. 不同资源的异常处理
  a.  Web接口  -->  写BlockExceptionHandler实现类
  b. @SentinelResource    -->   写blockHandler和fallback方法
  c. openfeign   -->   走自己的fallback方法
2. 重点掌握流控控制+熔断降级规则，了解热点参数规则
gateway 




环境配置
建module: gateway微服务

改pom
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>annotationProcessor</scope>
    </dependency>
</dependencies>
写yml
application.yml
spring:
  application:
    name: gateway
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
  profiles:
    include: route
server:
  port: 80
application-route.yml
先简单测试
spring:
  cloud:
    gateway:
      routes:
        - id: order
          uri: lb://service-order
          predicates:
            - Path=/api/order/**
        - id: product
          uri: lb://service-product
          predicates:
            - Path=/api/product/**
后期完整版
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: '*'
            allowedHeaders: '*'
            allowedMethods: '*'
      default-filters:
        - AddResponseHeader=X-Response-Abc,123
      routes:
        - id: bing
          uri: https://cn.bing.com/
          predicates:
            - name: Path
              args:
                patterns: /search
            - name: Query
              args:
                param: q
                regexp: haha
#            - Vip=user,lei
            - name: Vip
              args:
                param: user
                value: lei
          order: 10
        - id: order-route
          uri: lb://service-order
          predicates:
            - name: Path
              args:
                patterns: /api/order/**
                matchTrailingSlash: true
          filters:
            - RewritePath=/api/order/?(?<segment>.*), /$\{segment}
            - OnceToken=X-Response-Token,jwt
          order: 1
        - id: product-route
          uri: lb://service-product
          predicates:
            - Path=/api/product/**
          filters:
            - RewritePath=/api/product/?(?<segment>.*), /$\{segment}
          order: 2

启动类
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMainApplication.class, args);
    }
}
业务类
由于配置文件写了路由过滤规则，则需要改
修改OrderController

修改ProductFeignClient

修改ProductController


测试


Predicate - 断言
短写法
这里根据不同服务的predicates条件，将请求执行不同服务的uri
order：代表predicates执行顺序，越小越优先
spring:
  cloud:
    gateway:
      routes:
        - id: order
          uri: lb://service-order
          predicates:
            - Path=/api/order/**
        - id: product
          uri: lb://service-product
          predicates:
            - Path=/api/product/**
长写法

predicates可选列表



名	参数（个数/类型）	作用
After	1/datetime	在指定时间之后
Before	1/datetime	在指定时间之前
Between	2/datetime	在指定时间区间内
Cookie	2/string,regexp	包含cookie名且必须匹配指定值
Header	2/string,regexp	包含请求头且必须匹配指定值
Host	N/string	请求host必须是指定枚举值
Method	N/string	请求方式必须是指定枚举值
Path	2/List<String>,bool	请求路径满足规则，是否匹配最后的/
Query	2/string,regexp	包含指定请求参数
RemoteAddr	1/List<String>	请求来源于指定网络域(CIDR写法)
Weight	2/string,int	按指定权重负载均衡
XForwardedRemoteAddr 	1/List<string>	从X-Forwarded-For请求头中解析请求来源，并判断是否来源于指定网络域

自定义predicates
根据QueryRoutePredicateFactory仿写
@Component
public class VipRoutePredicateFactory extends AbstractRoutePredicateFactory<VipRoutePredicateFactory.Config> {

    public VipRoutePredicateFactory() {
        super(Config.class);
    }

    //段格式传参顺序
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("param", "value");
    }
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                // localhost/search?q=haha&user=leifengyang
                ServerHttpRequest request = serverWebExchange.getRequest();

                String first = request.getQueryParams().getFirst(config.param);

                return StringUtils.hasText(first) && first.equals(config.value);
            }
        };
    }


    @Validated
    public static class Config {

        @NotEmpty
        private String param;


        @NotEmpty
        private String value;

        public @NotEmpty String getParam() {
            return param;
        }

        public void setParam(@NotEmpty String param) {
            this.param = param;
        }

        public @NotEmpty String getValue() {
            return value;
        }

        public void setValue(@NotEmpty String value) {
            this.value = value;
        }
    }
}



Filter - 过滤器
写法
也有长写法和短写法


- RewritePath=/api/order/?(?<segment>.*), /$\{segment}  
这条规则的作用是：将路径 /api/order/{任意内容} 重写为 /{任意内容}。换句话说，它去掉了 /api/order/ 前缀，保留了后面的路径部分。
例如：
● /api/order/123 会被重写为 /123
● /api/order/abc/xyz 会被重写为 /abc/xyz
这类配置通常用于将请求从一个路径重定向到另一个路径，或者将路径中的某些部分移除。
在此处的作用是不用在各微服务加前缀，这里可以统一管理。



Filter可选列表
名	参数（个数/类型）	作用
AddRequestHeader	2/string	添加请求头
AddRequestHeadersIfNotPresent	1/List<string>	如果没有则添加请求头，key:value方式
AddRequestParameter	2/string、string	添加请求参数
AddResponseHeader	2/string、string	添加响应头
CircuitBreaker	1/string	仅支持forward:/inCaseOfFailureUseThis方式进行熔断
CacheRequestBody	1/string	缓存请求体
DedupeResponseHeader	1/string	移除重复响应头，多个用空格分割
FallbackHeaders	1/string	设置Fallback头
JsonToGrpc		请求体Json转为gRPC
LocalResponseCache	2/string	响应数据本地缓存
MapRequestHeader	2/string	把某个请求头名字变为另一个名字
ModifyRequestBody	仅 Java 代码方式	修改请求体
ModifyResponseBody	仅 Java 代码方式	修改响应体
PrefixPath	1/string	自动添加请求前缀路径
PreserveHostHeader	0	保护Host头
RedirectTo	3/string	重定向到指定位置
RemoveJsonAttributesResponseBody	1/string	移除响应体中的某些Json字段，多个用,分割
RemoveRequestHeader	1/string	移除请求头
RemoveRequestParameter	1/string	移除请求参数
RemoveResponseHeader	1/string	移除响应头
RequestHeaderSize	2/string	设置请求大小，超出则响应431状态码
RequestRateLimiter	1/string	请求限流
RewriteLocationResponseHeader	4/string	重写Location响应头
RewritePath	2/string	路径重写
RewriteRequestParameter	2/string	请求参数重写
RewriteResponseHeader	3/string	响应头重写
SaveSession	0	session保存，配合spring-session框架
SecureHeaders	0	安全头设置
SetPath	1/string	路径修改
SetRequestHeader	2/string	请求头修改
SetResponseHeader	2/string	响应头修改
SetStatus	1/int	设置响应状态码
StripPrefix	1/int	路径层级拆除
Retry	7/string	请求重试设置
RequestSize	1/string	请求大小限定
SetRequestHostHeader	1/string	设置Host请求头
TokenRelay	1/string	OAuth2的token转发

自定义Filter
@Component
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                return chain.filter(exchange).then(Mono.fromRunnable(()->{
                    //注意这里是response
                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = response.getHeaders();
                    String value = config.getValue();
                    if ("uuid".equalsIgnoreCase(value)) {
                        value = UUID.randomUUID().toString();
                    }
                    if ("jwt".equalsIgnoreCase(value)) {
                        value = "JWT";
                    }

                    headers.add(config.getName(), value);
                }));
            }
        };
    }
}

测试


设置默认filter


CORS - 跨域处理
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: '*'
            allowedHeaders: '*'
            allowedMethods: '*'
● 适用于所有路径（[/**]）。
● 允许任何来源（allowedOriginPatterns: '*'） 访问 API。
● 允许所有 HTTP 头部（allowedHeaders: '*'）。
● 允许所有请求方法（allowedMethods: '*'），包括 GET、POST、PUT、DELETE 等。

 [/**] 其中 /** 代表所有 API 路径  
注意：[/**]必须加方括号 []，否则 YAML 解析可能会出错。  

测试


GlobalFilter
/*
* 此实例作用：GlobalFilter 通常，全局过滤器在网关层 用于 拦截所有请求，
* 而局部过滤器（GatewayFilter）用于 单个路由。
* 案例：请求耗时=请求结束时间-请求进来时间
* */
@Component
@Slf4j
public class RTGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        long startTime = System.currentTimeMillis();

        log.info("请求【{}】开始，时间{}", uri, startTime);
        //=============上面是前置逻辑==================

        Mono<Void> filter = chain.filter(exchange).doFinally(r -> {
            long endTime = System.currentTimeMillis();
            log.info("请求【{}】结束，时间{}，耗时：{}ms", uri, endTime, endTime - startTime);
        });

        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
测试

小节
gateway主要是yml配置，重点:
1. 导依赖：gateway，nacos-discovery，lombok（@Slf4j用）
2. predicates的yml配置，自定义断言类
3. filters的yml配置，自定义filter类，设置默认filter
4. CORS跨域的yml配置
5. GlobalFilter全局过滤器
seata
seata服务器的web界面的端口是7091，而8091是TC协调者的TCP端口
是什么
说明：
1. seata有服务器端和客户端，客户端要连上服务器才能使用。
2. TC（事务协调者）在服务器端 得官网下载： 全局事务的管理者。用于维护全局和分支事务的状态，驱动TM的全局提交和回滚。TM和RM通过TC注册分支和汇报状态。
3. TM（事务管理器）在客户端：发起全局事务，定义全局事务的范围，操作全局事务的提交和回滚。
4. RM（资源管理器）在客户端：操作自己分支的事务提交和回滚。
5. 注意：seata的稳定性非常重要，如果TC崩了，那所有的事务管控都失效。



怎么用
环境搭建
微服务

SQL
在mysql中执行sql创建库表
CREATE DATABASE IF NOT EXISTS `storage_db`;
USE  `storage_db`;
DROP TABLE IF EXISTS `storage_tbl`;
CREATE TABLE `storage_tbl` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `commodity_code` varchar(255) DEFAULT NULL,
                               `count` int(11) DEFAULT 0,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO storage_tbl (commodity_code, count) VALUES ('P0001', 100);
INSERT INTO storage_tbl (commodity_code, count) VALUES ('B1234', 10);

-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `branch_id` bigint(20) NOT NULL,
                            `xid` varchar(100) NOT NULL,
                            `context` varchar(128) NOT NULL,
                            `rollback_info` longblob NOT NULL,
                            `log_status` int(11) NOT NULL,
                            `log_created` datetime NOT NULL,
                            `log_modified` datetime NOT NULL,
                            `ext` varchar(100) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE DATABASE IF NOT EXISTS `order_db`;
USE  `order_db`;
DROP TABLE IF EXISTS `order_tbl`;
CREATE TABLE `order_tbl` (
                             `id` int(11) NOT NULL AUTO_INCREMENT,
                             `user_id` varchar(255) DEFAULT NULL,
                             `commodity_code` varchar(255) DEFAULT NULL,
                             `count` int(11) DEFAULT 0,
                             `money` int(11) DEFAULT 0,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `branch_id` bigint(20) NOT NULL,
                            `xid` varchar(100) NOT NULL,
                            `context` varchar(128) NOT NULL,
                            `rollback_info` longblob NOT NULL,
                            `log_status` int(11) NOT NULL,
                            `log_created` datetime NOT NULL,
                            `log_modified` datetime NOT NULL,
                            `ext` varchar(100) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE DATABASE IF NOT EXISTS `account_db`;
USE  `account_db`;
DROP TABLE IF EXISTS `account_tbl`;
CREATE TABLE `account_tbl` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `user_id` varchar(255) DEFAULT NULL,
                               `money` int(11) DEFAULT 0,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO account_tbl (user_id, money) VALUES ('1', 10000);
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `branch_id` bigint(20) NOT NULL,
                            `xid` varchar(100) NOT NULL,
                            `context` varchar(128) NOT NULL,
                            `rollback_info` longblob NOT NULL,
                            `log_status` int(11) NOT NULL,
                            `log_created` datetime NOT NULL,
                            `log_modified` datetime NOT NULL,
                            `ext` varchar(100) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
使用步骤
1. 下载seata服务端：https://seata.apache.org/zh-cn/download/seata-server
2. 解压并启动：seata-server.bat 访问：http://localhost:7091/  账号密码都是seata
3. 在微服务中导入spring-cloud-starter-alibaba-seata依赖
4. 在resources里配置file.conf文件，每个seata服务都配置
service {
  #transaction service group mapping
  vgroupMapping.default_tx_group = "default"
  #only support when registry.type=file, please don't set multiple addresses
  default.grouplist = "127.0.0.1:8091"
  #degrade, current not support
  enableDegrade = false
  #disable seata
  disableGlobalTransaction = false
}
5. 在RM分支事务微服务中启动类上加入@EnableTransactionManagement，在具体事务service方法上加@Transactional
6. 在TM全局事务微服务中启动类上不加@EnableTransactionManagement，但在事务service方法上要加上@GlobalTransactional。这个注解是核心
7. 测试结果

测试本地事务
没有@EnableTransactionManagement，@Transactional两注解




有@Transactional，但没有@EnableTransactionManagement
会产生一个问题：没有@EnableTransactionManagement，异常数据也能回滚，为什么？
在 Spring Boot 中，不需要 @EnableTransactionManagement 也能回滚数据，因为 Spring Boot 默认开启了事务管理。如果你在 Spring 传统项目里，可能就需要手动启用事务管理。
所以还是启动类加上@EnableTransactionManagement

有@EnableTransactionManagement，@Transactional两注解
seata-account,  seata-storage单个本地事务是有效的，但是对于seata-order要远程调用seata-account，有异常订单不会创建，但是远程调用的余额扣减却成功了。
这就导致：分布式事务用这两个注解不管用，怎么办？

修改seata-order
去掉@EnableTransactionManagement
将@Transactional改成@GlobalTransactional



效果：seata-order出现异常，订单没创建，远程调用账户扣减数据能回滚
再测试有@EnableTransactionManagement，有@GlobalTransactional
效果：和上面一样。再一次证明springboot默认开启事务

测试分布式事务——模拟情景
采购服务要远程调用 扣库存和下订单服务，订单服务又要远程调用扣减余额服务，怎么保证分布式事务的数据一致性？
操作：
1. 先在各个模块启动类上加入@EnableTransactionManagement，全局事务（采购模块）除外。
2. 各分支事务加@Transactional，全局事务加@GlobalTransactional
3. 在订单模块模拟错误进行测试


情景：结算服务中事务上没有加@GlobalTransactional情况，远程调用扣减库存服务和创建订单服务（订单服务要远程调用扣减余额服务）。模拟订单服务出错，看扣减余额远程调用能否回滚
结果：库存扣减，余额扣减，订单没有生成；只有订单服务回滚，其他皆无效。



其他
如果导入了seata的依赖：spring-cloud-starter-alibaba-seata
所在模块的file.conf配置文件先自动配置，不行再将格式改成properties




seata各模式原理
区分狭义事务和广义事务
狭义事务：数据库的操作
广义事务：业务的事务（包括数据库操作），举例：事务中需要发邮件和发短信，这种不能撤回的情况，AT和XA模式不管用了。

AT模式
图片里是AT模式的二阶段提交协议，其他模式也是二阶段提交（一阶段：本地事务 二阶段：提交/回滚 ）

AT：系统默认使用，各分支事务要经过两阶段提交协议。
1. 第一阶段：本地提交
  a. 生成前镜像：将要操作的数据记录下来
  b. 执行SQL操作数据(期间启动MySQL的行锁，先对要操作的数据select ...for update（此时数据被锁住，普通的select可以读取，要看隔离级别。还有如果读操作的select..for update这种想加锁的读是不可以的），再执行后续sql，执行完则释放行锁)
  c. 生成后镜像：将操作后的数据记录下
  d. 前镜像和后镜像等待保存到uodo_log日志表中
  e. 向TC注册分支，在TC中申请一个全局锁，锁定操作的数据防止其他人操作，读同上，有锁才能操作。注意这里TC的全局锁不是MySQL的全局锁，MySQL的全局锁是锁整个数据库，而TC的全局锁相当于MySQL的行级别锁，只锁操作的数据。
  f. 本地事务提交；将业务数据和uodo_log日志表数据一起保存到当前事务的的表中
  g. 和TC汇报事务执行状态
2. 第二阶段：
  a. 若各分支事务都成功：删除uodo_log记录
    ⅰ. TC能感知到每个事务的状态，通知他们进行提交
    ⅱ. 给异步任务对列添加异步任务，异步+批量删除对应的uodo_log日志表的记录
  b. 若某个事务失败，TC会通知所有分支事务回滚：拿到前镜像，数据恢复，删除uodo_log记录
    ⅰ. 先找到uodo_log记录（通过XID，BranchID）
    ⅱ. 数据校验，后镜像和当前数据是否一致，一致就ok执行回滚；不一致说明当前数据被其他操作给篡改了，需要配置相应的策略（怎么处理这个脏数据，忽略还是人工处理？之类）
    ⅲ. 若一致，则回滚数据到前镜像的内容，完成后删除uodo_log记录
  c. 只要有分支事务没处理完，全局锁会一直存在。但是第一阶段执行事务是真正提交了的，不会在第二阶段一直阻塞数据库。


XA模式：
第一阶段不会提交数据，阻塞事务请求，在第二阶段确认提交再提交或者回滚。全局锁+MySQL行锁在第一阶段就开启，事务一开始就用阻塞模式，性能差。AT和XA区别是AT第一阶段执行完SQL释放行锁，XA是到第二阶段才提交SQL导致行锁从开始到最后，阻塞时间长性能差。但二者都是一直持有seata的全局锁的。
TCC模式：
适用于一些夹杂了 非数据库事物的代码
主要是广义上的事务，需要写侵入式的代码。举例业务需要三个事务，一个事务改数据库，一个发短信，一个发邮件，这就用AT和XA行不通了，无法回滚，如果全局事务失败，只能进行补偿性操作，例如再发邮件和短信提醒对方扣款失败或者订单失败等。
saga模式：
用于长事务，一时半会执行不完的事务。例如请假审批，其他模式都用了锁，如果长期锁在那是对系统是非常大的阻塞。saga是基于消息队列做的，后续有替代方案，所以这个几乎不用。


debug查看seata执行流程








小节
seata要点：
1. 使用。引入了alibaba-seata依赖，只要在分布式事务的方法加@GlobalTransactional，在其他本地事务加@EnableTransactionManagement，@Transactional即可。这样分布式事务中无论哪个环节异常，都会回滚。
2. 原理，以及四种模式的特点

