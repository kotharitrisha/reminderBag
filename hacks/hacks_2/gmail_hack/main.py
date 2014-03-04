import os
from smtplib import * 
import mimetypes
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import getpass

sender = raw_input("Email address of sender: ")
receiver = raw_input("Email address of receiver: ")
subject = raw_input("Subject: ")

#Message constructed
message = MIMEMultipart()
message['From'] = sender
message['To'] = receiver
message['Subject'] = subject
content = raw_input("Email content: ")
message.attach(MIMEText(content))

#SMTP server logging in, and authentication
server = SMTP("smtp.seas.upenn.edu", 587)
server.ehlo()
server.starttls()
server.ehlo()
seas_username = raw_input("SEAS username: ")
seas_password = getpass.getpass("SEAS password: ")
server.login(seas_username, seas_password)

#Final sending mail
server.sendmail(sender, receiver, message.as_string())
server.close()

