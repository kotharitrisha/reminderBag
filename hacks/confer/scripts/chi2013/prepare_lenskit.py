#!/usr/bin/python
import sys, os, json

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

from django.db import connection
from algorithm.utils import *



'''
@author: anant bhardwaj
@date: Feb 12, 2013

script for preparing data in lenskit format
'''


def get_id(authorId):
	cursor = connection.cursor()
	cursor.execute("SELECT auth_no, id FROM pcs_authors where id = '%s';" %(authorId))
	print authorId
	data = cursor.fetchall()
	print data
	return data[0][0]


def load_data():
	prefs = {}
	authors = []
	cursor = connection.cursor()
	cursor.execute("SELECT auth_no, id, likes FROM pcs_authors where likes!= 'NULL' and likes !='[]';")
	data = cursor.fetchall()
	for row in data:
		author_id = row[0]
		authors.append(row[1])
		author_prefs = {encode_paper_id(p):5.0 for p in json.loads(row[2])}
		prefs[author_id] = author_prefs

	cursor.execute("SELECT id, authorId, great, ok, notsure, notok, interested, name FROM authorsourcing;")
	data = cursor.fetchall()

	for row in data:
		if(row[0].strip()==''):
			continue
		author_id = encode_author_id(row[0].strip(), row[1].strip())

		# rate his own paper as great
		author_prefs = {encode_paper_id(unicode(row[0]).strip()):5.0}
					
		# great: 5, ok: 3.0, not_sure: 2.0, not_ok: 1.0
		if(row[6]!=''):
			author_prefs.update({encode_paper_id(unicode(p).strip()):4.0 for p in row[6].split(',')})
		if(row[2]!=''):
			author_prefs.update({encode_paper_id(unicode(p).strip()):5.0 for p in row[2].split(',')})
		if(row[3]!=''):
			author_prefs.update({encode_paper_id(unicode(p).strip()):3.0 for p in row[3].split(',')})
		if(row[4]!=''):
			author_prefs.update({encode_paper_id(unicode(p).strip()):2.0 for p in row[4].split(',')})
		if(row[5]!=''):
			author_prefs.update({encode_paper_id(unicode(p).strip()):1.0 for p in row[5].split(',')})
		if(author_id in prefs):
			prefs[author_id].update(author_prefs)
		else:
			prefs[author_id] = author_prefs
	
	return prefs

# insert data
def prepare(prefs):
	f = open('data/data_lenskit.txt','w')	
	for k,v in prefs.iteritems():
		for p, r in v.iteritems():
			f.write("%s\t%s\t%s\n" %(k, p, r))




def main():
	prefs = load_data()
  	prepare(prefs)
  	print "done."
  

if __name__ == '__main__':
    main()
