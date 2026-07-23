// crafted by amethyst oak for the glorious server of linux dreams and broken builds
// this bullshit is provided as-is. if bugs are found - Telegram: @justmineplayerB
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VexileLauncher extends JFrame {

private static final String CONFIG_FILE = "vexile.properties";
private static final String INSTANCES_FILE = "instances.json";
private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

// Update check constants
private static final String CURRENT_VERSION = "1.2";
private static final String VERSION_URL = "https://raw.githubusercontent.com/amethyst-oak/Vexile/main/version.txt";
private static final String REPO_URL = "https://github.com/amethyst-oak/Vexile";

// Palette
private static final Color COLOR_BG = new Color(0x18, 0x18, 0x1C);
private static final Color COLOR_PANEL = new Color(0x23, 0x23, 0x2A);
private static final Color COLOR_ACCENT = new Color(0x57, 0xF2, 0x87); // Зеленый
private static final Color COLOR_ACCENT_HOVER = new Color(0x43, 0xB5, 0x81);
private static final Color COLOR_ADD_BTN = new Color(0x57, 0xF2, 0x87);
private static final Color COLOR_TEXT = new Color(0xF2, 0xF3, 0xF5);
private static final Color COLOR_TEXT_MUTED = new Color(0x94, 0x9B, 0xA4);
private static final Color COLOR_GOLD = new Color(0xD4, 0xAF, 0x37);

private Properties config = new Properties();
private JPanel versionsGrid;
private JTextField nickField;
private JLabel statusLabel;
private String currentLang = "en";

private List<InstanceData> instanceList = new ArrayList<>();

// UI elements
private JLabel versionsHeaderLabel;
private JLabel newsHeaderLabel;
private JLabel nickLabel;
private StyledButton loginBtn;

private static final List<VersionButton> versionButtonList = new ArrayList<>();

private static final String[] LANGUAGES = {
    "English (en)", "Русский (ru)", "Українська (uk)", "Беларуская (be)",
    "Polski (pl)", "Deutsch (de)", "Français (fr)", "Español (es)"
};

private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();

static {
    Map<String, String> en = new HashMap<>();
    en.put("title", "Vexile");
    en.put("versions", "VERSIONS");
    en.put("news", "NEWS & UPDATES");
    en.put("nickname", "Nickname:");
    en.put("ready", "Ready to play");
    en.put("wiz_title", "Vexile - Initial Setup");
    en.put("wiz_welcome", "Welcome to Vexile");
    en.put("wiz_desc", "A modern, lightweight Minecraft launcher built for speed and simplicity.");
    en.put("wiz_select_lang", "Select your language:");
    en.put("wiz_finish", "Finish Setup");
    en.put("select_ver_title", "Select Minecraft Version");
    en.put("loading_manifest", "Fetching versions from Mojang...");
    en.put("launching", "Launching ");
    en.put("settings_title", "Vexile Settings");
    en.put("jvm_args_label", "JVM Arguments:");
    en.put("java_path_label", "Java Executable Path:");
    en.put("about_title", "About Vexile");
    en.put("about_text", "Vexile is a lightweight, customizable Minecraft launcher.\nCreated with Java Swing.");
    en.put("close", "Close");
    en.put("login_btn", "Login (Premium)");
    en.put("login_title", "Minecraft Premium Login");
    en.put("login_ms", "Sign in with Microsoft");
    en.put("login_or", "— OR —");
    en.put("login_email", "Email / Username:");
    en.put("login_pass", "Password / Auth Token:");
    en.put("login_submit", "Login");
    en.put("ver_ctx_rename", "Rename version");
    en.put("ver_ctx_delete", "Delete version");
    en.put("rename_title", "Rename Version");
    en.put("rename_prompt", "Enter new name for version:");
    TRANSLATIONS.put("en", en);

    Map<String, String> ru = new HashMap<>();
    ru.put("title", "Vexile");
    ru.put("versions", "ВЕРСИИ");
    ru.put("news", "НОВОСТИ И ОБНОВЛЕНИЯ");
    ru.put("nickname", "Никнейм:");
    ru.put("ready", "Готов к запуску");
    ru.put("wiz_title", "Vexile - Первоначальная настройка");
    ru.put("wiz_welcome", "Добро пожаловать в Vexile");
    ru.put("wiz_desc", "Современный и лёгкий лаунчер Minecraft, созданный для скорости и удобства.");
    ru.put("wiz_select_lang", "Выберите ваш язык:");
    ru.put("wiz_finish", "Завершить настройку");
    ru.put("select_ver_title", "Выбор версии Minecraft");
    ru.put("loading_manifest", "Загрузка списка версий с Mojang...");
    ru.put("launching", "Запуск ");
    ru.put("settings_title", "Настройки Vexile");
    ru.put("jvm_args_label", "Аргументы JVM:");
    ru.put("java_path_label", "Путь к Java (javaw.exe / java):");
    ru.put("about_title", "О Vexile");
    ru.put("about_text", "Vexile — кастомный и лёгкий лаунчер Minecraft.\nСоздан с использованием Java Swing.");
    ru.put("close", "Закрыть");
    ru.put("login_btn", "Вход (Premium)");
    ru.put("login_title", "Авторизация Premium");
    ru.put("login_ms", "Войти через Microsoft");
    ru.put("login_or", "— ИЛИ —");
    ru.put("login_email", "Email / Логин:");
    ru.put("login_pass", "Пароль / Токен:");
    ru.put("login_submit", "Войти");
    ru.put("ver_ctx_rename", "Переименовать версию");
    ru.put("ver_ctx_delete", "Удалить версию");
    ru.put("rename_title", "Переименование версии");
    ru.put("rename_prompt", "Введите новое имя для версии:");
    TRANSLATIONS.put("ru", ru);

    Map<String, String> uk = new HashMap<>();
    uk.put("title", "Vexile");
    uk.put("versions", "ВЕРСІЇ");
    uk.put("news", "НОВИНИ ТА ОНОВЛЕННЯ");
    uk.put("nickname", "Нікнейм:");
    uk.put("ready", "Готовий до запуску");
    uk.put("wiz_title", "Vexile - Початкове налаштування");
    uk.put("wiz_welcome", "Ласкаво просимо до Vexile");
    uk.put("wiz_desc", "Сучасний і легкий лаунчер Minecraft, створений для швидкості та зручності.");
    uk.put("wiz_select_lang", "Виберіть вашу мову:");
    uk.put("wiz_finish", "Завершити налаштування");
    uk.put("select_ver_title", "Вибір версії Minecraft");
    uk.put("loading_manifest", "Завантаження списку версій з Mojang...");
    uk.put("launching", "Запуск ");
    uk.put("settings_title", "Налаштування Vexile");
    uk.put("jvm_args_label", "Аргументи JVM:");
    uk.put("java_path_label", "Шлях до Java:");
    uk.put("about_title", "Про Vexile");
    uk.put("about_text", "Vexile — легкий та кастомний лаунчер Minecraft.\nСтворено за допомогою Java Swing.");
    uk.put("close", "Закрити");
    uk.put("login_btn", "Вхід (Premium)");
    uk.put("login_title", "Авторизація Premium");
    uk.put("login_ms", "Увійти через Microsoft");
    uk.put("login_or", "— АБО —");
    uk.put("login_email", "Email / Логін:");
    uk.put("login_pass", "Пароль / Токен:");
    uk.put("login_submit", "Увійти");
    uk.put("ver_ctx_rename", "Перейменувати версію");
    uk.put("ver_ctx_delete", "Видалити версію");
    uk.put("rename_title", "Перейменування версії");
    uk.put("rename_prompt", "Введіть нове ім'я для версії:");
    TRANSLATIONS.put("uk", uk);

    Map<String, String> be = new HashMap<>();
    be.put("title", "Vexile");
    be.put("versions", "ВЕРСІІ");
    be.put("news", "НАВІНЫ І АБНАЎЛЕННІ");
    be.put("nickname", "Нікнэйм:");
    be.put("ready", "Гатовы да запуску");
    be.put("wiz_title", "Vexile - Першапачатковая налада");
    be.put("wiz_welcome", "Сардэчна запрашаем у Vexile");
    be.put("wiz_desc", "Сучасны і лёгкі лаунчэр Minecraft, створаны для хуткасці і зручнасці.");
    be.put("wiz_select_lang", "Абярыце вашу мову:");
    be.put("wiz_finish", "Завяршыць наладу");
    be.put("select_ver_title", "Выбар версіі Minecraft");
    be.put("loading_manifest", "Загрузка спісу версій з Mojang...");
    be.put("launching", "Запуск ");
    be.put("settings_title", "Налады Vexile");
    be.put("jvm_args_label", "Аргументы JVM:");
    be.put("java_path_label", "Шлях да Java:");
    be.put("about_title", "Пра Vexile");
    be.put("about_text", "Vexile — лёгкі і кастомны лаунчэр Minecraft.\nСтворана на Java Swing.");
    be.put("close", "Закрыць");
    be.put("login_btn", "Увайсці (Premium)");
    be.put("login_title", "Аўтарызацыя Premium");
    be.put("login_ms", "Увайсці праз Microsoft");
    be.put("login_or", "— АБО —");
    be.put("login_email", "Email / Лагін:");
    be.put("login_pass", "Пароль / Токен:");
    be.put("login_submit", "Увайсці");
    be.put("ver_ctx_rename", "Перайменаваць версію");
    be.put("ver_ctx_delete", "Выдаліць версію");
    be.put("rename_title", "Перайменаванне версіі");
    be.put("rename_prompt", "Калі ласка, увядзіце новае імя для версіі:");
    TRANSLATIONS.put("be", be);

    Map<String, String> pl = new HashMap<>();
    pl.put("title", "Vexile");
    pl.put("versions", "WERSJE");
    pl.put("news", "WIADOMOŚCI I AKTUALIZACJE");
    pl.put("nickname", "Nazwa użytkownika:");
    pl.put("ready", "Gotowy do gry");
    pl.put("wiz_title", "Vexile - Konfiguracja wstępna");
    pl.put("wiz_welcome", "Witaj w Vexile");
    pl.put("wiz_desc", "Nowoczesny, lekki launcher Minecraft stworzony z myślą o szybkości i prostocie.");
    pl.put("wiz_select_lang", "Wybierz swój język:");
    pl.put("wiz_finish", "Zakończ konfigurację");
    pl.put("select_ver_title", "Wybierz wersję Minecraft");
    pl.put("loading_manifest", "Pobieranie wersji z Mojang...");
    pl.put("launching", "Uruchamianie ");
    pl.put("settings_title", "Ustawienia Vexile");
    pl.put("jvm_args_label", "Argumenty JVM:");
    pl.put("java_path_label", "Ścieżka Java:");
    pl.put("about_title", "O Vexile");
    pl.put("about_text", "Vexile to lekki, konfigurowable launcher Minecraft.\nStworzony w Java Swing.");
    pl.put("close", "Zamknij");
    pl.put("login_btn", "Zaloguj (Premium)");
    pl.put("login_title", "Logowanie Minecraft Premium");
    pl.put("login_ms", "Zaloguj się przez Microsoft");
    pl.put("login_or", "— LUB —");
    pl.put("login_email", "Email / Nazwa użytkownika:");
    pl.put("login_pass", "Hasło / Token auth:");
    pl.put("login_submit", "Zaloguj");
    pl.put("ver_ctx_rename", "Zmień nazwę wersji");
    pl.put("ver_ctx_delete", "Usuń wersję");
    pl.put("rename_title", "Zmiana nazwy wersji");
    pl.put("rename_prompt", "Wprowadź nową nazwę wersji:");
    TRANSLATIONS.put("pl", pl);

    Map<String, String> de = new HashMap<>();
    de.put("title", "Vexile");
    de.put("versions", "VERSIONEN");
    de.put("news", "NEWS & UPDATES");
    de.put("nickname", "Benutzername:");
    de.put("ready", "Bereit zum Spielen");
    de.put("wiz_title", "Vexile - Ersteinrichtung");
    de.put("wiz_welcome", "Willkommen bei Vexile");
    de.put("wiz_desc", "Ein moderner, leichter Minecraft-Launcher für Geschwindigkeit und Einfachheit.");
    de.put("wiz_select_lang", "Wähle deine Sprache:");
    de.put("wiz_finish", "Setup abschließen");
    de.put("select_ver_title", "Minecraft-Version auswählen");
    de.put("loading_manifest", "Lade Versionen von Mojang...");
    de.put("launching", "Starte ");
    de.put("settings_title", "Vexile Einstellungen");
    de.put("jvm_args_label", "JVM-Argumente:");
    de.put("java_path_label", "Java-Pfad:");
    de.put("about_title", "Über Vexile");
    de.put("about_text", "Vexile ist ein leichter, anpassbarer Minecraft-Launcher.\nErstellt mit Java Swing.");
    de.put("close", "Schließen");
    de.put("login_btn", "Anmelden (Premium)");
    de.put("login_title", "Minecraft Premium Anmeldung");
    de.put("login_ms", "Mit Microsoft anmelden");
    de.put("login_or", "— ODER —");
    de.put("login_email", "E-Mail / Benutzername:");
    de.put("login_pass", "Passwort / Auth-Token:");
    de.put("login_submit", "Anmelden");
    de.put("ver_ctx_rename", "Version umbenennen");
    de.put("ver_ctx_delete", "Version löschen");
    de.put("rename_title", "Version umbenennen");
    de.put("rename_prompt", "Geben Sie einen neuen Namen für die Version ein:");
    TRANSLATIONS.put("de", de);

    Map<String, String> fr = new HashMap<>();
    fr.put("title", "Vexile");
    fr.put("versions", "VERSIONS");
    fr.put("news", "ACTUALITÉS ET MISES À JOUR");
    fr.put("nickname", "Pseudo :");
    fr.put("ready", "Prêt à jouer");
    fr.put("wiz_title", "Vexile - Configuration initiale");
    fr.put("wiz_welcome", "Bienvenue sur Vexile");
    fr.put("wiz_desc", "Un lanceur Minecraft moderne et léger, conçu pour la rapidité et la simplicité.");
    fr.put("wiz_select_lang", "Sélectionnez votre langue :");
    fr.put("wiz_finish", "Terminer la configuration");
    fr.put("select_ver_title", "Sélectionner la version de Minecraft");
    fr.put("loading_manifest", "Chargement des versions depuis Mojang...");
    fr.put("launching", "Lancement de ");
    fr.put("settings_title", "Paramètres Vexile");
    fr.put("jvm_args_label", "Arguments JVM :");
    fr.put("java_path_label", "Chemin d'accès Java :");
    fr.put("about_title", "À propos de Vexile");
    fr.put("about_text", "Vexile est un lanceur Minecraft léger et personnalisable.\nCréé avec Java Swing.");
    fr.put("close", "Fermer");
    fr.put("login_btn", "Connexion (Premium)");
    fr.put("login_title", "Connexion Minecraft Premium");
    fr.put("login_ms", "Se connecter avec Microsoft");
    fr.put("login_or", "— OU —");
    fr.put("login_email", "E-mail / Nom d'utilisateur :");
    fr.put("login_pass", "Mot de passe / Jeton d'auth :");
    fr.put("login_submit", "Se connecter");
    fr.put("ver_ctx_rename", "Renommer la version");
    fr.put("ver_ctx_delete", "Supprimer la version");
    fr.put("rename_title", "Renommer la version");
    fr.put("rename_prompt", "Entrez un nouveau nom pour la version :");
    TRANSLATIONS.put("fr", fr);

    Map<String, String> es = new HashMap<>();
    es.put("title", "Vexile");
    es.put("versions", "VERSIONES");
    es.put("news", "NOTICIAS Y ACTUALIZACIONES");
    es.put("nickname", "Apodo:");
    es.put("ready", "Listo para jugar");
    es.put("wiz_title", "Vexile - Configuración inicial");
    es.put("wiz_welcome", "Bienvenido a Vexile");
    es.put("wiz_desc", "Un lanzador de Minecraft moderno y ligero creado para la velocidad y la simplicidad.");
    es.put("wiz_select_lang", "Selecciona tu idioma:");
    es.put("wiz_finish", "Finalizar configuración");
    es.put("select_ver_title", "Seleccionar versión de Minecraft");
    es.put("loading_manifest", "Obteniendo versiones de Mojang...");
    es.put("launching", "Iniciando ");
    es.put("settings_title", "Ajustes de Vexile");
    es.put("jvm_args_label", "Argumentos JVM:");
    es.put("java_path_label", "Ruta del ejecutable Java:");
    es.put("about_title", "Acerca de Vexile");
    es.put("about_text", "Vexile es un lanzador de Minecraft ligero y personalizable.\nCreado con Java Swing.");
    es.put("close", "Cerrar");
    es.put("login_btn", "Iniciar sesión (Premium)");
    es.put("login_title", "Inicio de sesión Premium");
    es.put("login_ms", "Iniciar sesión con Microsoft");
    es.put("login_or", "— O —");
    es.put("login_email", "Correo / Usuario:");
    es.put("login_pass", "Contraseña / Token:");
    es.put("login_submit", "Entrar");
    es.put("ver_ctx_rename", "Renombrar versión");
    es.put("ver_ctx_delete", "Eliminar versión");
    es.put("rename_title", "Renombrar versión");
    es.put("rename_prompt", "Ingrese un nuevo nombre para la versión:");
    TRANSLATIONS.put("es", es);
}

public static String categorizeVersion(String name, String typeFromManifest) {
    if (name == null) return "Release";
    String lower = name.toLowerCase();

    if (lower.startsWith("rd-") || lower.startsWith("cave-game")) {
        return "Pre-Classic";
    }
    if (lower.startsWith("c0.") || lower.startsWith("classic") || lower.startsWith("c_")) {
        return "Classic";
    }
    if (lower.startsWith("a1.") || lower.startsWith("alpha") || lower.startsWith("a_") || lower.startsWith("inf-")) {
        return "Alpha";
    }
    if (lower.startsWith("b1.") || lower.startsWith("beta") || lower.startsWith("b_")) {
        return "Beta";
    }
    if ("snapshot".equalsIgnoreCase(typeFromManifest) || lower.contains("-pre") || lower.contains("-rc") || lower.matches(".*\\d{2}w\\d{2}.*")) {
        return "Snapshot";
    }

    return "Release";
}

public static class InstanceData {
    public String name;
    public String category;
    public int amount;
    public boolean downloaded;

    public InstanceData(String name, String category, int amount, boolean downloaded) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.downloaded = downloaded;
    }
}

public VexileLauncher() {
    checkUpdates();
    loadConfig();
    loadInstances();
    checkAndRequestJavaPath();

    File cfgFile = new File(CONFIG_FILE);
    if (!cfgFile.exists()) {
        showSetupWizard();
    } else {
        initUI();
        setVisible(true);
    }
}

private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
}

private void checkAndRequestJavaPath() {
    String savedJava = config.getProperty("java_path", "");
    if (isWindows()) {
        if (savedJava.isEmpty() || !new File(savedJava).exists()) {
            String defaultJava = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe";
            if (!new File(defaultJava).exists()) {
                defaultJava = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
            }

            String input = (String) JOptionPane.showInputDialog(
                null,
                "I dont know... Download it please \nSet path to Java (java.exe / javaw.exe):",
                "Java Path Configuration",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                new File(defaultJava).exists() ? defaultJava : "C:\\Program Files\\Common Files\\Oracle\\Java\\javapath\\java.exe"
            );

            if (input != null && !input.trim().isEmpty()) {
                config.setProperty("java_path", input.trim());
                saveConfig();
            } else if (new File(defaultJava).exists()) {
                config.setProperty("java_path", defaultJava);
                saveConfig();
            }
        }
    } else {
        if (savedJava.isEmpty() || !new File(savedJava).exists()) {
            detectLinuxJavaAndEnvironment();
        }
    }
}

private void detectLinuxJavaAndEnvironment() {
    String distro = "";
    try (BufferedReader br = new BufferedReader(new FileReader("/etc/os-release"))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("ID=")) {
                distro = line.substring(3).replace("\"", "").trim();
                break;
            }
        }
    } catch (Exception ignored) {}

    boolean javaAvailable = false;
    try {
        Process p = Runtime.getRuntime().exec(new String[]{"which", "java"});
        javaAvailable = p.waitFor() == 0;
    } catch (Exception ignored) {}

    if (!javaAvailable) {
        if (distro.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "Your distro is not supported. Please, change your OS to play!",
                "Fatal Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        } else {
            JOptionPane.showMessageDialog(
                null,
                "Your (" + distro + ") distro doesn't have Java/is not supported. Please, change your OS to play!",
                "Fatal Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    } else {
        config.setProperty("java_path", "java");
        saveConfig();
    }
}

private void checkUpdates() {
    try {
        URL url = new URL(VERSION_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);

        if (conn.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String remoteVersion = reader.readLine().trim();
            reader.close();

            if (compareVersions(remoteVersion, CURRENT_VERSION) > 0) {
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "A new version of Vexile is available (" + remoteVersion + "). Do you want to update?",
                    "Vexile Update",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(REPO_URL));
                    }
                    System.exit(0);
                }
            }
        }
    } catch (Exception e) {
        System.out.println("Could not check for updates: " + e.getMessage());
    }
}

private int compareVersions(String v1, String v2) {
    String[] parts1 = v1.split("\\.");
    String[] parts2 = v2.split("\\.");
    int length = Math.max(parts1.length, parts2.length);
    for (int i = 0; i < length; i++) {
        int num1 = i < parts1.length ? Integer.parseInt(parts1[i].replaceAll("\\D+", "")) : 0;
        int num2 = i < parts2.length ? Integer.parseInt(parts2[i].replaceAll("\\D+", "")) : 0;
        if (num1 != num2) {
            return Integer.compare(num1, num2);
        }
    }
    return 0;
}

private String t(String key) {
    Map<String, String> map = TRANSLATIONS.getOrDefault(currentLang, TRANSLATIONS.get("en"));
    return map.getOrDefault(key, key);
}

private void loadConfig() {
    File cfg = new File(CONFIG_FILE);
    if (cfg.exists()) {
        try (InputStream in = new FileInputStream(cfg)) {
            config.load(in);
            currentLang = config.getProperty("language", getSystemLanguage());
        } catch (Exception ignored) {}
    } else {
        currentLang = getSystemLanguage();
    }
}

private String getSystemLanguage() {
    String sysLang = Locale.getDefault().getLanguage();
    return TRANSLATIONS.containsKey(sysLang) ? sysLang : "en";
}

private void saveConfig() {
    try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
        config.store(out, "Vexile Configuration");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void loadInstances() {
    File file = new File(INSTANCES_FILE);
    if (!file.exists()) {
        saveInstances();
        return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String content = sb.toString();

        Matcher matcher = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"category\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"amount\"\\s*:\\s*(\\d+)\\s*,\\s*\"downloaded\"\\s*:\\s*(true|false)\\s*\\}").matcher(content);
        while (matcher.find()) {
            String name = matcher.group(1);
            String category = matcher.group(2);
            int amount = Integer.parseInt(matcher.group(3));
            boolean downloaded = Boolean.parseBoolean(matcher.group(4));

            // Filtering soem mawlare
            if (!name.equalsIgnoreCase("BETACRAFT") && !name.equalsIgnoreCase("PLANETNOSTALGIA") && !name.equalsIgnoreCase("ALPHAPLACE")) {
                instanceList.add(new InstanceData(name, category, amount, downloaded));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void saveInstances() {
    try (FileWriter writer = new FileWriter(INSTANCES_FILE)) {
        writer.write("[\n");
        for (int i = 0; i < instanceList.size(); i++) {
            InstanceData inst = instanceList.get(i);
            writer.write("  {\n");
            writer.write("    \"name\": \"" + inst.name + "\",\n");
            writer.write("    \"category\": \"" + inst.category + "\",\n");
            writer.write("    \"amount\": " + inst.amount + ",\n");
            writer.write("    \"downloaded\": " + inst.downloaded + "\n");
            writer.write("  }" + (i < instanceList.size() - 1 ? "," : "") + "\n");
        }
        writer.write("]\n");
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void registerInstance(String verId, String category, boolean downloaded) {
    boolean exists = false;
    for (InstanceData inst : instanceList) {
        if (inst.name.equalsIgnoreCase(verId)) {
            inst.downloaded = downloaded;
            inst.category = category;
            exists = true;
            break;
        }
    }

    if (!exists) {
        instanceList.add(new InstanceData(verId, category, 1, downloaded));
    }

    saveInstances();
}

private void showSetupWizard() {
    JDialog wizard = new JDialog(this, t("wiz_title"), true);
    wizard.setSize(520, 360);
    wizard.setLocationRelativeTo(null);
    wizard.getContentPane().setBackground(COLOR_BG);
    wizard.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    centerPanel.setBackground(COLOR_BG);
    centerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

    JLabel titleLabel = new JLabel(t("wiz_welcome"));
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
    titleLabel.setForeground(COLOR_TEXT);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 360px;'>" + t("wiz_desc") + "</div></html>");
    descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    descLabel.setForeground(COLOR_TEXT_MUTED);
    descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel langLabel = new JLabel(t("wiz_select_lang"));
    langLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    langLabel.setForeground(COLOR_TEXT);
    langLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JComboBox<String> langBox = new JComboBox<>(LANGUAGES);
    langBox.setMaximumSize(new Dimension(280, 35));
    langBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
    langBox.setBackground(COLOR_PANEL);
    langBox.setForeground(COLOR_TEXT);

    for (int i = 0; i < LANGUAGES.length; i++) {
        if (LANGUAGES[i].contains("(" + currentLang + ")")) {
            langBox.setSelectedIndex(i);
            break;
        }
    }

    StyledButton finishBtn = new StyledButton(t("wiz_finish"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 14);

    langBox.addActionListener(e -> {
        String selected = (String) langBox.getSelectedItem();
        if (selected != null) {
            currentLang = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
            wizard.setTitle(t("wiz_title"));
            titleLabel.setText(t("wiz_welcome"));
            descLabel.setText("<html><div style='text-align: center; width: 360px;'>" + t("wiz_desc") + "</div></html>");
            langLabel.setText(t("wiz_select_lang"));
            finishBtn.setText(t("wiz_finish"));
        }
    });

    centerPanel.add(titleLabel);
    centerPanel.add(Box.createVerticalStrut(8));
    centerPanel.add(descLabel);
    centerPanel.add(Box.createVerticalStrut(18));
    centerPanel.add(langLabel);
    centerPanel.add(Box.createVerticalStrut(6));
    centerPanel.add(langBox);

    finishBtn.setPreferredSize(new Dimension(200, 40));
    finishBtn.addActionListener(e -> {
        config.setProperty("language", currentLang);
        config.setProperty("nickname", "amethyst-oak");
        config.setProperty("jvm_args", "-Xmx2G -XX:+UseG1GC");
        saveConfig();
        wizard.dispose();
        initUI();
        setVisible(true);
    });

    JPanel bottomPanel = new JPanel();
    bottomPanel.setBackground(COLOR_BG);
    bottomPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
    bottomPanel.add(finishBtn);

    wizard.add(centerPanel, BorderLayout.CENTER);
    wizard.add(bottomPanel, BorderLayout.SOUTH);
    wizard.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    wizard.setVisible(true);
}

private void initUI() {
    setTitle(t("title"));
    setSize(880, 600);
    setMinimumSize(new Dimension(750, 500));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(true);
    getContentPane().setBackground(COLOR_BG);
    setLayout(new BorderLayout(10, 10));

    JPanel mainContainer = new JPanel(new GridBagLayout());
    mainContainer.setBackground(COLOR_BG);
    mainContainer.setBorder(new EmptyBorder(15, 15, 10, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0, 5, 0, 5);

    // VERSIONS
    RoundedPanel versionsPanel = new RoundedPanel(16);
    versionsPanel.setBackground(COLOR_PANEL);
    versionsPanel.setLayout(new BorderLayout(6, 6));
    versionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    versionsHeaderLabel = new JLabel(t("versions"));
    versionsHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    versionsHeaderLabel.setForeground(COLOR_TEXT_MUTED);
    versionsPanel.add(versionsHeaderLabel, BorderLayout.NORTH);

    // GRID LAYOUT
    versionsGrid = new JPanel(new GridLayout(0, 3, 8, 8));
    versionsGrid.setBackground(COLOR_PANEL);

    // this is not featherlight, its atomlight (would've been..)
    JPanel versionsGridWrapper = new JPanel(new BorderLayout());
    versionsGridWrapper.setBackground(COLOR_PANEL);
    versionsGridWrapper.add(versionsGrid, BorderLayout.NORTH);

    JScrollPane scrollGrid = new JScrollPane(versionsGridWrapper);
    scrollGrid.setBorder(null);
    scrollGrid.getViewport().setBackground(COLOR_PANEL);
    versionsPanel.add(scrollGrid, BorderLayout.CENTER);

    StyledButton btnAdd = new StyledButton("+", COLOR_ADD_BTN, new Color(0x3A, 0x9E, 0x61), 16);
    btnAdd.setPreferredSize(new Dimension(60, 60));
    btnAdd.setFont(new Font("SansSerif", Font.BOLD, 24));
    btnAdd.setForeground(Color.BLACK);
    btnAdd.addActionListener(e -> fetchAndShowVersions());
    versionsGrid.add(btnAdd);

    versionButtonList.clear();
    for (InstanceData inst : instanceList) {
        addVersionButtonUI(inst.name, inst.category);
    }

    JPanel nickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    nickPanel.setBackground(COLOR_PANEL);

    nickLabel = new JLabel(t("nickname"));
    nickLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    nickLabel.setForeground(COLOR_TEXT);

    nickField = new JTextField(config.getProperty("nickname", "amethyst-oak"), 11);
    nickField.setFont(new Font("SansSerif", Font.PLAIN, 13));
    nickField.setBackground(COLOR_BG);
    nickField.setForeground(COLOR_TEXT);
    nickField.setCaretColor(COLOR_TEXT);
    nickField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BG, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
    ));

    StyledButton settingsBtn = new StyledButton("⚙", COLOR_BG, COLOR_ACCENT, 8);
    settingsBtn.setPreferredSize(new Dimension(30, 30));
    settingsBtn.setFont(new Font("SansSerif", Font.PLAIN, 15));
    settingsBtn.setToolTipText("Settings / Настройки");
    settingsBtn.addActionListener(e -> openSettingsDialog());

    nickPanel.add(nickLabel);
    nickPanel.add(nickField);
    nickPanel.add(settingsBtn);
    versionsPanel.add(nickPanel, BorderLayout.SOUTH);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 1.0;
    mainContainer.add(versionsPanel, gbc);

    // --- ПРАВАЯ ПАНЕЛЬ: НОВОСТИ ---
    RoundedPanel rightPanel = new RoundedPanel(16);
    rightPanel.setBackground(COLOR_PANEL);
    rightPanel.setLayout(new BorderLayout(8, 8));
    rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel rightContent = new JPanel();
    rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));
    rightContent.setBackground(COLOR_PANEL);

    newsHeaderLabel = new JLabel(t("news"));
    newsHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    newsHeaderLabel.setForeground(COLOR_TEXT_MUTED);
    newsHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    rightContent.add(newsHeaderLabel);
    rightContent.add(Box.createVerticalStrut(4));

    JPanel newsListContainer = new JPanel();
    newsListContainer.setLayout(new BoxLayout(newsListContainer, BoxLayout.Y_AXIS));
    newsListContainer.setBackground(COLOR_PANEL);

    newsListContainer.add(createNewsCard(
            "Vexile 1.2 Release",
            "July 2026",
            "★ Vexile 1.2 is officially released! ★\n\nWhat's new:\n• Fixed version paths to run/.minecraft/versions/{version}/\n• Added Java PATH support & Windows detection settings.\n• Fixed Authlib and modern client launch issues."
    ));
    newsListContainer.add(Box.createVerticalStrut(4));
    newsListContainer.add(createNewsCard(
            "Vexile 1.0 Release",
            "July 2026",
            "★ Vexile 1.0 is officially released! ★\n\nFeatures:\n• Modern dark rounded UI.\n• Dynamic version downloading straight from Mojang manifest."
    ));

    JScrollPane newsScroll = new JScrollPane(newsListContainer);
    newsScroll.setBorder(null);
    newsScroll.getViewport().setBackground(COLOR_PANEL);
    newsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
    rightContent.add(newsScroll);

    rightPanel.add(rightContent, BorderLayout.CENTER);

    JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    bottomRightPanel.setBackground(COLOR_PANEL);

    loginBtn = new StyledButton(t("login_btn"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 8);
    loginBtn.setForeground(Color.BLACK);
    loginBtn.setPreferredSize(new Dimension(140, 28));
    loginBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
    loginBtn.addActionListener(e -> openLoginDialog());

    bottomRightPanel.add(loginBtn);
    rightPanel.add(bottomRightPanel, BorderLayout.SOUTH);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 1.0;
    mainContainer.add(rightPanel, gbc);

    add(mainContainer, BorderLayout.CENTER);

    JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
    statusPanel.setBackground(COLOR_BG);
    statusPanel.setBorder(new EmptyBorder(2, 10, 5, 10));

    statusLabel = new JLabel(t("ready"));
    statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    statusLabel.setForeground(COLOR_TEXT_MUTED);
    statusPanel.add(statusLabel, BorderLayout.SOUTH);

    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    progressBar.setVisible(false);
    progressBar.setPreferredSize(new Dimension(0, 18));
    statusPanel.add(progressBar, BorderLayout.NORTH);

    add(statusPanel, BorderLayout.SOUTH);
}

private JProgressBar progressBar;

private void updateProgress(int value, String text) {
    SwingUtilities.invokeLater(() -> {
        if (!progressBar.isVisible()) {
            progressBar.setVisible(true);
        }
        progressBar.setValue(value);
        progressBar.setString(text);
    });
}

private void hideProgress() {
    SwingUtilities.invokeLater(() -> {
        progressBar.setVisible(false);
        progressBar.setValue(0);
        progressBar.setString("");
    });
}

private void openLoginDialog() {
    JDialog loginDialog = new JDialog(this, t("login_title"), true);
    loginDialog.setSize(380, 320);
    loginDialog.setLocationRelativeTo(this);
    loginDialog.getContentPane().setBackground(COLOR_BG);
    loginDialog.setLayout(new BorderLayout(10, 10));

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBackground(COLOR_BG);
    content.setBorder(new EmptyBorder(15, 20, 15, 20));

    StyledButton msBtn = new StyledButton(t("login_ms"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 8);
    msBtn.setForeground(Color.BLACK);
    msBtn.setMaximumSize(new Dimension(320, 36));
    msBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    msBtn.addActionListener(e -> {
        updateStatus("Initiating Microsoft OAuth authentication...");
        JOptionPane.showMessageDialog(loginDialog, "Redirecting to Microsoft Login...", "Premium Login", JOptionPane.INFORMATION_MESSAGE);
        loginDialog.dispose();
    });

    JLabel orLbl = new JLabel(t("login_or"));
    orLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
    orLbl.setForeground(COLOR_TEXT_MUTED);
    orLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel emailLbl = new JLabel(t("login_email"));
    emailLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
    emailLbl.setForeground(COLOR_TEXT);
    emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField emailField = new JTextField();
    emailField.setMaximumSize(new Dimension(320, 28));
    emailField.setBackground(COLOR_PANEL);
    emailField.setForeground(COLOR_TEXT);
    emailField.setCaretColor(COLOR_TEXT);
    emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BG, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
    ));
    emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel passLbl = new JLabel(t("login_pass"));
    passLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
    passLbl.setForeground(COLOR_TEXT);
    passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPasswordField passField = new JPasswordField();
    passField.setMaximumSize(new Dimension(320, 28));
    passField.setBackground(COLOR_PANEL);
    passField.setForeground(COLOR_TEXT);
    passField.setCaretColor(COLOR_TEXT);
    passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BG, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
    ));
    passField.setAlignmentX(Component.LEFT_ALIGNMENT);

    content.add(msBtn);
    content.add(Box.createVerticalStrut(10));
    content.add(orLbl);
    content.add(Box.createVerticalStrut(10));
    content.add(emailLbl);
    content.add(Box.createVerticalStrut(4));
    content.add(emailField);
    content.add(Box.createVerticalStrut(8));
    content.add(passLbl);
    content.add(Box.createVerticalStrut(4));
    content.add(passField);

    StyledButton submitBtn = new StyledButton(t("login_submit"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 8);
    submitBtn.setForeground(Color.BLACK);
    submitBtn.setPreferredSize(new Dimension(110, 32));
    submitBtn.addActionListener(e -> {
        String user = emailField.getText().trim();
        if (!user.isEmpty()) {
            nickField.setText(user);
            config.setProperty("nickname", user);
            saveConfig();
            updateStatus("Logged in as Premium user: " + user);
        }
        loginDialog.dispose();
    });

    JPanel bottom = new JPanel();
    bottom.setBackground(COLOR_BG);
    bottom.setBorder(new EmptyBorder(0, 0, 12, 0));
    bottom.add(submitBtn);

    loginDialog.add(content, BorderLayout.CENTER);
    loginDialog.add(bottom, BorderLayout.SOUTH);
    loginDialog.setVisible(true);
}

private void openSettingsDialog() {
    JDialog settingsDialog = new JDialog(this, t("settings_title"), true);
    settingsDialog.setSize(440, 450);
    settingsDialog.setLocationRelativeTo(this);
    settingsDialog.getContentPane().setBackground(COLOR_BG);
    settingsDialog.setLayout(new BorderLayout(10, 10));

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(COLOR_BG);
    contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

    JLabel langLbl = new JLabel(t("wiz_select_lang"));
    langLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    langLbl.setForeground(COLOR_TEXT);
    langLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    JComboBox<String> langBox = new JComboBox<>(LANGUAGES);
    langBox.setMaximumSize(new Dimension(360, 30));
    langBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
    langBox.setBackground(COLOR_PANEL);
    langBox.setForeground(COLOR_TEXT);
    langBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    for (int i = 0; i < LANGUAGES.length; i++) {
        if (LANGUAGES[i].contains("(" + currentLang + ")")) {
            langBox.setSelectedIndex(i);
            break;
        }
    }

    JLabel jvmLbl = new JLabel(t("jvm_args_label"));
    jvmLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    jvmLbl.setForeground(COLOR_TEXT);
    jvmLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField jvmField = new JTextField(config.getProperty("jvm_args", "-Xmx2G -XX:+UseG1GC"));
    jvmField.setMaximumSize(new Dimension(360, 30));
    jvmField.setFont(new Font("SansSerif", Font.PLAIN, 12));
    jvmField.setBackground(COLOR_PANEL);
    jvmField.setForeground(COLOR_TEXT);
    jvmField.setCaretColor(COLOR_TEXT);
    jvmField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BG, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
    ));
    jvmField.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel javaPathLbl = new JLabel(t("java_path_label"));
    javaPathLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    javaPathLbl.setForeground(COLOR_TEXT);
    javaPathLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField javaPathField = new JTextField(config.getProperty("java_path", ""));
    javaPathField.setMaximumSize(new Dimension(360, 30));
    javaPathField.setFont(new Font("SansSerif", Font.PLAIN, 12));
    javaPathField.setBackground(COLOR_PANEL);
    javaPathField.setForeground(COLOR_TEXT);
    javaPathField.setCaretColor(COLOR_TEXT);
    javaPathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BG, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
    ));
    javaPathField.setAlignmentX(Component.LEFT_ALIGNMENT);

    langBox.addActionListener(e -> {
        String selected = (String) langBox.getSelectedItem();
        if (selected != null) {
            currentLang = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
            config.setProperty("language", currentLang);
            saveConfig();
            updateLocalizedTexts();
            settingsDialog.setTitle(t("settings_title"));
            langLbl.setText(t("wiz_select_lang"));
            jvmLbl.setText(t("jvm_args_label"));
            javaPathLbl.setText(t("java_path_label"));
        }
    });

    JLabel aboutHeader = new JLabel(t("about_title"));
    aboutHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
    aboutHeader.setForeground(COLOR_ACCENT);
    aboutHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextArea aboutArea = new JTextArea(t("about_text"));
    aboutArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
    aboutArea.setForeground(COLOR_TEXT_MUTED);
    aboutArea.setBackground(COLOR_PANEL);
    aboutArea.setEditable(false);
    aboutArea.setLineWrap(true);
    aboutArea.setWrapStyleWord(true);
    aboutArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    aboutArea.setAlignmentX(Component.LEFT_ALIGNMENT);

    contentPanel.add(langLbl);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(langBox);
    contentPanel.add(Box.createVerticalStrut(10));
    contentPanel.add(jvmLbl);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(jvmField);
    contentPanel.add(Box.createVerticalStrut(10));
    contentPanel.add(javaPathLbl);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(javaPathField);
    contentPanel.add(Box.createVerticalStrut(12));
    contentPanel.add(aboutHeader);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(aboutArea);

    StyledButton closeBtn = new StyledButton(t("close"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
    closeBtn.setForeground(Color.BLACK);
    closeBtn.setPreferredSize(new Dimension(90, 32));
    closeBtn.addActionListener(e -> {
        config.setProperty("jvm_args", jvmField.getText().trim());
        config.setProperty("nickname", nickField.getText().trim());
        config.setProperty("java_path", javaPathField.getText().trim());
        saveConfig();
        settingsDialog.dispose();
    });

    JPanel bottom = new JPanel();
    bottom.setBackground(COLOR_BG);
    bottom.setBorder(new EmptyBorder(0, 0, 15, 0));
    bottom.add(closeBtn);

    settingsDialog.add(contentPanel, BorderLayout.CENTER);
    settingsDialog.add(bottom, BorderLayout.SOUTH);
    settingsDialog.setVisible(true);
}

private void updateLocalizedTexts() {
    setTitle(t("title"));
    versionsHeaderLabel.setText(t("versions"));
    newsHeaderLabel.setText(t("news"));
    nickLabel.setText(t("nickname"));
    statusLabel.setText(t("ready"));
    loginBtn.setText(t("login_btn"));
}

private JPanel createNewsCard(String title, String date, String fullContent) {
    RoundedPanel card = new RoundedPanel(10);
    card.setBackground(COLOR_BG);
    card.setLayout(new BorderLayout(4, 4));
    card.setBorder(new EmptyBorder(8, 10, 8, 10));
    card.setMaximumSize(new Dimension(375, 52));
    card.setCursor(new Cursor(Cursor.HAND_CURSOR));

    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
    titleLbl.setForeground(COLOR_TEXT);

    JLabel dateLbl = new JLabel(date);
    dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
    dateLbl.setForeground(COLOR_TEXT_MUTED);

    JLabel clickLbl = new JLabel("Read →");
    clickLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
    clickLbl.setForeground(COLOR_ACCENT);

    card.add(titleLbl, BorderLayout.NORTH);
    card.add(dateLbl, BorderLayout.WEST);
    card.add(clickLbl, BorderLayout.EAST);

    card.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            openNewsWindow(title, date, fullContent);
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            card.setBackground(new Color(0x2A, 0x2A, 0x33));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            card.setBackground(COLOR_BG);
        }
    });

    return card;
}

private void openNewsWindow(String title, String date, String content) {
    JDialog dialog = new JDialog(this, title, true);
    dialog.setSize(450, 320);
    dialog.setLocationRelativeTo(this);
    dialog.getContentPane().setBackground(COLOR_BG);
    dialog.setLayout(new BorderLayout(12, 12));

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(COLOR_BG);
    headerPanel.setBorder(new EmptyBorder(12, 12, 0, 12));

    JLabel titleLbl = new JLabel(title);
    titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
    titleLbl.setForeground(COLOR_TEXT);

    JLabel dateLbl = new JLabel(date);
    dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
    dateLbl.setForeground(COLOR_TEXT_MUTED);

    headerPanel.add(titleLbl, BorderLayout.NORTH);
    headerPanel.add(dateLbl, BorderLayout.SOUTH);

    JTextArea bodyArea = new JTextArea(content);
    bodyArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
    bodyArea.setForeground(COLOR_TEXT);
    bodyArea.setBackground(COLOR_PANEL);
    bodyArea.setEditable(false);
    bodyArea.setLineWrap(true);
    bodyArea.setWrapStyleWord(true);
    bodyArea.setMargin(new Insets(10, 10, 10, 10));

    JScrollPane scroll = new JScrollPane(bodyArea);
    scroll.setBorder(null);

    StyledButton closeBtn = new StyledButton(t("close"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
    closeBtn.setForeground(Color.BLACK);
    closeBtn.setPreferredSize(new Dimension(100, 32));
    closeBtn.addActionListener(e -> dialog.dispose());

    JPanel bottom = new JPanel();
    bottom.setBackground(COLOR_BG);
    bottom.setBorder(new EmptyBorder(0, 0, 10, 0));
    bottom.add(closeBtn);

    dialog.add(headerPanel, BorderLayout.NORTH);
    dialog.add(scroll, BorderLayout.CENTER);
    dialog.add(bottom, BorderLayout.SOUTH);
    dialog.setVisible(true);
}

private void fetchAndShowVersions() {
    JDialog dialog = new JDialog(this, t("select_ver_title"), true);
    dialog.setSize(440, 520);
    dialog.setLocationRelativeTo(this);
    dialog.getContentPane().setBackground(COLOR_BG);
    dialog.setLayout(new BorderLayout(10, 10));

    DefaultListModel<String> listModel = new DefaultListModel<>();
    Map<String, String> versionTypeMap = new HashMap<>();

    JList<String> versionList = new JList<>(listModel);
    versionList.setFont(new Font("SansSerif", Font.PLAIN, 14));
    versionList.setBackground(COLOR_PANEL);
    versionList.setForeground(COLOR_TEXT);
    versionList.setSelectionBackground(COLOR_ACCENT);
    versionList.setSelectionForeground(Color.BLACK);

    dialog.add(new JScrollPane(versionList), BorderLayout.CENTER);

    JLabel loadingLabel = new JLabel(t("loading_manifest"), SwingConstants.CENTER);
    loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    loadingLabel.setForeground(COLOR_TEXT_MUTED);
    loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
    dialog.add(loadingLabel, BorderLayout.NORTH);

    StyledButton selectBtn = new StyledButton("Add Version", COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
    selectBtn.setForeground(Color.BLACK);
    selectBtn.setEnabled(false);

    JPanel btnWrapper = new JPanel();
    btnWrapper.setBackground(COLOR_BG);
    btnWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
    btnWrapper.add(selectBtn);
    dialog.add(btnWrapper, BorderLayout.SOUTH);

    new Thread(() -> {
        try {
            String manifestJson = fetchUrlContent(MANIFEST_URL);
            Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"type\"\\s*:\\s*\"([^\"]+)\"").matcher(manifestJson);

            List<String> displayList = new ArrayList<>();
            while (matcher.find()) {
                String id = matcher.group(1);
                String type = matcher.group(2);
                String cat = categorizeVersion(id, type);

                if (isVersionInRange(id, cat)) {
                    versionTypeMap.put(id, cat);
                    displayList.add(id + " [" + cat + "]");
                }
            }

            SwingUtilities.invokeLater(() -> {
                for (String item : displayList) {
                    listModel.addElement(item);
                }
                loadingLabel.setText("Total versions found: " + displayList.size() + " (Range: rd-131655 to 1.0)");
                selectBtn.setEnabled(true);
            });
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> loadingLabel.setText("Error loading manifest: " + ex.getMessage()));
        }
    }).start();

    selectBtn.addActionListener(e -> {
        String selectedItem = versionList.getSelectedValue();
        if (selectedItem != null) {
            String verId = selectedItem.split(" \\[")[0];
            String cat = versionTypeMap.getOrDefault(verId, "Release");
            addVersionButtonUI(verId, cat);
            registerInstance(verId, cat, false);
            dialog.dispose();
        }
    });

    dialog.setVisible(true);
}

private boolean isVersionInRange(String versionId, String category) {
    if (versionId == null) return false;
    String lower = versionId.toLowerCase();

    // my glorious bullsiht
    if (lower.contains("betacraft") || lower.contains("planetnostalgia") || lower.contains("alphaplace")) {
        return false;
    }

    // Now, let the sink in...
    if (category.equals("Pre-Classic") || category.equals("Classic") || category.equals("Alpha") || category.equals("Beta")) {
        if (category.equals("Pre-Classic")) {
            return isPreClassicGreaterOrEqual(lower, "rd-131655");
        }
        return true;
    }

    // ONLY <= 1.0 (1.0.1, 1.0.2, etc.)
    if (category.equals("Release")) {
        return isReleaseUpToOneZero(lower);
    }

    // Yeah. Stripping 1.1+ because the bugs are fucking me out
    return false;
}

private boolean isReleaseUpToOneZero(String ver) {
    if (ver.equals("1.0") || ver.startsWith("1.0.")) {
        return true;
    }
    // checking for older releases
    if (ver.equals("rc1") || ver.equals("rc2") || ver.equals("1.0-rc1") || ver.equals("1.0-rc2")) {
        return true;
    }
    return false;
}

private boolean isPreClassicGreaterOrEqual(String ver, String target) {
    if (ver.equals(target)) return true;
    if (ver.startsWith("rd-") && target.startsWith("rd-")) {
        try {
            int vNum = Integer.parseInt(ver.replace("rd-", "").replaceAll("\\D+", ""));
            int tNum = Integer.parseInt(target.replace("rd-", "").replaceAll("\\D+", ""));
            return vNum >= tNum;
        } catch (Exception e) {
            return true;
        }
    }
    return true;
}

private void addVersionButtonUI(String verId, String category) {
    VersionButton btn = new VersionButton("<html><center>" + verId + "<br><font size='2' color='#949BA4'>" + category + "</font></center></html>", COLOR_ACCENT, COLOR_ACCENT_HOVER, 10, verId);
    btn.setForeground(Color.BLACK);
    // Grid of my hopes and dreames has been implemented
    btn.setPreferredSize(new Dimension(100, 50));
    btn.setFont(new Font("SansSerif", Font.BOLD, 12));

    btn.addActionListener(e -> {
        for (VersionButton vb : versionButtonList) {
            vb.setSelected(false);
        }
        btn.setSelected(true);

        new Thread(() -> prepareAndLaunch(verId, category)).start();
    });

    // RMB - calls a guy named "WindowGuy", and you can now delete versions! YAY!
    btn.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                showVersionContextMenu(e.getComponent(), e.getX(), e.getY(), verId, category);
            }
        }
    });

    versionButtonList.add(btn);
    versionsGrid.add(btn, versionsGrid.getComponentCount() - 1);
    versionsGrid.revalidate();
    versionsGrid.repaint();
}

private void showVersionContextMenu(Component invoker, int x, int y, String verId, String category) {
    JPopupMenu menu = new JPopupMenu();
    menu.setBackground(COLOR_PANEL);

    JMenuItem renameItem = new JMenuItem(t("ver_ctx_rename"));
    renameItem.setBackground(COLOR_PANEL);
    renameItem.setForeground(COLOR_TEXT);
    renameItem.addActionListener(e -> {
        String newName = (String) JOptionPane.showInputDialog(
            this,
            t("rename_prompt"),
            t("rename_title"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            verId
        );
        if (newName != null && !newName.trim().isEmpty()) {
            String trimmedNewName = newName.trim();
            for (InstanceData inst : instanceList) {
                if (inst.name.equalsIgnoreCase(verId)) {
                    inst.name = trimmedNewName;
                    break;
                }
            }
            saveInstances();
            refreshVersionsGridUI();
        }
    });

    JMenuItem deleteItem = new JMenuItem(t("ver_ctx_delete"));
    deleteItem.setBackground(COLOR_PANEL);
    deleteItem.setForeground(new Color(0xFF, 0x55, 0x55));
    deleteItem.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            t("ver_ctx_delete") + ": " + verId + "?",
            "Vexile",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            instanceList.removeIf(inst -> inst.name.equalsIgnoreCase(verId));
            saveInstances();
            refreshVersionsGridUI();
        }
    });

    menu.add(renameItem);
    menu.add(deleteItem);
    menu.show(invoker, x, y);
}

private void refreshVersionsGridUI() {
    versionsGrid.removeAll();
    versionButtonList.clear();

    StyledButton btnAdd = new StyledButton("+", COLOR_ADD_BTN, new Color(0x3A, 0x9E, 0x61), 16);
    btnAdd.setPreferredSize(new Dimension(60, 60));
    btnAdd.setFont(new Font("SansSerif", Font.BOLD, 24));
    btnAdd.setForeground(Color.BLACK);
    btnAdd.addActionListener(e -> fetchAndShowVersions());
    versionsGrid.add(btnAdd);

    for (InstanceData inst : instanceList) {
        addVersionButtonUI(inst.name, inst.category);
    }

    versionsGrid.revalidate();
    versionsGrid.repaint();
}

private boolean isVersionNewerOrEqual1_6(String versionId) {
    if (versionId == null) return false;
    String lower = versionId.toLowerCase();
    if (lower.startsWith("a") || lower.startsWith("b") || lower.startsWith("c") || lower.startsWith("rd-")) {
        return false;
    }

    try {
        String[] parts = versionId.split("\\.");
        if (parts.length >= 2 && parts[0].equals("1")) {
            int minor = Integer.parseInt(parts[1].replaceAll("\\D+", ""));
            return minor >= 6;
        }
    } catch (Exception ignored) {}

    return false;
}

private void prepareAndLaunch(String versionId, String category) {
    String username = nickField.getText().trim();
    if (username.isEmpty()) username = "amethyst-oak";

    File runDir = new File("run/.minecraft");
    File versionsDir = new File(runDir, "versions");
    File verDir = new File(versionsDir, versionId);
    File libDir = new File(runDir, "libraries");
    File assetsDir = new File(runDir, "assets");
    File jarFile = new File(verDir, versionId + ".jar");
    File nativesDir = new File(verDir, "natives");

    if (!verDir.exists()) verDir.mkdirs();
    if (!libDir.exists()) libDir.mkdirs();
    if (!nativesDir.exists()) nativesDir.mkdirs();
    if (!assetsDir.exists()) assetsDir.mkdirs();

    try {
        updateProgress(10, "Downloading " + versionId);
        updateStatus("Fetching version URL for " + versionId + "...");

        String jarUrl = MojangManifestParser.getClientJarUrl(versionId);

        updateStatus("Downloading " + versionId + "...");
        if (!jarFile.exists() || jarFile.length() == 0) {
            updateProgress(30, "Downloading " + versionId);
            downloadFile(jarUrl, jarFile);
        } else {
            updateStatus("Version " + versionId + " already exists, skipping download.");
        }

        updateProgress(50, "Downloading Libraries...");

        downloadLibrarySafe(libDir, "org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar", "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar");
        downloadLibrarySafe(libDir, "org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar", "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar");
        downloadLibrarySafe(libDir, "net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar", "https://libraries.minecraft.net/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar");

        downloadLibrarySafe(libDir, "net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar", "https://libraries.minecraft.net/net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar");
        downloadLibrarySafe(libDir, "com/google/guava/guava/17.0/guava-17.0.jar", "https://libraries.minecraft.net/com/google/guava/guava/17.0/guava-17.0.jar");
        downloadLibrarySafe(libDir, "com/google/code/gson/gson/2.2.4/gson-2.2.4.jar", "https://libraries.minecraft.net/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar");
        downloadLibrarySafe(libDir, "commons-io/commons-io/2.4/commons-io-2.4.jar", "https://libraries.minecraft.net/commons-io/commons-io/2.4/commons-io-2.4.jar");
        downloadLibrarySafe(libDir, "org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar", "https://libraries.minecraft.net/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar");
        downloadLibrarySafe(libDir, "com/mojang/authlib/1.5.25/authlib-1.5.25.jar", "https://libraries.minecraft.net/com/mojang/authlib/1.5.25/authlib-1.5.25.jar");
        downloadLibrarySafe(libDir, "org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar", "https://libraries.minecraft.net/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar");
        downloadLibrarySafe(libDir, "org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar", "https://libraries.minecraft.net/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar");

        File lwjglFile = new File(libDir, "org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar");
        File lwjglUtilFile = new File(libDir, "org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar");
        File jinputFile = new File(libDir, "net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar");
        File joptJar = new File(libDir, "net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar");
        File guavaJar = new File(libDir, "com/google/guava/guava/17.0/guava-17.0.jar");
        File gsonJar = new File(libDir, "com/google/code/gson/gson/2.2.4/gson-2.2.4.jar");
        File commonsIoJar = new File(libDir, "commons-io/commons-io/2.4/commons-io-2.4.jar");
        File lang3Jar = new File(libDir, "org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar");
        File authlibJar = new File(libDir, "com/mojang/authlib/1.5.25/authlib-1.5.25.jar");
        File log4jApiJar = new File(libDir, "org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar");
        File log4jCoreJar = new File(libDir, "org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar");

        File nativesJar = new File(verDir, "natives.jar");
        if (nativesDir.list() == null || nativesDir.list().length == 0) {
            updateStatus("Downloading & Extracting Natives...");
            downloadFile("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-linux.jar", nativesJar);
            extractZip(nativesJar, nativesDir);
            nativesJar.delete();
        }

        registerInstance(versionId, category, true);

        updateProgress(90, "Запуск...");
        updateStatus(t("launching") + versionId + " for " + username + "...");

        String javaPath = config.getProperty("java_path", "");
        if (javaPath.isEmpty() || !new File(javaPath).exists()) {
            javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + (isWindows() ? "javaw.exe" : "java");
            if (!new File(javaPath).exists()) {
                javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + (isWindows() ? "java.exe" : "java");
            }
        }

        String pathSeparator = isWindows() ? ";" : ":";
        String classPath = jarFile.getAbsolutePath() + pathSeparator +
                           joptJar.getAbsolutePath() + pathSeparator +
                           guavaJar.getAbsolutePath() + pathSeparator +
                           gsonJar.getAbsolutePath() + pathSeparator +
                           commonsIoJar.getAbsolutePath() + pathSeparator +
                           lang3Jar.getAbsolutePath() + pathSeparator +
                           authlibJar.getAbsolutePath() + pathSeparator +
                           log4jApiJar.getAbsolutePath() + pathSeparator +
                           log4jCoreJar.getAbsolutePath() + pathSeparator +
                           lwjglFile.getAbsolutePath() + pathSeparator +
                           lwjglUtilFile.getAbsolutePath() + pathSeparator +
                           jinputFile.getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(javaPath);

        String jvmArgsStr = config.getProperty("jvm_args", "-Xmx2G -XX:+UseG1GC");
        if (!jvmArgsStr.trim().isEmpty()) {
            String[] argsArr = jvmArgsStr.split("\\s+");
            command.addAll(Arrays.asList(argsArr));
        }

        command.add("-Djava.library.path=" + nativesDir.getAbsolutePath());

        boolean isNewVersion = isVersionNewerOrEqual1_6(versionId);
        String mainClass = isNewVersion ? "net.minecraft.client.main.Main" : "net.minecraft.client.Minecraft";

        command.add("-cp");
        command.add(classPath);
        command.add(mainClass);

        if (isNewVersion) {
            command.add("--username");
            command.add(username);
            command.add("--accessToken");
            command.add("0");
            command.add("--userProperties");
            command.add("{}");
            command.add("--version");
            command.add(versionId);
            command.add("--gameDir");
            command.add(runDir.getAbsolutePath());
            command.add("--assetsDir");
            command.add(assetsDir.getAbsolutePath());
            command.add("--assetIndex");
            command.add("1.8");
        } else {
            command.add(username);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(runDir);
        pb.inheritIO();
        pb.start();

        updateProgress(100, "Готово");
        try { Thread.sleep(1000); } catch (Exception ignored) {}
        hideProgress();
        updateStatus(t("ready"));

    } catch (Exception ex) {
        ex.printStackTrace();
        hideProgress();
        updateStatus("Error: " + ex.getMessage());
        JOptionPane.showMessageDialog(this, "Launch Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void downloadLibrarySafe(File libBaseDir, String relativePath, String primaryUrl) {
    File targetFile = new File(libBaseDir, relativePath);
    if (targetFile.exists() && targetFile.length() > 0) {
        return;
    }

    targetFile.getParentFile().mkdirs();

    List<String> mirrors = new ArrayList<>();
    mirrors.add(primaryUrl);
    mirrors.add("https://repo1.maven.org/maven2/" + relativePath);

    boolean success = false;
    for (String urlStr : mirrors) {
        try {
            downloadFile(urlStr, targetFile);
            if (targetFile.exists() && targetFile.length() > 0) {
                success = true;
                break;
            }
        } catch (Exception ignored) {}
    }

    if (!success) {
        System.out.println("Warning: Could not download library " + relativePath + " from any mirror.");
    }
}

private void extractZip(File zipFile, File destDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory() || entry.getName().startsWith("META-INF")) {
                continue;
            }
            File newFile = new File(destDir, entry.getName());
            try (FileOutputStream fos = new FileOutputStream(newFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
        }
    }
}

private String fetchUrlContent(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Vexile)");

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}

private void downloadFile(String fileUrl, File destination) throws IOException {
    URL url = new URL(fileUrl);
    HttpURLConnection http = (HttpURLConnection) url.openConnection();
    http.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
    http.setInstanceFollowRedirects(true);

    int responseCode = http.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
        downloadFile(http.getHeaderField("Location"), destination);
        return;
    }

    try (InputStream in = new BufferedInputStream(http.getInputStream());
         FileOutputStream out = new FileOutputStream(destination)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }
}

private void updateStatus(String text) {
    SwingUtilities.invokeLater(() -> statusLabel.setText(text));
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        VexileLauncher launcher = new VexileLauncher();
        if (new File(CONFIG_FILE).exists()) {
            launcher.setVisible(true);
        }
    });
}
}

class RoundedPanel extends JPanel {
private int cornerRadius;

public RoundedPanel(int radius) {
    super();
    this.cornerRadius = radius;
    setOpaque(false);
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
    g2.dispose();
}
}

class StyledButton extends JButton {

private Color normalColor;
private Color hoverColor;
private int cornerRadius;
private boolean isHovered = false;

public StyledButton(String text, Color normalColor, Color hoverColor, int cornerRadius) {
    super(text);
    this.normalColor = normalColor;
    this.hoverColor = hoverColor;
    this.cornerRadius = cornerRadius;

    setFont(new Font("SansSerif", Font.BOLD, 13));
    setForeground(Color.WHITE);
    setFocusPainted(false);
    setContentAreaFilled(false);
    setBorderPainted(false);
    setCursor(new Cursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            isHovered = true;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            isHovered = false;
            repaint();
        }
    });
}

@Override
protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isEnabled()) {
        g2.setColor(isHovered ? hoverColor : normalColor);
    } else {
        g2.setColor(new Color(0x3A, 0x3A, 0x42));
    }

    g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

    super.paintComponent(g2);
    g2.dispose();
}
}

class VersionButton extends StyledButton {
private boolean isSelected = false;
private String versionId;

public VersionButton(String text, Color normalColor, Color hoverColor, int cornerRadius, String versionId) {
    super(text, normalColor, hoverColor, cornerRadius);
    this.versionId = versionId;
}

public void setSelected(boolean selected) {
    this.isSelected = selected;
    repaint();
}

public boolean isSelectedVersion() {
    return isSelected;
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
        g2.dispose();
    }
}
}

class MojangManifestParser {
    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

public static String getClientJarUrl(String versionId) throws Exception {
    String manifestJson = fetchUrlContent(MANIFEST_URL);
    String versionJsonUrl = extractUrlForVersion(manifestJson, versionId);

    String versionJson = fetchUrlContent(versionJsonUrl);
    String clientJarUrl = extractClientUrl(versionJson);

    versionJson = versionJson.replaceAll("\"natives\":\\s*\\{[^}]*?\\}", "\"natives\": {\"linux\": \"natives\"}");
    return clientJarUrl;
}

private static String fetchUrlContent(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Vexile)");

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}

private static String extractUrlForVersion(String manifestJson, String versionId) {
    String regex1 = "\"id\"\\s*:\\s*\"" + Pattern.quote(versionId) + "\"[^}]*?\"url\"\\s*:\\s*\"(https://[^\"]+)\"";
    Matcher matcher = Pattern.compile(regex1).matcher(manifestJson);
    if (matcher.find()) return matcher.group(1);

    String regex2 = "\"url\"\\s*:\\s*\"(https://[^\"]+)\"[^}]*?\"id\"\\s*:\\s*\"" + Pattern.quote(versionId) + "\"";
    Matcher matcherAlternative = Pattern.compile(regex2).matcher(manifestJson);
    if (matcherAlternative.find()) return matcherAlternative.group(1);

    return null;
}

private static String extractClientUrl(String versionJson) {
    Matcher matcher = Pattern.compile("\"client\"\\s*:\\s*\\{[^}]*?\"url\"\\s*:\\s*\"(https://[^\"]+)\"").matcher(versionJson);
    if (matcher.find()) return matcher.group(1);
    return null;
}
}
