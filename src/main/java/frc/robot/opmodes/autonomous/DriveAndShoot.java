package frc.robot.opmodes.autonomous;

import frc.robot.RobotConfig;
import frc.robot.commands.autonomous.IntakeTask;
import frc.robot.commands.autonomous.MovementTask;
import frc.robot.commands.autonomous.ShootBallTask;
import frc.robot.commands.autonomous.ShootTask;
import frc.robot.commands.autonomous.WaitTask;
import frc.robot.utilities.AutoOpMode;

public class DriveAndShoot extends AutoOpMode {
    @Override
    public void sequence() {
        this.Sequencer.queue(new ShootTask(RobotConfig.Shooter.RPMS.get(0).Value, true));
        this.Sequencer.queue(new WaitTask(3000));
        this.Sequencer.queue(new ShootBallTask());
        this.Sequencer.queue(new WaitTask(5000));
        this.Sequencer.queue(new ShootTask(0, false));
        this.Sequencer.queue(new IntakeTask(0, RobotConfig.Magazine.INTAKE_SPEED));
        this.Sequencer.queue(new MovementTask(4200, -1, 0, 0.3));
        this.Sequencer.queue(new IntakeTask(0, 0));
    }
}
