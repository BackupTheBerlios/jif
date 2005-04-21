/*
 * SplashWindow.java
 *
 * This file is part of JIF.
 *
 * Jif is substantially an editor entirely written in java that allows the
 * file management for the creation of text-adventures based on Graham
 * Nelson's Inform standard [a programming language for Interactive Fiction].
 * With Jif, it's possible to edit, compile and run a Text Adventure in
 * Inform format.
 *
 * Copyright (C) 2004  Alessandro Schillaci
 *
 * WeB   : http://www.slade.altervista.org/JIF/
 * e-m@il: silver.slade@tiscalinet.it
 *
 * Jif is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jif; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * A Window with the main JIF splash screen
 * @author administrator
 */

public class SplashWindow extends JWindow{
        int waitTime = 2000;
        /**
         * Create a new istance of Slash screen
         * @param f The instance of jFrame
         */
        public SplashWindow(Frame f){
            super(f);
            JLabel l = new JLabel(new ImageIcon(getClass().getResource("/images/about.png")));
            getContentPane().add(l, BorderLayout.CENTER);
            pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension labelSize  = l.getPreferredSize();

            //setLocation(screenSize.width/2 - (labelSize.width/2), screenSize.height/2 - (labelSize.height/2));
            // Location, relative to jFrame
            setLocation(f.getWidth()/2 - (labelSize.width/2)+ f.getX(), f.getHeight()/2 - (labelSize.height/2) + f.getY());

            addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    setVisible(false);
                    dispose();
                }
            });
            final int pause = waitTime;
            final Runnable closerRunner = new Runnable(){
                public void run(){
                    setVisible(false);
                    dispose();
                }
            };
            Runnable waitRunner = new Runnable(){
                public void run(){
                    try{
                        Thread.sleep(pause);
                        SwingUtilities.invokeAndWait(closerRunner);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
            };
            setVisible(true);
            Thread splashThread = new Thread(waitRunner, "SplashThread");
            splashThread.start();
        }
    }
