import java.util.Date;
import java.net.*;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.awt.Robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import java.lang.Math;

import java.lang.Thread;

public class DataServer
{
	public static void dataRead(BufferedReader bfr) {
		System.out.println("Sensor Mode Identified.");
		try {
			float x = 0;
			float y = 0;
			float deltax = 0;
			float deltay = 0;
			float deltaz = 0;
			Robot robot = new Robot();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			System.out.println("Dimension:" + d);
			while(true) {
				if(bfr == null){
					System.out.println("The connection is over");
					return;
				}
				String data = bfr.readLine();
				if(data == null)
					return ;
				/* Do something with these data */
				int locationM = data.indexOf(":", 0);
				int locationF = data.indexOf(" ", locationM + 1);
				int locationS = data.indexOf(" ", locationF + 1);
				String sX = data.substring(locationM + 1, locationF);
				String sY = data.substring(locationF + 1, locationS);
				String sZ = data.substring(locationS + 1, data.length());
				/* Parse these three string to Float */
				Float aX = new Float(sX);
				Float aY = new Float(sY);
				Float aZ = new Float(sZ);

				float diff_x = aX.floatValue() - deltax;
				float diff_y = aY.floatValue() - deltay;
				float diff_z = aZ.floatValue() - deltaz;
				double diff = Math.sqrt((double)(diff_x *diff_x + diff_y * diff_y + diff_z * diff_z));

				if(diff < 0.5){
					x = 0;
					y = 0;
				}
				else {

					x += (float)(deltax * 0.1);
					y += (float)(deltay * 0.1);
				}

				Point point = MouseInfo.getPointerInfo().getLocation();
				robot.mouseMove(point.x + (int)(x * 10), point.y + (int)(y * 10));
				deltax = aX.floatValue();
				deltay = aY.floatValue();
				deltaz = aZ.floatValue();
			}
		}
		catch(IOException e){
			System.out.println("Data processing done.");
			return ;
		}
		catch(AWTException e) {
			System.out.println("Robot Failure.");
			return ;
		}
	}

	private static void touchRead(BufferedReader bfr) {
		System.out.println("Touch Mode Identified.");
		try {
			Robot robot = new Robot();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			System.out.println("Dimension:" + d);
			while(true) {
				if(bfr == null){
					System.out.println("The connection is over");
					return;
				}
				String data = bfr.readLine();
				int locationDescription = data.indexOf(":", 0);
				String description = data.substring(0, locationDescription);
				if(description.equals("MOVE")) {
					int locationB = data.indexOf(" ", locationDescription);
					String sX = data.substring(locationDescription + 1, locationB);
					String sY = data.substring(locationB + 1, data.length());
					Float fX = new Float(sX);
					Float fY = new Float(sY);
					float vx = fX.floatValue();
					float vy = fY.floatValue();

					System.out.println("vx:" + vx + " vy:" + vy);

					Point point = MouseInfo.getPointerInfo().getLocation();
					robot.mouseMove(point.x + (int)(vx / 50), point.y + (int)(vy / 50));
				}
				else if(description.equals("CLICK")) {
					long duration = Long.parseLong(data.substring(
							locationDescription + 1, data.length()));
					int mask = InputEvent.BUTTON1_DOWN_MASK;
					try{
						robot.mousePress(mask);
						Thread.sleep(duration);
						robot.mouseRelease(mask);
					}
					catch(InterruptedException e) {
						System.out.println("sleep interrupted.");
					}
				}
				else if(description.equals("RIGHT")) {
					long duration = Long.parseLong(data.substring(
							locationDescription + 1, data.length()));
					int mask = InputEvent.BUTTON3_DOWN_MASK;
					try{
						robot.mousePress(mask);
						Thread.sleep(duration);
						robot.mouseRelease(mask);
					}
					catch(InterruptedException e) {
						System.out.println("sleep interrupted.");
					}
				}
				else if(description.equals("SCOLL")){
					int locationB = data.indexOf(" ", locationDescription);
					String sX = data.substring(locationDescription + 1, locationB);
					String sY = data.substring(locationB + 1, data.length());
					Float fX = new Float(sX);
					Float fY = new Float(sY);
					float vx = fX.floatValue();
					float vy = fY.floatValue();

					System.out.println("vx:" + vx + " vy:" + vy);

					robot.mouseWheel((int)(vy/50));
				}
				else if(description.equals("DRAG")){
                    int locationB = data.indexOf(" ", locationDescription);
                    int mask = InputEvent.BUTTON1_DOWN_MASK;
                    String sX = data.substring(locationDescription + 1, locationB);
                    String sY = data.substring(locationB + 1, data.length());
                    Float fX = new Float(sX);
                    Float fY = new Float(sY);
                    float vx = fX.floatValue();
                    float vy = fY.floatValue();

                    System.out.println("vx:" + vx + " vy:" + vy);

                    robot.mousePress(mask);
                    robot.delay(10);
                    Point point = MouseInfo.getPointerInfo().getLocation();
                    robot.mouseMove(point.x + (int)(vx / 50), point.y + (int)(vy / 50));
                    robot.mouseRelease(mask);
                }
			}
		}
		catch(IOException e) {
			System.out.println("Data processing done.");
			return ;
		}
		catch(AWTException e) {
			System.out.println("Robot Failure.");
			return ;
		}
	}

	public static void main(String[] args)
	{
		/* This acceptance part should be put inside a loop*/
		while(true) {
			ServerSocket serverSock = null;
			Socket connectionSock = null;
			BufferedReader clientInput = null;
			InetAddress ia=null;
			try {
				ia=ia.getLocalHost();

				String localname = ia.getHostName();
				String localip = ia.getHostAddress();
				System.out.println("The name of the host is:" + localname);
				System.out.println("The IP of the host is:" + localip);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try{
				/* Whether this should be put into a loop ? */
				System.out.println(
						"Waiting for a connection on port 1700."
				);
				/* Should this socket be changed to one that is not hard-coded? */
				serverSock =
						new ServerSocket(1700);
				connectionSock = serverSock.accept();
				System.out.println("Acceptance Success.");
				clientInput =
						new BufferedReader(new InputStreamReader(
								connectionSock.getInputStream()
						));
				/*
					*DataOutputStream clientOutput =
					*    new DataOutputStream(
					*            connectionSock.getOutputStream());
					*/
				System.out.println(
						"Connection made, waiting for client " + "to inform its mode.");
				/* write a loop to readline */
				String modeIndicator = clientInput.readLine();
				if(modeIndicator.charAt(0) == 'S') {
					dataRead(clientInput);
				}
				else if(modeIndicator.charAt(0) == 'T') {
					touchRead(clientInput);
				}
				/*
				*String replyText =
				*    "Welcome, " + clientText + ", Today is " + now.toString() + "\n";
				*clientOutput.writeBytes(replyText);
				*System.out.println("Sent: " + replyText);
				*/
				/*
				*clientOutput.close();
				*/
			}
			catch(IOException e) {
				System.out.println("IO failed.");
			}
			catch(NullPointerException e){
				System.out.println("There is null pointer");
			}
			try {
				clientInput.close();
				connectionSock.close();
				serverSock.close();
			}
			catch(IOException e) {
				System.out.println("I can't handle that");
			}
		}
	}
}
