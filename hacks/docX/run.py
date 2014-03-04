
import os
from flask import Flask, request, redirect
from twilio.rest import TwilioRestClient

app = Flask(__name__)
account_sid= "ACf4705035a6d79ae5859ad143026b5cbb"
auth_token="fe9db2fafd17b354f0885cef8c8f043c"

@app.route("/twilio", methods=['GET','POST'])
def hello():
    client=TwilioRestClient(account_sid, auth_token)
    from_number=request.values.get('From', None)
    body=request.values.get('Body', None)
    parse(client, body, from_number)


if __name__=="__main__":
    app.run(debug=True)



def send_records(client, from_no):
    records="Last 3 medical records: "
    list = client.sms.messages.list(from_=from_no)

    y = 0

    for x in list:
        if x.body == "logs":
            continue
        x.body = x.body.replace("update ", "")
        records = records  + x.body + "; "
        y=y+1
        if y==3:
            break
    print records
    client.sms.messages.create(from_="+14083296276", to=from_no,
                               body= records)

def update(client, from_no):
    client.sms.messages.create(from_="+14083296276", to=from_no,
                               body= "Thanks You for updating")

def parse(client, message, from_no):
    splitter=message.split(" ")
    if splitter[0] == "update":
        update(client, from_no)
    if splitter[0] == "logs":
        send_records(client, from_no)


