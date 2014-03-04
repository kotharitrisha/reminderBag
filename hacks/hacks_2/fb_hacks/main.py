import os
import smtplib
import mimetypes
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import getpass

#Construction of Facebook message, and details
sender = raw_input("Email address of sender: ")
receiver = raw_input("Facebook email address of receiver: ")
message = MIMEMultipart()
message['From'] = sender
message['To'] = receiver
content = raw_input("Email content: ")
message.attach(MIMEText(content))

#Logging into server and authentication
server = smtplib.SMTP('smtp.seas.upenn.edu', 587)
server.ehlo()
server.starttls()
server.ehlo()
seas_username = raw_input("SEAS username: ")
seas_password = getpass.getpass("SEAS password: ")
server.login(seas_username, seas_password)

#Sending message
server.sendmail(sender, receiver, message.as_string())
server.close()

