package cc.shixw.geektime.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 极客时间课程下载工具
 */
public class GeekTimeArticleDownloadTool {

    /**
     * 课程列表URL
     */
    private static final String articles_url = "https://time.geekbang.org/serv/v1/column/articles";

    /**
     * 获取课程具体文章明细URL
     */
    private static final String article_info_url = "https://time.geekbang.org/serv/v1/article";

    private static final String cookie = " gksskpitn=921d7d8a-d17d-4e24-adcb-e94474fde7f8; _ga=GA1.2.906367990.1667781549; Hm_lvt_022f847c4e3acd44d4a2481d9187f1e6=1666400970; Hm_lvt_59c4ff31a9ee6263811b23eb921a5083=1666400970; LF_ID=80f4e8f-d7a5465-c0baa65-109dbe1; GCID=099e9c6-7bc04eb-b93c5b6-62bfb08; GRID=099e9c6-7bc04eb-b93c5b6-62bfb08; _gid=GA1.2.1918513256.1668042175; GCESS=BgYEiRhRgwME3k1sYwkBAQoEAAAAAA0BAQIE3k1sYwcEmp.bQgwBAQQEAI0nAAsCBgABCHs6IAAAAAAACAEDBQQAAAAA; mantis5539=7606e205c49e4c9e9c2bd4f00dce5b41@5539; MEIQIA_TRACK_ID=2Bjwbqq2nxpkPFpMv4ytoU4Uj0w; MEIQIA_VISIT_ID=2HKmet0PxFwLAkS86TyZAXzBH4p; _gat=1; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%222112123%22%2C%22first_id%22%3A%221844f86dca689f-041882ea285058c-18525635-2007040-1844f86dca7cf9%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E8%87%AA%E7%84%B6%E6%90%9C%E7%B4%A2%E6%B5%81%E9%87%8F%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC%22%2C%22%24latest_referrer%22%3A%22https%3A%2F%2Fwww.google.com.hk%2F%22%2C%22%24latest_landing_page%22%3A%22https%3A%2F%2Ftime.geekbang.org%2F%22%7D%2C%22%24device_id%22%3A%221844f86dca689f-041882ea285058c-18525635-2007040-1844f86dca7cf9%22%7D; SERVERID=1fa1f330efedec1559b3abbcb6e30f50|1668042670|1668042174; Hm_lpvt_59c4ff31a9ee6263811b23eb921a5083=1668042671; Hm_lpvt_022f847c4e3acd44d4a2481d9187f1e6=1668042671; gk_process_ev={%22count%22:7%2C%22utime%22:1668042206461%2C%22referrer%22:%22https://time.geekbang.org/%22%2C%22target%22:%22%22}";

    /**
     * 要下载的课程ID
     */
    private static final String cid = "100036401";

    private static final String articles_root_path = "geek-time-article-view/public/geek-time-articles/";
    /**
     * catalog
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws Exception {

        // 创建课程对应的目录
        createArticleRootFolder();
        // 查询对应的课程ID的目录信息
        List<Map<String,String>> list = fetchArticles(cid);
        // 将目录信息序列化后写入文件中
        writeArticleCatalog2RootFolder(list);

        // 遍历查询所有的文章信息

        for (Map<String, String> articleMap : list) {
            // 查询文章信息
            Map<String,String> articleInfo = getArticleInfo(articleMap.get("id"));
            // 下载文件离线文件，并放到目录中
            downloadArticleOffline(articleInfo);
            // 解压下载的离线文件
            unZipOfflineFile(articleMap);
            Thread.sleep(5000L);
        }

        System.out.println("=========");
    }

    /**
     * 解压缩离线文件
     * @param articleInfo
     */
    public static void unZipOfflineFile(Map<String, String> articleInfo ) throws IOException {
        String id = articleInfo.get("id");
        try {
            String path = articles_root_path+cid+File.separator+id+File.separator;
            ZipFile zipFile = new ZipFile(path+"articleInfoZip.zip");
            zipFile.extractAll(path+"articleInfo");

            zipFile.getFile().delete();// 删除压缩文件

            // 删除mp3文件
            File articleInfoFile = new File(path+"articleInfo");
            File[] files = articleInfoFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                if (temp.getName().endsWith(".mp3")){
                    temp.delete();
                }else
                    // 处理图片信息
                if (temp.getName().endsWith(".jpeg") || temp.getName().endsWith(".jpg")||temp.getName().endsWith(".png")){
                    // 将图片异动到image文件夹
                    temp.renameTo(new File(articles_root_path+cid+File.separator+"image"+File.separator+temp.getName()));
                }
            }

        } catch (Exception e) {
            System.out.println("解压缩文章:"+id+" 失败！！！！");
            e.printStackTrace();
        }
    }

    /**
     * 下载文章离线文件，并放到文件夹中
     * @param articleInfo
     */
    public static void downloadArticleOffline(Map<String, String> articleInfo) throws IOException {
        String id = articleInfo.get("id");
        String url = articleInfo.get("download_url");
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try {
            System.out.println("===========下载课程文章:"+id+" 对应的离线数据=========================================================");
            client = HttpClients.createDefault();
            HttpGet downloadHttpGet = new HttpGet(url);


            downloadHttpGet.setHeader("Origin", "https://time.geekbang.org");
            downloadHttpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
            downloadHttpGet.setHeader("Cookie",cookie);

            response = client.execute(downloadHttpGet);

            HttpEntity responseEntity = response.getEntity();
            InputStream inputStream = responseEntity.getContent();

            byte[] getData = readInputStream(inputStream);
            // 创建文章对应的目录
            File articleInfoFolder = new File(articles_root_path+cid+File.separator+id);
            if (articleInfoFolder.exists()){
                articleInfoFolder.delete();
            }
            articleInfoFolder.mkdir();// 创建文件夹
            // 写入下载的文件
            File articleInfoFile = new File(articleInfoFolder.getPath()+File.separator+"articleInfoZip.zip");
            // 写入文件
            FileOutputStream fos = new FileOutputStream(articleInfoFile);
            fos.write(getData);

            fos.close();
            System.out.println("===========下载课程文章:"+id+" 对应的离线数据==============完成===========================================");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            response.close();
            client.close();
        }
    }

    /**
     * 读取
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    /**
     * 将文章目录信息转换为json写入到根目录
     * @param list
     */
    public static void writeArticleCatalog2RootFolder(List<Map<String, String>> list) throws IOException {
        File file = new File(articles_root_path+cid+"/catalog.json");
        if (file.exists()){// 如果存在就删除
            file.delete();
        }
        file.createNewFile();
        // 写入文件
        Writer write = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));
        write.write(JSON.toJSONString(list));
        write.flush();
        write.close();
    }

    /**
     * 创建文章对应的根目录
     */
    public static void createArticleRootFolder() {
        File rootPath = new File(articles_root_path+cid);
        if (rootPath.exists()){ // 如果目录存在则删除
            rootPath.delete();
        }
        rootPath.mkdir();// 创建文件夹

        // 创建图片存储文件夹
        File imagePath = new File(rootPath.getPath()+File.separator+"image");
        if (imagePath.exists()){
            imagePath.delete();
        }
        imagePath.mkdir();
    }


    /**
     * 获取文章信息
     * @param articleId
     * @return
     */
    public static Map<String,String> getArticleInfo(String articleId) throws IOException {
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try {
            System.out.println("===========查询课程文章:"+articleId+" 对应的详细信息=========================================================");
            client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(article_info_url);


            StringEntity entity = new StringEntity("{\"id\":\""+articleId+"\",\"include_neighbors\":true,\"is_freelyread\":true}", Charset.forName("UTF-8"));
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Origin", "https://time.geekbang.org");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
            httpPost.setHeader("Cookie",cookie);

            response = client.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();

            String s = EntityUtils.toString(responseEntity, "UTF-8");
            System.out.println("查询课程文章:"+articleId+" 对应信息返回:"+s);
            JSONObject result = JSON.parseObject(s);
            // 获取data
            JSONObject data = result.getJSONObject("data");

            Map<String,String> articleInfoMap = new HashMap<String, String>();

            articleInfoMap.put("id",data.getString("id"));
            articleInfoMap.put("article_title",data.getString("article_title"));

            // 获取离线下载信息
            JSONObject offline = data.getJSONObject("offline");

            articleInfoMap.put("download_url",offline.getString("download_url"));
            articleInfoMap.put("file_name",offline.getString("file_name"));
            articleInfoMap.put("size",offline.getString("size"));

            System.out.println("===========查询课程文章:"+articleId+" 对应的详细信息===========完成==============================================");
            return articleInfoMap;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            response.close();
            client.close();
        }

        return null;
    }



    /**
     * 获取课程信息
     */
    public static List<Map<String,String>> fetchArticles(String cid) throws IOException {
        CloseableHttpResponse response = null;
        CloseableHttpClient client = null;
        try {
            System.out.println("===========查询课程:"+cid+" 对应的文章列表信息=========================================================");
            client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(articles_url);


            StringEntity entity = new StringEntity("{\"cid\":\""+cid+"\",\"size\":500,\"prev\":0,\"order\":\"earliest\",\"sample\":false}", Charset.forName("UTF-8"));
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Origin", "https://time.geekbang.org");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
            httpPost.setHeader("Cookie",cookie);

            response = client.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();

            String s = EntityUtils.toString(responseEntity, "UTF-8");
            System.out.println("查询课程:"+cid+"文章集合返回:"+s);
            JSONObject result = JSON.parseObject(s);
            // 获取data
            JSONObject data = result.getJSONObject("data");
            // 获取data中的list
            JSONArray articleList = data.getJSONArray("list");

            List<Map<String,String>> resultList = new ArrayList<Map<String, String>>();

            // 遍历转换
            for (int i = 0; i < articleList.size(); i++) {
                JSONObject article = articleList.getJSONObject(i);

                Map<String,String> articleMap = new HashMap<String, String>();
                articleMap.put("article_title",article.getString("article_title"));
                articleMap.put("id",article.getString("id"));


                resultList.add(articleMap);
            }
            System.out.println("===========查询课程:"+cid+" 对应的文章列表信息=====完成====================================================");
            return resultList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            response.close();
            client.close();
        }

        return null;
    }

}
