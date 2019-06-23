#encoding=utf-8
'''
Step4: transform the triples and represent entity, type and predicate with id
'''
eid = {}
tid = {}
pid = {}

with open('entity id file here','r') as e:
	for line in e:
		dub = line[:-1].split('\t')
		eid[dub[0]] = dub[1]
		

with open('type id file here','r') as t:
	for line in t:
		dub = line[:-1].split('\t')
		tid[dub[0]] = dub[1]
		

with open('predicate id file here','r') as p:
	for line in p:
		dub = line[:-1].split('\t')
		pid[dub[0]] = dub[1]

print("%d %d %d"%(len(eid),len(tid),len(pid)))

rt = open("output triple file here",'w')
with open('input triple file here','r') as f:
	i = 1;
	for line in f:
		tri = line[:-2].split('\t')
		if tri[1] == '<type>':
			if not tid.has_key(tri[2]):
				tid[tri[2]] = '-1'
			try:
				rt.write("%s\t%s\t%s\n"%(eid[tri[0]],pid[tri[1]],tid[tri[2]]))
			except KeyError:
				print(line)
				print(i)
		else:
			if tri[2][0]=='"':
				try:
					rt.write("%s\t%s\t-1\n"%(eid[tri[0]],pid[tri[1]]))
				except KeyError:
					print(line)
					print(i)
			else:
				try:
					rt.write("%s\t%s\t%s\n"%(eid[tri[0]],pid[tri[1]],eid[tri[2]]))
				except KeyError:
					print(line)
					print(i)
	
