package utils;

import com.globalvariable.IpAddr;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile) {
        //配置全局链接地址
        if (multipartFile == null) {
            return "nullerror";
        }
        String file = PmsUploadUtil.class.getResource("/tracker.conf").getPath();
        try {
            byte[] bytes = multipartFile.getBytes();
            ClientGlobal.init(file);
            TrackerClient trackerClient = new TrackerClient();
            //文件后缀名
            int k = multipartFile.getOriginalFilename().lastIndexOf(".");
            String extName = multipartFile.getOriginalFilename().substring(k + 1);
            System.out.println("---"+extName);
            //获得实例
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String[] upload_file = storageClient.upload_file(bytes, extName, null);
            String URL = IpAddr.FASTDFSIP;
            for (int i = 0; i < upload_file.length; i++) {
                String s = upload_file[i];
                URL += "/" + s;

            }
            System.out.println("---"+URL);
            return URL;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";


        }
    }
}
