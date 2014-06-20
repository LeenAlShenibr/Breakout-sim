/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
//import java.util.Scanner;

public class Breakout extends GraphicsProgram 
{

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	
	private GRect paddle;//The Paddle, declared here for manipulation purposes 
	private int PADDLE_Y =  HEIGHT - PADDLE_Y_OFFSET;//The actual Y Location for the Paddle(From Bottom)
	
	
/** The ball, the amount it moves along the y axes, and a random x trajectory*/	
	GOval ball;
	
	private double vx, vy = 3;
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
/** Game state instance variables*/	
	private int bricksRemaining = NBRICKS_PER_ROW *  NBRICK_ROWS;
	private int turnsRemaining = NTURNS;
	private GLabel turnsLabel = new GLabel("Remaing Turns: " + (turnsRemaining + 1), 10, 20);
	private GLabel bricksLabel = new GLabel("Remaing Bricks: " + bricksRemaining, WIDTH / 2, 20);
	
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	
	private boolean enterClicked = false;
	private boolean quit = false;
	private boolean mouseClick = false;
	
/* Method: run() */
/** Runs the Breakout program. */
	public void run() 
	{	
		addKeyListeners();
	
		startMenu();
		
		while (!quit)
		{
			playGame();
			menu();
		}
		
		System.exit(0);
	}
	
	public void playGame()
	{
		turnsRemaining = 3;
		bricksRemaining = NBRICKS_PER_ROW *  NBRICK_ROWS;
		updateGameStats();
		pause(50);
		
		setBricks();
		add(bricksLabel);
		add(turnsLabel);
		setPaddle();
		
		while (turnsRemaining >= 0 && bricksRemaining != 0)
		{
			mouseClick = false;
			while (!mouseClick)
			{
				System.out.print("");
			}
			
			playBall();
			
		}
		
		endGame();
	}
	
	/** Creates the Colorful bricks */
	public void setBricks()
	{
		int x = (WIDTH - (( NBRICKS_PER_ROW * BRICK_WIDTH ) + ( (NBRICKS_PER_ROW -1) * BRICK_SEP ))) / 2 ; 
		int color = 0, x2 = x;
		int y = BRICK_Y_OFFSET;
		
		Color[] colors = {Color.RED, Color.ORANGE,Color.YELLOW, Color.GREEN, Color.CYAN};
		
		for(int i = 0; i < NBRICKS_PER_ROW; i++)
		{
			for (int j = 0; j < NBRICK_ROWS; j++)
			{
				GRect rect = new GRect(x2,y, BRICK_WIDTH, BRICK_HEIGHT );
				rect.setFillColor(colors[color]);
				rect.setFilled(true);
				x2 += BRICK_WIDTH +  BRICK_SEP;
				add(rect);
			}
			
			x2 = x;
			y += BRICK_HEIGHT + BRICK_SEP;
			
			if( color > 4)
				color = 0;
			else if (i %2 != 0)
				color ++;
		}
		
	}
	
	/** makes the paddle/fills it and adds the listener which will respond to the mouse movements  */
	public void setPaddle()
	{
		paddle = new GRect(WIDTH / 2 - (PADDLE_WIDTH / 2), PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT );
		paddle.setFillColor(Color.BLACK);
		paddle.setFilled(true);
		add(paddle);
		addMouseListeners();
	}
	
	public void mouseDragged(MouseEvent e) 
	{
		 int x = e.getX();
		 
		 if(x < 0)
			 paddle.setLocation(0, PADDLE_Y);
		 else if(x > WIDTH - PADDLE_WIDTH)
			 paddle.setLocation(WIDTH - PADDLE_WIDTH, PADDLE_Y);
		 else
			 paddle.setLocation(x, PADDLE_Y);
	}
	
	public void mouseClicked(MouseEvent e)
	{
		mouseClick = true;
	}
	
	public void playBall()
	{
		//Creats the ball.
		ball = new GOval(WIDTH / 2 - (BALL_RADIUS / 2), HEIGHT / 2, BALL_RADIUS, BALL_RADIUS);
		ball.setFillColor(Color.BLACK);
		ball.setFilled(true);
		add(ball);
		
		//Sets the direction the ball will move in.
		boolean hitBottom = false;
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) 
			vx = -vx;

		int bounceNumber = 0;//Keeps track of the number of bounces -- to help control the speed.
	
		//Controls the physics of the ball. (Bounces, )
		while (hitBottom != true)
		{
			if(ball.getY() >= (HEIGHT - BALL_RADIUS))
			{
				hitBottom = true;
				pause(10);
				remove(ball);
				turnsRemaining--;
				updateGameStats();
				break;
			}
			
			if (ball.getY() >= (HEIGHT - BALL_RADIUS) || ball.getY() <= BALL_RADIUS )
			{
				vy = -vy;
				bounceClip.play();
			}
			else if (ball.getX() >= (WIDTH - BALL_RADIUS) || ball.getX() <= BALL_RADIUS )
			{
				bounceClip.play();
				vx= - vx;
			}
			
			
			GObject collider = getCollidingObject(ball);
			
			if (collider != null)
			{
				if (collider == paddle)
				{
					bounceClip.play();
					vy = -vy;
					bounceNumber++;
				}
				
				else if (collider ==  turnsLabel || collider ==  bricksLabel)
				{
					;
				}
				else
				{
					remove(collider);
					bricksRemaining --;
					bounceClip.play();
					updateGameStats();
					vy = -vy;
				}
			}
			
			ball.move(vx,vy);
			
			//Controls the balls speed.
			if(bounceNumber < 8)
			{
				pause(10);
			}
			else if (bounceNumber < 20)
			{
				pause(7);
			}
			else
			{
				pause(4);
			}
		}
		
	}
	
	private GObject getCollidingObject(GOval ball)
	{
		GObject obj;
		
		if(getElementAt(ball.getX(), ball.getY()) != null )
			return getElementAt(ball.getX(), ball.getY());
		
		else if (getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY()) != null)
			return getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY());
			
		else if (getElementAt(ball.getX(), ball.getY() + (2 * BALL_RADIUS)) != null)
			return getElementAt(ball.getX(), ball.getY() + (2 * BALL_RADIUS));
		
		else if (getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY() + (2 * BALL_RADIUS)) != null)
			return getElementAt(ball.getX() + (2 * BALL_RADIUS), ball.getY() + (2 * BALL_RADIUS));
		
		else
			return null;
	}
	
	public void updateGameStats()
	{
		turnsLabel.setLabel("Remaing Turns: " + (turnsRemaining + 1));
		bricksLabel.setLabel("Remaing Bricks: " + bricksRemaining);
	}
	
	private void endGame()
	{
		
		removeAll();
		repaint();
		
		GLabel end;
		
		if (bricksRemaining <= 0)
			end = new GLabel("Congratulations, you win!!");
		else
		{
			end = new GLabel("GAME OVER");
		}
		end.setFont("Times-32");
		end.setLocation((WIDTH / 2) - (end.getWidth() / 2), HEIGHT / 2);
		add(end);

		mouseClick = false;//in case the user clicked again and set the value to true
		while (!mouseClick)
		{
			System.out.print("");
		}
		
		mouseClick = false;
		
		removeAll();
		repaint();


	}

	public void startMenu()
	{
		GLabel message = new GLabel("Press Enter to start the game");
		message.setLocation((WIDTH / 2) - (message.getWidth() / 2), HEIGHT / 2);
		add(message);
		
		while (!enterClicked)
		{
			System.out.print("");
		}
		
		remove(message);
		repaint();
		enterClicked = false;
	}
	
	public void menu()
	{
		GLabel message = new GLabel("Press Enter to start game, or Q (capital) to quit");
		message.setLocation((WIDTH / 2) - (message.getWidth() / 2), HEIGHT / 2);
		add(message);
		
		while (!enterClicked && !quit)
		{
			System.out.print("");
		}
		
		removeAll();
		repaint();
		
		enterClicked = false;		

	}
	
	public void keyPressed(KeyEvent e) 
	{ 
		if(e.getKeyChar() == e.VK_ENTER) 
		{ 
			enterClicked = true;
		} 
		else if(e.getKeyChar() == e.VK_Q)
		{
			quit = true;
		}
	} 
	
}
