import java.util.Date;
import java.net.ServerSocket;
import java.net.Socket;
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
				String data = bfr.readLine();
				if(data == null)
					return ;
				/* Do something with these data */
				int locationF = data.indexOf(" ", 0);
				int locationS = data.indexOf(" ", locationF + 1);
				String sX = data.substring(0, locationF);
				String sY = data.substring(locationF + 1, locationS);
				String sZ = data.substring(locationS + 1, data.length());
				/* Parse these three string to Float */
				Float fX = new Float(sX);
				Float fY = new Float(sY);
				Float fZ = new Float(sZ);
				deltax = (deltax + fX.floatValue()) / 2;
				deltay = (deltay + fY.floatValue()) / 2;
				deltaz = (deltaz + fZ.floatValue()) / 2;

				x += deltax * 0.1;
				y += deltay * 0.1;
				
				Point point = MouseInfo.getPointerInfo().getLocation();
				robot.mouseMove(point.x + (int)(x * 10), point.y + (int)(y * 10));
				deltax = fX.floatValue();
				deltay = fY.floatValue();
				deltaz = fZ.floatValue();
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
					int mask = InputEvent.BUTTON2_DOWN_MASK;
					try{
						robot.mousePress(mask);
						Thread.sleep(duration);
						robot.mouseRelease(mask);
					}
					catch(InterruptedException e) {
						System.out.println("sleep interrupted.");
					}
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
			try{
				/* Whether this should be put into a loop ? */
				System.out.println(
					"Waiting for a connection on port 1765."
					);
				/* Should this socket be changed to one that is not hard-coded? */
				ServerSocket serverSock;
				Socket connectionSock;
				BufferedReader clientInput;
				serverSock = 
					new ServerSocket(1765);
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
				if(modeIndicator.charAt(0) == 'S')
					dataRead(clientInput);
				else if(modeIndicator.charAt(0) == 'T')
					touchRead(clientInput);
				/*
				*String replyText =
				*    "Welcome, " + clientText + ", Today is " + now.toString() + "\n";
				*clientOutput.writeBytes(replyText);
				*System.out.println("Sent: " + replyText);
				*/
				/*
				*clientOutput.close();
				*/
				clientInput.close();
				connectionSock.close();
				serverSock.close();
			}
			catch(IOException e) {
				System.out.println("IO failed.");
			}
		}
	}
}
