## How to generate fragments out of your own triples
There are three kinds of fragments in gAnswer: entity fragments, predicate fragments and type fragments. They are information extracted from the triples helping gAnswer improve its results. In this section we will show you how to generate your own fragments step by step with a simple example

### Step 1: Clean the triple files
Suppose we have a triple file containing only seven triples:
```java
<StudentA>  <major>  <computer_science>
<StudentB>  <friend_of> <StudentA>
<StudentA>  <name>  "Jeff"
<StudentB>  <name>  "Tom"
<StudentA>  <type>   <Person>
<StudentB>  <type>   <Person>
<computer_science>  <type>  <Subject>
```
This is the exactly form of triples we need to generate fragments. However sometimes the entity and predicate contain some extra information. Take dbpedia dataset as an example. The following is the original form of a dbpedia triple
```java
<http://dbpedia.org/resource/Alabama> <http://dbpedia.org/property/demonym> <http://dbpedia.org/resource/Adjectivals_and_demonyms_for_U.S._states> .
```
As you can see, every entity and predicate is marked with an URI, but we don't need the prefix of the URIs. See Step1_clean_triples.py. That is the code we use to clean dbpedia triples. 
Generally, please remember that making sure the entity and predicate names are clear enough to indicate their true meaning and contain no extra information is all you need to do in this step.
By the way, if you have more than one triple files, please combine them into one so that the following steps will be easier.

### Step 2: remove duplicate triples
One triple may occur more than once in the clean triple file, especially when you combine many triple files into one.
gAnswer is OK with receving duplicate triples but it will influence its performance.

### Step 3: extract entity, predicate and type name for id allocation
To save space cost, the fragment files are not constructed based on entity, predicate and type names themselves but their ids. Therefore, we must extract every entity, predicate and type name out of the triple file and give them a uniue id respectively. In our example,the id files will goes like this:
```java
//Entity ids
<StudentA>  1
<StudentB>  2
<computer_science>  3

//predicate ids
<major> 1
<friend_of> 2
<type>  3
<name>  4

//type ids
<Person>  1
<Subject> 2
```

### Step 4: represent triples with ids
For convenience, before we generate the fragments, we first replace all the name strings in triple file with corresponding ids.
In our example, the new triple file is like:
```java
1 1 3
2 2 1
1 4 -1
2 4 -1
1 3 1
2 3 1
3 3 2
```
