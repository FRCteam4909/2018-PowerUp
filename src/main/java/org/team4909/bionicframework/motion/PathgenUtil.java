package org.team4909.bionicframework.motion;

import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motion.TrajectoryPoint.TrajectoryDuration;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.modifiers.TankModifier;

/**
 * Path generation utility
 */
public class PathgenUtil {
	private Trajectory.Config config;
	private double drivebaseWidth;
	private double wheelCircumference;
	
	/**
	 * @param config Pathfinder Generation Configuration, as per Pathfinder documenation
	 * @param drivebaseWidth Drivebase width between center of wheels
	 * @param wheelDiameter Wheel diameter from end to end
	 */
	public PathgenUtil(Trajectory.Config config, double drivebaseWidth, double wheelDiameter) {
		this.config = config;
		this.drivebaseWidth = drivebaseWidth;
		this.wheelCircumference = Math.PI * wheelDiameter;
	}
	
	/**
	 * @param points Path consisting of waypoints to follow
	 * @return Returns SRX Motion Profiling Compliant Trajectories
	 */
	public TankTrajectory getTrajectory(Waypoint[] points) {
		Trajectory trajectory = Pathfinder.generate(points, config);
		
		TankModifier modifier = new TankModifier(trajectory).modify(drivebaseWidth);
		
		return new TankTrajectory(modifier.getLeftTrajectory(), modifier.getRightTrajectory());
	}
	
	/**
	 * SRX Motion Profiling Compliant Trajectory Abstraction Layer
	 */
	public class TankTrajectory {
		/**
		 * Trajectory of Drivetrain Left
		 */
		public final TrajectoryPoint[] left;
		
		/**
		 * Trajectory of Drivetrain Right
		 */
		public final TrajectoryPoint[] right;

		/**
		 * @param left Left Trajectory Generated from Pathfinder
		 * @param right Right Trajectory Generated from Pathfinder
		 */
		public TankTrajectory(Trajectory left, Trajectory right) {
			this.left = convertToSRXTrajectory(left);
			this.right = convertToSRXTrajectory(right);
		}
		
		private TrajectoryPoint[] convertToSRXTrajectory(Trajectory trajectory) {
			int length = trajectory.length();
			TrajectoryPoint[] parsedSRXTrajectory = new TrajectoryPoint[length];
			
			for (int i = 0; i < length; i++) {
				TrajectoryPoint point = new TrajectoryPoint();
				
				// Profile Data
				point.position = convertFeetToTicks(trajectory.get(i).position);
				point.velocity = (trajectory.get(i).velocity / config.max_velocity) / 10;
				
				// Configuration Data
				point.timeDur = TrajectoryDuration.Trajectory_Duration_0ms;
				point.profileSlotSelect0 = 0;
				point.zeroPos = (i == 0);
				point.isLastPoint = (i == length - 1);
				point.velocity = point.zeroPos ? point.velocity : 0;
				
				parsedSRXTrajectory[i] = point;
	        }
			
			return parsedSRXTrajectory;
		}
		
		private double convertFeetToTicks(double feet) {
			return 1024 * (feet / wheelCircumference);
		}
	}
}