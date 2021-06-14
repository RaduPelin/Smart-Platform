#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>
#define My_SSID "SmartPlatform00001TX"
#define My_PASS "SP00001TXProto"
#define My_FIREBASE_HOST "platform120715.firebaseio.com"
#define My_FIREBASE_AUTH "txsBJLNgKSeaunvXdoR2GOUsXpATo5l9WwjJ8XhQ"
#define leftEn 4
#define leftFront 16
#define leftBack 5
#define rightEn 0
#define rightFront 2
#define rightBack 14
#define trig 12
#define echoFront 15
#define echoBack 13
#define front_back 0
#define left_front 1
#define left_back 2
#define right_front 3
#define right_back 4
#define speed1 810
#define speed2 850
#define speed3 900
#define speed4 960
#define speed5 1023
#define maxCounterStepDistance 3
#define turnToleranceFrontBack 24
#define  turnTolerance LeftRight 10
#define realSpeed 2391
#define turnTime 3470
#define turnTimeFront_Back 602
#define turnTimeLeft_Right 251
#define carSize 16


class Step {
  private:
    String stepDirection;
    double stepDistance;
    String distanceUnits;
    int numberOfLapses;
    double stepDelay;
    String delayUnits;
   
  public:
    Step(String stepDirection, double stepDistance, String distanceUnits, int numberOfLapses, double stepDelay, String delayUnits);
    byte getDirection();
    double getRealDistance();
    byte getDistanceUnits();
    int getNumberOfLapses();
    double getRealDelay();
    byte getDelayUnits();
};

Step::Step(String stepDirection, double stepDistance, String distanceUnits, int numberOfLapses, double stepDelay, String delayUnits) {
  this->stepDirection = stepDirection;
  this->stepDistance = stepDistance;
  this->distanceUnits = distanceUnits;
  this->numberOfLapses = numberOfLapses;
  this->stepDelay = stepDelay;
  this->delayUnits = delayUnits;
}

byte Step::getDirection() 
{ 
  byte stepDir;
  if (!this->stepDirection.compareTo("Front")) {
    stepDir = 1;
  } else {
    if (!this->stepDirection.compareTo("Back")) {
      stepDir = 2;
    } else {
      if (!this->stepDirection.compareTo("Left")) {
        stepDir = 3;
      } else {
        stepDir = 4;
      }
    }
  }
  return stepDir;
}

byte Step::getDistanceUnits() 
{ 
  byte stepDistanceUnits;
  if (!this->distanceUnits.compareTo("Centimeters")) {
    stepDistanceUnits = 1;
  } else {
    stepDistanceUnits = 2;
  } 
  return stepDistanceUnits;
}

byte Step::getDelayUnits() 
{ 
  byte stepDelayUnits;
  if (!this->delayUnits.compareTo("Seconds")) {
    stepDelayUnits = 1;
  } else {
    if (!this->delayUnits.compareTo("Minutes")) {
      stepDelayUnits = 2;
    } else {
      stepDelayUnits = 3;
    }   
  }
  return stepDelayUnits;
}

double Step::getRealDistance()
{
  if (this->getDistanceUnits() == 1) {
    return this->stepDistance;
  } else {
    return this->stepDistance*100;
  }
}

int Step::getNumberOfLapses()
{
  return this->numberOfLapses;
}

double Step::getRealDelay()
{
  if (this->getDelayUnits() == 1) {
    return this->stepDelay*1000;
  } else {
    if (this->getDelayUnits() == 2) {
      return this->stepDelay*1000*60;
    } else {
      return this->stepDelay*1000*60*60;
    }
  }
}

byte carSpeed;
byte distancesFrontCounter = 0;
byte distancesBackCounter = 0;
float *distancesFront = new float[maxCounterStepDistance];
float *distancesBack = new float[maxCounterStepDistance];
bool comeBack;
int programSpeed;
bool isBlockedFront;
bool isBlockedBack;

void setup() {
   Serial.begin(9600);
   pinMode(leftEn, OUTPUT);
   pinMode(rightEn, OUTPUT);
   pinMode(leftFront, OUTPUT);
   pinMode(leftBack, OUTPUT);
   pinMode(rightFront, OUTPUT);
   pinMode(rightBack, OUTPUT);
   pinMode(trig, OUTPUT);
   pinMode(echoFront, INPUT);
   pinMode(echoBack, INPUT);
   wifiConnect();
}


void loop() {
	if(WiFi.status() != WL_CONNECTED) {
		stopCar(front_back);
	}
	
	String program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");

	while (Firebase.failed()) { 
		program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
	}

	if (!program.compareTo("")) {
		bool otherNetwork = Firebase.getBool("devices/SmartPlatform00001TX/otherNetwork");
		while (Firebase.failed()) {
			otherNetwork = Firebase.getBool("devices/SmartPlatform00001TX/otherNetwork");
		}
		bool connected = Firebase.getBool("devices/SmartPlatform00001TX/connected");
		while (Firebase.failed()) {
			connected = Firebase.getBool("devices/SmartPlatform00001TX/connected");
		}
		if (otherNetwork && connected) {
			String ssid = Firebase.getString("/devices/SmartPlatform00001TX/ssid");
			while (Firebase.failed()) {
				ssid = Firebase.getString("/devices/SmartPlatform00001TX/ssid");
			}
			String password = Firebase.getString("/devices/SmartPlatform00001TX/password");
			while (Firebase.failed()) {
				password = Firebase.getString("/devices/SmartPlatform00001TX/password");
			}
			Firebase.setBool("devices/SmartPlatform00001TX/wait", true);
      while (Firebase.failed()) {
        Firebase.setBool("devices/SmartPlatform00001TX/wait", true);
      }
      WiFi.disconnect();
			WiFi.begin(ssid.c_str(), password.c_str()); // Începe conexiunea
      Firebase.remove("devices/SmartPlatform00001TX/wait");
      while (Firebase.failed()) {
        Firebase.remove("devices/SmartPlatform00001TX/wait");
      }
			Firebase.setBool("devices/SmartPlatform00001TX/otherNetwork", false);
			while (Firebase.failed()) {
				Firebase.setBool("devices/SmartPlatform00001TX/otherNetwork", false);
			}
		} 
      
		if (!connected) {
			WiFi.begin(My_SSID, My_PASS);
			while (WiFi.status() != WL_CONNECTED) {
				delay(1000);  // dacă încă nu s-a conectat așteaptă o secundă și reîncearcă
			}
		}
      
		String state = Firebase.getString("/devices/SmartPlatform00001TX/state");
		while (Firebase.failed()) {
			state = Firebase.getString("/devices/SmartPlatform00001TX/state");
		}

      
		if (!state.compareTo("R-Stop")) {
			stopCar(front_back);
		}

		if (!state.compareTo("R-Speed1")) {
			setCarSpeed(speed1);
		}

        if (!state.compareTo("R-Speed2")) {
			setCarSpeed(speed2);
        }

		if (!state.compareTo("R-Speed3")) {
			setCarSpeed(speed3);
		}

		if (!state.compareTo("R-Speed4")) {
			setCarSpeed(speed4);
		}

		if (!state.compareTo("R-Speed5")) {
			setCarSpeed(speed5);
		}

		if (!state.compareTo("H-F")) {
			goFront();
			if(isBlockedBack) {
				getDistances(echoFront, "BACK");
			} else {
				getDistances(echoFront, "FRONT");
			}	
		}

		if (!state.compareTo("L-F")) {
			stopCar(front_back);
		}
	  
		if (!state.compareTo("H-B")) {
			goBack();
			if(isBlockedFront) {
				getDistances(echoFront, "FRONT");
			} else {
				getDistances(echoFront, "BACK");
			}
		}

		if (!state.compareTo("L-B")) {
			stopCar(front_back);
		}
         
		if (!state.compareTo("H-L_F")) {
			goFront();
			turnLeft();
			getDistances(echoFront, "FRONT");
		}
      
		if (!state.compareTo("L-L_F")) {
			stopCar(left_front);
			setCarSpeed(carSpeed);
		}
      
		if (!state.compareTo("H-L_B")) {
			goBack();
			turnLeft();
			getDistances(echoBack, "BACK");
		}
  
		if (!state.compareTo("L-L_B")) {
			stopCar(left_back);
			setCarSpeed(carSpeed);
		}
      
		if (!state.compareTo("H-R_F")) {
			goFront();
			turnRight();
			getDistances(echoFront, "FRONT");	
		}
  
		if (!state.compareTo("L-R_F")) {
			stopCar(right_front);
			setCarSpeed(carSpeed);
		}
      
		if (!state.compareTo("H-R_B")) {
			goBack();
			turnRight();
			getDistances(echoBack, "BACK");
		}
  
		if (!state.compareTo("L-R_B")) {
			stopCar(right_back);
			setCarSpeed(carSpeed);
		} 
    } else {
		double x = 0.0;
		double y = 0.0;
		double lx = 0.0;
		double ly = 0.0; // coordonate
      
		comeBack = Firebase.getBool("/devices/SmartPlatform00001TX/program/comeBack");
		while (Firebase.failed()) { 
			comeBack = Firebase.getBool("/devices/SmartPlatform00001TX/program/comeBack");
		}
  
		int totalSteps = Firebase.getInt("/devices/SmartPlatform00001TX/program/steps/total");
			while (Firebase.failed()) { 
			   totalSteps = Firebase.getInt("/devices/SmartPlatform00001TX/program/steps/total");
		}
  
      for (int i = 1; i <= totalSteps; i++) {
        String index = (String) i;
        String pos = "Step " + index;
        
        String stepDirection = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/direction");
        
        while (Firebase.failed()) { 
           stepDirection = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/direction");
        }
        String distanceUnits = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/distanceUnits");
        while (Firebase.failed()) { 
           distanceUnits = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/distanceUnits");
        }
        String delayUnits = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/delayUnits");
        while (Firebase.failed()) { 
           delayUnits = Firebase.getString("/devices/SmartPlatform00001TX/program/steps/"+pos+"/delayUnits");
        }
        double stepDistance = Firebase.getFloat("/devices/SmartPlatform00001TX/program/steps/"+pos+"/distance");
         while (Firebase.failed()) { 
           stepDistance = Firebase.getFloat("/devices/SmartPlatform00001TX/program/steps/"+pos+"/distance");
        }
        int numberOfLapses = Firebase.getInt("/devices/SmartPlatform00001TX/program/steps/"+pos+"/numberOfLapses");
         while (Firebase.failed()) { 
           numberOfLapses = Firebase.getInt("/devices/SmartPlatform00001TX/program/steps/"+pos+"/numberOfLapses");
        }
        double stepDelay = Firebase.getFloat("/devices/SmartPlatform00001TX/program/steps/"+pos+"/delay");
        while (Firebase.failed()) { 
           stepDelay = Firebase.getFloat("/devices/SmartPlatform00001TX/program/steps/"+pos+"/delay");
        }
  
        Step currentStep(stepDirection, stepDistance, distanceUnits, numberOfLapses, stepDelay, delayUnits);
        program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
        while (Firebase.failed()) { 
          program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
        }
        if(program.compareTo("")) {
          changePosition(&x, &lx, &y, &ly, currentStep.getDirection(), currentStep.getRealDistance(), currentStep.getNumberOfLapses(), currentStep.getRealDelay());
        } else {
          stopCar(front_back);
          return;
        }
      }

       program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
       while (Firebase.failed()) { 
        program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
       }
       if(program.compareTo("")) {
        if(comeBack) {
          platformComeBack(x, lx, y, ly);

          program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
          while (Firebase.failed()) { 
           program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
          }
           
          if(program.compareTo("")) {
           Firebase.setString("/devices/SmartPlatform00001TX/cameBackFrom", program);
           while (Firebase.failed()) { 
            Firebase.setString("/devices/SmartPlatform00001TX/cameBackFrom", program);
           }
          } else {
           stopCar(front_back);
           return;
         }
        } else {
           Firebase.setBool("/devices/SmartPlatform00001TX/program/wait", true);
           while (Firebase.failed()) { 
            Firebase.setBool("/devices/SmartPlatform00001TX/program/wait", true);
           }
           
           Firebase.setBool("/devices/SmartPlatform00001TX/program/alertOn", true);
           while (Firebase.failed()) { 
            Firebase.setBool("/devices/SmartPlatform00001TX/program/alertOn", true);
           }
  
           bool wait = true;
           while(wait) {
            wait = Firebase.getBool("/devices/SmartPlatform00001TX/program/wait");
            while (Firebase.failed()) { 
               wait = Firebase.getBool("/devices/SmartPlatform00001TX/program/wait");
            }
            delay(1000);
           }
  
           bool come = Firebase.getBool("/devices/SmartPlatform00001TX/program/comeBack");
           while (Firebase.failed()) { 
               come = Firebase.getBool("/devices/SmartPlatform00001TX/program/comeBack");
           }
           if(come) {
              platformComeBack(x, lx, y, ly);
              program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
              while (Firebase.failed()) { 
               program = Firebase.getString("/devices/SmartPlatform00001TX/program/name");
              }
              if(program.compareTo("")) {
               Firebase.setString("/devices/SmartPlatform00001TX/cameBackFrom", program);
               while (Firebase.failed()) { 
                Firebase.setString("/devices/SmartPlatform00001TX/cameBackFrom", program);
               }
              } else {
                 stopCar(front_back);
                 return;
              }
           }  
        }
      } else {
         stopCar(front_back);
         return;
      }
      Firebase.remove("/devices/SmartPlatform00001TX/program");
    }    
}

void platformComeBack(double x, double lx, double y, double ly) {
  double oyTime = 0.0;
  double oxTime = 0.0;
  setCarSpeed(speed5);
  if (x == lx && ly < y && y > 0) {
    if (x == 0) {
      oyTime = 60000*(abs(y))/realSpeed;
    } else {
      oyTime = 60000*(abs(y)-24)/realSpeed;
      oxTime = 60000*(abs(x)-14)/realSpeed;
    }
    goBack();
    delay((long)oyTime);
    if (x != 0) {
      if (x < 0) {
         turnLeft();
      } else {
         turnRight();
      }
      delay(turnTime); 
      stopCar(front_back);
      goFront();
      delay((long)oxTime);
      if (x < 0) {
       turnLeft();
      } else {
         turnRight();
      }
      delay(turnTime);
      stopCar(front_back);
      goBack();
      delay(turnTimeLeft_Right);
      stopCar(front_back);
    } else {
        stopCar(front_back);
    }
  } else {
    if (x == lx && ly > y && y > 0) {
      oyTime = 60000*(abs(y)-24-carSize)/realSpeed;
      oxTime = 60000*(abs(x)-14)/realSpeed;
      goFront();
      delay((long)oyTime);
      if (x != 0) {
        if (x < 0) {
           turnLeft();
           delay(turnTime);
           stopCar(left_front);
        } else {
           turnRight();
           delay(turnTime);
           stopCar(right_front);
        }
        delay((long)oxTime);
        if (x < 0) {
           turnLeft();
        } else {
           turnRight();
        }
        delay(turnTime);
        stopCar(front_back);
        goBack();
        delay(turnTimeLeft_Right);
        stopCar(front_back);
      } else {
        turnLeft();
        delay(turnTime);
        stopCar(front_back);
        goBack();
        delay(turnTimeLeft_Right);
        delay(turnTimeFront_Back);
        stopCar(front_back);
        goFront();
        turnLeft();
        delay(turnTime);
        stopCar(front_back);
        goBack();
        delay(turnTimeLeft_Right);
        stopCar(front_back);
      }
    } else {
      if (x == lx && ly < y && y < 0) {
        if (x == 0) {
          oyTime = 60000*(abs(y))/realSpeed;
        } else {
          oyTime = 60000*(abs(y)-24-carSize)/realSpeed;
          oxTime = 60000*(abs(x)-14)/realSpeed;
        }
        goFront();
        delay((long)oyTime);
        stopCar(front_back);
        if (x != 0) {
          goFront();
          if (x < 0) {
             turnRight();
             delay(turnTime);
             stopCar(right_front);
          } else {
             turnLeft();
             delay(turnTime);
             stopCar(left_front);
          }
          delay((long)oxTime);
          if (x < 0) {
           turnLeft();
          } else {
             turnRight();
          }
          delay(turnTime);
          stopCar(front_back);
          goBack();
          delay(turnTimeLeft_Right);
          stopCar(front_back);
        } else {
          stopCar(front_back);
        }
      } else {
        if (x == lx && ly > y && y < 0) {
          oyTime = 60000*(abs(y)-24)/realSpeed;
          oxTime = 60000*(abs(x)-14)/realSpeed;
          goBack();
          delay((long)oyTime);
          if (x != 0) {
            if (x < 0) {
               turnRight();
            } else {
               turnLeft();
            }
            delay(turnTime);
            stopCar(front_back);
            goFront();
            delay((long)oxTime);            
            if (x < 0) {
             turnLeft();
            } else {
               turnRight();
            }
            delay(turnTime);
            stopCar(front_back);
            goBack();
            delay(turnTimeLeft_Right);
            stopCar(front_back);
          } else {
              turnRight();
              delay(turnTime);
              stopCar(front_back);
              goFront();
              delay(turnTimeLeft_Right);
              turnLeft();
              delay(turnTime);
              stopCar(front_back);
              goBack();
              delay(turnTimeLeft_Right);
              stopCar(front_back);
          }
        } else {
            if (lx < x && x < 0) {
              oyTime = 60000*(abs(y))/realSpeed;
              oxTime = 60000*(abs(x)-24-carSize)/realSpeed;
              goFront();
              delay((long)oxTime);
              turnLeft();
              delay(turnTime);
              stopCar(front_back);
              goBack();
              delay(turnTimeLeft_Right);
              stopCar(front_back);
              if (y != 0) {
                if (y > 0) {
                  goBack();
                } else {
                  goFront();        
                }
                delay((long)oyTime);
                stopCar(front_back);
              }
            } else {
              if (lx < x && x > 0) {
                oyTime = 60000*(abs(y))/realSpeed;
                oxTime = 60000*(abs(x)-24)/realSpeed;
                goBack();
                delay((long)oxTime);
                turnRight();
                delay(turnTime);
                stopCar(front_back);
                goFront();
                delay(turnTimeLeft_Right);
                stopCar(front_back);
                if (y != 0) {
                  if (y > 0) {
                    goBack();
                  } else {
                    goFront();             
                  }
                  delay((long)oyTime);
                  stopCar(front_back);
                }
              } else {
                if (lx > x && x < 0) {
                  oyTime = 60000*(abs(y))/realSpeed;
                  oxTime = 60000*(abs(x)-24)/realSpeed;
                  goBack();
                  delay((long)oxTime);
                  turnLeft();
                  delay(turnTime);
                  stopCar(front_back);
                  goFront();
                  delay(turnTimeLeft_Right);
                  if (y != 0) {
                    if (y > 0) {
                      goBack();
                    } else {
                      goFront();
                    }
                    delay((long)oyTime);
                    stopCar(front_back);
                  }
                } else {
                  if (lx > x && x > 0) {
                    oyTime = 60000*(abs(y))/realSpeed;
                    oxTime = 60000*(abs(x)-24-carSize)/realSpeed;
                    goFront();
                    delay((long)oxTime);
                    turnRight();
                    delay(turnTime);
                    stopCar(front_back);
                    goBack();
                    delay(turnTimeLeft_Right);
                    if (y != 0) {
                      if (y > 0) {
                        goBack();
                      } else {
                        goFront();
                      }
                      delay((long)oyTime);
                      stopCar(front_back);
                    }
                  }      
                }
              }
            }
          }
        }
      }
    }
}

void changePosition(double *x, double *lx, double *y, double *ly, byte stepDirection, double stepDistance, int numberOfLapses, double stepDelay) {
 double lapseDistance = stepDistance/numberOfLapses;
 double timePerLapse = 60000*lapseDistance/realSpeed;
 setCarSpeed(speed5);
 if (*lx == *x && *ly <= *y) {
    if (stepDirection == 1) {
      *lx = *x;
      *ly = *y;
      *y = *y + stepDistance;
      for (int j = 1; j <= numberOfLapses; j++) {
        goFront();
        delay((long)timePerLapse);
        stopCar(front_back);
        delay((long)stepDelay);  
      }
    } else {
      if (stepDirection == 2) {
        *lx = *x;
        *ly = *y;
        *y = *y - stepDistance;
        for (int j = 1; j <= numberOfLapses; j++) {
          goBack();
          delay((long)timePerLapse);
          stopCar(front_back);
          delay((long)stepDelay);  
        }
       } else {
         if (stepDirection == 3) {
           *ly = *y;
           *lx = *x;
           *x = *x - stepDistance;
           goBack(); 
           delay(turnTimeFront_Back) ;
           stopCar(front_back);
           goFront();
           turnLeft(); 
           delay(turnTime); 
           stopCar(front_back);
           goBack();
           delay(turnTimeLeft_Right);
           for (int j = 1; j <= numberOfLapses; j++) {
              goFront();
              delay((long)timePerLapse);
              stopCar(front_back);
              delay((long)stepDelay);  
           }
         } else {
           if (stepDirection == 4) {
             *ly = *y;
             *lx = *x;
             *x = *x + stepDistance;
             goBack(); //go back with full speed 24 cm
             delay(turnTimeFront_Back) ;
             stopCar(front_back);
             goFront();
             turnRight(); // make a 90 degree turn with full speed
             delay(turnTime); 
             stopCar(front_back);
             goBack();
             delay(turnTimeLeft_Right);
             stopCar(front_back);
             for (int j = 1; j <= numberOfLapses; j++) {
                goFront();
                delay((long)timePerLapse);
                stopCar(front_back);
                delay((long)stepDelay);  
             }
           }
         }
       }
    }
  } else {
    if (*lx == *x && *ly >= *y) {
      if (stepDirection == 1) {
        *lx = *x;
        *ly = *y;
        *y = *y - stepDistance;
        for (int j = 1; j <= numberOfLapses; j++) {
          goFront();
          delay((long)timePerLapse);
          stopCar(front_back);
          delay((long)stepDelay);  
        }
      } else {
        if (stepDirection == 2) {
          *lx = *x;
          *ly = *y;
          *y = *y + stepDistance;
          for (int j = 1; j <= numberOfLapses; j++) {
            goBack();
            delay((long)timePerLapse);
            stopCar(front_back);
            delay((long)stepDelay);  
          }
         } else {
           if (stepDirection == 3) {
             *ly = *y;
             *lx = *x;
             *x = *x + stepDistance;
             goBack(); //go back with full speed 24 cm
             delay(turnTimeFront_Back) ;
             stopCar(front_back);
             goFront();
             turnLeft(); // make a 90 degree turn with full speed
             delay(turnTime); 
             stopCar(front_back);
             goBack();
             delay(turnTimeLeft_Right);
             stopCar(front_back);
             for (int j = 1; j <= numberOfLapses; j++) {
                goFront();
                delay((long)timePerLapse);
                stopCar(front_back);
                delay((long)stepDelay);  
             }
           } else {
             if (stepDirection == 4) {
               *ly = *y;
               *lx = *x;
               *x = *x - stepDistance;
               goBack(); //go back with full speed 24 cm
               delay(turnTimeFront_Back) ;
               stopCar(front_back);
               goFront();
               turnRight(); // make a 90 degree turn with full speed
               delay(turnTime); 
               stopCar(front_back);
               goBack();
               delay(turnTimeLeft_Right);
               stopCar(front_back);
               for (int j = 1; j <= numberOfLapses; j++) {
                  goFront();
                  delay((long)timePerLapse);
                  stopCar(front_back);
                  delay((long)stepDelay);  
               }
             }
           }
         }
      }
     } else {
        if (*lx > *x) {
          if (stepDirection == 1) {
            *lx = *x;
            *ly = *y;
            *x = *x - stepDistance;
            for (int j = 1; j <= numberOfLapses; j++) {
              goFront();
              delay((long)timePerLapse);
              stopCar(front_back);
              delay((long)stepDelay);  
            }
          } else {
            if (stepDirection == 2) {
              *lx = *x;
              *ly = *y;
              *x = *x + stepDistance;
              for (int j = 1; j <= numberOfLapses; j++) {
                goBack();
                delay((long)timePerLapse);
                stopCar(front_back);
                delay((long)stepDelay);  
              }
            } else {
              if (stepDirection == 3) {
                *ly = *y;
                *lx = *x;
                *y = *y - stepDistance;
                goBack(); //go back with full speed 24 cm
                delay(turnTimeFront_Back) ;
                stopCar(front_back);
                goFront();
                turnLeft(); // make a 90 degree turn with full speed
                delay(turnTime); 
                stopCar(front_back);
                goBack();
                delay(turnTimeLeft_Right);
                stopCar(front_back);
                for (int j = 1; j <= numberOfLapses; j++) {
                  goFront();
                  delay((long)timePerLapse);
                  stopCar(front_back);
                  delay((long)stepDelay);  
                }
              } else {
                if (stepDirection == 4) {
                  *ly = *y;
                  *lx = *x;
                  *y = *y + stepDistance;
                  goBack(); //go back with full speed 24 cm
                  delay(turnTimeFront_Back) ;
                  stopCar(front_back);
                  goFront();
                  turnRight(); // make a 90 degree turn with full speed
                  delay(turnTime); 
                  stopCar(front_back);
                  goBack();
                  delay(turnTimeLeft_Right);
                  stopCar(front_back);
                  for (int j = 1; j <= numberOfLapses; j++) {
                    goFront();
                    delay((long)timePerLapse);
                    stopCar(front_back);
                    delay((long)stepDelay);  
                  }
                }
              }
            }
          }
        } else {
          if (stepDirection == 1) {
            *lx = *x;
            *ly = *y;
            *x = *x + stepDistance;
            for (int j = 1; j <= numberOfLapses; j++) {
              goFront();
              delay((long)timePerLapse);
              stopCar(front_back);
              delay((long)stepDelay);  
            }
          } else {
            if (stepDirection == 2) {
              *lx = *x;
              *ly = *y;
              *x = *x - stepDistance;
              for (int j = 1; j <= numberOfLapses; j++) {
                goBack();
                delay((long)timePerLapse);
                stopCar(front_back);
                delay((long)stepDelay);  
              }
            } else {
              if (stepDirection == 3) {
                  *ly = *y;
                  *lx = *x;
                  *y = *y - stepDistance;
                  goBack(); //go back with full speed 24 cm
                  delay(turnTimeFront_Back) ;
                  stopCar(front_back);
                  goFront();
                  turnLeft(); // make a 90 degree turn with full speed
                  delay(turnTime); 
                  stopCar(front_back);
                  goBack();
                  delay(turnTimeLeft_Right);
                  stopCar(front_back);
                  for (int j = 1; j <= numberOfLapses; j++) {
                    goFront();
                    delay((long)timePerLapse);
                    stopCar(front_back);
                    delay((long)stepDelay);  
                 }
              } else {
                if (stepDirection == 4) {
                  *ly = *y;
                  *lx = *x;
                  *y = *y - stepDistance;
                  goBack(); //go back with full speed 24 cm
                  delay(turnTimeFront_Back) ;
                  stopCar(front_back);
                  goFront();
                  turnRight(); // make a 90 degree turn with full speed
                  delay(turnTime); 
                  stopCar(front_back);
                  goBack();
                  delay(turnTimeLeft_Right);
                  stopCar(front_back);
                  for (int j = 1; j <= numberOfLapses; j++) {
                    goFront();
                    delay((long)timePerLapse);
                    stopCar(front_back);
                    delay((long)stepDelay);  
                  }
                }
              }
            }
          }
        }
      }
    }
}

void goFront() {
  digitalWrite(leftFront, HIGH);
  digitalWrite(leftBack, LOW);
  digitalWrite(rightFront, HIGH);
  digitalWrite(rightBack, LOW);
}

void goBack() {
  digitalWrite(leftFront, LOW);
  digitalWrite(leftBack, HIGH);
  digitalWrite(rightFront, LOW);
  digitalWrite(rightBack, HIGH);
}

void turnLeft() {
  analogWrite(rightEn, speed5);
  digitalWrite(leftFront, LOW);
  digitalWrite(leftBack, LOW);
}

void turnRight() {
  analogWrite(leftEn, speed5);
  digitalWrite(rightFront, LOW);
  digitalWrite(rightBack, LOW);
}

void setCarSpeed(int speeds) {
   carSpeed = speeds;
   analogWrite(rightEn, speeds);
   analogWrite(leftEn, speeds);
}

void stopCar(int type) {
  switch(type) {
    case 1 :  digitalWrite(rightFront, HIGH);  // pornesc doar roțile din stânga și merg înainte
              digitalWrite(rightBack, LOW);
              break;
    case 2 :  digitalWrite(rightFront, LOW);   // pornesc doar roțile din stânga și merg înapoi
              digitalWrite(rightBack, HIGH);
              break;
    case 3 :  digitalWrite(leftFront, HIGH);   // pornesc doar roțile din dreapta și merg înainte
              digitalWrite(leftBack, LOW);
              break;
    case 4 :  digitalWrite(leftFront, LOW);    // pornesc doar roțile din dreapta și merg înapoi
              digitalWrite(leftBack, HIGH);
              break;
    default:  digitalWrite(leftFront, LOW);     // opresc toate roțile
              digitalWrite(leftBack, LOW);
              digitalWrite(rightFront, LOW);
              digitalWrite(rightBack, LOW); 
              break;
  }
}

void wifiConnect() {
  WiFi.begin(My_SSID, My_PASS); // Începe conexiunea
  while (WiFi.status() != WL_CONNECTED) {
      delay(1000);  // dacă încă nu s-a conectat așteaptă o secundă și reîncearcă
  }
  
  Firebase.begin(My_FIREBASE_HOST,My_FIREBASE_AUTH);  // Începe conexiunea cu Firebase

  StaticJsonBuffer<200> jsonBuffer; // Crează un buffer static
  JsonObject& root = jsonBuffer.createObject(); // Stochează într-un nou obiect Json
  root["blocked"] = ""; // adaugă noduri
  root["distanceFront"] = 1000000.0;
  root["distanceBack"] = 1000000.0;
  root["state"] = "R-Stop";
  JsonObject& defaultSettings = root.createNestedObject("default"); // crează un nod cu alte subnoduri
  defaultSettings["ssid"] = My_SSID;  // adaugă subnoduri
  defaultSettings["password"] = My_PASS;
  Firebase.set("/devices/SmartPlatform00001TX", root);
  while (Firebase.failed()) {
      Firebase.set("/devices/SmartPlatform00001TX", root);
  }
}

void getDistances(int echo, String what) {
    Firebase.setString("/devices/SmartPlatform00001TX/blocked", "");
    digitalWrite(trig, LOW);
    delayMicroseconds(2);
    digitalWrite(trig, HIGH);
    delayMicroseconds(10);
    digitalWrite(trig, LOW);
   
    // Măsoară răspunsul de la senzori
   
    float duration = pulseIn(echo, HIGH);
    pulseIn(echo, LOW);
    // Determină distanța pe baza duratei
    // Folosește 343 metri pe secundă ca viteză a sunetului
        
    float distance = (duration / 2) * 0.0343;
    
    if (!what.compareTo("FRONT")) {
        distancesBackCounter = 0;
        Firebase.setFloat("/devices/SmartPlatform00001TX/distanceFront", distance);
         while (Firebase.failed()) {
            Firebase.setFloat("/devices/SmartPlatform00001TX/distanceFront",distance);
         }

        distancesFront[distancesFrontCounter] = distance;
        if(distancesFrontCounter == maxCounterStepDistance) {
            float sumOfDistances = 0;
            for(int i = 0; i < maxCounterStepDistance; i++) {
                sumOfDistances += distancesFront[i];
            }
            if(sumOfDistances/maxCounterStepDistance < 30) {
                Firebase.setString("/devices/SmartPlatform00001TX/blocked", "FRONT");
                Firebase.setString("/devices/SmartPlatform00001TX/state", "R_Stop");
				isBlockedFront = true;
				stopCar(front_back);
            } else {
				Firebase.setString("/devices/SmartPlatform00001TX/blocked", "");
				isBlockedFront = false;
			}
            distancesFrontCounter = 0;
        } else {
          distancesFrontCounter++;
        }
    } else if (!what.compareTo("BACK")) {
        distancesFrontCounter = 0;
        Firebase.setFloat("/devices/SmartPlatform00001TX/distanceBack", distance);
          while (Firebase.failed()) {
            Firebase.setFloat("/devices/SmartPlatform00001TX/distanceBack", distance);
          }

        distancesBack[distancesBackCounter] = distance;
        if(distancesBackCounter == maxCounterStepDistance) {
            float sumOfDistances = 0;
            for(int i = 0; i < maxCounterStepDistance; i++) {
                sumOfDistances += distancesBack[i];
            }
            if(sumOfDistances/maxCounterStepDistance < 30) {
                Firebase.setString("/devices/SmartPlatform00001TX/blocked", "BACK");
				Firebase.setString("/devices/SmartPlatform00001TX/state", "R_Stop");
				isBlockedBack = true;
				stopCar(front_back);
            } else {
				Firebase.setString("/devices/SmartPlatform00001TX/blocked", "");
				isBlockedBack = false;
			}
            distancesBackCounter = 0;
        } else {
          distancesBackCounter++;
        }
    }     
}
