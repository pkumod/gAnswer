# encoding=utf-8
'''
Step3: extract entity, type and predicate out of the original triple files and allocate ids
'''
entities = set()
types = set()
predicate = set()
with open('triple file here','r') as f:
	i = 1
	k = 0
	for line in f.readlines():
		tri = line[:-2].split('\t')
		entities.add(tri[0])
		predicate.add(tri[1])
		if len(tri)==2:
			print("%s:%d"%(line,i))
			i += 1
			k += 1
			print(tri)
			continue
		if '"' in tri[2][0] or '"' in tri[2][0]:
			continue
		entities.add(tri[2])
		if tri[1]=='<type>':
			types.add(tri[2])
		if i%10000 == 0:
			print(i)
		i += 1
	print(i)
	print(k)

e = open('entity id file','w')
t = open('type id file','w')
p = open('predicate id file','w')

k = 0
for item in entities:
	if item[-1]!='\n':
		e.write(item+'\t%d'%k+'\n')
	else:
		e.write(item[:-1]+'\t%d'%k+'\n')
	k += 1

k = 0	
for item in types:
	if item[-1]!='\n':
		t.write(item+'\t%d'%k+'\n')
	else:
		t.write(item[:-1]+'\t%d'%k+'\n')
	k += 1

k = 0	
for item in predicate:
	if item[-1]!='\n':
		p.write(item+'\t%d'%k+'\n')
	else:
		p.write(item[:-1]+'\t%d'%k+'\n')
	k += 1
