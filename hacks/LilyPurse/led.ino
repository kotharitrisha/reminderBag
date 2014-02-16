int ledp = 12;
int ledi = 9;
int ledu = 5;
int buzzer = 3;

int photocellPin = 0;
int val = 0;
char ibyte;
char discon;
void setup() {  //SETUP
  pinMode(ledi,OUTPUT);
  pinMode(ledu,OUTPUT);
  pinMode(ledp,OUTPUT);
  pinMode(buzzer,OUTPUT);
  Serial.begin(115200);
}

void loop() {
  
 /* while(1){
    digitalWrite(buzzer,HIGH);
    delay(1000);
    digitalWrite(buzzer,LOW);
    delay(1000);
}*/  // Wait for data
  while (Serial.available() == 0){
    digitalWrite(ledi,LOW);
    digitalWrite(ledp,LOW);
    digitalWrite(ledu,LOW);
    digitalWrite(buzzer,LOW);
  }
    
  //if (Serial.available())
    //digitalWrite(led,HIGH);
    
   // Read byte and light LEDs accordingly
    ibyte = Serial.read();   
//    if (Serial.available())
  //    discon = Serial.read();
  // if (discon == 'd')
  //     break;   
    if (ibyte == 'p'){
      digitalWrite(ledp,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledp,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledp,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledp,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledp,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledp,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledp,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledp,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledp,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledp,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
    } 
    if (ibyte == 'i'){
      digitalWrite(ledi,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledi,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledi,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledi,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledi,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledi,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledi,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledi,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledi,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledi,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
    } 
    if (ibyte == 'u'){
      digitalWrite(ledu,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledu,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledu,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledu,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledu,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledu,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledu,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledu,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
      digitalWrite(ledu,HIGH);
      digitalWrite(buzzer,HIGH);
      delay(2000);
      digitalWrite(ledu,LOW);
      digitalWrite(buzzer,LOW);
      delay(2000);
    } 

   // Serial.flush();
   
   while(Serial.available()>0) Serial.read();
}
