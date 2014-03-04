import sys, os, operator, json

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

from py4j.java_gateway import JavaGateway
from py4j.java_collections import ListConverter
from utils import *


class Recommender:
	def __init__(self):
		self.gateway = JavaGateway()


	def get_item_based_recommendations(self, paper_id_list):
		encoded_id_list = [str(encode_paper_id(id)) for id in paper_id_list]
		j_id_list = ListConverter().convert(encoded_id_list, self.gateway._gateway_client)
		recs = self.gateway.entry_point.recommend(j_id_list)
		res=[]
		for rec in recs:
			r = rec.split(',')
			id = long(r[0])
			if(id!=0):
				paper_id = decode_paper_id(id)
				res.append({'id': paper_id, 'score': float(r[1])})
		return res


	def get_users_recommendations(self, authors_list):
		j_id_list = ListConverter().convert(authors_list, self.gateway._gateway_client)
		recs = self.gateway.entry_point.recommend_users(j_id_list)
		res=[]
		for rec in recs:
			r = rec.split(',')
			res.append({'id': r[0], 'score': float(r[1])})
		return res

def main():
	r = Recommender()
	#res = r.get_item_based_recommendations(['pn1460'])
	res = r.get_users_recommendations(['13514'])
	print res


if __name__ == "__main__":
	main()
