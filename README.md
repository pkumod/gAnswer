# GAnswer系统

GAnswer系统是由北京大学计算机技术研究所数据管理实验室邹磊老师牵头开发的自然语言问答系统。gAnswer能够将自然语言问题转化成包含语义信息的查询图，然后，将查询图转化成标准的SPARQL查询，并将这些查询在图数据库中执行，最终得到用户的答案。我们使用数据驱动的消歧方式，具体来讲，在生成查询图的阶段保留多种实体和谓词的链接方案，在查询执行的阶段根据匹配情况消除歧义（错误链接）。

这是TKDE 2018论文 [Answering Natural Language Questions by Subgraph Matching over Knowledge Graphs](docs/TKDE18_gAnswer.pdf)的代码实现。

**帮助文档请点击此处 [中文(ZH)](docs/gAnswer_help.pdf)**

## 快速开始
首先您需要从[此处](https://pan.baidu.com/s/1LHGO0cU5et5o5nQWc3UvVg)下载系统必需的数据文件dbpedia16.rar，提取码为1mcr，您需要将其解压到data文件夹下。

### 使用jar包部署
我们推荐您使用我们提供的打包好的jar文件部署gAnswer，具体步骤为：

- 下载Ganswer.jar与dbpedia16.rar两个文件，我们推荐您从github的release页面下载最新版的Ganswer.jar，以保证稳定性。
- 在控制台下解压Ganswer.jar
```java
jar -xvf Ganswer.jar
```
- 您应该解压到主工程目录下，请保证Ganswer.jar文件与解压得到的文件处在同一路径下。
- 在控制台下解压dbpedia16.rar，注意，这时，您需要把解压得到的文件置于Ganswer.jar文件所在的路径的data文件夹下。下方的示例默认dbpedia16.rar与Ganswer.jar已经处于同一文件夹下。
```java
unrar x dbpedia16.rar ./data/
```
- 在控制台下运行jar包。
```java
java -jar Ganswer.jar
```
- 等待系统初始化结束，出现Server Ready！字样后，则说明初始化成功，您可以开始通过Http请求访问gAnswer的服务了。

这部分信息可以在帮助文档的‘“2.1.1 开始使用”’一章找到。



### 使用eclipse运行
当您使用eclipse运行gAnswer系统时，只需要通过clone或者download获取工程源码，然后按正常步骤导入Eclipse工程，同时将lib中的jar包加入Build Path中即可。由于外部jar包过大，无法上传github，您可以从[此处](https://pan.baidu.com/s/1ZfdKDtuE6PLby1koEs6aFg)下载所有需要的外部jar包,提取码为ud2v
这时，您同样需要下载解压dbpedia16.rar,并解压到工程文件根目录下的data文件夹中。与数据路径相关的参数，您可以在qa.Globals.localPath中找到

### 注意事项
要运行gAnswer系统，需要较多的包依赖、文件依赖和外部接口依赖，关于这部分要求，请您参阅帮助文档的“2.4 安装指南”。

## 其他事项

我们非常欢迎您使用gAnswer，并向我们提出您的宝贵意见或者bug报告。

如果您的意见或者报告被采纳，我们会将您的贡献记录在我们的帮助文档中。

我们针对gAnswer系统已经发表了多篇论文，您可以在帮助文档的“3.2 出版物”一章找到相关信息。


