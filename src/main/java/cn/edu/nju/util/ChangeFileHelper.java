package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextStaticRepo;
import cn.edu.nju.pattern.Pattern;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by njucjc at 2018/1/22
 */
public class ChangeFileHelper {
    private List<Pattern> patternList = new ArrayList<>();

    public ChangeFileHelper(String patternXmlPath) {
        parsePatternXml(patternXmlPath);
    }

    private void parsePatternXml(String patternXmlPath) {
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(patternXmlPath);
            // 通过document对象获取根节点patterns
            Element patterns = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator it = patterns.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element pattern = (Element) it.next();
                Iterator itt = pattern.elementIterator();
                String[] attr = new String[7];
                int i = 0;
                while (itt.hasNext()) {
                    Element patternChild = (Element) itt.next();
                  //  System.out.println("name：" + patternChild.getName() + " value：" + patternChild.getStringValue());
                    attr[i++] = patternChild.getStringValue();
                }
                Pattern p = new Pattern(attr[0], Long.parseLong(attr[1]), attr[2], attr[3], attr[4], attr[5], attr[6]);
                patternList.add(p);
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String parseChangeFile(String changeFilePath) {
        Map<String, List<String>> contextChangeMap = new TreeMap<>();
        ContextStaticRepo contextStaticRepo = new ContextStaticRepo(changeFilePath);
        String path = changeFilePath.split("\\.")[0] + "_transformed.txt";
        File file = new File(path);

        Context context;
        List<String> res = new ArrayList<>();
        try {
            while ((context = contextStaticRepo.getContext()) != null) {
                String currentTimestamp = context.getTimestamp();

                Iterator<Map.Entry<String, List<String>>> it = contextChangeMap.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<String, List<String>> entry = it.next();
                    String timestamp = entry.getKey();
                    if(TimestampHelper.timestampCmp(timestamp, currentTimestamp) < 0) {
                        res.addAll(contextChangeMap.get(timestamp));
                        it.remove();
                    }
                }

                String str = context.allForString();
                for(Pattern pattern: patternList) {
                    if(pattern.isBelong(context)) {
                        res.add(("+," + pattern.getId() + "," + str));
                        String key = TimestampHelper.plusMillis(currentTimestamp, pattern.getFreshness());
                        context.setTimestamp(key);
                        if(!contextChangeMap.containsKey(key)) {
                            contextChangeMap.put(key, new ArrayList<>());
                        }
                        contextChangeMap.get(key).add("-," + pattern.getId() + "," + context.allForString());
                    }
                    context.setTimestamp(currentTimestamp);
                }
            }

            Iterator<Map.Entry<String, List<String>>> it = contextChangeMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<String>> entry = it.next();
                String key = entry.getKey();
                res.addAll(contextChangeMap.get(key));
            }

            if(file.exists()) {
                file.delete();
            }

            file.createNewFile();
            FileWriter writer = new FileWriter(file, true);

            for (String line : res) {
                writer.write(line + "\n");
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }


    public static void main(String[] args) {
        if(args.length == 2) {
            ChangeFileHelper changeFileHelper = new ChangeFileHelper(args[0]);
            File file = new File(args[1]);
            if (file.isFile()) {
                System.out.println(args[1]);
                changeFileHelper.parseChangeFile(args[1]);
            }
            else {
                File [] fs = file.listFiles();
                if (fs != null) {

                    List<File> fileList = Arrays.asList(fs);
                    fileList.sort((Comparator<File>) (o1, o2) -> {
                        if (o1.isDirectory() && o2.isFile())
                            return -1;
                        if (o1.isFile() && o2.isDirectory())
                            return 1;
                        return o1.getName().compareTo(o2.getName());
                    });

                    for (File f : fileList) {
                        System.out.println(f.getPath());
                        changeFileHelper.parseChangeFile(f.getPath());
                    }
                }

            }
        }
        else {
            System.out.println("Args Error.");
        }
//        ChangeFileHelper changeFileHelper = new ChangeFileHelper("resource/consistency_patterns.xml");
//        changeFileHelper.parseChangeFile("resource/changes/00_small.txt");
    }
}
