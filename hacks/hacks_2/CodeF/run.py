from flask import Flask, request, redirect #imports
from twilio.rest import TwilioRestClient

app = Flask(__name__)
 
#@app.route("/")

#def responseMessage():
account_sid = "AC0d6876c62dad85921396d0be389b68b3"
auth_token  = "662e15601a3bdb6e6ca46f0c226ab750"
client = TwilioRestClient(account_sid, auth_token)
message_str = "Thank you for your post"
message = client.sms.messages.create(body=message_str,
                                     to="+15126668669", from_="+17652744048")
print message.sid     
    
 
#if __name__ == "__main__":
#    app.run(debug=True)
