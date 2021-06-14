
int enA = 5;
int in1 = 4;
int in2 = 3;


void setup() {
  Serial.begin(9600);
  pinMode(enA, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
}

void loop() {
  analogWrite(enA, 200);
  digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW); 
  delay(1000);
  digitalWrite(in1, LOW);
  digitalWrite(in2, LOW);  
}
