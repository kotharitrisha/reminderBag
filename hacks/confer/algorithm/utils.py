#!/usr/bin/python

'''
@author: anant bhardwaj
@date: Feb 12, 2013

some utils
'''


def encode_author_id(paper_id, authorId):
	ret = 0
	author_id = 0
	if(len(authorId) > 4 and authorId.startswith('auth')):
		author_id = int(authorId[4:])
	if(paper_id.startswith('alt')):
		ret= '1%05d%06d' %(int(paper_id[3:]), author_id)
	elif(paper_id.startswith('case')):
		ret= '2%05d%06d' %(int(paper_id[4:]), author_id)
	elif(paper_id.startswith('crs')):
		ret= '3%05d%06d' %(int(paper_id[3:]), author_id)
	elif(paper_id.startswith('pan')):
		ret= '4%05d%06d' %(int(paper_id[3:]), author_id)
	elif(paper_id.startswith('pn')):
		ret= '5%05d%06d' %(int(paper_id[2:]), author_id)
	elif(paper_id.startswith('sig')):
		ret= '6%05d%06d' %(int(paper_id[3:]), author_id)
	elif(paper_id.startswith('tochi')):
		ret= '7%05d%06d' %(int(paper_id[5:]), author_id)
	return long(ret)


def encode_paper_id(paper_id):
	ret = ''
	if(paper_id.startswith('alt')):
		ret= '1%05d' %(long(paper_id[3:]))
	elif(paper_id.startswith('case')):
		ret= '2%05d' %(long(paper_id[4:]))
	elif(paper_id.startswith('crs')):
		ret= '3%05d' %(long(paper_id[3:]))
	elif(paper_id.startswith('pan')):
		ret= '4%05d' %(long(paper_id[3:]))
	elif(paper_id.startswith('pn')):
		ret= '5%05d' %(long(paper_id[2:]))
	elif(paper_id.startswith('sig')):
		ret= '6%05d' %(long(paper_id[3:]))
	elif(paper_id.startswith('tochi')):
		ret= '7%05d' %(long(paper_id[5:]))
	return long(ret)

def decode_paper_id(long_id):
	str_id = str(long_id)
	prefix = int(str_id[0:1])
	paper_id_str = str(int(str_id[1:]))
	paper_id_pre= ''
	if(prefix == 1):
		paper_id_pre= 'alt'
	elif(prefix == 2):
		paper_id_pre= 'case'
	elif(prefix == 3):
		paper_id_pre= 'crs'
	elif(prefix == 4):
		paper_id_pre= 'pan'
	elif(prefix == 5):
		paper_id_pre= 'pn'
	elif(prefix == 6):
		paper_id_pre= 'sig'
	elif(prefix == 7):
		paper_id_pre= 'tochi'
	return paper_id_pre+paper_id_str



def main():
  pass
  

if __name__ == '__main__':
    main()
