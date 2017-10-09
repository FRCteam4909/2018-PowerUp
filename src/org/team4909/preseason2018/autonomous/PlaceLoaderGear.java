package org.team4909.preseason2018.autonomous;

import org.team4909.bionic.utils.commands.DriveDistance;
import org.team4909.bionic.utils.commands.RotateToAngle;
import org.team4909.bionic.utils.commands.SetDriveDirection;
import org.team4909.bionic.utils.commands.SetDriveGear;
import org.team4909.bionic.utils.subsystems.BionicDrivetrain.Direction;
import org.team4909.bionic.utils.subsystems.BionicDrivetrain.Gear;
import org.team4909.preseason2018.core.Robot;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.WaitCommand;

public class PlaceLoaderGear extends CommandGroup {
	public PlaceLoaderGear() {
		//addParallel(new HoldGear());
		addParallel(new SetDriveDirection(Robot.drivetrain, Direction.Forward));
		addSequential(new SetDriveGear(Robot.drivetrain, Gear.Low));
		
		addSequential(new DriveDistance(Robot.drivetrain, Robot.drivetrain.drivePIDConstants, 36));
	
		addSequential(new RotateToAngle(Robot.drivetrain, Robot.drivetrain.rotatePIDConstants, -30));
	
		addSequential(new DriveDistance(Robot.drivetrain, Robot.drivetrain.drivePIDConstants, 60.128));

		addSequential(new RotateToAngle(Robot.drivetrain, Robot.drivetrain.rotatePIDConstants, 60));

		addSequential(new DriveDistance(Robot.drivetrain, Robot.drivetrain.drivePIDConstants, 49));
		
		//addParallel(new DropGear());
		addParallel(new DriveDistance(Robot.drivetrain, Robot.drivetrain.drivePIDConstants, 4));
		addSequential(new WaitCommand(.5));
    	
		addSequential(new DriveDistance(Robot.drivetrain, Robot.drivetrain.drivePIDConstants, -10));
	}
}
