// connect motor controller pins to ESP8266 digital pins
//DIGITAL PINS
/* D0 = 16
   D1 = 5
   D2 = 4
   D3 = 0
   D4 = 2
   D5 = 14
   D6 = 12
   D7 = 13
   D8 = 15
   */
//PWM PINS
/* D1, D2, D3, D4, D5, D6, D7, RSV */
   
// connect 5V output of one driver to the vin pin of ESP8266
int leftEn = 4;
int left_front = 16;
int left_back = 5;
int rightEn = 0;
int right_front = 2;
int right_back = 14;

void setup()
{
  // set all the motor control pins to outputs
  pinMode(leftEn, OUTPUT);
  pinMode(left_back, OUTPUT);
  pinMode(left_front, OUTPUT);
  pinMode(rightEn, OUTPUT);
  pinMode(right_back, OUTPUT);
  pinMode(right_front, OUTPUT);
}
void demoOne()
{
  // this function will run the motors in both directions at a fixed speed
  // turn on motors
  digitalWrite(left_front, HIGH);
  digitalWrite(left_back, LOW);
  digitalWrite(right_front, HIGH);
  digitalWrite(right_back, LOW);
  // set speed to 200 out of possible range 0~255
  analogWrite(leftEn, 965);
  analogWrite(rightEn, 965);
  delay(2000);
  digitalWrite(left_front, LOW);
  digitalWrite(left_back, HIGH);
  digitalWrite(right_front, HIGH);
  digitalWrite(right_back, LOW);
  delay(2000);
  // now turn off motors
  digitalWrite(left_front, LOW);
  digitalWrite(left_back, LOW);
  digitalWrite(right_front, LOW);
  digitalWrite(right_back, LOW);
}

void loop()
{
  demoOne();
  delay(1000);
  
}
