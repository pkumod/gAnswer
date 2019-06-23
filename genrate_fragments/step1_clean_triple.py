import re
'''
Step 1: Clean the triple file. In the dbpedia case, we just need the  part of resource URI that indicate entity/type/predicate names.
'''
fileName = []#List of triple files to be process
notRdf = open('./notRdf.txt','w')#Record the lines that refers to a type but not rdf:type 
for index2,fname in enumerate(fileName):
	f = open('./'+fname)
	triple = open('output triple files here','w')
	prefix_f = open('output prefix files here','w')# save the prefix in files in case of it may be useful in the future. 
	i = 0
	count = 0
	prefix_set = set()
	for line in f:
		if line[0] != '<':
			print(i)
			i = i + 1
			count += 1
			continue
		line = line[:-3].replace('> <','>$-$-$<').replace('> "','>$-$-$"')
		line = line.split('$-$-$')
		if i==0:
			i += 1
			continue
		new_line=[]
		if "type>" in line[1]:
			if "rdf" not in line[1]:
				notRdf.write(str(line)+'\n')
				continue
		for index,item in enumerate(line):
			if not item:
				count +=1
				break  
			if item[0]=='<':
				pos = item.rfind('/')
				word = item[pos+1:-1].split("#")
				if len(word)<2:
					new_line.append('<'+word[0]+'>')
				else:
					new_line.append('<'+word[1]+'>')
				if index == 1:
					tmp = new_line[1][1:len(new_line[1])-1]
					pos2 = line[1].rfind(tmp)
					prefix = line[1][1:pos2-1]
					prefix_set.add(tmp + '^^^'+prefix+'\n')
					continue
			elif item.count('"') >=2:
				item = item.split('^^')[0].split('@')[0]
				pattern = re.compile('"(.*)"')
				word = '"'+''.join(pattern.findall(item))+'"'
				new_line.append(word)
				continue
			else:
				print(i)
		i += 1
		#print('\t'.join(new_line))
		if i%1000000==0:
			print("%d:%d"%(8,i))
		triple.write('\t'.join(new_line)+'\n')
	for item in prefix_set:
		prefix_f.write(item)
	f.close()
	triple.close()
	prefix_f.close()
    
