# Graduation Project
## 1. Configuration
###  create a file named **config.properties**
```
#描述rule的xml文件路径
ruleFilePath=consistency_rules.xml
#描述pattern的xml文件路径
patternFilePath=consistency_patterns.xml
#描述context的txt文件路径
dataFilePath=data/00_small.txt
#描述change的文件路径
changeFilePath=changes/00_small_change.txt
#指定输出log的路径
logFilePath=PCC_00_small.log
#kernel function
cudaSourceFilePath=src/main/kernel/kernel.cu
#算法选择（目前可选项是ECC、PCC、Con-C和GAIN）
technique=PCC
#描述类型
changeHandlerType=dynamic-time-based
#描述几个context change触发一次check（即batch值，可选项是正整数或是GEAS）
schedule=1
taskNum=4
on-demand=off
paramFilePath=param.properties
```

### create a file named **param.properties**
```
#sdelay (ms)
maxDelay=67
# step
step=5
# CPU usage for PCC
cpuUsageLow = 0.5
```
## 2. Run
### (1) Clone this repository.
```
$ git clone https://github.com/growinware-cn/CMID.git 
or 
$ git clone https://gitee.com/growinware/CMID.git
```
### (2) Import this project with **IntelliJ IDEA**.
### (3) Build this project.
### (4) Start to run.
```
$ java Main pathToConfigFile //config.properties
```
