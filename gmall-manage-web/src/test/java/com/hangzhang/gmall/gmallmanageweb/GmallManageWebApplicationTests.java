package com.hangzhang.gmall.gmallmanageweb;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws Exception{
        //配置全局链接地址
        String file = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();
        ClientGlobal.init(file);
        TrackerClient trackerClient=new TrackerClient();
        //获得实例
        TrackerServer trackerServer=trackerClient.getConnection();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String[] upload_file = storageClient.upload_file("d://a.jpg","jpg", null);
        String URL = "http://192.168.0.199";
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            URL+="/"+s;

        }
        System.out.println("url = " + URL);
    }


}
