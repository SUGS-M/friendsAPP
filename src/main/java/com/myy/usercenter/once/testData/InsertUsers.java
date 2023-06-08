package com.myy.usercenter.once.testData;

import com.myy.usercenter.mapper.UserMapper;
import com.myy.usercenter.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    //@Override
    /**直接加载进入
     //时间 3924ms  1000条
     //245845ms 10万条
     //26756ms 1万条
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        System.out.println("goodgoodgood");
        stopWatch.start();
        final int INSERT_NUM = 10000;
        for (int i=0; i<INSERT_NUM; i++){
            User user = new User();
            user.setUsername("Msir");
            user.setUserAccount("fakeM");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("11111111");
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("stopWatch.getTotalTimeMillis():"+stopWatch.getTotalTimeMillis());
    }
     */

    /**
    //直接加载进入（分批->优化数据库连接次数）
    //时间 1962ms  1000条
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        System.out.println("goodgoodgood");
        stopWatch.start();
        List<User> userList = new ArrayList<>();
        final int INSERT_NUM = 1000;
        for (int i=0; i<INSERT_NUM; i++){
            User user = new User();
            user.setUsername("Msir");
            user.setUserAccount("fakeM");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("11111111");
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("");
            userList.add(user);
            //userMapper.insert(user);
        }
        userService.saveBatch(userList,10);
        stopWatch.stop();
        System.out.println("stopWatch.getTotalTimeMillis():"+stopWatch.getTotalTimeMillis());
    }
    */



    /**并发
    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 分十组
        int batchSize = 1000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("Msir");
                user.setUserAccount("fakeM");
                user.setAvatarUrl("");
                user.setGender(0);
                user.setUserPassword("11111111");
                user.setPhone("");
                user.setEmail("");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("");
                user.setTags("");
                userList.add(user);
                //userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        // 20 秒 10 万条
        //2307ms  1万条
        stopWatch.stop();
        System.out.println("stopWatch.getTotalTimeMillis():"+stopWatch.getTotalTimeMillis());
        //System.out.println(stopWatch.getTotalTimeMillis());
    } */


}
