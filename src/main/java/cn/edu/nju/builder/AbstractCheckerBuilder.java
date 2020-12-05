package cn.edu.nju.builder;

import cn.edu.nju.change.*;
import cn.edu.nju.checker.*;
import cn.edu.nju.memory.GPUContextMemory;
import cn.edu.nju.memory.GPUResult;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.BatchScheduler;
import cn.edu.nju.scheduler.GEASOptScheduler;
import cn.edu.nju.scheduler.GEAScheduler;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.Accuracy;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.PTXFileHelper;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.JCudaDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static jcuda.driver.JCudaDriver.*;

public abstract class AbstractCheckerBuilder implements CheckerType{



    protected List<Checker> checkerList;

    /*调度checker的策略*/
    protected Scheduler scheduler;

    public static String dataFilePath; //context data

    public static String changeFilePath; //context change

    /*所有pattern*/
    protected Map<String, Pattern> patternMap;

    protected Map<String, Checker> checkerMap;

    private int checkType = ECC_TYPE;

    private int scheduleType;


    private int taskNum = 8;

    private ExecutorService checkExecutorService;

    protected ChangeHandler changeHandler;

    protected String changeHandlerType;

    private String kernelFilePath;

    private String oracleFilePath;

    private CUcontext cuContext;

    private GPUResult gpuResult;

    private List<String> contexts;

    public AbstractCheckerBuilder(String configFilePath) {
        if (!isFileExists(configFilePath)) {
            System.out.println("[INFO] 配置文件不存在: " + configFilePath);
            System.exit(1);
        }
        parseConfigFile(configFilePath);
    }

    private void parseConfigFile(String configFilePath) {
        System.out.println("[INFO] 系统启动，开始解析配置文件......");
        //不要随意更换处理顺序
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(configFilePath);
            properties.load(fis);
            fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //check type
        String technique = properties.getProperty("technique");
        if (technique == null) {
            System.out.println("[INFO] technique项无配置");
            System.exit(1);
        }
        else if("pcc".equals(technique.toLowerCase())) {
            this.checkType = PCC_TYPE;
        } else if("ecc".equals(technique.toLowerCase())){
            this.checkType = ECC_TYPE;
        } else if("con-c".equals(technique.toLowerCase())) {
            this.checkType = CON_TYPE;
        } else if("gain".equals(technique.toLowerCase())) {
            this.checkType = GAIN_TYPE;
        } else if ("cpcc".equals(technique.toLowerCase())) {
            this.checkType = CONPCC_TYPE;
        }else {
            System.out.println("[INFO] technique项配置错误：" + technique);
            System.exit(1);
        }

        //taskNum
        String taskNumStr = properties.getProperty("taskNum");
        if (taskNumStr == null) {
            System.out.println("[INFO] taskNum项无配置");
            System.exit(1);
        }
        else if(taskNumStr.matches("[0-9]+")) {
            this.taskNum = Integer.parseInt(taskNumStr);
            if (taskNum == 0) {
                System.out.println("[INFO] taskNum项配置错误: " + taskNumStr);
                System.exit(1);
            }
        } else {
            if (!"-1".equals(taskNumStr)) {
                System.out.println("[INFO] taskNum项配置错误: " + taskNumStr);
                System.exit(1);
            }
        }

        this.checkExecutorService = Executors.newFixedThreadPool(taskNum);


        //context file path
        this.dataFilePath = properties.getProperty("dataFilePath");
        this.changeFilePath = properties.getProperty("changeFilePath");

        if (this.dataFilePath == null) {
            System.out.println("[INFO] dataFilePath项无配置");
            System.exit(1);
        }
        else if (this.changeFilePath == null) {
            System.out.println("[INFO] changeFilePat项无配置");
            System.exit(1);
        }
        else {
            if(!isFileExists(this.dataFilePath)) {
                System.out.println("[INFO] dataFilePath配置中的文件不存在：" + this.dataFilePath);
                System.exit(1);
            }
            else if (!isFileExists(this.changeFilePath)) {
                System.out.println("[INFO] changeFilePath配置中的文件不存在：" + this.changeFilePath);
                System.exit(1);
            }
        }
        
        String cudaSourceFilePath = "src/main/kernel/kernel.cu";
        //如果是GAIN需要初始化GPU内存
        if(this.checkType == GAIN_TYPE) {
            //开启异常捕获
            JCudaDriver.setExceptionsEnabled(true);

            //初始化设备
            cuInit(0);
            CUdevice device = new CUdevice();
            cuDeviceGet(device, 0);
            cuContext = new CUcontext();
            cuCtxCreate(cuContext, 0, device);

            // initGPUMemory();
            contexts = fileReader(this.dataFilePath);
            gpuResult = new GPUResult();
            compileKernelFunction(cudaSourceFilePath);
        }

        //pattern
        String patternFilePath = properties.getProperty("patternFilePath");
        if (patternFilePath == null) {
            System.out.println("[INFO] patternFilePath项无配置");
            System.exit(1);
        }
        parsePatternFile(patternFilePath);

        //rule
        String ruleFilePath = properties.getProperty("ruleFilePath");
        if (ruleFilePath == null) {
            System.out.println("[INFO] ruleFilePath项无配置");
            System.exit(1);
        }
        parseRuleFile(ruleFilePath);

        //schedule
        String schedule = properties.getProperty("schedule");
        //change handler
        this.changeHandlerType = properties.getProperty("changeHandlerType");
        if (this.changeHandlerType == null) {
            System.out.println("[INFO] changeHandlerType项无配置");
            System.exit(1);
        }
        if (schedule == null) {
            System.out.println("[INFO] schedule项无配置");
            System.exit(1);
        }
        else if ("immed".equals(schedule.toLowerCase())) {
            this.scheduler = new BatchScheduler(1);
            this.scheduleType = 1;
        }
        else if(schedule.toLowerCase().matches("batch-[0-9]+")) {
            this.scheduler = new BatchScheduler(Integer.parseInt(schedule.split("-")[1]));
            this.scheduleType = Integer.parseInt(schedule.split("-")[1]);
        }
        else if ("geas-ori".equals(schedule.toLowerCase()) && this.changeHandlerType.contains("change-based")) {
            this.scheduler = new GEAScheduler(this.checkerList);
            this.scheduleType = 0;
        }
        else if ("geas-opt".equals(schedule.toLowerCase()) && this.changeHandlerType.contains("change-based")) {
            this.scheduler = new GEASOptScheduler(this.checkerList);
            this.scheduleType = -2;
        }
        else {
            this.scheduleType = -1;
            System.out.println("[INFO] schedule项配置错误: " + schedule);
            System.exit(1);
        }

        System.out.println("[INFO] 检测技术：" + technique);
        System.out.println("[INFO] 调度策略：" + schedule);


        //log
        String logFilePath = properties.getProperty("logFilePath");
        if (logFilePath == null) {
            System.out.println("[INFO] logFilePath项无配置");
            System.exit(1);
        }
        LogFileHelper.initLogger(logFilePath);

        //oracle
        this.oracleFilePath = properties.getProperty("oracleFilePath");

        if(this.oracleFilePath != null && !isFileExists(this.oracleFilePath)) {
            System.out.println("[INFO] oracleFilePath配置中的文件不存在：" + this.oracleFilePath);
            System.exit(1);
        }

        //change handle
        configChangeHandler();

        System.out.println("[INFO] 配置文件解析完毕......");
    }

    private boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    private void configChangeHandler() {
        if(this.changeHandlerType.contains("time-based")) {
            this.changeHandler = new TimebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }
        else if(this.changeHandlerType.contains("change-based")) {
            this.changeHandler = new ChangebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }
    }

    private void parsePatternFile(String patternFilePath) {
        this.patternMap = new ConcurrentHashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(patternFilePath);

            NodeList patternList = document.getElementsByTagName("pattern");
            System.out.println("[INFO] pattern文件：" + patternFilePath + "，总共" + patternList.getLength() + "个patterns");
            for (int i = 0; i < patternList.getLength(); i++) {
                Node patNode = patternList.item(i);
                NodeList childNodes = patNode.getChildNodes();

                Map<String, Boolean> member = new HashMap<>();

                String id = null;
                member.put("id", false);

                long freshness = 0L;
                member.put("freshness", false);

                String category = null;
                member.put("category", false);

                String subject = null;
                member.put("subject", false);

                String predicate = null;
                member.put("predicate", false);

                String object = null;
                member.put("object", false);

                String site = null;
                member.put("site", false);

                for(int j = 0; j < childNodes.getLength(); j++) {
                    if (childNodes.item(j).getNodeName().startsWith("#")) {
                        continue;
                    }
                    switch (childNodes.item(j).getNodeName()) {
                        case "id":
                            member.put("id", true);
                            id = childNodes.item(j).getTextContent();
                            break;
                        case "freshness":
                            try {
                                member.put("freshness", true);
                                freshness = Long.parseLong(childNodes.item(j).getTextContent());
                            } catch (NumberFormatException e) {
                                System.out.println("[INFO] pattern的freshness配置错误");
                                System.exit(1);
                            }
                            break;
                        case "category":
                            member.put("category", true);
                            category = childNodes.item(j).getTextContent();
                            break;
                        case "subject":
                            member.put("subject", true);
                            subject = childNodes.item(j).getTextContent();
                            break;
                        case "predicate":
                            member.put("predicate", true);
                            predicate = childNodes.item(j).getTextContent();
                            break;
                        case "object":
                            member.put("object", true);
                            object = childNodes.item(j).getTextContent();
                            break;
                        case "site":
                            member.put("site", true);
                            site = childNodes.item(j).getTextContent();
                            break;
                        default:
                            System.out.println("[INFO] '" + patternFilePath + "'文件中存在不可识别pattern标识符：" + childNodes.item(j).getNodeName());
                            System.exit(1);
                    }
                }

                for(String key : member.keySet()) {
                    if (!member.get(key)) {
                        System.out.println("[INFO] '" + patternFilePath + "'文件中缺少pattern标识符：" + key);
                        System.exit(1);
                    }
                }



                patternMap.put(id, new Pattern(id, freshness, category, subject, predicate, object, site));
            }

            /* for(String key : patternMap.keySet()) {
                System.out.println("[DEBUG] " + patternMap.get(key));
            } */

        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[INFO] patternFilePath配置中的文件不存在：" + patternFilePath);
            System.exit(1);
        }

        if (patternMap.isEmpty()) {
            System.out.println("[INFO] pattern文件中没有pattern");
            System.exit(1);
        }
    }

    private void parseRuleFile(String ruleFilePath) {
        this.checkerList = new CopyOnWriteArrayList<>();
        this.checkerMap = new ConcurrentHashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(ruleFilePath);

            NodeList ruleList = document.getElementsByTagName("rule");
            System.out.println("[INFO] rule文件：" + ruleFilePath + "，总共" + ruleList.getLength() + "条rules");

            for(int i = 0; i < ruleList.getLength(); i++){
                STNode treeHead = new STNode();

                Node ruleNode = ruleList.item(i);

                Node idNode = ruleNode.getChildNodes().item(1);
                Node formulaNode = ruleNode.getChildNodes().item(3);

                Map<String,STNode> stMap = new HashMap<>();
                buildSyntaxTree(formulaNode.getChildNodes(), treeHead, stMap, ruleFilePath);

                assert treeHead.hasChildNodes():"[INFO] Create syntax tree failed !";

                STNode root = (STNode)treeHead.getFirstChild();
                root.setParentTreeNode(null);

                Checker checker = null;
                if(checkType == PCC_TYPE) {
                    checker = new PccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                }
                else if (checkType == ECC_TYPE){
                    checker = new EccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                } else if(checkType == CON_TYPE){ //CON-C
                    checker = new ConChecker(idNode.getTextContent(), root, this.patternMap, stMap, taskNum, checkExecutorService);
                } else if(checkType == GAIN_TYPE) {
                    checker = new GAINChecker(idNode.getTextContent(), root, this.patternMap, stMap,
                            kernelFilePath, //kernel function
                            contexts, cuContext, gpuResult); //GPU memory
                } else if(checkType == CONPCC_TYPE) {
                    checker = new ConPccChecker(idNode.getTextContent(), root, this.patternMap, stMap, taskNum, checkExecutorService);
                }

                checkerList.add(checker);
                for(String key : stMap.keySet()) {
                    checkerMap.put(stMap.get(key).getContextSetName(), checker);
                }

                //System.out.println("[DEBUG] " + checker.getName());
                //checker.printSyntaxTree();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[INFO] ruleFilePath配置中的文件不存在：" +  ruleFilePath);
            System.exit(1);
        }

        if (checkerList.isEmpty()) {
            System.out.println("[INFO] rule文件中没有rule");
            System.exit(1);
        }
    }

    protected List<String> fileReader(String filePath) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fr);

            String change;
            while ((change = bufferedReader.readLine()) != null) {
                list.add(change);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private void buildSyntaxTree(NodeList list, STNode root, Map<String,STNode> stMap, String ruleFilePath) {
        for(int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE && !list.item(i).getNodeName().equals("param")) {
                Element e = (Element)list.item(i);
                STNode stNode = null;
                String nodeName = e.getNodeName();

                switch (nodeName) {
                    case "forall":
                        stNode = new STNode(nodeName, STNode.UNIVERSAL_NODE, e.getAttribute("in"));
                        stMap.put(e.getAttribute("in"), stNode);
                        break;
                    case "exists":
                        stNode = new STNode(nodeName, STNode.EXISTENTIAL_NODE, e.getAttribute("in"));
                        stMap.put(e.getAttribute("in"),stNode);
                        break;
                    case "and":
                        stNode = new STNode(nodeName, STNode.AND_NODE);
                        break;
                    case "not":
                        stNode = new STNode(nodeName, STNode.NOT_NODE);
                        break;
                    case "implies":
                        stNode = new STNode(nodeName, STNode.IMPLIES_NODE);
                        break;
                    case "bfunction":
                        stNode = new STNode(e.getAttribute("name"), STNode.BFUNC_NODE);
                        break;
                    default:
                        System.out.println("[INFO] '" + ruleFilePath +  "'文件中存在非法的一致性规则标识符：" + nodeName);
                        System.exit(1);
                        break;
                }

                buildSyntaxTree(e.getChildNodes(), stNode, stMap, ruleFilePath);
                root.addChildeNode(stNode);
            }
        }
    }

    private void compileKernelFunction(String cudaSourceFilePath) {

        try {
            this.kernelFilePath = PTXFileHelper.preparePtxFile(cudaSourceFilePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        checkExecutorService.shutdown();
        if(checkType == GAIN_TYPE) {
            GPUContextMemory.getInstance(contexts).free();
            for (Checker checker : checkerList) {
                checker.reset();
            }
        }
    }

    protected void update(int checkType, int scheduleType) {
        boolean isUpdate = false;

        //update checkers
        if(this.checkType != checkType) {
            isUpdate = true;
            List<Checker> curList = new CopyOnWriteArrayList<>();
            Map<String, Checker> curMap = new ConcurrentHashMap();

            for(Checker checker : checkerList) {

                Checker c = null;
                if(checkType == PCC_TYPE) {
                    c = new PccChecker(checker);
                }
                else if(checkType == ECC_TYPE) {
                    c = new EccChecker(checker);
                }
                else if(checkType == CON_TYPE) {
                    c = new ConChecker(checker, taskNum, this.checkExecutorService);
                }
                else if(checkType == CONPCC_TYPE) {
                    c = new ConPccChecker(checker, taskNum, this.checkExecutorService);
                }
                else {
                    assert false:"Type Error.";
                }

                curList.add(c);

                Map<String, STNode> stMap = checker.getStMap();
                for(String key : stMap.keySet()) {
                    curMap.put(stMap.get(key).getContextSetName(), c);
                }
                checker.reset();
            }
            this.checkType = checkType;
            this.checkerList = curList;
            this.checkerMap = curMap;
        }

        //update scheduler
        if(scheduleType != this.scheduleType) {
            isUpdate = true;
            if(scheduleType == 0) {
                this.scheduler = new GEAScheduler(this.checkerList);
            }
            else {
                this.scheduler = new BatchScheduler(scheduleType);
            }

            this.scheduleType = scheduleType;
        }

        if(isUpdate) {
            this.changeHandler.update(this.checkerMap,this.scheduler, this.checkerList);
        }
    }

    protected void accuracy(String logFilePath) {
        if (this.oracleFilePath != null) {
            Accuracy.main(new String[]{logFilePath, this.oracleFilePath});
        }
    }


    protected int computeWorkload() {
        int workload = 0;
        for(Checker checker: checkerList) {
            workload += checker.getWorkload();
        }
        return  workload;
    }
}
