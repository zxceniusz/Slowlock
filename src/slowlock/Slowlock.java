/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package slowlock;  
/*
 * TrayIconDemo.java
 */
 
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
 
public class Slowlock {
    
    public static boolean isLocked = false;
    public static boolean isMouseDown = false;
    
    public static int Time = 0;
    public static int Wait = 200;
    
    public static int downx;
    public static int downy;
    
    public static int tolerance = 60;   
    
    public static MenuItem LockItem;
    public static TrayIcon trayIcon;
    public static MenuItem exitItem;
    
    public static Timer AppTimer;
    
    public static final String eol = System.getProperty("line.separator");  
    
    public static String UNLOCKEDHOSTSFILE = "#emptyhosts";
    public static String LOCKEDHOSTSFILE = (
            "#blockedhosts" + eol + 
            "0.0.0.0 warsztat.gd" + eol + 
            "0.0.0.0 gazeta.pl" + eol + 
            "0.0.0.0 www.gazeta.pl" + eol + 
            "0.0.0.0 www.wykop.pl" + eol +
            "0.0.0.0 wykop.pl" + eol +
            "0.0.0.0 polygamia.pl" + eol +
            "0.0.0.0 kwejk.pl" + eol +
            "0.0.0.0 mail.google.com" + eol +
            "0.0.0.0 reader.google.com" + eol +
            "0.0.0.0 www.mochibot.com" + eol +
            "0.0.0.0 mochibot.com" + eol +
            "0.0.0.0 en.mochimedia.com" + eol +
            "0.0.0.0 polygamia.pl" + eol +
            "0.0.0.0 jawnesny.pl" + eol +
            "0.0.0.0 www.jawnesny.pl" + eol);    
    
    
    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
     
    private static void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        trayIcon =
                new TrayIcon(createImage("images/red.gif", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();
         

      MenuItem instructionItem = new MenuItem("click and hold for a long time to unlock");
      instructionItem.setEnabled(false);
      exitItem = new MenuItem("Exit");
      LockItem = new MenuItem("Lock");
      

      //popup.add(instructionItem);
        popup.add(LockItem);
        popup.add(exitItem);
        
         
        trayIcon.setPopupMenu(popup);
         
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
        
        LockItem.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                isMouseDown = false;
                isLocked = true;
                ReplaceHostsFile(isLocked);
            }
        });
        
      MouseListener ml;
      ml = new MouseListener() 
      {
        public void mouseClicked(MouseEvent e) 
        {

        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) 
        {
            if (isLocked)
            {
                downx = e.getPoint().x;
                downy = e.getPoint().y;
                isMouseDown = true;
            }
            else
            {
                /*
                isMouseDown = false;
                isLocked = true;
                ReplaceHostsFile(isLocked);
                 */
            }
        }

        public void mouseReleased(MouseEvent e) 
        {
            Time = 0;
            isMouseDown = false;
        }
      };
      
      trayIcon.addMouseListener(ml);
      
      trayIcon.setImageAutoSize(true);
      
      ActionListener onTime = new ActionListener() 
      {
        public void actionPerformed(ActionEvent actionEvent) 
        {
            AppTimer.restart();
            LockItem.setEnabled(!isLocked);
            exitItem.setEnabled(!isLocked);
            
            if (isLocked) SetIconRed();
            else SetIconGreen();
            Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
            if (isMouseDown)
            {                
                System.out.println("Time: " + Time);
                
                if (((mousePos.x - downx) > tolerance)||
                        ((mousePos.x - downx) < -tolerance) ||
                        ((mousePos.y - downy) > tolerance) ||
                        ((mousePos.y - downy) < -tolerance))
                {
                    isMouseDown = false;
                    Time = 0;
                }
                
                if (isMouseDown)
                {
                    if (isLocked)
                    {
                        Time++;
                        if (Time > Wait)
                        {
                            isLocked = !isLocked;    
                            ReplaceHostsFile(isLocked);
                            isMouseDown = false;
                            System.out.println("Time: Has Come!");
                        }
                    }
                }
            }
        }
      };
      
    AppTimer = new Timer(100, onTime);
    AppTimer.start();
    
    ReplaceHostsFile(isLocked);
    }
    
    public static void ReplaceHostsFile(boolean locked)
    {
        String str;
        if (!locked)
            str = UNLOCKEDHOSTSFILE;
        else
            str = LOCKEDHOSTSFILE;
        
	try
	{
            String filename = "C:\\Windows\\System32\\drivers\\etc\\hosts";
            FileWriter file = new FileWriter(filename);
            BufferedWriter out = new BufferedWriter (file);
            out.write(str);
            out.close();
	}
	catch (IOException e)
	{
            System.out.println(e.getMessage());
	}     
    }
    
    public static void SetIconRed()
    {
        trayIcon.setImage(createImage("images/red.gif", "tray icon"));        
    }
    
    public static void SetIconGreen()
    {
        trayIcon.setImage(createImage("images/green.gif", "tray icon"));        
    }
     
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = Slowlock.class.getResource(path);
         
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else 
        {
            return (new ImageIcon(imageURL, description)).getImage();          
        }
    }
}