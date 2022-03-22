package frc.robot;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Scalar;

import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.utilities.AutoConfigurable;
import frc.robot.utilities.ShooterSetting;

public final class RobotConfig extends AutoConfigurable {
    public static class Drivetrain {
        public static double FORWARD_SENSITIVITY = 0.8; //0.5
        public static double TURN_SENSITIVITY = 0.8; //0.5

        public static double PowerCurve(double x) {
            if (x > 1) return 1;
            if (x < -1) return -1;

            return Math.pow(x, 3);
        }
    }

    public static class Shooter {
        public static List<ShooterSetting> RPMS = new ArrayList<>() { {
            add(new ShooterSetting("Close Low", 1100.0, true, true));
            add(new ShooterSetting("Close High", 2400.0, false, true)); //r:false
            add(new ShooterSetting("Middle High", 2600.0, true, true)); //r:false
            add(new ShooterSetting("Far High", 2800.0, true, true)); //3250 //r:false
            //add(new ShooterSetting("Full Power", 5000.0));
            //new Pair<>("Auto Shot", -1);
        } };

        public static double SHOOTER_RPM_TOLERANCE = 20;

        public static double TURRET_SPEED = 0.5; //0.7

        public static double TURRET_FRONT_POSITION = -0.1;
        public static double TURRET_BACK_POSITION = -0.6;

        public static double FORWARD_SOFT_LIMIT = 0;
        public static double REVERSE_SOFT_LIMIT = -0.689;

        public static double TURRET_VERSAPLANETARY_RATIO = 16;
        public static double TURRET_RATIO = 14;

        public static int TURRET_CURRENT_LIMIT = 5;

        public static class Aimbot {
            public static double CameraHeightMeters = 38.5; // From floor to camera PLEASE CHANGE
            public static double HubHeightMeters = 50;  //From floor to reflective tape PLEASE CHANGE
            public static double CameraPitchRadians = 0;
            public static String CameraName = "USB_Camera";
            public static double hubCenterConstant = 100;  
            public static int TurretMotorPort = 2;
            public static double maxHoodAngle = 24;
            public static double minHoodAngle = 5;
        }

        public static class ShooterPIDF { //Tu= 0.242
            public static float P = 0.00005f;//0.0000042f;
            public static float I = 0.0000004f; //0.00000002f;//0.000001;//0.0005f;//0.121f;
            public static float D = 0.00002f;//0.03025f;
            public static float FF = 0.00015f; //0.00022f;
        }

        public static class TurretPIDF {
            public static float P = 0.1f;
            public static float I = 0.0f;
            public static float D = 1.0f;
            public static float FF = 0.0f;
        }

        public static double RPMPowerCurve(double x) {
            if (x > 1) return 1;
            if (x < -1) return -1;

            return x;
        }

        public static double TurretPowerCurve(double x) {
            if (x > 1) return 1;
            if (x < -1) return -1;

            return x; //Math.pow(x, 3);
        }
    }

    public static class Intake {
        public static double INTAKE_SPEED = 0.8;

        public static double PowerCurve(double x) {
            if (x > 1) return 1;
            if (x < -1) return -1;

            return x;
        }
    }

    public static class Magazine {
        public static double IDLE_SPEED = 0.4;
        public static double INTAKE_SPEED = 0.65;
        public static double OUTTAKE_SPEED = -0.75;
        public static double LAUNCH_SPEED = 0.25;

        public static double SECONDS_TO_EJECT_HORIZONTAL = 2;
        public static double SECONDS_TO_SEND_BALL = 1;

        public static double SECONDS_RELOAD_RUN_IN = 0.05;
        public static double SECONDS_RELOAD_RUN_UP = 0.065;
        public static double SECONDS_RELOAD_RUN_DOWN = 0.20;

        public static int NUMBER_OF_LEDS = 128;

        public static AddressableLEDBuffer zeroPattern(AddressableLEDBuffer in) {
            for (int i = 0; i < in.getLength(); i++) {
                in.setRGB(i, 0, 151, 157);
            }

            return in;
        }

        public static AddressableLEDBuffer onePattern(AddressableLEDBuffer in) {
            for (int i = 0; i < in.getLength(); i++) {
                in.setRGB(i, 255, 255, 0);
            }

            return in;
        }

        public static AddressableLEDBuffer twoPattern(AddressableLEDBuffer in) {
            for (int i = 0; i < in.getLength(); i++) {
                //int hue = (i + ((int) System.currentTimeMillis() / 10) % in.getLength()) / in.getLength() * 180;
                //in.setHSV(i, hue, 255, 255);
                in.setRGB(i, 0, 255, 0);

            }

            return in;
        }

        public static AddressableLEDBuffer overfullPattern(AddressableLEDBuffer in) {
            for (int i = 0; i < in.getLength(); i++) {
                //int value = (int) (Math.sin(System.currentTimeMillis() / 150) * 125 + 192);
                //in.setHSV(i, 0, 255, value);
                in.setHSV(i, 0, 255, 255);
            }

            return in;
        }
    }

    public static class Climber {
        public static double BINARY_CLIMB_SPEED = 1.0;
        public static double ANALOG_CLIMB_SPEED = 0.8;

        public static double PowerCurve(double x, double tz) {
            if (tz >= 1) return x;
            //if (x > 1) return 1;
            //if (x < -1) return -1;

            return (0.8 * x) * tz + 0.2;
        }
    }

    public static class ComputerVision {
        public static String TARGETING_CAMERA_URL = "mjpeg://wpilib1.local:1181";
        public static Scalar MIN_HSV_RANGE = new Scalar(0, 0, 0);
        public static Scalar MAX_HSV_RANGE = new Scalar(255, 255, 255);
        public static double HORIZONTAL_FOV = 135;
        public static double VERTICAL_FOV = 135;
    }
}
