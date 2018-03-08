#include <SoftwareSerial.h>

#define led 6
#define rx 2
#define tx 3
#define photosensor0 A0
#define photosensor1 A1

SoftwareSerial BTserial(2, 3);


char b = ' ';
char c = ' ';
int i = 0;
String text;
int initialReading_A0 = 0;
int initialReading_A1 = 0;
byte output = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  BTserial.begin(9600);
  pinMode(led, OUTPUT);
  pinMode(photosensor0, INPUT);
  pinMode(photosensor1, INPUT);
  initialReading_A0 = analogRead(photosensor0);
  initialReading_A1 = analogRead(photosensor1);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(BTserial.available())
  {
    b = BTserial.read();
    Serial.write(b);
  }

  if(analogRead(photosensor0) - initialReading_A0 >= 50 && analogRead(photosensor1) - initialReading_A1 >= 50){
      Serial.println(3);
      BTserial.print(3);
      BTserial.println("~");
      delay(100);
  }
  else if(analogRead(photosensor0) - initialReading_A0 >= 50){
      Serial.println(2);
      BTserial.print(2);
      BTserial.println("~");
      delay(100);
  }
  else if(analogRead(photosensor1) - initialReading_A1 >= 50){
      Serial.println(1);
      BTserial.print(1);
      BTserial.println("~");
      delay(100);
  }
  else{
      Serial.println(0);
      BTserial.print(0);
      BTserial.println("~");
      delay(100);
  }

  
}
