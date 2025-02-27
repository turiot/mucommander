/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.mucommander.cache.WindowsStorage;
import com.mucommander.ui.macosx.IMacOsWindow;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.helper.FocusRequester;


/**
 * FocusDialog is a modal dialog which extends JDialog to provide the following additional functionalities :
 * <ul>
 *   <li>focus can be requested on a specified JComponent once the dialog has been made visible</li>
 *   <li>the screen location of the window can be set relatively to a Component specified in the constructor</li>
 *   <li>a minimum and/or maximum size can be specified and will be used by {@link #pack()} to calculate the effective dialog size</li>
 *   <li>by default, the 'Escape' key disposes the dialog, this can be disabled using {@link #setKeyboardDisposalEnabled(boolean)}</li>
 * </ul>
 * @author Maxence Bernard
 */
public class FocusDialog extends JDialog implements WindowListener, IMacOsWindow {
	private static final Logger LOGGER = LoggerFactory.getLogger(FocusDialog.class);
    private static final EmptyBorder BORDER = new EmptyBorder(6, 8, 6, 8);
	
    /** Minimum dimensions of this dialog, may be null */
    private Dimension minimumDimension;

    /** Maximum dimensions of this dialog, may be null */
    private Dimension maximumDimension;

    /** Has this window been activated yet ? */
    private boolean firstTimeActivated;

    /** The component that will receive the focus when this window is activated for the first time, may be null */
    private JComponent initialFocusComponent;

    private Component locationRelativeComp;

    private boolean keyboardDisposalEnabled = true;

    /**
     * Suffix of keyname for position/size storage
     */
    private String storageSuffix = null;

    private boolean storeSizes = true;

    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";

    private static long lastCreateTime;
    private static String lastCreateTitle;
    private static Class lastCreateClass;
    /**
     * Saved to restore focus
     */
    private Component ownerFocusedComponent;

    
    public FocusDialog(Frame owner, String title, Component locationRelativeComp) {
        super(owner, title, true);
        init(locationRelativeComp);
        if (owner != null) {
            ownerFocusedComponent = owner.getFocusOwner();
        }
        boolean kill = false;
        if (title != null && title.equals(lastCreateTitle)) {
            long dt = System.currentTimeMillis() - lastCreateTime;
            // sometimes EventDispatchThread duplicates events that caused double windows
            if (dt < 250 && lastCreateClass != null && lastCreateClass.equals(getClass())) {
                kill = true;
            }
        }
        lastCreateTime = System.currentTimeMillis();
        lastCreateTitle = title;
        lastCreateClass = getClass();
        if (kill) {
            dispose();
            throw new RuntimeException("EventDispatchThread error");
        }
//        if (owner != null) {
//            showOnScreen(owner);
//        }
    }

    public FocusDialog(Dialog owner, String title, Component locationRelativeComp) {
        super(owner, title, true);
        init(locationRelativeComp);
        if (owner != null) {
            ownerFocusedComponent = owner.getFocusOwner();
        }

        boolean kill = false;
        if (title != null && title.equals(lastCreateTitle)) {
            long dt = System.currentTimeMillis() - lastCreateTime;
            // sometimes EventDispatchThread duplicates events that caused double windows
            if (dt < 250 && lastCreateClass != null && lastCreateClass.equals(getClass())) {
                kill = true;
            }
        }
        lastCreateTime = System.currentTimeMillis();
        lastCreateTitle = title;
        lastCreateClass = getClass();
        if (kill) {
            dispose();
            throw new RuntimeException("EventDispatchThread error");
        }
//        if (owner != null) {
//            showOnScreen(owner);
//        }
    }


    public void showOnScreen(Window parent) {
        if (getWidth() == 0) {
            return;
        }
System.out.println("SIZE " + getWidth() + "x" + getHeight() + "     " + getLocation());
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        final GraphicsDevice frameDevice = parent.getGraphicsConfiguration().getDevice();
        for (GraphicsDevice graphicsDevice : devices) {
System.out.println(frameDevice.getIDstring() + " ' " + graphicsDevice.getIDstring());
            if (frameDevice.equals(graphicsDevice) || frameDevice.getIDstring().equals(graphicsDevice.getIDstring())) {
                final Rectangle monitorBounds = graphicsDevice.getDefaultConfiguration().getBounds();
                final int x = monitorBounds.x + (monitorBounds.width - getWidth()) /2;
                final int y = monitorBounds.y + (monitorBounds.height - getHeight()) / 2;
System.out.println("  " + monitorBounds.width + 'x' + monitorBounds.height + "    " + monitorBounds.x + ' ' + monitorBounds.y);
                setLocation(x, y);
            }
        }
    }

    private void init(Component locationRelativeComp) {
        this.locationRelativeComp = locationRelativeComp;
        setLocationRelativeTo(locationRelativeComp);

        initLookAndFeel();

        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setBorder(BORDER);
        setResizable(true);

        // Important: dispose (release resources) window on close, default is HIDE_ON_CLOSE
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
        // Catch escape key presses and have them close the dialog by mapping the escape keystroke to a custom dispose Action
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();
        AbstractAction disposeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                if (keyboardDisposalEnabled) {
                    cancel();
                }
            }
        };
	
        // Maps the dispose action to the 'Escape' keystroke
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CUSTOM_DISPOSE_EVENT);
        actionMap.put(CUSTOM_DISPOSE_EVENT, disposeAction);
		
        // Maps the dispose action to the 'Apple+W' keystroke under Mac OS X
        if (OsFamily.MAC_OS_X.isCurrent()) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
        }

        // Under Windows, Alt+F4 automatically disposes the dialog, nothing to do
    }

    /**
     * Method called when the user has canceled through the escape key.
     * <p>
     * This method is equivalent to a call to {@link #dispose()}. It's meant to be
     * overridden by those implementations of <code>FocusDialog</code> that need to init
     * code before canceling the dialog.
     */
    public void cancel() {
        dispose();
    }


    @Override
    public void dispose() {
        try {
            WindowsStorage.getInstance().put(this, storageSuffix);
            saveState();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.dispose();

        // fixed issue: return to main frame form FocusDialog
        if ((ownerFocusedComponent instanceof JRootPane && getOwner() instanceof MainFrame)) {
            ownerFocusedComponent = null;
        }

        FocusRequester.requestFocus(ownerFocusedComponent != null ? ownerFocusedComponent : getOwner());
    }

    /**
     * Sets the component that will receive focus once this dialog has been made visible.
     *
     * @param initialFocusComponent the component that will receive focus once this dialog has been made visible, if
     * null, the first component in the dialog will receive focus.
     */
    public void setInitialFocusComponent(JComponent initialFocusComponent) {
        this.initialFocusComponent = initialFocusComponent;

        if (initialFocusComponent == null) {
            removeWindowListener(this);
        } else {
            addWindowListener(this);
        }
    }
	
	
    /**
     * Sets a maximum width and height for this dialog.
     */
    @Override
    public void setMaximumSize(Dimension dimension) {
        this.maximumDimension = dimension;
    }

    /**
     * Sets a minimum width and height for this dialog.
     */
    @Override
    public void setMinimumSize(Dimension dimension) {
        this.minimumDimension = dimension;
    }


    /**
     * Specifies whether this dialog can be automatically disposed using the 'Escape' key and 'Apple+W' under Mac OS X.
     * If enabled, {@link #dispose()} will be called when one of those keystrokes is pressed from any component
     * within this dialog.
     *
     * @param enabled true to enable automatic keyboard disposal, false to disable it
     */
    protected void setKeyboardDisposalEnabled(boolean enabled) {
        this.keyboardDisposalEnabled = enabled;
    }


    /**
     * Overrides Window.pack() to take into account minimum and maximum dialog size (if specified).
     */
    @Override
    public void pack()  {
        super.pack();

        if (maximumDimension != null) {
            DialogToolkit.fitToMaxDimension(this, maximumDimension);
        } else {
            super.setMaximumSize(getSize());
            DialogToolkit.fitToScreen(this);
        }

        if (minimumDimension != null) {
            DialogToolkit.fitToMinDimension(this, minimumDimension);
        } else {
            super.setMinimumSize(getSize());
        }
    }

    protected void packDialog() {
        super.pack();
    }

    protected void setMinimumSizeDialog(Dimension d) {
        minimumDimension = d;
        super.setMinimumSize(d);
    }

    protected void setMaximumSizeDialog(Dimension d) {
        maximumDimension = d;
        super.setMaximumSize(d);
    }

	
    /**
     * Packs this dialog, makes it non-resizable and visible.
     */
    public void showDialog() {
        if (!WindowsStorage.getInstance().init(this, storageSuffix, storeSizes)) {
            pack();
            if (locationRelativeComp == null) {
                DialogToolkit.centerOnScreen(this);
            } else {
                final int x = locationRelativeComp.getX() + (locationRelativeComp.getWidth() - getWidth()) / 2;
                final int y = locationRelativeComp.getY() + (locationRelativeComp.getHeight() - getHeight()) / 2;
                setLocation(x, y);
            }
        }
        SwingUtilities.invokeLater(this::toFront);
        setVisible(true);
    }


    /**
     * Return <code>true</code> if the dialog has been activated (see WindowListener.windowActivated()).
     *
     * @return <code>true</code> if the dialog has been activated
     */
    public boolean isActivated() {
        return firstTimeActivated;
    }


    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // (this method is called each time the dialog is activated)
        if (!firstTimeActivated && initialFocusComponent != null) {
            LOGGER.trace("requesting focus on initial focus component");

            // First try using requestFocusInWindow() which is preferred over requestFocus(). If it fails
            // (returns false), call requestFocus:
            // "The focus behavior of this method can be implemented uniformly across platforms, and thus developers are
            // strongly encouraged to use this method over requestFocus when possible. Code which relies on requestFocus
            // may exhibit different focus behavior on different platforms."
            if (!initialFocusComponent.requestFocusInWindow()) {
                LOGGER.trace("requestFocusInWindow failed, calling requestFocus");
                FocusRequester.requestFocus(initialFocusComponent);
            }

            firstTimeActivated = true;
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    protected void saveState() {

    }

    public void setStorageSuffix(String storageSuffix) {
        this.storageSuffix = storageSuffix;
    }

    public void setStoreSizes(boolean storeSizes) {
        this.storeSizes = storeSizes;
    }

    public FocusDialog returnFocusTo(Component c) {
        this.ownerFocusedComponent = c;
        return this;
    }

    public Component getReturnFocusTo() {
        return ownerFocusedComponent;
    }


    protected void fixHeight() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension preferredSize = getPreferredSize();
                int width = getWidth();
                int minWidth = minimumDimension != null ? minimumDimension.width : preferredSize.width;
                setSize(new Dimension(Math.max(width, minWidth), preferredSize.height));
                super.componentResized(e);
            }
        });
    }

    protected static String i18n(String key, String... params) {
        return Translator.get(key, params);
    }
}
