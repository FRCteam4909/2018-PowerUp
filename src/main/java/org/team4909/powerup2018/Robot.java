package org.team4909.powerup2018;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import jaci.pathfinder.Waypoint;
import openrio.powerup.MatchData.GameFeature;
import org.team4909.bionicframework.hardware.core.Arduino;
import org.team4909.bionicframework.hardware.core.RoboRio;
import org.team4909.bionicframework.hardware.sensors.gyro.BionicNavX;
import org.team4909.bionicframework.hardware.motor.BionicSRX;
import org.team4909.bionicframework.hardware.motor.BionicSpark;
import org.team4909.bionicframework.hardware.motor.BionicVictorSP;
import org.team4909.bionicframework.hardware.motor.MotorSubsystem;
import org.team4909.bionicframework.hardware.pneumatics.BionicSingleSolenoid;
import org.team4909.bionicframework.interfaces.Commandable;
import org.team4909.bionicframework.subsystems.drive.BionicDrive;
import org.team4909.bionicframework.subsystems.drive.motion.DrivetrainConfig;
import org.team4909.bionicframework.operator.controllers.BionicF310;
import org.team4909.bionicframework.subsystems.elevator.ElevatorSubsystem;

public class Robot extends RoboRio {
    /* Subsystem Initialization */
    private static Arduino arduino;
    private static BionicDrive drivetrain;
    private static MotorSubsystem intake;
    private static ElevatorSubsystem elevator;
    private static MotorSubsystem winch;
    private static MotorSubsystem hookDeploy;

    /* OI Initialization */
    private static BionicF310 driverGamepad;
    private static BionicF310 manipulatorGamepad;

    /* Auto Commands */
    private static SendableChooser autoChooser;
    private static Command autoCommand;

    @Override
    public void robotInit() {
        driverGamepad = new BionicF310(0, 0.1, 0.8);
        manipulatorGamepad = new BionicF310(1, 0.1, 0.5);

        drivetrain = new BionicDrive(
                new BionicSRX(
                        2,false,
                        FeedbackDevice.QuadEncoder, false,
                        1.3,0,4,
                        1
                ),
                new BionicSRX(
                        4,true,
                        FeedbackDevice.QuadEncoder, false,
                        1.3,0,4,
                        4
                ),
                driverGamepad, BionicF310.LY, -1.0,
                driverGamepad, BionicF310.RX, -1.0,
                new DrivetrainConfig(
                        50, 0.5,120,
                        5.759,11.126,117.809,
                        10,2.599
                ),
                new BionicNavX(),
                new BionicSingleSolenoid(0)
        );
        driverGamepad.buttonPressed(BionicF310.LT, 0.1, drivetrain.invertDirection());
        driverGamepad.buttonPressed(BionicF310.RT, 0.1, drivetrain.changeGear());

        intake = new MotorSubsystem(
                new BionicSpark(0, true),
                new BionicSpark(1, false)
        );
        manipulatorGamepad.buttonHeld(BionicF310.LT, 0.1,intake.setPercentOutput(1.0));
        manipulatorGamepad.buttonHeld(BionicF310.RT, 0.1,intake.setPercentOutput(-1.0));
        manipulatorGamepad.buttonHeld(BionicF310.B, intake.setPercentOutput(-0.5));

        winch = new MotorSubsystem(
                new BionicVictorSP(2, true),
                new BionicVictorSP(3, false)
        );
        driverGamepad.buttonHeld(BionicF310.LB, winch.setPercentOutput(-0.5));
        driverGamepad.buttonHeld(BionicF310.RB, winch.setPercentOutput(1.0));

        hookDeploy = new MotorSubsystem(
                new BionicSpark(4,false)
        );

        elevator = new ElevatorSubsystem(
                new BionicSRX(
                        3, true,
                        FeedbackDevice.CTRE_MagEncoder_Relative, false,
                        1.0,0,0
                ),
                manipulatorGamepad, BionicF310.LY,-1,
                35250, 0
        );

        autoChooser = new SendableChooser();
        autoChooser.addDefault("Do Nothing", null);
        autoChooser.addObject("Break Baseline", drivetrain.driveWaypoints(new Waypoint[]{
                new Waypoint(1.59,0,0),
                new Waypoint(9,0,0)
        }));
        autoChooser.addObject("Left Start Scale L/R", new CubePlaceAuto(
                intake.setPercentOutput(1.0),
                intake.setPercentOutput(-1.0),
                elevator.holdPosition(15000),
                GameFeature.SWITCH_NEAR, drivetrain,
                new Waypoint[]{
                        new Waypoint(1.59, 23.11, 0),
                        new Waypoint(16, 22, 0),
                        new Waypoint(23.41, 19.5, 0)
                },
                new Waypoint[]{
                        new Waypoint(1.59, 23.11, 0),
                        new Waypoint(15, 23.11, 0),
                        new Waypoint(23.41, 7.5, 0)
                }));
        autoChooser.addObject("Center Start Switch L/R", new CubePlaceAuto(
                intake.setPercentOutput(1.0),
                intake.setPercentOutput(-1.0),
                elevator.holdPosition(34000),
                GameFeature.SCALE, drivetrain,
                new Waypoint[]{
                        new Waypoint(1.59, 13.1, 0),
                        new Waypoint(10.41, 18, 0)
                },
                new Waypoint[]{
                        new Waypoint(1.59, 13.1, 0),
                        new Waypoint(10.41, 9, 0)
                }));
        autoChooser.addObject("Right Start Scale L/R", new CubePlaceAuto(
                intake.setPercentOutput(1.0),
                intake.setPercentOutput(-1.0),
                elevator.holdPosition(34000),
                GameFeature.SCALE, drivetrain,
                new Waypoint[]{
                        new Waypoint(1.59, 3.9, 0),
                        new Waypoint(15, 3.9, 0),
                        new Waypoint(23.41, 19.5, 0)
                },
                new Waypoint[]{
                        new Waypoint(1.59, 3.9, 0),
                        new Waypoint(16, 5, 0),
                        new Waypoint(23.41, 7.5, 0)
                }));
        autoChooser.addObject("DEBUG ONLY: Do Rotation Test", drivetrain.driveRotationTest());
        SmartDashboard.putData( "autochooser", autoChooser);
    }

    @Override
    public void teleopPeriodic() {
        hookDeploy.set(manipulatorGamepad, BionicF310.RY, 0.5);
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        drivetrain.resetProfiling();

        autoCommand = (Command) autoChooser.getSelected();
        if (autoCommand != null) {
            autoCommand.cancel();
            autoCommand.start();
        }
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        drivetrain.resetProfiling();

        if (autoCommand != null) {
            autoCommand.cancel();
        }
    }

    @Override
    protected void dashboardPeriodic() {
        drivetrain.profiling = SmartDashboard.getBoolean("Drivetrain Profiling", false);
        SmartDashboard.putBoolean("Drivetrain Profiling", drivetrain.profiling);
    }

    @Override
    protected void robotEnabled() {
        elevator.holdCurrentPosition();
    }

    @Override
    protected void robotDisabled() {
        if (autoCommand != null) {
            autoCommand.cancel();
        }
    }
}
