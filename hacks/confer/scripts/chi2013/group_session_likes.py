#!/usr/bin/python
import sys, os, operator

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

from django.db import connection
from algorithm.utils import *
from db.session import *


'''
@author: anant bhardwaj
@date: Feb 12, 2013

script for preparing data in lenskit format
'''


sessions = Session().sessions
p_counts = {}
s_counts = {}

def load_data():
	cursor = connection.cursor()
	cursor.execute("SELECT auth_no, likes FROM pcs_authors where likes!= 'NULL' and likes !='[]';")
	data = cursor.fetchall()
	for row in data:
		papers = json.loads(row[1])
		for p in papers:
			if(p in p_counts):
				p_counts[p] = p_counts[p] + 1
			else:
				p_counts[p] = 1

	
	for s in sessions:
		s_counts[s] = 0
		for p in sessions[s]['submissions']:
			s_counts[s] = s_counts[s] + p_counts[p]

	sorted_sessions = sorted(s_counts.iteritems(), key=operator.itemgetter(1), reverse = True)	
	
	return sorted_sessions


	




def main():
	load_data()
	sorted_papers = sorted(p_counts.iteritems(), key=operator.itemgetter(1), reverse = True)
	print sorted_papers	
	'''
	counts = load_data()
	for c in counts:
		print '%s,%s,%s,%s,%s,"%s"' %(c[0], c[1], sessions[c[0]]['date'], sessions[c[0]]['time'], sessions[c[0]]['room'], sessions[c[0]]['s_title'])
  	print "done."
  	'''

if __name__ == '__main__':
    main()
