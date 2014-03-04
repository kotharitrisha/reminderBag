#!/usr/bin/python
import sys, os, json, random

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
	data = cursor.fetchall()
	if(len(data) > 0):
		return data[0][0]
	else:
		return None


def load_data():
	prefs = {}
	cursor = connection.cursor()
	cursor.execute("SELECT authorId, id from authors;")
	data = cursor.fetchall()
	for row in data:
		if(row[0].strip()==''):
			continue
		id = get_id(row[0])
		if(id != None):
			prefs[encode_paper_id(row[1])] = {id:5.0}

	cursor.execute("SELECT authorId, great, ok, notsure, notok, interested FROM authorsourcing;")
	data = cursor.fetchall()
	for row in data:
		if(row[0].strip()==''):
			continue
		id = get_id(row[0])
		if(id != None):
			if(row[1]!=''):
				for p in row[1].split(','):
					prefs[encode_paper_id(p)].update({id:5.0})
			if(row[2]!=''):
				for p in row[2].split(','):
					prefs[encode_paper_id(p)].update({id:3.0})

			if(row[3]!=''):
				for p in row[3].split(','):
					prefs[encode_paper_id(p)].update({id:3.0})

			if(row[4]!=''):
				for p in row[4].split(','):
					prefs[encode_paper_id(p)].update({id:1.0})
	cursor.execute("SELECT auth_no, likes FROM pcs_authors where likes!= 'NULL' and likes !='[]';")
	data = cursor.fetchall()
	for row in data:
		papers = json.loads(row[1])
		for p in papers:
			prefs[encode_paper_id(p)].update({row[0]:5.0-random.random()})

			
	
	return prefs

# insert data
def prepare(prefs):
	f = open('data/data_lenskit_user.txt','w')	
	for k,v in prefs.iteritems():
		for p, r in v.iteritems():
			f.write("%s\t%s\t%s\n" %(k, p, r))




def main():
	prefs = load_data()
	print prefs;
  	prepare(prefs)
  	print "done."
  

if __name__ == '__main__':
    main()
