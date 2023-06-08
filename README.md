"userAccount":"admin",
"userPassword":"11111111"

错的多了，就解决bug快了
插叙:根据id/[queryWrapper]/其他(tags、name等)
---
如何写项目:
                    部署环境[JDK(settinng)、Maven(project)]
					初始模板[统一、分页、逻辑删除、swagger、Exce]->修改配置(MySQL、Redis)
					文档设计->数据库表->代码生成(XML、逻辑删除)->业务逻辑->敲代码(基础CRUD、功能接口)->测试
如何更改初始项目:
            启动->①重构[包路径]  ②修改Scan扫描注解[路径]   ③JDK配置[project]+Maven[配置]+YML[mysql、redis]   ④更新依赖
如何插表[列/表]:
            ①表->生成代码 ②实体类[逻辑删除]+XML[列明注入]+包路径修改？
如何解决bug:看正确方法、对照
-----



第一天
1)初始化开源模板     ->更换包名->全局重构+Scan扫描命令(手动)+APP启动(手动)
                  ->密码加密  ->不能数据库直接添加->注册逻辑(请求封装体/api)+登录逻辑(请求封装体/api)
2)标签查询         ->数据库/实体类/xml/脱敏函数(添加)+querywarpper查询+时间测试/测试类/debug+逻辑删除问题
                  ->（SQL查询版）+（Gson查询版）
                  ->用户脱敏（脱敏函数添加tags字段）| mysql配置信息

第三天
1)接口文档生成        ->swarge+Knife4j ->依赖+配置(类)文件+yml配置+注解
                   ->api文档显示异常  -> @EnableSwagger2WebMvc
2)excel上传         ->依赖+实体类+监听类+脚本类 or  直接读取 ->存入数据库

第四天
1)分布式session登录  ->redis安装 + 依赖 + yml配置
                   ->模拟多台服务器      ->java -jar user-center-backend-0.0.1-SNAPSHOT.jar --server.port=8081
                   ->取消redis         ->注释依赖 -> clean  ->install

第五天
1)用户修改接口       ->[权限判断]+update更新(实体类)
2)用户登录接口

第六天   ->好好看看user的CRUD业务逻辑
1)首页用户推荐接口          ->list查询 ->page分页(页码[front]、每页个数[front] + 配置类)
2)导入/导出数据[假数据]     ->导出(可视化:①表格or②SQL语句)  导入([①]表格、[②]SQL语句、[③]定时任务)
                         ->频繁连接([①]批量saveBatch)and循环的绝对线性([②]并发Threads)
                         ->①一次性任务   ②测试类     定时任务单次执行、test类(打包有问题)

第七天     数据查询慢->缓存->缓存预热  |  离线准备好or实时呈现
1)首页用户推荐接口          ->list->page->redis缓存[序列化配置类]->缓存预热(定时任务->[redis缓存第一次慢])
                         ->注意:过期时间+设计key
2)定时任务(保证第一次进来也很快)

第八天   分布式锁 -> Redission(无感知的使用redis) ->（思考为什么用、怎么用）-> （分布式 -> 无锁情况[资源浪费|重复数据]、分布式锁[抢锁原理|setnx乐观锁|过期日期|释放锁]、续期[看门狗]、释放别人锁、原子性操作）
         -> 依赖+配置类+锁(原代码基础上)