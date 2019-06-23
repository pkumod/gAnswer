#encoding=utf-8
basic = []
yago = []
b = 0
y = 100000
'''
	In dbpedia dataset we use two sorts of type: yago type and basic type
	yago type refers to type with yago prefix
	basic type refers to objects pointed to by rdf:type
	this script divide this two kinds of types into different files.
'''
with open('type id file here') as f:
	for line in f:
		dou = line[:-1].split('\t')
		if dou[0][:6] == '<yago:':
			yago.append(dou[0]+"\t%d\n"%y)
			y+=1
		else:
			basic.append(dou[0]+"\t%d\n"%b)
			b+=1

with open('basic types id file here','w') as f:
	for str in basic:
		f.write(str)
with open("yago type id file here",'w') as f:
	for str in yago:
		f.write(str)

