from server.models import *
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

def send_email(addr, subject, msg_body):	
	email_subject = subject
	from_addr="confer@csail.mit.edu"
	to_addr = [addr, 'confer@csail.mit.edu']
	
	msg = MIMEMultipart()
	msg['From'] = 'Confer Team <confer@csail.mit.edu>'
	msg['To'] = ",".join(to_addr)
	msg['Subject'] = email_subject
	msg.attach(MIMEText(msg_body))

	username = 'anantb'
	password = 'JcAt250486'
	smtp_conn = smtplib.SMTP_SSL('cs.stanford.edu', 465)
	smtp_conn.login(username, password)		
	smtp_conn.sendmail(from_addr, to_addr, msg.as_string())
	smtp_conn.close() 


def insert_log(registration, action, data=None):
	if(data):
		l = Logs(registration = registration, action = action, data= data)
		l.save()
	else:
		l = Logs(registration = registration, action = action)
		l.save()


def get_registration(login, conf):
	try:
		user = User.objects.get(email = login)
		conference = Conference.objects.get(unique_name = conf)
		try:
			registration = Registration.objects.get(user = user, conference = conference)
			return registration
		except Registration.DoesNotExist:
			registration = Registration(user = user, conference = conference)
			registration.save()
			return registration
	except:
		print sys.exc_info()
		return None
