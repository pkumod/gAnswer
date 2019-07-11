# GAnswer系统

GAnswer系统是由北京大学计算机技术研究所数据管理实验室邹磊老师牵头开发的自然语言问答系统。gAnswer能够将自然语言问题转化成包含语义信息的查询图，然后，将查询图转化成标准的SPARQL查询，并将这些查询在图数据库中执行，最终得到用户的答案。我们使用数据驱动的消歧方式，具体来讲，在生成查询图的阶段保留多种实体和谓词的链接方案，在查询执行的阶段根据匹配情况消除歧义（错误链接）。

这是TKDE 2018论文 [Answering Natural Language Questions by Subgraph Matching over Knowledge Graphs](docs/TKDE18_gAnswer.pdf) 的代码实现。

**帮助文档请点击此处 [中文(ZH)](docs/gAnswer_help.pdf) [English](docs/gAnswer_help_en.pdf)**

## 快速开始
首先您需要从[此处](https://pan.baidu.com/s/1LHGO0cU5et5o5nQWc3UvVg)下载系统必需的数据文件dbpedia16.rar，提取码为1mcr，您需要将其解压到data文件夹下。
由于完整的数据文件需要较大的内存支持（20GB），您也可以选择下载我们从DBpedia 2016中抽取生成的[小规模数据](https://pan.baidu.com/s/1Txe_cwpuoohJXH70yfxB-Q)（需要5GB内存），提取码为zuue。注意，问答系统在小规模数据集上的能力是受限的，在[官网](http://ganswer.gstore-pku.com/)的问题样例和QALD系列问题之外，您可能需要根据数据文件自行选择合适的问题来进行测试。

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
unrar x DBpedia2016.rar ./data/
```
- 这时，您现在的工程目录结构应该是这样的：
>Main_project_directory
>>Ganswer.jar<br />
>>unzipped files from Ganswer.jar<br />
>>data
>>>unzipped files from dbpedia16.rar<br />
- 在控制台下运行jar包。
```java
java -jar Ganswer.jar
```
- 等待系统初始化结束，出现Server Ready！字样后，则说明初始化成功，您可以开始通过Http请求访问gAnswer的服务了。

### 通过http请求使用GAnswer
我们为您提供了一个简单的样例，以说明如何通过http请求，获取GAnswer服务。
您可以通过类似下面的url来访问GAnswer：
http://[ip]:[port]/gSolve/?data={maxAnswerNum:1, maxSparqlNum:2, question:Who is the wife of Donald Trump?}
<br />其中，[ip]和[port]分别为您启动GAnswer服务的ip地址和端口（端口系统默认为9999），您需要通过在http请求中添加“data”参数，传递一个json字符串给GAnswer。
在这个样例中，您实际传递的json数据为：
```json
{
  "maxAnswerNum":"1",
  "maxSparqlNum":"2",
  "question":"Whos is the wife of Donald Trump?"
}
```
其中，maxAnswerNum和maxSparqlNum分别规定了返回的答案和sparql的数量上限，这两个数据项都是可选的。
一般情况下，这时GAnswer会返回一个json字符串，其中包含了系统生成的sparql和问题答案。
```json
{
  "question":"Who is the wife of Donald Trump?",
  "vars":["?wife"],
  "sparql":["select DISTINCT ?wife  where { <Donald_Trump>\t<spouse>\t?wife. } LIMIT 1","select DISTINCT ?wife  where { ?wife\t<spouse>\t<Donald_Trump>. } LIMIT 1"],
  "results":{"bindings":[{"?wife":{"type":"uri","value":"<Ivana_Trump>"}}]},
  "status":"200"
}
```
详细信息可以在帮助文档的‘“2.1.1 开始使用”’一章找到。



### 使用eclipse运行
当您使用eclipse运行gAnswer系统时，只需要通过clone或者download获取工程源码，然后按正常步骤导入Eclipse工程，同时将lib中的jar包加入Build Path中即可。由于外部jar包过大，无法上传github，您可以从[此处](https://pan.baidu.com/s/18IegmEgj02fF9KQFwaQr0g)下载所有需要的外部jar包,提取码为64jd。或者通过[Google Drive](https://drive.google.com/file/d/1tEsi4pBOBHd2gmwVgIOgt-ypJZQH9G3S)下载。
这时，您同样需要下载解压dbpedia16.rar,并解压到工程文件根目录下的data文件夹中。与数据路径相关的参数，您可以在qa.Globals.localPath中找到

### 注意事项
要运行gAnswer系统，需要较多的包依赖、文件依赖和外部接口依赖，关于这部分要求，请您参阅帮助文档的“2.4 安装指南”。
在生成SPARQL查询后，系统默认调用部署在远程服务器上的gStore查询引擎来查找答案。这意味着额外的网络传输开销和可能存在的排队等待开销。
因此我们强烈建议您在自己的服务器上部署gStore查询引擎并建立对应的知识库。您需要：

- 下载[DBpedia2016 triples文件](https://pan.baidu.com/s/1l5Oui65sDn8QPYmA0rUvuA)，提取码89yy。
- 部署[gStore](http://gstore-pku.com)查询引擎，并使用下载的triples文件来构建数据库。值得提醒的是，DBpedia 2016 triples文件大小为9.9GB，构建数据库需要较大的内存(>10GB)和较长的时间(10小时左右)。

## 其他事项

我们非常欢迎您使用gAnswer，并向我们提出您的宝贵意见或者bug报告。

如果您的意见或者报告被采纳，我们会将您的贡献记录在我们的帮助文档中。

我们针对QA任务和gAnswer系统发表了多篇论文，您可以在帮助文档的“3.2 出版物”一章找到相关信息。


## 在gAnswer上使用你自己的数据
如果您希望将您自己的三元组数据集移植到gAnswer上，那么您需要利用这些三元组为gAnswer重新生成fragments。 我们提供了一个[详细的教程](genrate_fragments/How_to_generate_fragments.md)来帮助您完成这项工作。
