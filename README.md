2020.9.6 构建读取配置文件类，创建对象模型

2020.9.7 创建数据库连接池

数据库连接的配置文件默认放在conf目录下的db.conf文件中

连接池分为三类，分别为：NewCacheDBPool、NewFixedDBPool、NewSingleDBPool
- NewCacheDBPool：关闭数据库连接的时候完成连接池数量的维护，定时任务中清除不可用连接
- NewFixedDBPool：为了保证固定连接数的大小，不随意释放连接，关闭的时候将连接添加到连接池中。在定时任务中，如果碰到不可用连接，则创建新的连接并替换
- NewSingleDBPool：不需要维护连接池，只需要完成连接的创建和销毁