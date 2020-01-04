# GAnswer系统(中文问答版本)

GAnswer系统是由北京大学计算机技术研究所数据管理实验室邹磊老师牵头开发的自然语言问答系统。gAnswer能够将自然语言问题转化成包含语义信息的查询图，然后，将查询图转化成标准的SPARQL查询，并将这些查询在图数据库中执行，最终得到用户的答案。我们使用数据驱动的消歧方式，具体来讲，在生成查询图的阶段保留多种实体和谓词的链接方案，在查询执行的阶段根据匹配情况消除歧义（错误链接）。

这是TKDE 2018论文 [Answering Natural Language Questions by Subgraph Matching over Knowledge Graphs](docs/TKDE18_gAnswer.pdf) 的代码实现。

**帮助文档请点击此处 [中文(ZH)](docs/gAnswer_help.pdf) [English](docs/gAnswer_help_en.pdf)**

## 快速开始
这是GAnswer系统的中文问答版本，基于中文知识库PKUBASE。
支持全量PKUBASE需要较大的内存(大于16G)，我们缺省提供了PKUBASE的一个非常小的子图，其相关文件存储在data/pkukbase文件夹下，只涉及CCKS 2019相关问题所需的三元组。
若您希望QA系统建立在全量PKUBASE上，需要从[此处](https://pan.baidu.com/s/1R3UTK3kRnGsFr0MjITOMEw)下载系统必需的数据文件pkubase.rar，提取码为tku6，您需要将其解压后替换data文件夹内的文件。

### 使用jar包部署
待更新。

### 通过http请求使用GAnswer
待更新。

### 使用eclipse运行
当您使用eclipse运行gAnswer系统时，只需要通过clone或者download获取工程源码，然后按正常步骤导入Eclipse工程，同时将lib中的jar包加入Build Path中即可。由于外部jar包过大，无法上传github，您可以从[此处](https://pan.baidu.com/s/1ZMY0hHD2Bm9dKMP7Cq8Mkw)下载所有需要的外部jar包,提取码为a5nf
这时，您同样需要下载解压pkubase.rar,并解压到工程文件根目录下的data文件夹中。项目的主入口文件为qa.GAnswer，与数据路径相关的参数，您可以在qa.Globals.localPath中找到。

### 注意事项
要运行gAnswer系统，需要较多的包依赖、文件依赖和外部接口依赖，关于这部分要求，请您参阅帮助文档的“2.4 安装指南”（待更新）。
在生成SPARQL查询后，系统默认调用部署在远程服务器上的gStore查询引擎来查找答案。这意味着额外的网络传输开销和可能存在的排队等待开销。
因此我们强烈建议您在自己的服务器上部署gStore查询引擎并建立对应的知识库。您需要：

- 下载[pkubase triples文件](https://pan.baidu.com/s/1KsRhgLWsmsUGdeARjwyZNA)，提取码u7m2。
- 部署[gStore](http://gstore-pku.com)查询引擎，并使用下载的triples文件来构建数据库。值得提醒的是，pkubase triples文件大小为2.25GB，构建数据库需要较大的内存(>10GB)和较长的时间。

## 其他事项

我们非常欢迎您使用gAnswer，并向我们提出您的宝贵意见或者bug报告。

如果您的意见或者报告被采纳，我们会将您的贡献记录在我们的帮助文档中。

我们针对QA任务和gAnswer系统发表了多篇论文，您可以在帮助文档的“3.2 出版物”一章找到相关信息。


## 在gAnswer上使用你自己的数据
如果您希望将您自己的三元组数据集移植到gAnswer上，那么您需要利用这些三元组为gAnswer重新生成fragments。 我们提供了一个[详细的教程](genrate_fragments/How_to_generate_fragments.md)来帮助您完成这项工作。
