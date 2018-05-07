//Sam Carrillo
//3.20.18
//CSC318
//Hospital Simulation

/*
    This is a simulation program written in Java. It represents a Hospital.
    patients arrive at a hospital which has two
    service bays. If either bay is empty patients move immediately into that
    bay for treatment. If both bays are full, patients move into line. After
    patients have spent a random time in line, they may die and leave the line.
    The simulation has the following events:
    Event type      Description
    1               Patient arrives at the hospital
    2               Patient enters line at the hospital
    3               Patient enters service bay 1
    4               Patient enters service bay 2
    5               Patient leaves service bay 1
    6               Patient leaves service bay 2
    7               Patient dies and leaves waiting in line
    8               Simulation shut down
    The simulation has two main structures:
    The event queue, a set of ordered (time lower to higher) of linked list event objects
    The waiting line, a set of linked (by a linked list) objects representing the patients
 */

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        double Bigtime = 0.0;
        double Endtime = 100.0;
        double Eventtime;
        double deltime;
        double criticalTime = 0.0;
        Scanner scan = new Scanner(System.in);

        //Now create an Event Manager EventQueue and a Queue Simulation Manager QueueMgr
        GenericManager<Event> EventQueue = new GenericManager<Event>();
        //Now create a queue of patients waiting for service
        GenericManager<Patient> MyQueue = new GenericManager<Patient>();

        int criticalTimeID = 0;
        int illness = 0;
        int numInQueue;
        int doctorCount;
        int numInEvent;
        double heartPatientInLine = 0, gastroPatientInLine = 0, bleedingPatientInLine = 0;
        double heartPatientInServer = 0, gastroPatientInServer = 0, bleedingPatientInServer = 0;
        double heartPatientBalk = 0, gastroPatientBalk = 0, bleedingPatientBalk = 0;
        double totalHeart = 0, totalGastro = 0, totalBleeding = 0;
        double totalthrusystem = 0, totalthruline = 0, totalthrufac = 0;
        double totaltimeinline = 0.0, totaltimeinsystem = 0.0, totaltimeinservers = 0.0,
                totaltimeinservers2 = 0.0, ttil, ttis;
        double totaltimeinline2 = 0.0, totaltimeinsystem2 = 0.0;
        int MyCriticalPatient;
        int theybalked = 0;
        boolean busy1 = false;
        boolean busy2 = false;// ----- in the case of two servers
        Patient served1 = new Patient(-9, getDiagnosis());
        Patient served2 = new Patient(-9, getDiagnosis());// ----- in the case of two servers
        double deltimeserv;
        double timearrive = 0, deltimearv = 0;

        //this is a working patient object for those entering the queue
        Patient newPatient;
        //this is a working patient object for those exiting the queue
        Patient treatPatient = new Patient(-9, getDiagnosis());
        //Now create the last event of the simulation
        Event treatEvent = new Event(8, Endtime, 0);
        //Add the event in the queue
        EventQueue.addinorder(treatEvent);
        //Now add the arrival for the first patient
        deltimearv = TimetoArriveorServe(3);

        //this is the designation for how many doctors (1 or 2)
        System.out.println("How many doctors? 1 or 2");
        doctorCount = scan.nextInt();
        if (doctorCount > 2 || doctorCount < 1) {
            System.out.println("Not a valid number of doctors.");
            System.exit(0);//blow up if not valid
        }

        //The event time is current time plus the delta time
        Eventtime = Bigtime + deltimearv;
        System.out.println("The first patient arrives at: " + Eventtime);
        //Create the event for the first patient to arrive
        treatEvent = new Event(1, Eventtime, 0);
        //Add the event in the queue
        EventQueue.addinorder(treatEvent);

        //Now start processing the events
        //get the first event off the event queue
        treatEvent = EventQueue.getvalue(0);
        while (treatEvent.getEventType() != 8) {
            //this is a valid event. Get ready to update the time
            deltime = treatEvent.getTime() - Bigtime;

            //now update everybody with this deltime
            ttil = UpdatePatients(MyQueue, deltime);
            totaltimeinline += ttil;
            totaltimeinline2 += ttil * ttil;

            //now update everybody in the servers
            ttis = UpdateDoctors(served1, busy1, served2, busy2, deltime);// ----- in the case of two servers
            totaltimeinservers += ttis;
            totaltimeinservers2 += ttis * ttis;

            //increment the simulation clock time
            Bigtime = treatEvent.getTime();
            //get the number in the patient queue at this time
            numInQueue = MyQueue.getCount();

            if (doctorCount == 2) {
                //THIS IF SELECTS WHICH CASE-SWITCH STATEMENT TO GO INTO BASED ON
                //WHAT WAS INPUT FOR "DOCTOR COUNT" EARLIER ON LINE 80

                switch (treatEvent.getEventType()) {
                    case 1: //Patient arrives at the hospital
                        //server 1 is not busy and there is no one in the patient queue, put the patient in the server 1
                        if ((busy1 == false) && (numInQueue <= 0)) {

                            newPatient = new Patient(-9, getDiagnosis());

                            illness = newPatient.getDiagnosis();
                            //set the arrival time for this patient
                            newPatient.setTimeArrive(Bigtime);
                            //put this patient in server 1
                            busy1 = true;
                            served1 = newPatient;

                            if (illness == 1) {
                                //heart patients arrive at 2 per hour
                                deltimeserv = TimetoArriveorServe(2);
                            } else if (illness == 2) {
                                //gastro patients arrive at 4 per hour
                                deltimeserv = TimetoArriveorServe(4);
                            } else {
                                //bleeding patients arrive at 6 per hour
                                deltimeserv = TimetoArriveorServe(6);
                            }
                            Eventtime = deltimeserv + Bigtime;
                            //create the event for when the next person that arrives gets in line
                            treatEvent = new Event(5, Eventtime, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);

                            //the first server is busy but the second server is empty
                        } else if ((busy1 == true) && (busy2 == false) && (numInQueue <= 0)) {
                            //server 2 is open and there is no one in the patient queue, send the patient to server 2

                            newPatient = new Patient(-9, getDiagnosis());

                            illness = newPatient.getDiagnosis();
                            //set the arrival time for this patient
                            newPatient.setTimeArrive(Bigtime);
                            //put this patient in server 2
                            busy2 = true;
                            served2 = newPatient;

                            if (illness == 1) {
                                //heart patients arive at a rate of 2/hr
                                deltimeserv = TimetoArriveorServe(2);
                            } else if (illness == 2) {
                                //gastro patients arrive at a rate of 4/hr
                                deltimeserv = TimetoArriveorServe(4);
                            } else {
                                //bleeding patients arrive at a rate of 6/hr
                                deltimeserv = TimetoArriveorServe(6);
                            }
                            Eventtime = deltimeserv + Bigtime;
                            //create the event for when the next person that arrives gets in line
                            treatEvent = new Event(6, Eventtime, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);

                        } else if ((busy1 == true) && (busy2 == true)) {
                            //both servers are busy put the patient in line

                            //Give the patient an id for critical time for death
                            criticalTimeID++;
                            newPatient = new Patient(criticalTimeID, getDiagnosis());
                            //Set the arrival of the new patient
                            newPatient.setTimeArrive(Bigtime);

                            if (illness == 1) {
                                //Add at front to prioritize heart patient
                                MyQueue.addatfront(newPatient);
                                //Set the critical time for the heart patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            } else if (illness == 2) {
                                //Add the gastro patient to the end of the line
                                MyQueue.addatend(newPatient);
                                //Set the critical time for the gastro patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            } else {
                                //Add the bleeding patient to the end of the line
                                MyQueue.addatend(newPatient);
                                //Set the critical time for the bleeding patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            }
                            //create the event
                            treatEvent = new Event(7, criticalTime, criticalTimeID);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        }
                        //generate the event for the next patient to arrive
                        deltimearv = TimetoArriveorServe(3);
                        //the event time is current time plus the delta time
                        Eventtime = Bigtime + deltimearv;

                        //create the event
                        treatEvent = new Event(1, Eventtime, 0);
                        //put this event in the event queue
                        EventQueue.addinorder(treatEvent);
                        break;

                    case 2:
                        System.out.println("This is event 2, we have incorporated it in the"
                                + "arrival event if we are here we are in trouble.");
                        break;
                    case 3: //patient enters service bay 1
                        numInQueue = MyQueue.getCount();

                        //The first server is full so get in line
                        if ((busy1 == false) && (numInQueue > 0)) {

                            treatPatient = MyQueue.getvalue(0);
                            //get the patient critical event
                            MyCriticalPatient = treatPatient.getCriticalTime();
                            illness = treatPatient.getDiagnosis();
                            //purge the event from the event queue
                            PurgeEvent(EventQueue, MyCriticalPatient);

                            totalthruline++;
                            MyQueue.removeIt(0);
                            //put this patient in server 1
                            busy1 = true;
                            served1 = treatPatient;

                            if (illness == 1) {
                                //increment heart patients that go through the line
                                deltimeserv = TimetoArriveorServe(2);
                                heartPatientInLine++;
                            } else if (illness == 2) {
                                //increment gastro patients that go through the line
                                deltimeserv = TimetoArriveorServe(4);
                                gastroPatientInLine++;
                            } else {
                                //increment bleeding patients that go through the line
                                deltimeserv = TimetoArriveorServe(6);
                                bleedingPatientInLine++;
                            }
                            Eventtime = deltimeserv + Bigtime;
                            //crate the next event
                            treatEvent = new Event(5, Eventtime, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        } else {
                            //either we are busy and have had an event collision or there is no one in the line
                            System.out.println("In event 3 patients enters service bay 1 unable to process event.");
                        }
                        break;
                    case 4://patient enters service bay 2
                        numInQueue = MyQueue.getCount();
                        if ((busy2 == false) && (numInQueue > 0)) {
                            //the patient can enter bay 2 get the patient from the front of the line
                            treatPatient = MyQueue.getvalue(0);
                            //get the patient critical event
                            MyCriticalPatient = treatPatient.getCriticalTime();
                            //purge the event from the event queue
                            PurgeEvent(EventQueue, MyCriticalPatient);

                            illness = treatPatient.getDiagnosis();
                            totalthruline++;
                            MyQueue.removeIt(0);
                            //put this patient in server 2
                            busy2 = true;
                            served2 = treatPatient;

                            if (illness == 1) {
                                //increment heart patients that go through the line
                                deltimeserv = TimetoArriveorServe(2);
                                heartPatientInLine++;
                            } else if (illness == 2) {
                                //increment gastro patients that go through the line
                                deltimeserv = TimetoArriveorServe(4);
                                gastroPatientInLine++;
                            } else {
                                //increment bleeding patients that go through the line
                                deltimeserv = TimetoArriveorServe(4);
                                bleedingPatientInLine++;
                            }
                            Eventtime = deltimeserv + Bigtime;
                            treatEvent = new Event(6, Eventtime, -9);
                            //put this event in the event queue
                            numInEvent = EventQueue.addinorder(treatEvent);
                        } else {
                            //either we are busy and have had an event collusion or there is no one in the line
                            System.out.println("In event 4 patients enters service bay 2 unable to process event");
                        }
                        break;
                    case 5://patient leaves service bay 1
                        busy1 = false;
                        illness = treatPatient.getDiagnosis();
                        if (illness == 1) {
                            heartPatientInServer++;
                        } else if (illness == 2) {
                            gastroPatientInServer++;
                        } else {
                            bleedingPatientInServer++;
                        }
                        totalthrusystem++;
                        numInQueue = MyQueue.getCount();

                        if (numInQueue > 0) {
                            //there are patients in line, generate a 'patient enter service bay 1' now at Bigtime
                            //NOTE PROBLEMS WITH COLLISION EVENTS
                            treatEvent = new Event(3, Bigtime + 0.01, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        }
                        break;
                    case 6: //patient leaves service bay 2
                        busy2 = false;
                        illness = treatPatient.getDiagnosis();
                        if (illness == 1) {
                            //increment heart patients that go through the server
                            heartPatientInServer++;
                        } else if (illness == 2) {
                            //increment gastro patients that go through the server
                            gastroPatientInServer++;
                        } else {
                            //increment bleeding patients that go through the server
                            bleedingPatientInServer++;
                        }
                        totalthrusystem++;
                        numInQueue = MyQueue.getCount();

                        if (numInQueue > 0) {
                            //there are patients in line, generate a 'patient enter service bay 1' now at Bigtime
                            //NOTE PROBLEMS WITH COLLISION EVENTS
                            treatEvent = new Event(4, Bigtime + 0.01, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        }
                        break;
                    case 7://patient dies and leaves the waiting line
                        MyCriticalPatient = treatEvent.getPatient();
                        //patient dies
                        PatientDeath(MyQueue, MyCriticalPatient);
                        illness = treatPatient.getDiagnosis();

                        if (illness == 1) {
                            //increment heart patients that die
                            heartPatientInLine++;
                            heartPatientBalk++;
                        } else if (illness == 2) {
                            //increment gastro patients that die
                            gastroPatientInServer++;
                            gastroPatientBalk++;
                        } else {
                            //increment bleeding patients that die
                            bleedingPatientInServer++;
                            bleedingPatientBalk++;
                        }

                        theybalked++;
                        totalthruline++;
                        break;
                    case 8: //this is the shutdown event
                        System.out.println("This event is type 8 and we are in the switch statement TROUBLE!");
                        continue;

                    default:
                        System.out.println("This is a bad event type" + treatEvent.getEventType() + " at time"
                                + treatEvent.getTime());
                }//end of switch
                //this event is processed and deleted from the event queue
                EventQueue.removeIt(0);

                //now get the next event
                treatEvent = EventQueue.getvalue(0);
            } else {
                //THIS IS THE ELSE WHICH DECIDES THE CASE FOR ONLY 1 DOCTOR

                switch (treatEvent.getEventType()) {
                    case 1: //Patient arrives at the hospital
                        //server 1 is not busy and there is no one in the patient queue, put the patient in the server 1
                        if ((busy1 == false) && (numInQueue <= 0)) {

                            newPatient = new Patient(-9, getDiagnosis());

                            illness = newPatient.getDiagnosis();
                            //set the arrival time for this patient
                            newPatient.setTimeArrive(Bigtime);
                            //put this patient in server 1
                            busy1 = true;
                            served1 = newPatient;

                            if (illness == 1) {
                                //heart patients arrive at 2 per hour
                                deltimeserv = TimetoArriveorServe(2);
                            } else if (illness == 2) {
                                //gastro patients arrive at 4 per hour
                                deltimeserv = TimetoArriveorServe(4);
                            } else {
                                //bleeding patients arrive at 6 per hour
                                deltimeserv = TimetoArriveorServe(6);
                            }
                            Eventtime = deltimeserv + Bigtime;
                            //create the event for when the next person that arrives gets in line
                            treatEvent = new Event(5, Eventtime, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);

                            //the server is busy, get in line
                        } else if ((busy1 == true)) {
                            //Give the patient an id for critical time for death
                            criticalTimeID++;
                            newPatient = new Patient(criticalTimeID, getDiagnosis());
                            //Set the arrival of the new patient
                            newPatient.setTimeArrive(Bigtime);

                            if (illness == 1) {
                                //Add at front to prioritize heart patient
                                MyQueue.addatfront(newPatient);
                                //Set the critical time for the heart patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            } else if (illness == 2) {
                                //Add the gastro patient to the end of the line
                                MyQueue.addatend(newPatient);
                                //Set the critical time for the gastro patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            } else {
                                //Add the bleeding patient to the end of the line
                                MyQueue.addatend(newPatient);
                                //Set the critical time for the bleeding patient
                                criticalTime = Bigtime + (getCriticalPeriod(illness));
                            }

                            //create the event
                            treatEvent = new Event(7, criticalTime, criticalTimeID);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        }
                        //generate the event for the next patient to arrive
                        deltimearv = TimetoArriveorServe(3);
                        //the event time is current time plus the delta time
                        Eventtime = Bigtime + deltimearv;

                        //create the event
                        treatEvent = new Event(1, Eventtime, 0);
                        //put this event in the event queue
                        EventQueue.addinorder(treatEvent);
                        break;

                    case 2:
                        System.out.println("This is event 2, we have incorporated it in the"
                                + "arrival event if we are here we are in trouble.");
                        break;
                    case 3: //patient enters service bay 1
                        numInQueue = MyQueue.getCount();

                        //The first server is full so get in line
                        if ((busy1 == false) && (numInQueue > 0)) {

                            treatPatient = MyQueue.getvalue(0);
                            //get the patient critical event
                            MyCriticalPatient = treatPatient.getCriticalTime();
                            illness = treatPatient.getDiagnosis();
                            //purge the event from the event queue
                            PurgeEvent(EventQueue, MyCriticalPatient);

                            totalthruline++;
                            MyQueue.removeIt(0);
                            //put this patient in server 1
                            busy1 = true;
                            served1 = treatPatient;

                            if (illness == 1) {
                                //increment heart patients that go through the line
                                deltimeserv = TimetoArriveorServe(2);
                                heartPatientInLine++;
                            } else if (illness == 2) {
                                //increment gastro patients that go through the line
                                deltimeserv = TimetoArriveorServe(4);
                                gastroPatientInLine++;
                            } else {
                                //increment bleeding patients that go through the line
                                deltimeserv = TimetoArriveorServe(6);
                                bleedingPatientInLine++;
                            }

                            Eventtime = deltimeserv + Bigtime;
                            //crate the next event
                            treatEvent = new Event(5, Eventtime, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        } else {
                            //either we are busy and have had an event collision or there is no one in the line
                            System.out.println("In event 3 patients enters service bay 1 unable to process event.");
                        }
                        break;
                    case 5://patient leaves service bay 1
                        busy1 = false;
                        illness = treatPatient.getDiagnosis();
                        if (illness == 1) {
                            heartPatientInServer++;
                        } else if (illness == 2) {
                            gastroPatientInServer++;
                        } else {
                            bleedingPatientInServer++;
                        }
                        totalthrusystem++;
                        numInQueue = MyQueue.getCount();

                        if (numInQueue > 0) {
                            //there are patients in line, generate a 'patient enter service bay 1' now at Bigtime
                            //NOTE PROBLEMS WITH COLLISION EVENTS
                            treatEvent = new Event(3, Bigtime + 0.01, -9);
                            //put this event in the event queue
                            EventQueue.addinorder(treatEvent);
                        }
                        break;
                    case 7://patient dies and leaves the waiting line
                        MyCriticalPatient = treatEvent.getPatient();
                        //patient dies
                        PatientDeath(MyQueue, MyCriticalPatient);
                        illness = treatPatient.getDiagnosis();

                        if (illness == 1) {
                            //increment heart patients that die
                            heartPatientInLine++;
                            heartPatientBalk++;
                        } else if (illness == 2) {
                            //increment gastro patients that die
                            gastroPatientInServer++;
                            gastroPatientBalk++;
                        } else {
                            //increment bleeding patients that die
                            bleedingPatientInServer++;
                            bleedingPatientBalk++;
                        }

                        theybalked++;
                        totalthruline++;
                        break;
                    case 8: //this is the shutdown event
                        System.out.println("This event is type 8 and we are in the switch statement TROUBLE!");
                        continue;

                    default:
                        System.out.println("This is a bad event type" + treatEvent.getEventType() + " at time"
                                + treatEvent.getTime());
                }//end of switch
                //this event is processed and deleted from the event queue
                EventQueue.removeIt(0);

                //now get the next event
                treatEvent = EventQueue.getvalue(0);
            }
        }//end of simulation loop

        totalthrufac = theybalked + totalthrusystem;
        totalBleeding = bleedingPatientBalk + bleedingPatientInServer;
        totalGastro = gastroPatientBalk + gastroPatientInServer;
        totalHeart = heartPatientBalk + heartPatientInServer;
        //Now for the statistics
        System.out.println();
        System.out.println("************************ Statistics ************************");
        //total # of patients serviced, by illness
        System.out.println("There were a total of " + totalHeart + " heart patients going through the hospital.");
        System.out.println("There were a total of " + totalBleeding + " bleeding patients going through the hospital.");
        System.out.println("There were a total of " + totalGastro + " gastro patients going through the hospital.");
        System.out.println();
        //total # of patients died, by illness
        System.out.println("There were a total of " + heartPatientBalk + " heart patients that died while in line.");
        System.out.println("There were a total of " + gastroPatientBalk + " gastro patients that died while in line.");
        System.out.println("There were a total of " + bleedingPatientBalk + " bleeding patients that died while in line.");
        System.out.println();
        //average time waited, by illness
        System.out.println("Heart patients spent an average time in line of " + totalHeart / totalthruline * 60 + " minutes.");
        System.out.println("Gastro patients spent an average time in line of " + totalGastro / totalthruline * 60 + " minutes.");
        System.out.println("Bleeding patients spent an average time in line of " + totalBleeding / totalthruline * 60 + " minutes.");
        System.out.println();

    }//end of main

    public static int getDiagnosis() {
        //the random process generator that decides the illness of any given patient
        int x;
        int diagnosis;
        x = (int) (Math.random() * 100);

        if (x <= 30) {
            //Heart patients @ 30
            diagnosis = 1;
        } else if (x <= 50) {
            //Gastro patients @ 50
            diagnosis = 2;
        } else {
            //Bleeding patients @ 50+
            diagnosis = 3;
        }
        return diagnosis;
    }

    public static double getCriticalPeriod(int x) {
        double u1 = Math.random();
        double u2 = Math.random();
        double v;
        double time;
        double stddev;
        double mean;

        //Using Box-Mueller:
        v = Math.sqrt(-2 * Math.log(u1) * Math.cos(2 * Math.PI * u2));

        if (x == 1) {
            stddev = 0.166667;
            mean = 0.5833333;
            time = v * stddev + mean;
            //time = mean;
        } else if (x == 2) {
            stddev = 0.5;
            mean = 1.33333333;
            time = v * stddev + mean;
            //time = mean;
        } else {
            stddev = 0.333333;
            mean = 1.0833333;
            time = v * stddev + mean;
            //time = mean;
        }
        return time;
    }

    public static void PatientDeath(GenericManager<Patient> patientLine, int criticalID) {
        //this function removes a dead patient from the queue line patientLine.
        //It traverses the line, finds the patient with balkId and removes them
        int i, numinline, criticalTimeID;
        Patient treatPatient = new Patient(-9, getDiagnosis());
        //prepare to traverse the customer line
        numinline = patientLine.getCount();
        treatPatient = patientLine.getvalue(0);
        criticalTimeID = treatPatient.getCriticalTime();
        i = 0;
        while ((criticalTimeID != criticalID) && (i <= numinline - 1)) {
            treatPatient = patientLine.getvalue(i);
            criticalTimeID = treatPatient.getCriticalTime();
            i++;
        }
        //removing customer i from the line
        if (i == 0) {
            //remove first customer in line
            patientLine.removeIt(0);
        } else if ((criticalTimeID == criticalID) && (i > 0)) patientLine.removeIt(i - 1);
        return;

    }

    public static void PurgeEvent(GenericManager<Event> EventQueue, int criticalID) {
        //this function removes a death event from the event queue
        //it traverses the event queue, finds the event with criticalID and removes it
        int i, numinqueue, EcriticalID;
        Event workevent = new Event(1, 1.0, 1);
        //prepare to traverse the event queue
        numinqueue = EventQueue.getCount();
        workevent = EventQueue.getvalue(0);
        EcriticalID = workevent.getPatient();
        i = 0;
        while ((EcriticalID != criticalID) && (i <= (numinqueue - 1))) {
            workevent = EventQueue.getvalue(i);
            EcriticalID = workevent.getPatient();
            i++;
        }
        //removing event from the event queue
        if (EcriticalID == criticalID)
            EventQueue.removeIt(i - 1);
        return;
    }

    public static double TimetoArriveorServe(double rate) {
        //this is the random process to determine the time to arrive or the service time
        //rate is the arrival or service rate.
        double deltime;
        double bigx;
        bigx = Math.random();
        if (bigx > 0.9) {
            bigx = Math.random();
        }
        //this is the lognormal approximation of poisson distribution
        deltime = -Math.log(1.0 - bigx) / rate;
        return deltime;
    }

    public static double UpdatePatients(GenericManager<Patient> patientLine, double deltime) {
        //this function adds up all the time spent for a patient in line for this deltime
        double linetime = 0.0;
        int patientinline;
        patientinline = patientLine.getCount();
        if (patientinline == 0) {
            return linetime;
        } else {
            return linetime = deltime * patientinline;
        }
    }

    public static double UpdateDoctors(Patient s1, boolean b1, Patient s2, boolean b2, double deltime) {
        //this function updates the time for patient in the servers
        double treatTime = 0.0;
        if (b1 && b2)
            return treatTime = 2 * deltime;
        else if (b1 || b2)
            treatTime = deltime;
        return treatTime;
    }
}

class Patient implements Comparable {
    /*
        This is the patient object class. It stores the time the patient gets
        in line, time  they get in servers, and their time in system. It also keeps
        an ID for the death event associated with this patient.
     */

    protected double timeInLine;
    protected double timeInServer;
    protected double timeInSystem;
    protected double timeArrive;
    protected int diagnosis;
    protected int mynum;
    protected int criticalTime;//this is the unique identifier of the death event

    //the patient constructor
    public Patient(int x, int y) {
        timeInLine = timeInServer = timeInSystem = 0;
        mynum = x;
        criticalTime = x;
        diagnosis = y;
    }

    //the comparable
    public int compareTo(Object o) {
        if (getTimeInLine() < ((Patient) o).getTimeInLine())
            return 1;
        else if (getTimeInLine() < ((Patient) o).getTimeInLine())
            return -1;
        else
            return 0;
    }

    //**** Getters and Setters for Customer *****
    public void setTimeArrive(double x) {
        timeArrive = x;
    }

    public double getTimeInLine() {
        return timeInLine;
    }

    public int getCriticalTime() {
        return criticalTime;
    }

    public int getDiagnosis() {
        return diagnosis;
    }
}

class Event implements Comparable {

    /*
        This is the event class. Events hold an event type, an event time
        and in the case of a death event, a pointer to the patient when the
        event is a death event.
     */

    protected double time; //this is the time of the event
    protected int MyCust; //if this is a death event, this unique id of the dying customer
    protected int eType; //this is the event type

    //the event constructor
    public Event(int etype, double etime, int patientDies) {
        eType = etype;
        time = etime;
        if (eType == 7) {
            //this is a death event
            MyCust = patientDies;
        } else {
            MyCust = -9;
        }
    }

    //the comparable
    public int compareTo(Object o) {
        if (getTime() > ((Event) o).getTime())
            return 1;
        else if (getTime() < ((Event) o).getTime())
            return -1;
        else
            return 0;
    }

    //***** the getters and setters ******
    public double getTime() {
        return time;
    }

    public int getEventType() {
        return eType;
    }

    public int getPatient() {
        return MyCust;
    }
}

class GenericManager<T extends Comparable> {
    protected ArrayList<T> mylist = new ArrayList<T>();
    protected int count;//the next available value in array myArray

    //the generic constructor
    public GenericManager() {
        count = 0;
    }

    public int addatend(T x) {
        //this method adds values at the end of myarray
        mylist.add(count++, x);
        return count;
    }

    public int getCount() {
        return count;
    }

    public int addinorder(T x) {
        //this places the objects from smaller to larger
        int i;
        if ((count == 0) || ((x.compareTo(mylist.get(0))) == -1 || (x.compareTo(mylist.get(0)) == 0))) {
            //this is less than or equal to the first entry
            mylist.add(0, x);
        } else if ((x.compareTo(mylist.get(count - 1)) == 1) || (x.compareTo(mylist.get(count - 1)) == 0)) {
            //x is greater than the last entry
            mylist.add(count, x);
        } else {
            //this object is greater than the first and less than the last
            i = 0;
            while ((i < count) && (x.compareTo(mylist.get(i)) == 1)) i++;
            mylist.add(i, x);
        }
        //add one to count
        count++;
        return count;
    }//end of add in order

    public int addatfront(T x) {
        //add this object at the front of the list
        mylist.add(0, x);
        count++;
        return count;
    }

    public T getvalue(int i) {
        //this gets values from myArray
        if (i < count)
            return mylist.get(i);
        else {
            return mylist.get(0);
        }
    }

    public void removeIt(int i) {
        //this removes the i'th value from the list
        if ((i >= 0) && (i <= count - 1)) {
            mylist.remove(i);
            count--;
        }
        return;
    }
}