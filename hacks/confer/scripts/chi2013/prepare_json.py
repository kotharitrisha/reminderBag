import sys, os, json

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

from db.entity import *
from db.session import *



def main():
	e = Entity()
	s = Session()
	f = open('server/static/data/papers.json','w')
	f.write(json.dumps(e.entities))
	f = open('server/static/data/sessions.json','w')
	f.write(json.dumps(s.sessions))
	print "done"


if __name__ == "__main__":
	main()
