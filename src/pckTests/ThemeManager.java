package pckTests;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemeManager.java
 * Central theme controller for the entire application.
 *
 * Holds color palettes for both DARK and LIGHT modes.
 * Any component that needs to react to theme changes registers
 * a ThemeListener — when toggleTheme() is called, all listeners
 * fire automatically so every window updates at once.
 *
 * Usage:
 *   // Register a component to react to theme changes
 *   ThemeManager.addListener(() -> applyTheme());
 *
 *   // Toggle from anywhere (e.g. a settings button)
 *   ThemeManager.toggleTheme();
 *
 *   // Read current colors anywhere
 *   panel.setBackground(ThemeManager.CLR_BG);
 */
public class ThemeManager {

    // ====================================================
    //  THEME DEFINITIONS
    //  Add new color roles here as the project grows.
    //  Both themes must define every role.
    // ====================================================

    // --- DARK THEME ---
    private static final Color DARK_BG              = new Color(18,  18,  18);
    private static final Color DARK_BG_SECONDARY    = new Color(28,  28,  28);
    private static final Color DARK_BG_PANEL        = new Color(245, 245, 245);  // login right panel stays light
    private static final Color DARK_SURFACE         = new Color(32,  32,  32);
    private static final Color DARK_BORDER          = new Color(55,  55,  55);
    private static final Color DARK_TEXT_PRIMARY    = new Color(240, 240, 240);
    private static final Color DARK_TEXT_SECONDARY  = new Color(140, 140, 140);
    private static final Color DARK_TEXT_PLACEHOLDER= new Color(90,  90,  90);
    private static final Color DARK_INPUT_BG        = new Color(28,  28,  28);
    private static final Color DARK_TITLEBAR        = new Color(18,  18,  18);
    private static final Color DARK_SIDEBAR         = new Color(18,  18,  18);

    // --- LIGHT THEME ---
    private static final Color LIGHT_BG             = new Color(245, 245, 245);
    private static final Color LIGHT_BG_SECONDARY   = new Color(255, 255, 255);
    private static final Color LIGHT_BG_PANEL       = new Color(255, 255, 255);
    private static final Color LIGHT_SURFACE        = new Color(235, 235, 235);
    private static final Color LIGHT_BORDER         = new Color(220, 220, 220);
    private static final Color LIGHT_TEXT_PRIMARY   = new Color(18,  18,  18);
    private static final Color LIGHT_TEXT_SECONDARY = new Color(100, 100, 100);
    private static final Color LIGHT_TEXT_PLACEHOLDER=new Color(180, 180, 180);
    private static final Color LIGHT_INPUT_BG       = new Color(255, 255, 255);
    private static final Color LIGHT_TITLEBAR       = new Color(28,  28,  28);   // title bar stays dark in light mode
    private static final Color LIGHT_SIDEBAR        = new Color(28,  28,  28);   // sidebar stays dark in light mode

    // --- SHARED ACCENT COLORS (same in both themes) ---
    public static final Color CLR_BLUE         = new Color(37,  99,  235);
    public static final Color CLR_BLUE_HOVER   = new Color(29,  78,  216);
    public static final Color CLR_GREEN        = new Color(22,  163, 74);
    public static final Color CLR_YELLOW       = new Color(234, 179, 8);
    public static final Color CLR_RED          = new Color(220, 38,  38);
    public static final Color CLR_BLUE_LIGHT   = new Color(219, 234, 254);
    public static final Color CLR_GREEN_LIGHT  = new Color(220, 252, 231);
    public static final Color CLR_YELLOW_LIGHT = new Color(254, 249, 195);
    public static final Color CLR_RED_LIGHT    = new Color(254, 226, 226);

    // ====================================================
    //  STATE
    // ====================================================
    public enum Theme { DARK, LIGHT }

    private static Theme currentTheme = Theme.DARK;  // App starts in dark mode

    // ====================================================
    //  CURRENT THEME COLOR ACCESSORS
    //  These always return the correct color for whatever
    //  theme is currently active.
    // ====================================================
    public static Color getBg()              { return isDark() ? DARK_BG              : LIGHT_BG;             }
    public static Color getBgSecondary()     { return isDark() ? DARK_BG_SECONDARY    : LIGHT_BG_SECONDARY;   }
    public static Color getBgPanel()         { return isDark() ? DARK_BG_PANEL        : LIGHT_BG_PANEL;       }
    public static Color getSurface()         { return isDark() ? DARK_SURFACE         : LIGHT_SURFACE;        }
    public static Color getBorder()          { return isDark() ? DARK_BORDER          : LIGHT_BORDER;         }
    public static Color getTextPrimary()     { return isDark() ? DARK_TEXT_PRIMARY    : LIGHT_TEXT_PRIMARY;   }
    public static Color getTextSecondary()   { return isDark() ? DARK_TEXT_SECONDARY  : LIGHT_TEXT_SECONDARY; }
    public static Color getTextPlaceholder() { return isDark() ? DARK_TEXT_PLACEHOLDER: LIGHT_TEXT_PLACEHOLDER;}
    public static Color getInputBg()         { return isDark() ? DARK_INPUT_BG        : LIGHT_INPUT_BG;       }
    public static Color getTitleBar()        { return isDark() ? DARK_TITLEBAR        : LIGHT_TITLEBAR;       }
    public static Color getSidebar()         { return isDark() ? DARK_SIDEBAR         : LIGHT_SIDEBAR;        }

    // ====================================================
    //  LISTENER SYSTEM
    // ====================================================
    public interface ThemeListener {
        void onThemeChanged();
    }

    private static final List<ThemeListener> listeners = new ArrayList<>();

    public static void addListener(ThemeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    // ====================================================
    //  CONTROLS
    // ====================================================
    public static void toggleTheme() {
        currentTheme = isDark() ? Theme.LIGHT : Theme.DARK;
        notifyListeners();
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        notifyListeners();
    }

    public static Theme getTheme()  { return currentTheme; }
    public static boolean isDark()  { return currentTheme == Theme.DARK; }
    public static boolean isLight() { return currentTheme == Theme.LIGHT; }

    private static void notifyListeners() {
        for (ThemeListener l : listeners) l.onThemeChanged();
    }

    private ThemeManager() {}  // Utility class — never instantiated
}
