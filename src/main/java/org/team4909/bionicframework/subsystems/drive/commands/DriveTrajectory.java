package org.team4909.bionicframework.subsystems.drive.commands;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.command.Command;
import org.team4909.bionicframework.hardware.motor.BionicSRX;
import org.team4909.bionicframework.subsystems.drive.BionicDrive;
import org.team4909.bionicframework.subsystems.drive.motion.DrivetrainProfileUtil;
import org.team4909.bionicframework.subsystems.drive.motion.DrivetrainTrajectory;

public class DriveTrajectory extends Command {
    private final DrivetrainTrajectory trajectory;

    private final BionicDrive bionicDrive;
    private final BionicSRX leftSRX, rightSRX;

    public DriveTrajectory(BionicDrive bionicDrive, BionicSRX leftSRX, BionicSRX rightSRX, DrivetrainTrajectory trajectory) {
        requires(bionicDrive);
        setInterruptible(false);

        this.trajectory = trajectory;

        this.bionicDrive = bionicDrive;
        this.leftSRX = leftSRX;
        this.rightSRX = rightSRX;
    }

    @Override
    protected void initialize() {
        if (bionicDrive.encoderOverride){
            cancel();
        }

        leftSRX.initMotionProfile(trajectory.profileIntervalMs / 2, trajectory.left);
        rightSRX.initMotionProfile(trajectory.profileIntervalMs / 2, trajectory.right);

        bionicDrive.resetProfiling();
    }

    @Override
    protected void execute() {
        leftSRX.runMotionProfile();
        rightSRX.runMotionProfile();
    }

    @Override
    protected boolean isFinished() {
        return leftSRX.isMotionProfileFinished() && rightSRX.isMotionProfileFinished();
    }

    @Override
    protected void end() {
        System.out.println("Final Heading: " + bionicDrive.getHeading() + "rad");
    }
}