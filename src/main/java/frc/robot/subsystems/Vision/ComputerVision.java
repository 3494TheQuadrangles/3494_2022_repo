package frc.robot.subsystems.Vision;

import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.cscore.HttpCamera;
import edu.wpi.first.cscore.MjpegServer;
import edu.wpi.first.cscore.VideoSource.ConnectionStrategy;
import frc.robot.RobotConfig;
import frc.robot.RobotConfig.Shooter.VisionSettings;
import frc.robot.utilities.di.DiInterfaces.IDisposable;
import frc.robot.utilities.di.DiInterfaces.IInitializable;
import frc.robot.utilities.wpilibdi.DiSubsystem;

public class ComputerVision extends DiSubsystem implements IInitializable, IDisposable  {
    Thread cvThread;

    HttpCamera targetingCamera;
    CvSink targetingCameraSink;

    //int cvSourceHandle;
    CvSource source;
    MjpegServer server;

    public static class TargetingCameraProperties {
        public static double Pitch = 0;
        public static double Yaw = 0;

        public static double x = 0;
        public static double y = 0;
    }

    @Override
    public void onInitialize() {
        this.targetingCamera = new HttpCamera("Targeting Camera", RobotConfig.Shooter.VisionSettings.TARGETING_CAMERA_URL);
        this.targetingCamera.setConnectionStrategy(ConnectionStrategy.kKeepOpen);
        
        this.targetingCameraSink = new CvSink("Targeting Camera CV");
        this.targetingCameraSink.setSource(this.targetingCamera);

        this.source = CameraServer.putVideo("CV Stream", 160, 120);
        
        this.cvThread = new Thread(() -> {
            Mat inMat = new Mat();
            Mat hsvMat = new Mat();
            Mat filteredMat = new Mat();
            Rect targetRect;
            Point rectCenter;

            while (!Thread.interrupted()) {
                long frameTime = this.targetingCameraSink.grabFrame(inMat);
                if (frameTime == 0) {
                    this.source.notifyError(this.targetingCameraSink.getError());
                    continue;
                }
                
                Imgproc.cvtColor(inMat, hsvMat, Imgproc.COLOR_BGR2HSV);

                Core.inRange(hsvMat, RobotConfig.Shooter.VisionSettings.MIN_HSV_RANGE, RobotConfig.Shooter.VisionSettings.MAX_HSV_RANGE, filteredMat);
                
                targetRect = Imgproc.boundingRect(filteredMat);
                rectCenter = new Point((targetRect.x + targetRect.width / 2), (targetRect.y + targetRect.height / 2));

                TargetingCameraProperties.Yaw = (rectCenter.x / filteredMat.width()) * RobotConfig.Shooter.VisionSettings.HORIZONTAL_FOV - (RobotConfig.Shooter.VisionSettings.HORIZONTAL_FOV / 2);
                double bottomToTop =1-(rectCenter.y / 120); // center of bounding box from 0 (bottom) to 1 (top)
                double bottomFOVPitch = RobotConfig.Shooter.VisionSettings.CameraPitchRadians-(RobotConfig.Shooter.VisionSettings.VERTICAL_FOV/2); //pitch of the bottom of the camera image
                //System.out.println(rectCenter.y +"||"+ RobotConfig.Shooter.VisionSettings.CameraPitchRadians  +"||"+ RobotConfig.Shooter.VisionSettings.VERTICAL_FOV);
                TargetingCameraProperties.Pitch =bottomToTop*RobotConfig.Shooter.VisionSettings.VERTICAL_FOV + bottomFOVPitch;
            
                TargetingCameraProperties.x = rectCenter.x;
                TargetingCameraProperties.y = rectCenter.y;
                Imgproc.cvtColor(filteredMat, filteredMat, Imgproc.COLOR_GRAY2BGR);

                Imgproc.drawMarker(filteredMat, rectCenter, new Scalar(0, 255, 255));
                Imgproc.rectangle(filteredMat, targetRect, new Scalar(0, 255, 255));

                this.source.putFrame(filteredMat);
            }
        });
        this.cvThread.setDaemon(true);
        this.cvThread.start();
    }
    public static void InitializeHashMap(){
        RobotConfig.Shooter.VisionSettings.frictionLookUpMap.put(0.0, 0.0);
        RobotConfig.Shooter.VisionSettings.frictionLookUpMap.put(20.0, 4000.5);
        RobotConfig.Shooter.VisionSettings.frictionLookUpMap.put(10.0, 2000.0);


    }
    public static double findShooterPower(PointV hubCenter){
        double power;
        double distance = Math.sqrt(Math.pow(hubCenter.x, 2) + Math.pow(hubCenter.y, 2));
        //Compute possible shots and find viable shots
        double velAtLowAngle = getVel(distance, RobotConfig.Shooter.VisionSettings.maxHoodAngle, RobotConfig.Shooter.VisionSettings.HubHeightMeters);
        double velAtHighAngle = getVel(distance, RobotConfig.Shooter.VisionSettings.minHoodAngle, RobotConfig.Shooter.VisionSettings.HubHeightMeters);
        //double velAtHighAngle = getVel(distance, RobotConfig.Shooter.VisionSettings.minHoodAngle, RobotConfig.Shooter.VisionSettings.HubHeightMeters);

        double heightAtHubEdgeLowAngle = getheight(distance-0.61, RobotConfig.Shooter.VisionSettings.maxHoodAngle, velAtLowAngle);
        double heightAtHubEdgeHighAngle = getheight(distance-0.61, RobotConfig.Shooter.VisionSettings.minHoodAngle, velAtHighAngle);
        //double heightAtHubEdgeHighAngle = getheight(distance-0.61, RobotConfig.Shooter.VisionSettings.minHoodAngle, velAtHighAngle);
        
        if (heightAtHubEdgeHighAngle > RobotConfig.Shooter.VisionSettings.HubHeightMeters + RobotConfig.Shooter.VisionSettings.ballRadiusMeters){ //(heightAtHubEdgeLowAngle > RobotConfig.Shooter.VisionSettings.HubHeightMeters + RobotConfig.Shooter.VisionSettings.ballRadiusMeters){
            //System.out.println(RobotConfig.Shooter.VisionSettings.minHoodAngle);
           
            System.out.println("When the shooter is at"+RobotConfig.Shooter.VisionSettings.minHoodAngle+ "degrees, At a distance of "+distance + " and a velocity of "+velAtHighAngle + " we hit the hub" + heightAtHubEdgeHighAngle);
        }
        if (heightAtHubEdgeLowAngle > RobotConfig.Shooter.VisionSettings.HubHeightMeters + RobotConfig.Shooter.VisionSettings.ballRadiusMeters){ //(heightAtHubEdgeLowAngle > RobotConfig.Shooter.VisionSettings.HubHeightMeters + RobotConfig.Shooter.VisionSettings.ballRadiusMeters){
            System.out.println("When the shooter is at 21 degrees, At a distance of "+distance + " and a velocity of "+velAtLowAngle + " we hit the hub");
        }
        
        power = velAtHighAngle;
        return power;
    }
    public static double getheight(double distance, double angle, double v0){
        return distance*Math.tan(angle)- (9.81*Math.pow(distance, 2))/(2*Math.pow((v0*Math.cos(angle)), 2)) + RobotConfig.Shooter.VisionSettings.ShooterHieghtMeters;
    }
    public static double getVel(double distance, double angle, double height){
        return distance/Math.cos(angle) * Math.sqrt(-9.81/(2*(height-distance*Math.tan(angle)-RobotConfig.Shooter.VisionSettings.ShooterHieghtMeters)));
    }
    public static double getRPMEquation(double ballVelocity){
        return Math.pow(ballVelocity, 2) + 6*ballVelocity + 4;
    }
    public static double getRPMLookUp(double ballVelocity){
        Map.Entry<Double, Double> closeVelLess = Map.entry(0.0, 0.0);
        Map.Entry<Double, Double> closeVelMore = Map.entry(0.0, 0.0);
        boolean firstLess = true;
        boolean firstMore = true;
        //Find the vel in the lookup map of either side of the target Velocity
        for(Map.Entry<Double, Double> set : RobotConfig.Shooter.VisionSettings.frictionLookUpMap.entrySet()) {
            if(set.getKey() < ballVelocity){
                if(firstLess) { closeVelLess = set; firstLess = false;}
                else if(set.getKey() < closeVelLess.getKey()){
                    closeVelLess = set;
                }
            }
            else if(set.getKey() > ballVelocity){
                if(firstMore){closeVelMore = set; firstMore = false;}
                if(set.getKey() > closeVelMore.getKey()){
                    closeVelMore = set;
                }

            }
            else{
                return set.getValue();
            }
        }
        //Using thoose use linear interpolation to estimate what our RPM (set.getValue()) should be
        return (closeVelMore.getValue() + closeVelLess.getValue())/2;
    }
    public static double calculateDistanceToTargetMeters(
            double targetMetersAboveCamera,
            double targetPitchRadians) {
        return targetMetersAboveCamera/ Math.tan(targetPitchRadians);
        }

    @Override
    public void onDispose() {
        this.targetingCamera.close();
    }
}
