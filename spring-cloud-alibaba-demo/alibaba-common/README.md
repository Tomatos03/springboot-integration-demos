# alibaba-common 模块

alibaba-common 是 spring-cloud-alibaba-demo 项目的公共模块，主要用于存放各个业务子模块都可能会用到的通用代码和资源。  
本模块的主要内容包括：

- **entity/**：公共实体类，供各业务模块复用
- **dto/**：数据传输对象
- **vo/**：视图对象
- **utils/**：工具类（如日期、字符串、加解密等）
- **constant/**：常量定义
- **exception/**：统一异常处理相关类

通过将通用代码集中在 alibaba-common，可以有效减少重复开发，提高代码复用性和维护效率。  
其他业务模块可通过依赖本模块，直接使用其中的公共资源。