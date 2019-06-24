## How to generate fragments out of your own triples
There are three kinds of fragments in gAnswer: entity fragments, predicate fragments and type fragments. They are information extracted from the triples helping gAnswer improve its results. In this section we will show you how to generate your own fragments step by step with a simple example

### Step 1: Clean the triple files
Suppose we have a triple file containing only 3 triples:
```java
<StudentA>  <major>  <computer_science>
<StudentA>  <type>   <Person>
<computer_science>  <type>  <subject>
```
This is the exactly form of triples we need to generate fragments. However sometimes the entity and predicate contain some extra information. Take dbpedia dataset as an example. The following is the original form of a dbpedia triple
```java
<http://dbpedia.org/resource/Alabama> <http://dbpedia.org/property/demonym> <http://dbpedia.org/resource/Adjectivals_and_demonyms_for_U.S._states> .
```
As you can see, every entity and predicate is marked with an URI, but we don't need the prefix of the URIs. See Step1_clean_triples.py. That is the code we use to clean dbpedia triples. 
Generally, please remember that making sure the entity and predicate names are clear enough to indicate their true meaning and contain no extra information is all you need to do in this step.
By the way, if you have more than one triple files, please combine them into one so that the following steps will be easier.
