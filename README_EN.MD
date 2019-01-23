# GAnswer System

GAnswer system is a natural language QA system developed by Institute of Computer Science & Techonology Data Management Lab, Peking University, led by Prof. Zou Lei. GAnswer is able to translate natural language questions to query graphs containing semantic information. Then, the system can further turn query graphs into standard SPARQL query, which will be executed in graph databases, in order to attain answers for the users. We apply an innovative data-driven method for semantic disambiguation. In details, while generating query graphs, we maintain multiple plans for entities and predicate mappings and we conduct semantic disambiguation in the query execution phrase according to entities and predicate matches ( incorrect mappings ).

This is an implementation for TKDE 2018 paper [Answering Natural Language Questions by Subgraph Matching over Knowledge Graphs](docs/TKDE18_gAnswer.pdf) 

**For help document, click here [中文(ZH)](docs/gAnswer_help.pdf), the english version will be uploaded as soon as possible**

## Quick Start
First you must download necessary data files dbpedia16.rar [here](https://pan.baidu.com/s/1LHGO0cU5et5o5nQWc3UvVg). The extaction code is 1mcr. You should unzip the file into directory named data.

### Deploy GAnswer via jar
We recommend you to deploy GAnswer using the jar files we supply. The specific procedure is as follows：

- Download 2 files: Ganswer.jar and dbpedia16.rar. We strongly recommend that you download the up-to-date version of Ganswer.jar from the github releases of this project, to ensure stability.
- Unzip Ganswer.jar
```java
jar -xvf Ganswer.jar
```
- You should unzip it into the main project directory. Please make sure that Ganswer.jar itself is under the same path with the unzipped files.
- Unzip dbpedia16.rar. Note that you must place the unzipped files into a directory named data, and this directory should be under the same path with 
```java
unrar x dbpedia16.rar ./data/
```
- In other words, your project directory should look like this:
>Main_project_directory
>>Ganswer.jar<br />
>>unzipped files from Ganswer.jar<br />
>>data
>>>unzipped files from dbpedia16.rar<br />
- Run the jar file
```java
java -jar Ganswer.jar
```
- Wait for the initialization procedure. When you see "Server Ready!", the initialization is successful and you can access GAnswer service via Http requests.

About GAnswer Http API, information can be found in Chapter 2.1.1 in help document.



### Run GAnswer in Eclipse
If you would like to run GAnswer in Eclipse, you need to clone or download the source code and import the project into Eclipse. Afterwards, the jar files in lib directory should be added to Build Path.
Due to the sizes, these jar files can not be uploaded to github. Therefore, you can download them [here](https://pan.baidu.com/s/1ZfdKDtuE6PLby1koEs6aFg). The extract code is ud2v.
Meanwhile, dbpedia16.rar is also needed. Please unzipped it into directory named data under the project main directory. Parameters about data path can be found in qa.Globals.localPath.

### Notice
To run GAnswer, you have to deal with multiple dependencies involving jar, data files and external API. Related information is in Chapter 2.4 in the help document.

## Other Business

You are welcome to use GAnswer and tell us your valuable advice or bug report.

If your advice or report are accepted, your contribution will be recorded in our help document.

We have published some paper about GAnswer and QA task, which you can find in Chapter 3.2 in help document.

