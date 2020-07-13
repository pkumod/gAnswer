# GAnswer System

GAnswer system is a natural language QA system developed by Institute of Computer Science & Techonology Data Management Lab, Peking University, led by Prof. Zou Lei. GAnswer is able to translate natural language questions to query graphs containing semantic information. Then, the system can further turn query graphs into standard SPARQL query, which will be executed in graph databases, in order to attain answers for the users. We apply an innovative data-driven method for semantic disambiguation. In details, while generating query graphs, we maintain multiple plans for entities and predicate mappings and we conduct semantic disambiguation in the query execution phrase according to entities and predicate matches ( incorrect mappings ).

This is an implementation for TKDE 2018 paper [Answering Natural Language Questions by Subgraph Matching over Knowledge Graphs](docs/TKDE18_gAnswer.pdf) 

**For help document, click here [中文(ZH)](docs/gAnswer_help.pdf), [English](docs/gAnswer_help_en.pdf)

## Quick Start
First you must download necessary data files dbpedia16.rar [here](https://pan.baidu.com/s/1LHGO0cU5et5o5nQWc3UvVg). The extaction code is 1mcr. This is a Baidu Netdisk link. If you have trouble opening it, please try this google drive [link](https://drive.google.com/open?id=1hmqaftrTo0_qQNRApCuxFXaBx7SosNVy).You should unzip the file into directory named data.Since the complete data require at least 20 GB of main memory, you may try a [smaller dataset](https://pan.baidu.com/s/1Txe_cwpuoohJXH70yfxB-Q) (5GB required). The extract code is zuue. Please notice that the system performance is limited on the small data. We make sure that this data set is capable for example questions on our [official website](http://ganswer.gstore-pku.com/) as well as in QALD data set. Otherwise, you should choose suitable question for testing based on the data files.

For those who want to set up their own gstore service, we also supply [dbpedia triple dataset](https://pan.baidu.com/s/1jQ_jGTniflzoqBhpz5tjnw) and the extract code is rpev.

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

### Use GAnswer via http request
Here is an example of how to call GAnswer service via http request.
Having started GAnswerHttp, you can activate GAnswer by url as follow:
http://[ip]:[port]/gSolve/?data={maxAnswerNum:1, maxSparqlNum:2, question:Who is the wife of Donald Trump?}
<br />
Here,[ip] and [port] is the ip and port number of GAnswerHttp service (the default port is 9999). By the "data" parameter in the url, you can send a json string to GAnswer.
In this example, you are actually sending the following json data：
```json
{
  "maxAnswerNum":"1",
  "maxSparqlNum":"2",
  "question":"Whos is the wife of Donald Trump?"
}
```
Here, maxAnswerNum and maxSparqlNum respetively limit the number of answers and sparql the system will return. Both of them are optional.
If everything goes well, GAnswer will return a json string containing system-generated sparql and corresponding answer.
```json
{
  "question":"Who is the wife of Donald Trump?",
  "vars":["?wife"],
  "sparql":["select DISTINCT ?wife  where { <Donald_Trump>\t<spouse>\t?wife. } LIMIT 1","select DISTINCT ?wife  where { ?wife\t<spouse>\t<Donald_Trump>. } LIMIT 1"],
  "results":{"bindings":[{"?wife":{"type":"uri","value":"<Ivana_Trump>"}}]},
  "status":"200"
}
```
For more detail, please check Chapter 2.1.1 of the user guide.

### Run GAnswer in Eclipse
If you would like to run GAnswer in Eclipse, you need to clone or download the source code and import the project into Eclipse. Afterwards, the jar files in lib directory should be added to Build Path.
Due to the sizes, these jar files can not be uploaded to github. Therefore, you can download them [here](https://pan.baidu.com/s/18IegmEgj02fF9KQFwaQr0g). The extract code is 64jd. You can also download the lib zip through [Google Drive](https://drive.google.com/file/d/1tEsi4pBOBHd2gmwVgIOgt-ypJZQH9G3S).
Meanwhile, dbpedia16.rar is also needed. Please unzipped it into directory named data under the project main directory. Parameters about data path can be found in qa.Globals.localPath.

### Notice
To run GAnswer, you have to deal with multiple dependencies involving jar, data files and external API. Related information is in Chapter 2.4 in the help document.
Having generated sparql querires, by default the system will access a remote gStore for answer, which means extra time may be needed.Therefore, we strongly recommend you to deploy gStore on your own server for best performance.

- Download [DBpedia2016 triple file](https://pan.baidu.com/s/1l5Oui65sDn8QPYmA0rUvuA) and extract code is 89yy.
- Deploy [gStore](http://gstore-pku.com) and use DBpedia2016 triple file to build your own database. What's worth mentioning is that the DBpedia 2016 triples file is about 9.9GB and the construction needs more than 10GB of main memory and costs more about 10 hours.

## Other Business

You are welcome to use GAnswer and tell us your valuable advice or bug report.

If your advice or report are accepted, your contribution will be recorded in our help document.

We have published some paper about GAnswer and QA task, which you can find in Chapter 3.2 in help document.

## How to make your own data available on gAnswer
You may have your own set of triples and want to put them into gAnswer.Then you should generate a new set of fragments from your own triples. We have a [detailed tutorial](genrate_fragments/How_to_generate_fragments.md) to help you out.

