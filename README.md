### Campus takeout

Just a project for java beginners to learn

------

1. 项目背景

   本项目是为校园外卖定制的一款软件，包括管理后台和移动端应用两部分，其中系统管理后台主要是内部管理人员使用，可以对餐厅的菜品、订单等进行维护。移动端应用主要是提供给消费者，可以在线浏览菜品、添加购物车、下单等。

2. 技术架构

   应用层：SpringBoot+SpringMVC+Spring Session
   
   数据层：Mysql+Mybatis+Redis
   
   代码管理工具类：Git+Maven

3. 项目模块

   common：全局配置模块，负责全局校验、全局的异常处理、序列化等

   config：配置模块，包含布隆过滤器的配置类、Mybatis plus的配置类、redis的配置类、以及MVC框架的配置类，从HTTP请求中获得信息，提取参数，并分发给不同的处理服务。

   controller：控制模块，针对前端的请求(post、get、put、delete)做出不同的回复

   dto和entity：entity是实体类，是数据库中的字段映射，dto是entity的增强，为了满足前后端的数据类型匹配

   filter：检查用户是否已经完成登录

   mapper：mapper模块，将mapper交给Spring管理，给mapper接口自动生成一个接口实现类

   service：服务模块，实现数据验证、数据处理、以及事物控制等，提供访问业务逻辑的接口

   utils：工具模块，提供了验证码生成以及验证的方法，以及调用阿里API，完成验证码的发送

