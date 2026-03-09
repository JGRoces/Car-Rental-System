package pckUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UIAssets.java
 * Single source of truth for all visual assets in the application.
 *
 * ── Fonts        → public static final  (compile-time constants, zero call overhead)
 * ── Accent colors → public static final  (never change between themes)
 * ── Theme colors  → public static Color get___()  (ternary, negligible overhead)
 * ── Theme toggle  → UIAssets.toggleTheme()  (notifies all registered listeners)
 *
 * Usage in any GUI:
 *   label.setFont(UIAssets.FONT_TITLE);
 *   label.setForeground(UIAssets.getTextPrimary());
 *   UIAssets.addListener(this::applyTheme);
 */
public class UIAssets {

    // ====================================================
    //  TYPOGRAPHY SCALE  —  Segoe UI, one family, all weights
    // ====================================================
    //  FONT_DISPLAY    — 28 Bold  — Login screen welcome title
    //  FONT_TITLE      — 22 Bold  — Page titles (Overview, Management…)
    //  FONT_H1         — 18 Bold  — Active tab label in top bar
    //  FONT_H2         — 15 Bold  — Section headers, settings group labels
    //  FONT_H3         — 13 Bold  — Card labels, field labels, sub-tab buttons
    //  FONT_BODY       — 13 Plain — General descriptions, table content
    //  FONT_SUBTITLE   — 13 Plain — Descriptive line under page title (alias of BODY)
    //  FONT_NAV        — 13 Plain — Sidebar nav items (inactive)
    //  FONT_NAV_BOLD   — 13 Bold  — Sidebar nav items (active)
    //  FONT_BUTTON     — 14 Bold  — All button labels
    //  FONT_INPUT      — 14 Plain — Form text fields and password fields
    //  FONT_SMALL      — 11 Plain — Captions, helper text, timestamps, footer
    //  FONT_STAT_VALUE — 32 Bold  — Big numbers on overview stat cards
    // ====================================================
    public static final Font FONT_DISPLAY    = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_H1         = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_H2         = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_H3         = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_NAV        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_NAV_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_INPUT      = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD,  32);

    // ====================================================
    //  ACCENT COLORS  —  Same in both themes
    // ====================================================
    public static final Color CLR_BLUE          = new Color(37,  99,  235);
    public static final Color CLR_BLUE_HOVER    = new Color(29,  78,  216);
    public static final Color CLR_BLUE_LIGHT    = new Color(219, 234, 254);
    public static final Color CLR_GREEN         = new Color(22,  163, 74);
    public static final Color CLR_GREEN_LIGHT   = new Color(220, 252, 231);
    public static final Color CLR_YELLOW        = new Color(234, 179, 8);
    public static final Color CLR_YELLOW_LIGHT  = new Color(254, 249, 195);
    public static final Color CLR_RED           = new Color(220, 38,  38);
    public static final Color CLR_RED_LIGHT     = new Color(254, 226, 226);

    // Chrome — always dark regardless of theme (title bar, sidebar, top bar)
    public static final Color CLR_CHROME        = new Color(18,  18,  18);
    public static final Color CLR_CHROME_HOVER  = new Color(32,  32,  32);
    public static final Color CLR_CHROME_BORDER = new Color(38,  38,  38);
    public static final Color CLR_SIDEBAR_ACTIVE = CLR_BLUE;

    // ====================================================
    //  DARK PALETTE
    //  Background #000000  |  Card / Panel #0e0e0e
    // ====================================================
    private static final Color DARK_BG               = new Color(0,   0,   0  ); // #000000 page bg
    private static final Color DARK_BG_SECONDARY     = new Color(14,  14,  14 ); // #0e0e0e elevated panels
    private static final Color DARK_SURFACE          = new Color(14,  14,  14 ); // #0e0e0e cards
    private static final Color DARK_BORDER           = new Color(30,  30,  30 ); // subtle dividers
    private static final Color DARK_TEXT_PRIMARY     = new Color(240, 240, 240); // near-white text
    private static final Color DARK_TEXT_SECONDARY   = new Color(130, 130, 130); // muted labels
    private static final Color DARK_TEXT_PLACEHOLDER = new Color(65,  65,  65 ); // ghost placeholder
    private static final Color DARK_INPUT_BG         = new Color(14,  14,  14 ); // #0e0e0e input fields
    private static final Color DARK_CARD             = new Color(14,  14,  14 ); // #0e0e0e stat cards

    // ====================================================
    //  LIGHT PALETTE
    //  Background #efefef  |  Card / Panel #ffffff
    // ====================================================
    private static final Color LIGHT_BG               = new Color(239, 239, 239); // #efefef page bg
    private static final Color LIGHT_BG_SECONDARY     = new Color(255, 255, 255); // #ffffff elevated panels
    private static final Color LIGHT_SURFACE          = new Color(255, 255, 255); // #ffffff cards
    private static final Color LIGHT_BORDER           = new Color(218, 218, 218); // sits between bg and card
    private static final Color LIGHT_TEXT_PRIMARY     = new Color(17,  17,  17 ); // near-black text
    private static final Color LIGHT_TEXT_SECONDARY   = new Color(107, 107, 107); // muted labels
    private static final Color LIGHT_TEXT_PLACEHOLDER = new Color(180, 180, 180); // ghost placeholder
    private static final Color LIGHT_INPUT_BG         = new Color(255, 255, 255); // #ffffff input fields
    private static final Color LIGHT_CARD             = new Color(255, 255, 255); // #ffffff stat cards

    // ====================================================
    //  STATE
    // ====================================================
    public enum Theme { DARK, LIGHT }
    private static Theme currentTheme = Theme.LIGHT;

    // ====================================================
    //  THEME-AWARE COLOR ACCESSORS
    // ====================================================
    public static Color getBg()              { return isDark() ? DARK_BG               : LIGHT_BG;               }
    public static Color getBgSecondary()     { return isDark() ? DARK_BG_SECONDARY     : LIGHT_BG_SECONDARY;     }
    public static Color getSurface()         { return isDark() ? DARK_SURFACE          : LIGHT_SURFACE;          }
    public static Color getBorder()          { return isDark() ? DARK_BORDER           : LIGHT_BORDER;           }
    public static Color getTextPrimary()     { return isDark() ? DARK_TEXT_PRIMARY     : LIGHT_TEXT_PRIMARY;     }
    public static Color getTextSecondary()   { return isDark() ? DARK_TEXT_SECONDARY   : LIGHT_TEXT_SECONDARY;   }
    public static Color getTextPlaceholder() { return isDark() ? DARK_TEXT_PLACEHOLDER : LIGHT_TEXT_PLACEHOLDER; }
    public static Color getInputBg()         { return isDark() ? DARK_INPUT_BG         : LIGHT_INPUT_BG;         }
    public static Color getCard()            { return isDark() ? DARK_CARD             : LIGHT_CARD;             }

    // Chrome colors are theme-independent — always return the dark values
    public static Color getTitleBar()     { return CLR_CHROME;       }
    public static Color getSidebar()      { return CLR_CHROME;       }
    public static Color getSidebarHover() { return CLR_CHROME_HOVER; }
    public static Color getTopBar()       { return CLR_CHROME;       }

    // ====================================================
    //  LISTENER SYSTEM
    // ====================================================
    public interface ThemeListener { void onThemeChanged(); }
    private static final List<ThemeListener> listeners = new ArrayList<>();

    public static void addListener(ThemeListener l)    { listeners.add(l);    }
    public static void removeListener(ThemeListener l) { listeners.remove(l); }

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

    public static Theme   getTheme() { return currentTheme;              }
    public static boolean isDark()   { return currentTheme == Theme.DARK; }
    public static boolean isLight()  { return currentTheme == Theme.LIGHT; }

    private static void notifyListeners() {
        for (ThemeListener l : listeners) l.onThemeChanged();
    }

    private UIAssets() {}
}
