// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 3
 * Name: Amy Booth
 * Username: boothamy
 * ID: 300653766
 */

import ecs100.*;

import java.util.*;
import java.io.*;

/**
 * Simulation of a Hospital ER
 * <p>
 * The hospital has a collection of Departments, including the ER department, each of which has
 * and a treatment room.
 * <p>
 * When patients arrive at the hospital, they are immediately assessed by the
 * triage team who determine the priority of the patient and (unrealistically) a sequence of treatments
 * that the patient will need.
 * <p>
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 * <p>
 * READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl {

    /**
     * The map of the departments.
     * The names of the departments should be "ER", "X-Ray", "MRI", "UltraSound" and "Surgery"
     * The maximum patients should be 8 for "ER", 3 for "X-Ray", 1 for "MRI", 2 for "UltraSound" and
     * 3 for "Surgery"
     */

    private Map<String, Department> departments = new HashMap<String, Department>();

    // Copy the code from HospitalERCore and then modify/extend to handle multiple departments

    // fields for the statistics
    private int totalTreated = 0;
    private double totalWaitTime = 0;
    private double totalTreatTime = 0;

    private int prio1Treated = 0;
    private double prio1WaitTime = 0;
    private double prio1TreatTime = 0;

    private int prio2Treated = 0;
    private double prio2WaitTime = 0;
    private double prio2TreatTime = 0;

    private int prio3Treated = 0;
    private double prio3WaitTime = 0;
    private double prio3TreatTime = 0;

    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick


    /**
     * stop any running simulation
     * Define the departments available and put them in the map of departments.
     * Each department needs to have a name and a maximum number of patients that
     * it can be treating at the same time.
     * reset the statistics
     */
    public void reset(boolean usePriorityQueues) {
        running = false;
        UI.sleep(2 * delay);  // to make sure that any running simulation has stopped

        time = 0;           // set the "tick" to zero.

        // set up departments
        Department er = new Department("ER", 8, usePriorityQueues);
        departments.put("ER", er);
        Department xRay = new Department("X-Ray", 3, usePriorityQueues);
        departments.put("X-Ray", xRay);
        Department mri = new Department("MRI", 1, usePriorityQueues);
        departments.put("MRI", mri);
        Department ultra = new Department("UltraSound", 2, usePriorityQueues);
        departments.put("UltraSound", ultra);
        Department surg = new Department("Surgery", 3, usePriorityQueues);
        departments.put("Surgery", surg);

        // reset statistics
        totalTreated = 0;
        totalWaitTime = 0;
        totalTreatTime = 0;
        prio1Treated = 0;
        prio1WaitTime = 0;
        prio1TreatTime = 0;
        prio2Treated = 0;
        prio2WaitTime = 0;
        prio2TreatTime = 0;
        prio3Treated = 0;
        prio3WaitTime = 0;
        prio3TreatTime = 0;

        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */
    public void run() {
        if (running) {
            return;
        } // don't start simulation if already running one!
        running = true;
        while (running) {
            time++; // Advance the time by one "tick"

            // Find all patients in treatment room who've finished current treatment & discharge them
            for (Department dept : departments.values()) {
                List<Patient> discharged = dept.dischargePatients();
                for (Patient p : discharged) {
                    p.removeCurrentTreatment();
                    // move them to their next treatment or discharge completely if finished
                    if (p.allTreatmentsCompleted()) {
                        discharge(p);
                    } else {
                        Department newDept = departments.get(p.getCurrentDepartment());
                        newDept.addPatient(p);
                    }
                }
                dept.processTick(); // every patient in dept processes 1 tick
                dept.tryTreat(); // tries to move patients from the waiting room to the treatment room
            }

            // Gets any new patient that has arrived and adds them to the waiting room
            Patient newPatient = PatientGenerator.getNextPatient(time);
            if (newPatient != null) {
                UI.println(time + ": Arrived: " + newPatient);
                Department newDept = departments.get(newPatient.getCurrentDepartment());
                newDept.addPatient(newPatient);
            }
            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();

    }


    /**
     * Report that a patient has been discharged, along with any
     * useful statistics about the patient
     */
    public void discharge(Patient p) {
        double wait = p.getTotalWaitingTime();
        double treat = p.getTotalTreatmentTime();
        //update total stats
        totalWaitTime += wait;
        totalTreatTime += treat;
        totalTreated++;
        //update prio stats
        if (p.getPriority() == 1) {
            prio1WaitTime += wait;
            prio1TreatTime += treat;
            prio1Treated++;
        } else if (p.getPriority() == 2) {
            prio2WaitTime += wait;
            prio2TreatTime += treat;
            prio2Treated++;
        } else if (p.getPriority() == 3) {
            prio3WaitTime += wait;
            prio3TreatTime += treat;
            prio3Treated++;
        }
        UI.println(time + ": Discharged: " + p);
    }

    /**
     * Report summary statistics about the simulation
     */
    public void reportStatistics() {
        //stats for total patients
        double avgWaitTime = totalWaitTime / totalTreated;
        double avgTreatTime = totalTreatTime / totalTreated;
        UI.printf("Processed %d patients with average waiting time of %.2f minutes \n" +
                        "and average treatment time of %.2f minutes\n",
                totalTreated, avgWaitTime, avgTreatTime);
        //stats for prio 1 patients
        double avgPrio1Wait = prio1WaitTime / prio1Treated;
        double avgPrio1Treat = prio1TreatTime / prio1Treated;
        UI.printf("Processed %d priority 1 patients with average waiting time of %.2f minutes \n"
                + "and average treatment time of %.2f minutes\n", prio1Treated, avgPrio1Wait, avgPrio1Treat);
        //stats for prio 2 patients
        double avgPrio2Wait = prio2WaitTime / prio2Treated;
        double avgPrio2Treat = prio2TreatTime / prio2Treated;
        UI.printf("Processed %d priority 2 patients with average waiting time of %.2f minutes \n"
                + "and average treatment time of %.2f minutes\n", prio2Treated, avgPrio2Wait, avgPrio2Treat);
        //stats for prio 3 patients
        double avgPrio3Wait = prio3WaitTime / prio3Treated;
        double avgPrio3Treat = prio3TreatTime / prio3Treated;
        UI.printf("Processed %d priority 3 patients with average waiting time of %.2f minutes \n"
                + "and average treatment time of %.2f minutes\n", prio3Treated, avgPrio3Wait, avgPrio3Treat);
    }


    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     * Note the dimensions of sliders may be adversely affected if the user's screen or system app scale is too small.
     */
    public void setupGUI() {
        UI.addButton("Reset (Queue)", () -> {
            this.reset(false);
        });
        UI.addButton("Reset (Pri Queue)", () -> {
            this.reset(true);
        });
        UI.addButton("Start", () -> {
            if (!running) {
                run();
            }
        });   //don't start if already running!
        UI.addButton("Pause & Report", () -> {
            running = false;
        });
        UI.addSlider("Speed", 1, 400, (401 - delay),
                (double val) -> {
                    delay = (int) (401 - val);
                });
        UI.addSlider("Av arrival interval", 1, 50, PatientGenerator.getArrivalInterval(),
                PatientGenerator::setArrivalInterval);
        //priority probability adjustments
        UI.addSlider("Prob of Pri 1", 1, 100, PatientGenerator.getProbPri1(),
                PatientGenerator::setProbPri1);
        UI.addSlider("Prob of Pri 2", 1, 100, PatientGenerator.getProbPri2(),
                PatientGenerator::setProbPri2);
        //treatment probability adjustments
        UI.addSlider("Prob of MRI", 1, 100, PatientGenerator.getProbMRI(),
                PatientGenerator::setProbMRI);
        UI.addSlider("Prob of Surgery", 1, 100, PatientGenerator.getProbSurgery(),
                PatientGenerator::setProbSurgery);
        UI.addSlider("Prob of X-Ray", 1, 100, PatientGenerator.getProbXRay(),
                PatientGenerator::setProbXRay);
        UI.addSlider("Prob of UltraSound", 1, 100, PatientGenerator.getProbUltraSound(),
                PatientGenerator::setProbUltraSound);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000, 600);
        UI.setDivider(0.5);
    }

    /**
     * Redraws all the departments
     */
    public void redraw() {
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0, 32, 400, 32);
        double y = 80;
        for (Department dept : departments.values()) {
            dept.redraw(y);
            UI.drawLine(0, y + 2, 400, y + 2);
            y += 50;
        }
    }

    /**
     * Construct a new HospitalER object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments) {
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }


}
