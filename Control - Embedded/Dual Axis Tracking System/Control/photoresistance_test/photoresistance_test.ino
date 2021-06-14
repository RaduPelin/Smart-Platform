int pinl_top = 0; 
int pinr_top = 1; 
int pinl_down = 2;
int pinr_down = 3; 

void setup() {
  Serial.begin(9600);
}

void loop() {
  int left_top;
  int right_top;
  int left_down;
  int right_down;
  int readValues[5][4][20];

  Serial.print("The program read five sets of data for five levels of light to detect the tolerance between the sensors.\n");
  Serial.print("Every set of data contains 4*20 values read from the sensors.\n");
  for(int i = 0; i < 5; i++) {
    Serial.print("\nSet the light level in maximum five seconds.\n\n");
    delay(5000);
   
    Serial.print("LEFT-TOP\tRIGHT-TOP\tLEFT-DOWN\tRIGHT-DOWN\tTolerance:L-T:R-T\tTolerance:L-T:L-D\tTolerance:L-D:R-D\tTolerance:R-T:R-D\n");
    for(int j = 0; j < 20; j++) {
      left_top = analogRead(pinl_top);
      right_top = analogRead(pinr_top);
      left_down = analogRead(pinl_down);
      right_down = analogRead(pinr_down);
      readValues[i][0][j] = left_top;
      readValues[i][1][j] = right_top;
      readValues[i][2][j] = left_down;
      readValues[i][3][j] = right_down;
      Serial.print(left_top);
      Serial.print("\t\t");
      Serial.print(right_top);
      Serial.print("\t\t");
      Serial.print(left_down);
      Serial.print("\t\t");
      Serial.print(right_down);
      Serial.print("\t\t");
      Serial.print(abs(left_top - right_top));
      Serial.print("\t\t\t");
      Serial.print(abs(left_top-left_down)); 
      Serial.print("\t\t\t");
      Serial.print(abs(left_down-right_down));
      Serial.print("\t\t\t");
      Serial.print(abs(right_top-right_down));
      Serial.print("\t\t\t");
      Serial.print("\n");
      delay(100);
    }
  }
  
  double toleranceLTRT  = 0;
  double toleranceLTLD  = 0;
  double toleranceLDRD  = 0;
  double toleranceRTRD  = 0;
  
  for(int i = 0; i < 5; i++) {
	toleranceLTRT += getAverrageTolerance(readValues[i][0], readValues[i][1]);
  }
  toleranceLTRT /= 5;
  Serial.print("Tolerance between LEFT TOP sensor and RIGHT TOP sensor is: ");
  Serial.print(toleranceLTRT);
  Serial.print("\n");

  for(int i = 0; i < 5; i++) {
	toleranceLTLD += getAverrageTolerance(readValues[i][0], readValues[i][2]);
  }
  toleranceLTLD /= 5;
  Serial.print("Tolerance between LEFT TOP and LEFT DOWN sensors is: ");
  Serial.print(toleranceLTLD);
  Serial.print("\n");
  
  for(int i = 0; i < 5; i++) {
	toleranceLDRD += getAverrageTolerance(readValues[i][2], readValues[i][3]);
  }
  toleranceLDRD /= 5;
  Serial.print("Tolerance between LEFT DOWN and RIGHT DOWN sensors is: ");
  Serial.print(toleranceLDRD);
  Serial.print("\n");

  for(int i = 0; i < 5; i++) {
	toleranceRTRD += getAverrageTolerance(readValues[i][1], readValues[i][3]);
  }
  toleranceRTRD /= 5;
  Serial.print("Tolerance between RIGHT TOP and RIGHT DOWN sensors is: ");
  Serial.print(toleranceRTRD);
  Serial.print("\n");

  Serial.print("Unplug");
  delay(100000);  
}

double getAverrageTolerance(int set1[20], int set2[20]) {
  double averrage = 0;
  for(int i = 0; i < 20; i++) {
    if(set1[i] >= set2[i]) {
      averrage = averrage + (set1[i] - set2[i]);
    } else {
      averrage = averrage + (set2[i] - set1[i]);
    }
  }
  return averrage/20;
}
