package org.team4909.preseason2018.core;

import org.team4909.bionic.utils.drivetrain.BionicDriveOIConstants;
import org.team4909.bionic.utils.drivetrain.BionicDrivetrain;
import org.team4909.bionic.utils.drivetrain.BionicSensoredTankDrive;
import org.team4909.bionic.utils.oi.BionicAxisHandle;
import org.team4909.bionic.utils.setpoints.PIDConstants;
import org.team4909.bionic.utils.subsystems.Arduino;
import org.team4909.preseason2018.autonomous.AutonomousMap;
import org.team4909.preseason2018.subsystems.GearLoader;
import org.team4909.preseason2018.subsystems.Shooter;

import com.ctre.CANTalon;
import com.ctre.phoenix.MotorControl.SmartMotorController;
import com.ctre.phoenix.MotorControl.CAN.TalonSRX;
import com.ctre.phoenix.Sensors.PigeonImu;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.command.Scheduler;

public class Robot extends IterativeRobot {
	public static OI oi;
	public static AutonomousMap autonomousMap;
	
	public static BionicDrivetrain drivetrain;
	public static DoubleSolenoid flag;
	
	public static Arduino arduino;
	
	public static Shooter shooter;
	
	public static GearLoader gearLoader;
	
	private void subsystemInit() {
		drivetrain = new BionicDrivetrain(
			// TODO: Change Wheel Radius and Drivetrain Width
			new BionicSensoredTankDrive(
				new TalonSRX(3), new TalonSRX(1),
				SmartMotorController.FeedbackDevice.QuadEncoder,
				0f, 0f, new PigeonImu(1), 1f,
				new BionicDriveOIConstants(1.0, -1.0)
			),
			new BionicAxisHandle(oi.driverGamepad, oi.driverGamepadDriveSpeedAxis),
			new BionicAxisHandle(oi.driverGamepad, oi.driverGamepadDriveRotationAxis)
		);
		
		flag = new DoubleSolenoid(0, 1);

		arduino = new Arduino(4);
		
		shooter = new Shooter(new CANTalon(2), new PIDConstants(0.04, 0, 0), 0);
		
		gearLoader = new GearLoader(new Spark(0), new AnalogPotentiometer(0), new PIDConstants(0.04, 0, 0));
	}
	
	@Override
	public void robotInit() {
		// Initialize Operator Input
		oi = new OI();
		
		// Initialize Subsystems
		subsystemInit();

		// Initialize Commands
		oi.initButtons();
		autonomousMap = new AutonomousMap();
		
		// Initialize Dashboard
		DashboardConfig.init();
	}

	@Override public void autonomousInit() { 
		autonomousMap.startCommand();

		arduino.sendData(6);
	}
	
	@Override public void teleopInit() {
		autonomousMap.endCommand();
		
		arduino.sendData(6);	
	}
	
	@Override public void disabledInit() { arduino.sendData(7); }

	@Override public void disabledPeriodic() { Scheduler.getInstance().run(); }
	@Override public void autonomousPeriodic() { Scheduler.getInstance().run(); }
	@Override public void teleopPeriodic() { Scheduler.getInstance().run(); }
}
