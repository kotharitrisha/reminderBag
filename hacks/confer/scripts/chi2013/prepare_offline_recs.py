import sys, os, json

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

from db.entity import *
from algorithm.recommend import *

f = open('server/static/data/offline_recs.txt','w')
r = Recommender()

def main():
	recs= {}
	e = Entity()
	for p_id in e.entities:
		rec = r.get_item_based_recommendations([p_id])
		recs[p_id] = rec[0:20]
	f.write(json.dumps(recs))


if __name__ == "__main__":
	main()
