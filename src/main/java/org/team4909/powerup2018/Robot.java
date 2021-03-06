package org.team4909.powerup2018;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
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
import org.team4909.bionicframework.subsystems.drive.commands.DriveDistance;
import org.team4909.bionicframework.subsystems.drive.motion.DrivetrainConfig;
import org.team4909.bionicframework.subsystems.elevator.ElevatorSubsystem;
import org.team4909.bionicframework.subsystems.leds.arduino.Neopixels;
import org.team4909.bionicframework.subsystems.leds.pcm.RGBStrip;
import org.team4909.powerup2018.autonomous.*;

/*
   Controls on Gamepads:
   Port 0: Drive, Using Joysticks
   Port 1: Elevator & Intake (LY = up and down elevator,
   RY = up and down climber, LT = intake, RT = outtake,
   RB/LB = up and down on intake arm rotation,
   B = slow outtake
 */

public class Robot extends RoboRio {
    /* Controller Initialization */
    private static BionicF310 driverGamepad;
    private static BionicF310 manipulatorGamepad;
    private static BionicF310 debugGamepad;

    /* Subsystem Initialization */
    public static BionicDrive drivetrain;
    private static ElevatorSubsystem elevator;
    private static MotorSubsystem intake;
    private static MotorSubsystem winch;
    private static MotorSubsystem hookDeploy;
    public static MotorSubsystem intakeRotator;

    /* Cosmetic Subsystems */
    private static Arduino arduino;
    private static Neopixels lightSaberNeopixels;
    private static RGBStrip underglowLEDs;
    private static SendableChooser underglowChooser = new SendableChooser();

    @Override
    protected void controllerInit() {
        driverGamepad = new BionicF310(0, 0, 0.6);//.8
        manipulatorGamepad = new BionicF310(1, 0.1, 0.5);
    }

    @Override
    public void controllerPeriodic() {
        hookDeploy.set(manipulatorGamepad, BionicF310.RY, 0.5);
    }

    @Override
    protected void subsystemInit() {
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();

        drivetrain = new BionicDrive(
                new BionicSRX(
                        2, false,
                        FeedbackDevice.QuadEncoder, true,
                        1, 0.00001, 0,
                        1
                ),
                new BionicSRX(
                        4, true,
                        FeedbackDevice.QuadEncoder, true,
                        1, 0.00001, 0,
                        4
                ),
                driverGamepad, BionicF310.LY, -1.0, 0.10,
                driverGamepad, BionicF310.RX, -0.6, 0.10, //rotationMult: -.75
                new DrivetrainConfig(
                        25, 0.5, 360,
                        21.76, 41.88, 654.49,
                        3, 2.74
                ),
                new BionicNavX(),
                new BionicSingleSolenoid(0)
        );
        driverGamepad.buttonPressed(BionicF310.LT, 0.1, drivetrain.invertDirection());
        driverGamepad.buttonPressed(BionicF310.RT, 0.1, drivetrain.changeGear());

        elevator = new ElevatorSubsystem(
                new BionicSRX(
                        3, false,
                        FeedbackDevice.CTRE_MagEncoder_Relative, false,
                        0.28, 0, 0,
                        3
                ),
                manipulatorGamepad, BionicF310.LY, -0.45,
                33150
        );

        /**
        manipulatorGamepad.povActive(BionicF310.Top, elevator.holdPosition(28400));
        manipulatorGamepad.povActive(BionicF310.Left, elevator.holdPosition(11000));
        manipulatorGamepad.povActive(BionicF310.Right, elevator.holdPosition(11000));
        manipulatorGamepad.povActive(BionicF310.Bottom, elevator.holdPosition(1410));
        **/

        intake = new MotorSubsystem(
                new BionicSpark(0, true),
                new BionicSpark(1, false)
        );
        manipulatorGamepad.buttonHeld(BionicF310.LT, 0.1, intake.setPercentOutput(1.0));
        manipulatorGamepad.buttonHeld(BionicF310.RT, 0.1, intake.setPercentOutput(-1.0));
        manipulatorGamepad.buttonHeld(BionicF310.B, intake.setPercentOutput(-0.5));

        winch = new MotorSubsystem(
                new BionicVictorSP(2, true),
                new BionicVictorSP(3, true)
        );
        driverGamepad.buttonHeld(BionicF310.Back, winch.setPercentOutput(-0.5));
        driverGamepad.buttonHeld(BionicF310.RB, winch.setPercentOutput(1.0));

        hookDeploy = new MotorSubsystem(
                new BionicSpark(4, false)
        );

        intakeRotator = new MotorSubsystem(
                new BionicVictorSP(5,false)
        );

        manipulatorGamepad.buttonHeld(BionicF310.RB,intakeRotator.setPercentOutput(-.85));
        manipulatorGamepad.buttonHeld(BionicF310.LB,intakeRotator.setPercentOutput(0.5));

        arduino = new Arduino(4);
        lightSaberNeopixels = new Neopixels(arduino, 5, 32);
        driverGamepad.povActive(BionicF310.Top, lightSaberNeopixels.set(Neopixels.Pattern.LightSaber));
        driverGamepad.povActive(BionicF310.Bottom, lightSaberNeopixels.set(Neopixels.Color.BionicGreen));
        driverGamepad.povActive(BionicF310.TopRight, lightSaberNeopixels.set(Neopixels.Pattern.LevelUp));
        driverGamepad.povActive(BionicF310.TopLeft, lightSaberNeopixels.set(Neopixels.Pattern.PingPong));
        driverGamepad.povActive(BionicF310.Right, lightSaberNeopixels.set(Neopixels.Pattern.RainbowSegment));
        driverGamepad.povActive(BionicF310.Left, lightSaberNeopixels.set(Neopixels.Pattern.RainbowStrip));
        driverGamepad.povActive(BionicF310.BottomRight, lightSaberNeopixels.set(Neopixels.Pattern.Fire));
        driverGamepad.povActive(BionicF310.BottomLeft, lightSaberNeopixels.set(Neopixels.Color.Random));

        underglowLEDs = new RGBStrip(3, 5, 4);
        driverGamepad.buttonPressed(BionicF310.Back, underglowLEDs.setAllianceColor());
        driverGamepad.buttonPressed(BionicF310.Start, underglowLEDs.set(RGBStrip.Color.Magenta));
        driverGamepad.buttonPressed(BionicF310.A, underglowLEDs.set(RGBStrip.Color.Lime));
        driverGamepad.buttonPressed(BionicF310.B, underglowLEDs.set(RGBStrip.Color.White));
        driverGamepad.buttonPressed(BionicF310.Y, underglowLEDs.set(RGBStrip.Color.Yellow));
        driverGamepad.buttonPressed(BionicF310.X, underglowLEDs.set(RGBStrip.Color.Cyan));

        underglowChooser.addDefault("Alliance Color", underglowLEDs.setAllianceColor());
        underglowChooser.addObject("Black", underglowLEDs.set(RGBStrip.Color.Black));
        underglowChooser.addObject("White", underglowLEDs.set(RGBStrip.Color.White));
        underglowChooser.addObject("Lime", underglowLEDs.set(RGBStrip.Color.Lime));
        underglowChooser.addObject("Yellow", underglowLEDs.set(RGBStrip.Color.Yellow));
        underglowChooser.addObject("Cyan", underglowLEDs.set(RGBStrip.Color.Cyan));
        underglowChooser.addObject("Magenta", underglowLEDs.set(RGBStrip.Color.Magenta));
        SmartDashboard.putData("Underglow Color: ", underglowChooser);
    }

    @Override
    protected void autoChooserInit() {
        autoChooser.addObject("Break Baseline", new DriveDistance(125, 0.02,0,0));
        autoChooser.addObject("Left Start Switch ONLY", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new LeftSwitchFromLeft(intake, elevator, drivetrain),
                new BreakBaseline(drivetrain)
        ));
//        autoChooser.addObject("Left Start Scale Preferred", new GameFeatureSide(
//                GameFeature.SCALE,
//                new LeftScaleFromLeft(intake, elevator, drivetrain),
//                new GameFeatureSide(
//                        GameFeature.SWITCH_NEAR,
//                        new LeftSwitchFromLeft(intake, elevator, drivetrain),
//                        new BreakBaseline(drivetrain)
//                )
//        ));
//        autoChooser.addObject("Left Start Switch Preferred", new GameFeatureSide(
//                GameFeature.SWITCH_NEAR,
//                new LeftSwitchFromLeft(intake, elevator, drivetrain),
//                new GameFeatureSide(
//                        GameFeature.SCALE,
//                        new LeftScaleFromLeft(intake, elevator, drivetrain),
//                        new BreakBaseline(drivetrain)
//                )
//        ));
        autoChooser.addObject("Center Switch", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new LeftSwitchFromCenter(intake, elevator, drivetrain),
                new RightSwitchFromCenter(intake, elevator, drivetrain)
        ));
        autoChooser.addObject("Center Switch Double", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new DoubleLeftSwitchFromCenter(intake, elevator, drivetrain),
                new DoubleRightSwitchFromCenter(intake, elevator, drivetrain)
        ));
        autoChooser.addObject("Right Start Switch ONLY", new GameFeatureSide(
                GameFeature.SWITCH_NEAR,
                new BreakBaseline(drivetrain),
                new RightSwitchFromRight(intake, elevator, drivetrain)
        ));
//        autoChooser.addObject("Right Start Scale Preferred", new GameFeatureSide(
//                GameFeature.SCALE,
//                new GameFeatureSide(
//                        GameFeature.SWITCH_NEAR,
//                        new BreakBaseline(drivetrain),
//                        new RightSwitchFromRight(intake, elevator, drivetrain)
//                ),
//                new RightScaleFromRight(intake, elevator, drivetrain)
//        ));
//        autoChooser.addObject("Right Start Switch Preferred", new GameFeatureSide(
//                GameFeature.SWITCH_NEAR,
//                new GameFeatureSide(
//                        GameFeature.SCALE,
//                        new BreakBaseline(drivetrain),
//                        new RightScaleFromRight(intake, elevator, drivetrain)
//                ),
//                new RightSwitchFromRight(intake, elevator, drivetrain)
//        ));
//        autoChooser.addObject("DEBUG: Drive Straight", drivetrain.driveDistance(6));
//        autoChooser.addObject("DEBUG: Drive Rotate", drivetrain.driveRotation(90));
//        autoChooser.addObject("DEBUG: Base Line", new DriveDistance(-12,0.02,0,0));
//        autoChooser.addObject("DEBUG: Right", new RightSwitchFromCenter(125, 0.02,0,0));
    }

    @Override
    protected void dashboardPeriodic() {
        super.dashboardPeriodic();

        SmartDashboard.putNumber("Left Encoder", Robot.drivetrain.leftSRX.getSelectedSensorPosition());

        drivetrain.profiling = SmartDashboard.getBoolean("Drivetrain Profiling", false);
        drivetrain.encoderOverride = SmartDashboard.getBoolean("Drivetrain Encoder Override", false);
        SmartDashboard.putBoolean("Drivetrain Profiling", drivetrain.profiling);
        SmartDashboard.putBoolean("Drivetrain Encoder Override", drivetrain.encoderOverride);
        SmartDashboard.putBoolean("Is High Gear?", drivetrain.getGear());

        elevator.encoderOverride = SmartDashboard.getBoolean("Elevator Encoder Override", false);
        SmartDashboard.putBoolean("Elevator Encoder Override", elevator.encoderOverride);
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        ((Command) underglowChooser.getSelected()).start();
    }

    @Override
    public void robotPeriodic() {
        super.robotPeriodic();

        double elevatorCoefficient = (.03 / 34000);

        if (elevator.getCurrentPosition() > 20000) {
            drivetrain.speedDeltaLimit = 0.01;
        } else {
            drivetrain.speedDeltaLimit = 0.04 - (elevatorCoefficient * elevator.getCurrentPosition());
        }

        drivetrain.rotationDeltaLimit = 2; //0.04 - (elevatorCoefficient * elevator.getCurrentPosition());
        SmartDashboard.putNumber("Heading" ,Robot.drivetrain.getHeading());
    }

    @Override
    protected void robotEnabled() {
        super.robotEnabled();

        drivetrain.resetProfiling();
        elevator.holdCurrentPosition();
    }
}
