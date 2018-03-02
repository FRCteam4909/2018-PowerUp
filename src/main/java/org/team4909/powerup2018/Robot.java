package org.team4909.powerup2018;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import openrio.powerup.MatchData.GameFeature;
import org.team4909.bionicframework.hardware.core.Arduino;
import org.team4909.bionicframework.hardware.core.RoboRio;
import org.team4909.bionicframework.hardware.motor.BionicSRX;
import org.team4909.bionicframework.hardware.motor.BionicSpark;
import org.team4909.bionicframework.hardware.motor.BionicVictorSP;
import org.team4909.bionicframework.hardware.motor.MotorSubsystem;
import org.team4909.bionicframework.hardware.pneumatics.BionicSingleSolenoid;
import org.team4909.bionicframework.hardware.sensors.gyro.BionicNavX;
import org.team4909.bionicframework.operator.controllers.BionicF310;
import org.team4909.bionicframework.subsystems.drive.BionicDrive;
import org.team4909.bionicframework.subsystems.drive.motion.DrivetrainConfig;
import org.team4909.bionicframework.subsystems.elevator.ElevatorSubsystem;
import org.team4909.bionicframework.subsystems.leds.pcm.RGBStrip;
import org.team4909.powerup2018.autonomous.*;

public class Robot extends RoboRio {
    /* Controller Initialization */
    private static BionicF310 driverGamepad;
    private static BionicF310 manipulatorGamepad;

    /* Subsystem Initialization */
    private static BionicDrive drivetrain;
    private static ElevatorSubsystem elevator;
    private static MotorSubsystem intake;
    private static MotorSubsystem winch;
    private static MotorSubsystem hookDeploy;

    /* Cosmetic Subsystems */
    private static Arduino arduino;
    private static RGBStrip rgbStrip;
    private static SendableChooser underglowChooser = new SendableChooser();

    @Override
    protected void controllerInit() {
        driverGamepad = new BionicF310(0, 0.1, 0.8);
        manipulatorGamepad = new BionicF310(1, 0.1, 0.5);
    }

    @Override
    public void controllerPeriodic() {
        hookDeploy.set(manipulatorGamepad, BionicF310.RY, 0.5);
    }

    @Override
    protected void subsystemInit() {
        drivetrain = new BionicDrive(
                new BionicSRX(
                        2, false,
                        FeedbackDevice.QuadEncoder, false,
                        1, 0, 0, // P:1.7 I:0 D:7
                        1
                ),
                new BionicSRX(
                        4, true,
                        FeedbackDevice.QuadEncoder, false,
                        1, 0, 0,
                        4
                ),
                driverGamepad, BionicF310.LY, -1.0, 0.10,
                driverGamepad, BionicF310.RX, -1.0, 0.10,
                new DrivetrainConfig(
                        25, 0.5, 120,
                        12.000, 11.126, 117.809,
                        3, 2.74
                ),
                new BionicNavX(),
                new BionicSingleSolenoid(0)
        );
        driverGamepad.buttonPressed(BionicF310.LT, 0.1, drivetrain.invertDirection());
        driverGamepad.buttonPressed(BionicF310.RT, 0.1, drivetrain.changeGear());

        elevator = new ElevatorSubsystem(
                new BionicSRX(
                        3, true,
                        FeedbackDevice.CTRE_MagEncoder_Relative, false,
                        1.0, 0, 0
                ),
                manipulatorGamepad, BionicF310.LY, -1,
                33150
        );
        manipulatorGamepad.povActive(BionicF310.Top, elevator.holdPosition(28400));
        manipulatorGamepad.povActive(BionicF310.CenterLeft, elevator.holdPosition(11000));
        manipulatorGamepad.povActive(BionicF310.CenterRight, elevator.holdPosition(11000));
        manipulatorGamepad.povActive(BionicF310.Bottom, elevator.holdPosition(1410));

        intake = new MotorSubsystem(
                new BionicSpark(0, true),
                new BionicSpark(1, false)
        );
        manipulatorGamepad.buttonHeld(BionicF310.LT, 0.1, intake.setPercentOutput(1.0));
        manipulatorGamepad.buttonHeld(BionicF310.RT, 0.1, intake.setPercentOutput(-1.0));
        manipulatorGamepad.buttonHeld(BionicF310.B, intake.setPercentOutput(-0.5));

        winch = new MotorSubsystem(
                new BionicVictorSP(2, true),
                new BionicVictorSP(3, false)
        );
        driverGamepad.buttonHeld(BionicF310.LB, winch.setPercentOutput(-0.5));
        driverGamepad.buttonHeld(BionicF310.RB, winch.setPercentOutput(1.0));

        hookDeploy = new MotorSubsystem(
                new BionicSpark(4, false)
        );

        arduino = new Arduino(4);

        rgbStrip = new RGBStrip(3, 5, 4);
        underglowChooser.addDefault("Alliance Color", rgbStrip.setAllianceColor());
        underglowChooser.addObject("Black", rgbStrip.set(RGBStrip.Colors.Black));
        underglowChooser.addObject("White", rgbStrip.set(RGBStrip.Colors.White));
        underglowChooser.addObject("Lime", rgbStrip.set(RGBStrip.Colors.Lime));
        underglowChooser.addObject("Yellow", rgbStrip.set(RGBStrip.Colors.Yellow));
        underglowChooser.addObject("Cyan", rgbStrip.set(RGBStrip.Colors.Cyan));
        underglowChooser.addObject("Magenta", rgbStrip.set(RGBStrip.Colors.Magenta));
        SmartDashboard.putData("Underglow Color: ", underglowChooser);
    }

    @Override
    protected void autoChooserInit() {
        autoChooser.addObject("Break Baseline", new BreakBaseline(drivetrain));
        autoChooser.addObject("Left Start Scale Preferred", new GameFeatureSide(
                GameFeature.SCALE,
                new LeftScaleFromLeft(intake, elevator, drivetrain),
                new GameFeatureSide(
                        GameFeature.SWITCH_NEAR,
                        new LeftSwitchFromLeft(intake, elevator, drivetrain),
                        new BreakBaseline(drivetrain)
                )
        ));
        autoChooser.addObject("Left Start Switch Preferred", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new LeftSwitchFromLeft(intake, elevator, drivetrain),
                new GameFeatureSide(
                        GameFeature.SCALE,
                        new LeftScaleFromLeft(intake, elevator, drivetrain),
                        new BreakBaseline(drivetrain)
                )
        ));
        autoChooser.addObject("Center Switch", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new LeftSwitchFromCenter(intake, elevator, drivetrain),
                new RightSwitchFromCenter(intake, elevator, drivetrain)
        ));
        autoChooser.addObject("Right Start Scale Preferred", new GameFeatureSide(
                GameFeature.SCALE,
                new GameFeatureSide(
                        GameFeature.SWITCH_NEAR,
                        new BreakBaseline(drivetrain),
                        new RightSwitchFromRight(intake, elevator, drivetrain)
                ),
                new RightScaleFromRight(intake, elevator, drivetrain)
        ));
        autoChooser.addObject("Right Start Switch Preferred", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new GameFeatureSide(
                        GameFeature.SCALE,
                        new BreakBaseline(drivetrain),
                        new RightScaleFromRight(intake, elevator, drivetrain)
                ),
                new RightSwitchFromRight(intake, elevator, drivetrain)
        ));
    }

    @Override
    protected void dashboardPeriodic() {
        super.dashboardPeriodic();

        drivetrain.profiling = SmartDashboard.getBoolean("Drivetrain Profiling", false);
        drivetrain.encoderOverride = SmartDashboard.getBoolean("Drivetrain Encoder Override", false);
        SmartDashboard.putBoolean("Drivetrain Profiling", drivetrain.profiling);
        SmartDashboard.putBoolean("Drivetrain Encoder Override", drivetrain.encoderOverride);
        SmartDashboard.putBoolean("Is High Gear?", drivetrain.getGear());

        elevator.encoderOverride = SmartDashboard.getBoolean("Elevator Encoder Override", false);
        SmartDashboard.putBoolean("Elevator Encoder Override", elevator.encoderOverride);

        ((Command) underglowChooser.getSelected()).start();
    }

    @Override
    public void robotPeriodic() {
        super.robotPeriodic();

        double elevatorCoefficient = (.05 / 34000);

        if (elevator.getCurrentPosition() > 20000) {
            drivetrain.speedDeltaLimit = 0.0085;
        } else {
            drivetrain.speedDeltaLimit = 0.04 - (elevatorCoefficient * elevator.getCurrentPosition());
        }

        if (elevator.getCurrentPosition() > 20000) {
            drivetrain.rotationDeltaLimit = 0.004;
        } else if (elevator.getCurrentPosition() > 15000) {
            drivetrain.rotationDeltaLimit = 0.005;
        } else if (elevator.getCurrentPosition() > 10000) {
            drivetrain.rotationDeltaLimit = 0.006;
        } else {
            drivetrain.rotationDeltaLimit = 0.04 - (elevatorCoefficient * elevator.getCurrentPosition());
        }
    }

    @Override
    protected void robotEnabled() {
        super.robotEnabled();

        drivetrain.resetProfiling();
        elevator.holdCurrentPosition();
    }
}
