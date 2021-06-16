> 在k8s中部署Eureka集群
## 一、背景
普通后端如果想要同时起多个服务来进行``负载均衡``，可以通过部署``Deployment``并调整``Pod``的数量，然后交由``Service``来代理这些``Pod``，
而对于``Eureka``而言，这样做就没那么方便了，因为``Eureka``之间还需要``互相注册``，因此需要做一些特殊的改动。
主要用到了``StatefulSet``和``Headless Service``这两个控制器
## 二、StatefulSet
这是一个用于管理有状态程序的控制器，它在管理``Pod``时，确保每一个``Pod``有一个``按顺序增长的ID``。它与``Deployment``相似，也是基于一个``Pod``模板进行管理其``Pod``，最大的不同在于
它始终给``Pod``分配不变的名称。
### 1、使用场景
* 稳定且唯一的网络标识，``Pod``重新调度后并不会更改``PodName``和``HostName``，基于``Headless Service``实现
* 稳定的持久化存储，每个``Pod``始终对应属于自己的存储，基于``PersistentVolumeClaimTemplate``
* 有序的增加(从0到N-1)、减少副本(从N-1到0)
* 按顺序的滚动更新
### 2、DNS格式
``StatefulSet``中每个``Pod``的DNS格式为
```
StatefulSetName-{0..N-1}.ServiceName.namespace.svc.cluster.local
```
* ``ServiceName``为``Headless Service``的名字
* ``0..N-1``为``Pod``所在的序号，从``0``开始到``N-1``
* ``StatefulSetName``为``StatefulSet``的名字
* ``namespace``为命名空间，``Headless Service和StatefulSet必须在相同的namespace``
* ``cluster.local``为``Cluster Domain``    

``如果Pod在同一个命名空间内，可以省略.namespace.svc.cluster.local``
### 3、HeadLess Service
``HeadLess Service``和普通的``Service``最大的区别在于它代理的``每一个Pod都会有对应一个域名``(上节提到的DNS格式)，在实际使用它的时候，我们需要将它的``clusterIP``属性设置为``None``
来表明它是一个``HeadLess Service``
## 三、搭建Eureka项目
### 1、创建项目
这里自行创建Eureka项目哦，这里不做演示
### 2、启动类增加注解
```java
/**
 * 增加@EnableEurekaServer来开启Eureka服务
 * @author Gjing
 */
@EnableEurekaServer
@SpringBootApplication
public class K8sEurekaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(K8sEurekaDemoApplication.class, args);
    }
}
```
### 3、配置文件
这里使用了变量的形式设置配置的值，这样就可以根据不同的环境来分别配置了。如果对变量的使用不熟悉的话建议恶补一下SpringBoot项目在Yaml文件使用变量。这里只是演示的配置，在实际使用时
根据业务需求来合理配置
```yaml
server:
  port: ${EUREKA_HOST:8761}
spring:
  application:
    name: ${SERVER_NAME:k8s-eureka-demo}
eureka:
  client:
    fetch-registry: ${FETCH_EUREKA:false}
    register-with-eureka: ${REGISTER_EUREKA:false}
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
  instance:
    # 当前实例的主机名称
    hostname: ${HOST_NAME:localhost}
  server:
    # 关闭安全模式
    enable-self-preservation: false
```