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
Generally speaking, there are three segment
This is the exactly form of triples we need to generate fragments. However sometimes the entity and predicate contain some extra information. Take dbpedia dataset as an example. The following is the original form of a dbpedia triple
```java
<http://dbpedia.org/resource/Alabama> <http://dbpedia.org/property/demonym> <http://dbpedia.org/resource/Adjectivals_and_demonyms_for_U.S._states> .
```
As you can see, every entity and predicate is marked with an URI, but we don't need the prefix of the URIs. See Step1_clean_triples.py. That is the code we use to clean dbpedia triples. 
Generally, please remember that making sure the entity and predicate names are clear enough to indicate their true meaning and contain no extra information is all you need to do in this step.
By the way, if you have more than one triple files, please combine them into one so that the following steps will be easier.

### Step 2: remove duplicate triples
One triple may occur more than once in the clean triple file, especially when you combine many triple files into one.
gAnswer is OK with receiving duplicate triples but it will influence its performance.

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
Notice that we use -1 to represent values that a not entity nor type, such as numbers and literals.

### Step 5: generate entity fragments
Finally we are going to generate entity fragments now. Every entity has its own piece of fragment.Fragments are information about the edges related with the entity as well as its neighbor entities.First let's clearify the idea of subject and object in a triple. A triple consist of three parts: subject, predicate and object. For example:
```java
<StudentA>  <major>  <computer_science>
```
Here *studentA* is subject, *major* is predicate and *computer_science* is object. Basically, the first element is subject, the second is predicate and the third is object. Sometimes it is the object is not an entity nor type. Value like number and string can also become object.

We define 5 kinds of edges:
1.InEntEdge: The entity is the object of the edge and the subject is also an entity.
2.OutEntEdge: The entity is the subject of the edge and the object is also an entity.
3.InEdge: The entity is the object of the edge.
4.OutEdge: The entity is the subject of the edge.
5.typeEdge: The entity ts the subject of the edge whose predicate is *type* and its object is a type.
