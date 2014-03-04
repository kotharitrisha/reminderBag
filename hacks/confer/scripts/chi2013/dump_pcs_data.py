import sys, os, json, csv, MySQLdb

if __name__ == "__main__":
	p = os.path.abspath(os.path.dirname(__file__))
	if(os.path.abspath(p+"/..") not in sys.path):
		sys.path.append(os.path.abspath(p+"/.."))
	os.environ.setdefault("DJANGO_SETTINGS_MODULE", "server.settings")

conn = MySQLdb.connect(host="mysql.csail.mit.edu", user="cobi", passwd="su4Biha", db="cobi")
cursor = conn.cursor()

def dump_authors_data():
	cursor.execute("""DELETE FROM pcs_authors;""")
	data = []
	header= True
	f = open('data/sigchiUsers.csv','r')
	reader = csv.reader(f)
	for row in reader:
		if(header or len(row)==0):
			header = False
		else:
			print row
			x= len(row)
			while(x<12):
				row.append('')
				x += 1

			cursor.execute(r"""INSERT INTO pcs_authors (id, given_name, middle_initial, family_name, 
				email1, email2, email3, dept1, instituition1, city1, state1, country1 ) 
				values('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');""" 
					%(conn.escape_string('auth'+row[0]), conn.escape_string(row[1]),
					conn.escape_string(row[2]), conn.escape_string(row[3]), 
					conn.escape_string(row[4]), conn.escape_string(row[5]),
					conn.escape_string(row[6]), conn.escape_string(row[7]),
					conn.escape_string(row[8]), conn.escape_string(row[9]),
					conn.escape_string(row[10]), conn.escape_string(row[11])
					))
			data.append(row)


	print len(data)


	


if __name__ == "__main__":
	dump_authors_data()
