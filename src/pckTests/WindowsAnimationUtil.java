package pckTests;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Library;

import javax.swing.*;
import java.awt.*;

/**
 * WindowsAnimationUtil.java
 * Re-enables native Windows DWM animations on undecorated JFrames
 * WITHOUT showing the Windows title bar.
 *
 * How it works:
 *   1. Adds WS_CAPTION + WS_THICKFRAME back to the window style
 *      so DWM recognizes it as an animatable window
 *   2. Immediately sets DWMWA_NCRENDERING_POLICY to DWMNCRP_DISABLED
 *      so DWM does NOT render the native title bar / non-client area
 *   Result: native animations work, our custom title bar is the only one visible
 *
 * Requirements:
 *   jna-5.14.0.jar and jna-platform-5.14.0.jar in /lib and Referenced Libraries
 *
 * Usage — call ONCE after setVisible(true):
 *   WindowsAnimationUtil.enableDWMAnimations(this);
 */
public class WindowsAnimationUtil {

    // Windows native style flags
    private static final int WS_CAPTION    = 0x00C00000;
    private static final int WS_THICKFRAME = 0x00040000;

    // DWM attribute constants
    // DWMWA_NCRENDERING_POLICY = 2 — controls non-client area rendering
    // DWMNCRP_DISABLED         = 1 — tells DWM: do NOT render the title bar
    private static final int DWMWA_NCRENDERING_POLICY = 2;
    private static final int DWMNCRP_DISABLED         = 1;

    // -------------------------
    // DWM API interface via JNA
    // -------------------------
    interface Dwmapi extends Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class,
                                       W32APIOptions.DEFAULT_OPTIONS);

        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute,
                                   IntByReference pvAttribute, int cbAttribute);
    }

    private WindowsAnimationUtil() {}

    /**
     * Enables native DWM animations without showing the Windows title bar.
     * Must be called AFTER setVisible(true).
     */
    public static void enableDWMAnimations(JFrame frame) {
        try {
            HWND hwnd = getHWND(frame);
            if (hwnd == null) {
                System.err.println("[WindowsAnimationUtil] Could not get native window handle.");
                return;
            }

            // Step 1 — Add WS_CAPTION + WS_THICKFRAME so DWM animates the window
            int currentStyle = User32.INSTANCE.GetWindowLong(hwnd, WinUser.GWL_STYLE);
            int newStyle      = currentStyle | WS_CAPTION | WS_THICKFRAME;
            User32.INSTANCE.SetWindowLong(hwnd, WinUser.GWL_STYLE, newStyle);

            // Step 2 — Disable DWM non-client rendering so the native title bar
            //           is NOT drawn on top of our custom one
            IntByReference policy = new IntByReference(DWMNCRP_DISABLED);
            Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_NCRENDERING_POLICY,
                                                   policy, Integer.BYTES);

            System.out.println("[WindowsAnimationUtil] DWM animations enabled — title bar hidden.");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("[WindowsAnimationUtil] JNA not found. " +
                               "Add jna-5.14.0.jar and jna-platform-5.14.0.jar to Referenced Libraries.");
        } catch (Exception e) {
            System.err.println("[WindowsAnimationUtil] Error: " + e.getMessage());
        }
    }

    private static HWND getHWND(JFrame frame) {
        try {
            Pointer ptr = Native.getWindowPointer(frame);
            return new HWND(ptr);
        } catch (Exception e) {
            return null;
        }
    }
}
