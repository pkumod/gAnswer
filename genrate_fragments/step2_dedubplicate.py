# encoding=utf-8
'''
Step2: remove the dubplicate triples.
'''
triples = set()
j = 1
i = 1
with open('./pkubase/pkubase-triples.txt','r') as f:
    while 1:
        line = f.readline()
        if not line:
            break
        triples.add(line)
        if j % 100000 == 0:
            print("%d:%d"%(i,j))
        j += 1
j = 1
i = 2
with open('./pkubase/pkubase-types.txt','r') as f:
    while 1:
        line = f.readline()
        if not line:
            break
        triples.add(line)
        if j % 100000 == 0:
            print("%d:%d"%(i,j))
        j += 1
print(len(triples))
wf = open('./pkubase/pkubase_clean.txt','w')
for item in triples:
    wf.write(item)
