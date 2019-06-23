#encoding=utf-8
inEnEdge = {}
outEnEdge = {}
inEdge={}
outEdge = {}
types = {}
with open('triple file represented by ids here','r') as f:
	i = 1
	for line in f:
		tri = line[:-1].split('\t')
		
		if tri[1] == 'id of <type>' and tri[2]!='-1':
			if types.has_key(tri[0]):
				types[tri[0]].add(tri[2])
			else:
				types[tri[0]] = set()
				types[tri[0]].add(tri[2])
		else:
			if outEdge.has_key(tri[0]):
				outEdge[tri[0]].add(tri[1])
			else:
				outEdge[tri[0]] = set()
				outEdge[tri[0]].add(tri[1])
				
			if tri[2]!='-1':
				if outEnEdge.has_key(tri[0]):
					if outEnEdge[tri[0]].has_key(tri[2]):
						outEnEdge[tri[0]][tri[2]].add(tri[1])
					else:
						outEnEdge[tri[0]][tri[2]] = set()
						outEnEdge[tri[0]][tri[2]].add(tri[1])
				else:
					outEnEdge[tri[0]]={}
					outEnEdge[tri[0]][tri[2]] = set()
					outEnEdge[tri[0]][tri[2]].add(tri[1])
			
				if inEdge.has_key(tri[2]):
					inEdge[tri[2]].add(tri[1])
				else:
					inEdge[tri[2]] = set()
					inEdge[tri[2]].add(tri[1])
				if inEnEdge.has_key(tri[2]):
					if inEnEdge[tri[2]].has_key(tri[0]):
						inEnEdge[tri[2]][tri[0]].add(tri[1])
					else:
						inEnEdge[tri[2]][tri[0]] = set()
						inEnEdge[tri[2]][tri[0]].add(tri[1])
				else:
					inEnEdge[tri[2]] = {}
					inEnEdge[tri[2]][tri[0]] = set()
					inEnEdge[tri[2]][tri[0]].add(tri[1])
		if i%10000 == 0:
			print(i)
		i += 1
print(len(inEnEdge))
print(len(outEnEdge))
print(len(inEdge))
print(len(outEdge))
print(len(types))
wr = open('output fragment file','w')
for i in range(12301050):#here we should iterate every entitiy
	if i%10000 == 0:
		print(i)
	eid = "%d"%i
	ret = ""
	tmp = ""
	if inEnEdge.has_key(eid):
		tmp = ""
		for k in inEnEdge[eid].keys():
			tmp += k
			tmp += ':'
			for item in inEnEdge[eid][k]:
				if item == '-1':
					continue
				tmp += item + ';'
			tmp += ','
	ret += tmp
	tmp = ""
	ret += '|'
	
	if outEnEdge.has_key(eid):
		tmp = ""
		for k in outEnEdge[eid].keys():
			tmp += k
			tmp += ':'
			for item in outEnEdge[eid][k]:
				if item == '-1':
					continue
				tmp += item + ';'
			tmp += ','
	ret += tmp
	tmp = ""
	ret += '|'
	
	if inEdge.has_key(eid):
		tmp = ""
		for item in inEdge[eid]:
			if item == '-1':
				continue
			tmp += item + ','
	ret += tmp
	tmp=""
	ret += '|'
	
	if outEdge.has_key(eid):
		tmp = ""
		for item in outEdge[eid]:
			if item == '-1':
				continue
			tmp += item + ','
	ret += tmp
	tmp=""
	ret += '|'
	
	if types.has_key(eid):
		tmp = ""
		for item in types[eid]:
			if item == '-1':
				continue
			tmp += item + ','
	ret += tmp
	tmp=""
	wr.write("%s\t%s\n"%(eid,ret))
	
	
			
	
			
			
			
				
	
