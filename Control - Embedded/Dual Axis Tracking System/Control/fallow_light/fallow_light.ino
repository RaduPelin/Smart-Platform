#include <Servo.h>
#include <Wire.h>
#include <HMC5883L_Simple.h>
Servo vertical;
Servo horizontal;

int pinl_top = 0; 
int pinr_top = 1; 
int pinl_down = 2;
int pinr_down = 3; 

//Toleranțe
double toleranceLTRT = 50.0;
double toleranceLTLD = 50.0;
double toleranceLDRD = 50.0;
double toleranceRTRD = 50.0;

HMC5883L_Simple compass;
double last_heading;
double next_heading;

int vertical_limitLow = 0;
int vertical_limitHigh  = 180; // echivalent cu  270
int verticalValue = 90; // echivalent cu  135

int horizontal_limitLow = 45;
int horizontal_limitHigh = 135;
int horizontalValue = 90;

void setup() {
  vertical.attach(9);
  vertical.write(verticalValue);  
  horizontal.attach(8);
  horizontal.write(horizontalValue);  
  Wire.begin();
  compass.SetDeclination(5, 45, "E");
  compass.SetSamplingMode(COMPASS_SINGLE);
  compass.SetScale(COMPASS_SCALE_130);
  compass.SetOrientation(COMPASS_HORIZONTAL_X_NORTH);
  delay(3000);
}

void loop() {
  int left_top;
  int right_top;
  int left_down;
  int right_down;
  
 readData(&left_top, &right_top, &left_down, &right_down);
 
  do {
     readData(&left_top, &right_top, &left_down, &right_down);
  }
  while(allEqual(left_top,right_top,left_down,right_down)); 

  //Cadranul Dreapta Jos înregistrează fluxul maxim de lumină
  if (!isGreater("LEFT_DOWN","RIGHT_DOWN",left_down,right_down) && !isGreater("RIGHT_TOP","RIGHT_DOWN",right_top,right_down) && verticalValue > vertical_limitLow) {
     while (!isEqual("LEFT_DOWN","RIGHT_DOWN",left_down,right_down) && verticalValue != vertical_limitLow) {
        verticalValue--;
        vertical.write(verticalValue);
        delay(10);
        readData(&left_top, &right_top, &left_down, &right_down);
     } 
  }

   //Cadranul Stânga Jos înregistrează fluxul maxim de lumină
  if (isGreater("LEFT_DOWN","RIGHT_DOWN",left_down,right_down) && !isGreater("LEFT_TOP","LEFT_DOWN",left_top,left_down) && verticalValue < vertical_limitHigh) {
     while (!isEqual("LEFT_DOWN","RIGHT_DOWN",left_down,right_down) && verticalValue != vertical_limitHigh) {
        verticalValue++;
        vertical.write(verticalValue);
        delay(10);
        readData(&left_top, &right_top, &left_down, &right_down);
     } 
  }

  //Cadranul Dreapta Sus înregistrează fluxul maxim de lumină
  if (!isGreater("LEFT_TOP","RIGHT_TOP",left_top,right_top) && isGreater("RIGHT_TOP","RIGHT_DOWN",right_top,right_down) && verticalValue < vertical_limitHigh) {
     while (!isEqual("LEFT_TOP","RIGHT_TOP",left_top,right_top) && verticalValue != vertical_limitHigh) {
        verticalValue++;
        vertical.write(verticalValue);
        delay(10);
        readData(&left_top, &right_top, &left_down, &right_down);
     } 
  }

  //Cadranul Stânga Sus înregistrează fluxul maxim de lumină
  if (isGreater("LEFT_TOP","RIGHT_TOP",left_top,right_top) && isGreater("LEFT_TOP","LEFT_DOWN",left_top,left_down) && verticalValue > vertical_limitLow) {
     while (!isEqual("LEFT_TOP","RIGHT_TOP",left_top,right_top) && verticalValue != vertical_limitLow) {
        verticalValue--;
        vertical.write(verticalValue);
        delay(10);
        readData(&left_top, &right_top, &left_down, &right_down);
     } 
  }
 
  //Cadranele Stânga Sus și Dreapta Sus înregistrează fluxul maxim de lumină
  if (isEqual("LEFT_TOP","RIGHT_TOP",left_top,right_top) && isGreater("LEFT_TOP","LEFT_DOWN",left_top,left_down)) {
      while (!isEqual("LEFT_TOP","LEFT_DOWN",left_top,left_down) && horizontalValue > horizontal_limitLow) {
        horizontalValue--;
        horizontal.write(horizontalValue);
        delay(10);
        readData(&left_top, &right_top, &left_down, &right_down);
      }  
  }

  //Cadranele Stânga Jos și Dreapta Jos înregistrează fluxul maxim de lumină
  if (isEqual("LEFT_DOWN","RIGHT_DOWN",left_down,right_down) && !isGreater("RIGHT_TOP","RIGHT_DOWN",right_top,right_down)) {
    while (!isEqual("RIGHT_TOP","RIGHT_DOWN",right_top,right_down) && horizontalValue < horizontal_limitHigh) {
      horizontalValue++;
      horizontal.write(horizontalValue);
      delay(10);
      readData(&left_top, &right_top, &left_down, &right_down);
    }
  }
}

void readData(int *left_t, int *right_t, int *left_d, int *right_d) {
    *left_t = analogRead(pinl_top);
    *right_t  = analogRead(pinr_top);
    *left_d = analogRead(pinl_down);
    *right_d = analogRead(pinr_down);
}

bool allEqual(int position1Value, int position2Value, int position3Value, int position4Value) {
  if(isEqual("LEFT_TOP","RIGHT_TOP",position1Value,position2Value) && isEqual("LEFT_TOP","LEFT_DOWN",position1Value,position3Value) && isEqual("LEFT_DOWN","RIGHT_DOWN",position3Value,position4Value) && isEqual("RIGHT_TOP","RIGHT_DOWN",position2Value,position4Value)){
      last_heading = compass.GetHeadingDegrees();
      for(int i = 1; i <= 1800; i++) {  // i < 1800
          next_heading = compass.GetHeadingDegrees();
          delay(1000);
          if((abs) (next_heading - last_heading) > 10) {
              break;
          }
      }
      
      return true;
  } else return false;
}

bool isEqual(String position1, String position2, int position1Value, int position2Value) {
	if(position1 == "LEFT_TOP" && position2 == "RIGHT_TOP") 
		return position1Value - position2Value <= toleranceLTRT && position1Value - position2Value >= -toleranceLTRT; 
	if(position1 == "LEFT_DOWN" && position2 == "RIGHT_DOWN") 
		return position1Value - position2Value <= toleranceLDRD && position1Value - position2Value >= -toleranceLDRD; 
	if(position1 == "LEFT_TOP" && position2 == "LEFT_DOWN") 
		return position1Value - position2Value <= toleranceLTLD && position1Value - position2Value >= -toleranceLTLD; 
	if(position1 == "RIGHT_TOP" && position2 == "RIGHT_DOWN") 
		return position1Value - position2Value <= toleranceRTRD && position1Value - position2Value >= -toleranceRTRD; 
}

bool isGreater(String position1, String position2, int position1Value, int position2Value) {
	if(position1 == "LEFT_TOP" && position2 == "RIGHT_TOP") 
		return position1Value - toleranceLTRT > position2Value; 
	if(position1 == "LEFT_DOWN" && position2 == "RIGHT_DOWN") 
		return position1Value - toleranceLDRD > position2Value; 
	if(position1 == "LEFT_TOP" && position2 == "LEFT_DOWN") 
		return position1Value - toleranceLTLD > position2Value; 
	if(position1 == "RIGHT_TOP" && position2 == "RIGHT_DOWN") 
		return position1Value - toleranceRTRD > position2Value;  
}
