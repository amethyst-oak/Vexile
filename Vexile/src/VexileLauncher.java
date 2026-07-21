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
    private static final String CURRENT_VERSION = "1.1";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/amethyst-oak/Vexile/main/version.txt";
    private static final String REPO_URL = "https://github.com/amethyst-oak/Vexile";

    // Palette
    private static final Color COLOR_BG = new Color(0x18, 0x18, 0x1C);
    private static final Color COLOR_PANEL = new Color(0x23, 0x23, 0x2A);
    private static final Color COLOR_ACCENT = new Color(0x58, 0x65, 0xF2);
    private static final Color COLOR_ACCENT_HOVER = new Color(0x47, 0x52, 0xC4);
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

    // RetroMode state
    private boolean isRetroMode = true;
    private JPanel serversContainer;
    private StyledButton retroBtn;

    // UI elements
    private JLabel versionsHeaderLabel;
    private JLabel serversHeaderLabel;
    private JLabel newsHeaderLabel;
    private JLabel nickLabel;
    private StyledButton loginBtn;

    private static final String[] LANGUAGES = {
        "English (en)", "Русский (ru)", "Українська (uk)", "Беларуская (be)",
        "Polski (pl)", "Deutsch (de)", "Français (fr)", "Español (es)"
    };

    private static final Map<String, Map<String, String>> TRANSLATIONS = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();
        en.put("title", "Vexile");
        en.put("versions", "VERSIONS");
        en.put("servers", "FEATURED SERVERS");
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
        TRANSLATIONS.put("en", en);

        Map<String, String> ru = new HashMap<>();
        ru.put("title", "Vexile");
        ru.put("versions", "ВЕРСИИ");
        ru.put("servers", "СПИСОК СЕРВЕРОВ");
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
        TRANSLATIONS.put("ru", ru);
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

        File cfgFile = new File(CONFIG_FILE);
        if (!cfgFile.exists()) {
            showSetupWizard();
        } else {
            initUI();
            setVisible(true);
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
                isRetroMode = Boolean.parseBoolean(config.getProperty("retromode", config.getProperty("oldify", "true")));
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
            config.setProperty("retromode", String.valueOf(isRetroMode));
            config.store(out, "Vexile Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadInstances() {
        File file = new File(INSTANCES_FILE);
        if (!file.exists()) return;

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
                instanceList.add(new InstanceData(name, category, amount, downloaded));
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
            config.setProperty("nickname", "Player");
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

        // --- ЛЕВАЯ ПАНЕЛЬ: ВЕРСИИ ---
        RoundedPanel versionsPanel = new RoundedPanel(16);
        versionsPanel.setBackground(COLOR_PANEL);
        versionsPanel.setLayout(new BorderLayout(6, 6));
        versionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        versionsHeaderLabel = new JLabel(t("versions"));
        versionsHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        versionsHeaderLabel.setForeground(COLOR_TEXT_MUTED);
        versionsPanel.add(versionsHeaderLabel, BorderLayout.NORTH);

        versionsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        versionsGrid.setBackground(COLOR_PANEL);

        JScrollPane scrollGrid = new JScrollPane(versionsGrid);
        scrollGrid.setBorder(null);
        scrollGrid.getViewport().setBackground(COLOR_PANEL);
        versionsPanel.add(scrollGrid, BorderLayout.CENTER);

        // Кнопка добавления +
        StyledButton btnAdd = new StyledButton("+", COLOR_ADD_BTN, new Color(0x43, 0xB5, 0x81), 16);
        btnAdd.setPreferredSize(new Dimension(60, 60));
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.addActionListener(e -> fetchAndShowVersions());
        versionsGrid.add(btnAdd);

        for (InstanceData inst : instanceList) {
            addVersionButtonUI(inst.name, inst.category);
        }

        JPanel nickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nickPanel.setBackground(COLOR_PANEL);

        nickLabel = new JLabel(t("nickname"));
        nickLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nickLabel.setForeground(COLOR_TEXT);

        nickField = new JTextField(config.getProperty("nickname", "Player"), 11);
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

        // --- ПРАВАЯ ПАНЕЛЬ: СЕРВЕРА СВЕРХУ + НОВОСТИ СНИЗУ ---
        RoundedPanel rightPanel = new RoundedPanel(16);
        rightPanel.setBackground(COLOR_PANEL);
        rightPanel.setLayout(new BorderLayout(8, 8));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel rightContent = new JPanel();
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));
        rightContent.setBackground(COLOR_PANEL);

        JPanel serverHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        serverHeaderPanel.setBackground(COLOR_PANEL);
        serverHeaderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        serversHeaderLabel = new JLabel(t("servers"));
        serversHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        serversHeaderLabel.setForeground(COLOR_TEXT_MUTED);

        retroBtn = new StyledButton(getRetroBtnText(), isRetroMode ? COLOR_ACCENT : COLOR_BG, COLOR_ACCENT_HOVER, 8);
        retroBtn.setPreferredSize(new Dimension(110, 24));
        retroBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        retroBtn.addActionListener(e -> toggleRetroMode());

        serverHeaderPanel.add(serversHeaderLabel);
        serverHeaderPanel.add(Box.createHorizontalStrut(90));
        serverHeaderPanel.add(retroBtn);

        rightContent.add(serverHeaderPanel);
        rightContent.add(Box.createVerticalStrut(4));

        serversContainer = new JPanel();
        serversContainer.setLayout(new BoxLayout(serversContainer, BoxLayout.Y_AXIS));
        serversContainer.setBackground(COLOR_PANEL);
        serversContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightContent.add(serversContainer);
        refreshServersList();

        rightContent.add(Box.createVerticalStrut(10));

        // Блок новостей
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
                "Vexile 1.0 Release",
                "July 2026",
                "★ Vexile 1.0 is officially released! ★\n\nFeatures:\n• Modern dark rounded UI.\n• Dynamic RetroMode: Toggle between modern servers/versions and retro Alpha/Beta/Classic modes instantly!\n• Dynamic version downloading straight from Mojang manifest."
        ));
        newsListContainer.add(Box.createVerticalStrut(4));
        newsListContainer.add(createNewsCard(
                "Multiplayer & Tunnel Support",
                "July 2026",
                "Connect with friends effortlessly!\n\nVexile natively supports customized server tunnel configurations for retro Alpha/Beta multiplayer gameplay."
        ));

        JScrollPane newsScroll = new JScrollPane(newsListContainer);
        newsScroll.setBorder(null);
        newsScroll.getViewport().setBackground(COLOR_PANEL);
        newsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContent.add(newsScroll);

        rightPanel.add(rightContent, BorderLayout.CENTER);

        // Нижняя панель справки/входа в Premium
        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomRightPanel.setBackground(COLOR_PANEL);

        loginBtn = new StyledButton(t("login_btn"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 8);
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

        // --- СТАТУС БАР ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusPanel.setBackground(COLOR_BG);
        statusLabel = new JLabel(t("ready"));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private String getRetroBtnText() {
        return "RetroMode: " + (isRetroMode ? "ON" : "OFF");
    }

    private void toggleRetroMode() {
        isRetroMode = !isRetroMode;
        saveConfig();
        retroBtn.setText(getRetroBtnText());
        refreshServersList();
        updateStatus("RetroMode set to: " + (isRetroMode ? "ENABLED (Retro Alpha/Beta/1.0 Servers)" : "DISABLED (Modern Servers 1.8+)"));
    }

    private void refreshServersList() {
        serversContainer.removeAll();

        if (isRetroMode) {
            serversContainer.add(createServerCard("Alpha Place", "alphaplace.net", "Alpha 1.1.2_01", false, true));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("AlwaysAlpha", "alwaysalpha.net", "Alpha 1.1.2_01", false, true));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("Planet Nostalgia", "planetnostalgia.xyz", "a1.1.2_01", false, true));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("Betacraft", "betacraft.uk", "Beta 1.7.3", true, false));
        } else {
            serversContainer.add(createServerCard("Hypixel Network", "mc.hypixel.net", "1.8 - 1.20+", true, false));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("2b2t", "2b2t.org", "1.12.2 / 1.20+", true, false));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("Complex MC", "hub.mc-complex.com", "1.18 - 1.20+", false, true));
            serversContainer.add(Box.createVerticalStrut(4));
            serversContainer.add(createServerCard("CubeCraft", "play.cubecraft.net", "1.8 - 1.20+", true, false));
        }

        serversContainer.revalidate();
        serversContainer.repaint();
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

        StyledButton msBtn = new StyledButton(t("login_ms"), new Color(0x00, 0x67, 0xB8), new Color(0x00, 0x5A, 0x9E), 8);
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
        settingsDialog.setSize(400, 390);
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
        langBox.setMaximumSize(new Dimension(340, 30));
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
        jvmField.setMaximumSize(new Dimension(340, 30));
        jvmField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        jvmField.setBackground(COLOR_PANEL);
        jvmField.setForeground(COLOR_TEXT);
        jvmField.setCaretColor(COLOR_TEXT);
        jvmField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BG, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        jvmField.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(jvmLbl);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(jvmField);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(aboutHeader);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(aboutArea);

        StyledButton closeBtn = new StyledButton(t("close"), COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
        closeBtn.setPreferredSize(new Dimension(90, 32));
        closeBtn.addActionListener(e -> {
            config.setProperty("jvm_args", jvmField.getText().trim());
            config.setProperty("nickname", nickField.getText().trim());
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
        serversHeaderLabel.setText(t("servers"));
        newsHeaderLabel.setText(t("news"));
        nickLabel.setText(t("nickname"));
        statusLabel.setText(t("ready"));
        loginBtn.setText(t("login_btn"));
    }

    private JPanel createServerCard(String name, String ip, String info, boolean isPremium, boolean isCracked) {
        RoundedPanel card = new RoundedPanel(10);
        card.setBackground(COLOR_BG);
        card.setLayout(new BorderLayout(6, 0));
        card.setBorder(new EmptyBorder(5, 8, 5, 8));
        card.setMaximumSize(new Dimension(375, 44));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COLOR_BG);

        JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        titleLine.setBackground(COLOR_BG);

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(COLOR_TEXT);
        titleLine.add(nameLbl);

        if (isPremium) {
            JLabel premBadge = new JLabel("★ PREMIUM");
            premBadge.setFont(new Font("SansSerif", Font.BOLD, 9));
            premBadge.setForeground(Color.BLACK);
            premBadge.setOpaque(true);
            premBadge.setBackground(COLOR_GOLD);
            premBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
            titleLine.add(premBadge);
        }

        if (isCracked) {
            JLabel crackedLbl = new JLabel("[Cracked]");
            crackedLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            crackedLbl.setForeground(COLOR_ADD_BTN);
            titleLine.add(crackedLbl);
        }

        JLabel ipLbl = new JLabel(ip + " • " + info);
        ipLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        ipLbl.setForeground(COLOR_TEXT_MUTED);

        infoPanel.add(titleLine);
        infoPanel.add(ipLbl);

        StyledButton copyBtn = new StyledButton("Copy IP", COLOR_ACCENT, COLOR_ACCENT_HOVER, 6);
        copyBtn.setPreferredSize(new Dimension(65, 24));
        copyBtn.setFont(new Font("SansSerif", Font.BOLD, 10));

        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ip), null);
            updateStatus("Copied IP: " + ip);
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(copyBtn, BorderLayout.EAST);

        return card;
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
        versionList.setSelectionForeground(Color.WHITE);

        dialog.add(new JScrollPane(versionList), BorderLayout.CENTER);

        JLabel loadingLabel = new JLabel(t("loading_manifest"), SwingConstants.CENTER);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        loadingLabel.setForeground(COLOR_TEXT_MUTED);
        loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialog.add(loadingLabel, BorderLayout.NORTH);

        StyledButton selectBtn = new StyledButton("Add Version", COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
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

                    if (isRetroMode) {
                        if (cat.equals("Alpha") || cat.equals("Beta") || cat.equals("Classic") || cat.equals("Pre-Classic") || id.startsWith("1.0") || id.startsWith("1.1")) {
                            versionTypeMap.put(id, cat);
                            displayList.add(id + " [" + cat + "]");
                        }
                    } else {
                        if (!cat.equals("Alpha") && !cat.equals("Beta") && !cat.equals("Classic") && !cat.equals("Pre-Classic") && !id.startsWith("1.0") && !id.startsWith("1.1") && !id.startsWith("1.2") && !id.startsWith("1.3") && !id.startsWith("1.4") && !id.startsWith("1.5") && !id.startsWith("1.6") && !id.startsWith("1.7")) {
                            versionTypeMap.put(id, cat);
                            displayList.add(id + " [" + cat + "]");
                        }
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    for (String item : displayList) {
                        listModel.addElement(item);
                    }
                    loadingLabel.setText("Total versions found: " + displayList.size() + (isRetroMode ? " (RetroMode Active)" : " (Modern 1.8+ Mode Active)"));
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

    private void addVersionButtonUI(String verId, String category) {
        StyledButton btn = new StyledButton("<html><center>" + verId + "<br><font size='2' color='#949BA4'>" + category + "</font></center></html>", COLOR_ACCENT, COLOR_ACCENT_HOVER, 10);
        btn.setPreferredSize(new Dimension(100, 60));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));

        btn.addActionListener(e -> new Thread(() -> prepareAndLaunch(verId, category)).start());

        versionsGrid.add(btn, versionsGrid.getComponentCount() - 1);
        versionsGrid.revalidate();
        versionsGrid.repaint();
    }

    private void prepareAndLaunch(String versionId, String category) {
        String username = nickField.getText().trim();
        if (username.isEmpty()) username = "Player";

        File runDir = new File("run/.minecraft");
        File binDir = new File(runDir, "bin");
        File jarFile = new File(binDir, "minecraft.jar");
        File lwjglFile = new File(binDir, "lwjgl.jar");
        File lwjglUtilFile = new File(binDir, "lwjgl_util.jar");
        File jinputFile = new File(binDir, "jinput.jar");
        File nativesDir = new File(binDir, "natives");

        if (!binDir.exists()) binDir.mkdirs();
        if (!nativesDir.exists()) nativesDir.mkdirs();

        try {
            updateStatus("Fetching version URL for " + versionId + "...");

            String jarUrl = MojangManifestParser.getClientJarUrl(versionId);

            updateStatus("Downloading " + versionId + "...");
            downloadFile(jarUrl, jarFile);

            if (!lwjglFile.exists()) {
                downloadFile("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar", lwjglFile);
            }
            if (!lwjglUtilFile.exists()) {
                downloadFile("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar", lwjglUtilFile);
            }
            if (!jinputFile.exists()) {
                downloadFile("https://libraries.minecraft.net/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar", jinputFile);
            }

            File nativesJar = new File(binDir, "natives-linux.jar");
            if (!new File(nativesDir, "liblwjgl.so").exists() && !new File(nativesDir, "lwjgl.dll").exists()) {
                updateStatus("Downloading & Extracting Natives...");
                downloadFile("https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-linux.jar", nativesJar);
                extractZip(nativesJar, nativesDir);
                nativesJar.delete();
            }

            registerInstance(versionId, category, true);

            updateStatus(t("launching") + versionId + " for " + username + "...");

            String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String classPath = jarFile.getAbsolutePath() + ":" +
                               lwjglFile.getAbsolutePath() + ":" +
                               lwjglUtilFile.getAbsolutePath() + ":" +
                               jinputFile.getAbsolutePath();

            List<String> command = new ArrayList<>();
            command.add(javaPath);

            String jvmArgsStr = config.getProperty("jvm_args", "-Xmx2G -XX:+UseG1GC");
            if (!jvmArgsStr.trim().isEmpty()) {
                String[] argsArr = jvmArgsStr.split("\\s+");
                command.addAll(Arrays.asList(argsArr));
            }

            command.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
            command.add("-cp");
            command.add(classPath);
            command.add("net.minecraft.client.Minecraft");
            command.add(username);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(runDir);
            pb.inheritIO();
            pb.start();

            updateStatus(t("ready"));

        } catch (Exception ex) {
            ex.printStackTrace();
            updateStatus("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Launch Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

class MojangManifestParser {

    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static String getClientJarUrl(String versionId) throws Exception {
        String manifestJson = fetchUrlContent(MANIFEST_URL);
        String versionJsonUrl = extractUrlForVersion(manifestJson, versionId);

        if (versionJsonUrl == null) {
            throw new Exception("Version " + versionId + " not found!");
        }

        String versionJson = fetchUrlContent(versionJsonUrl);
        String clientJarUrl = extractClientUrl(versionJson);

        if (clientJarUrl == null) {
            throw new Exception("client.jar URL not found for " + versionId);
        }

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
        String regex = "\"id\"\\s*:\\s*\"" + Pattern.quote(versionId) + "\"[^}]*?\"url\"\\s*:\\s*\"(https://[^\"]+)\"";
        Matcher matcher = Pattern.compile(regex).matcher(manifestJson);
        if (matcher.find()) return matcher.group(1);

        regex = "\"url\"\\s*:\\s*\"(https://[^\"]+)\"[^}]*?\"id\"\\s*:\\s*\"" + Pattern.quote(versionId) + "\"";
        matcher = Pattern.compile(regex).matcher(manifestJson);
        if (matcher.find()) return matcher.group(1);

        return null;
    }

    private static String extractClientUrl(String versionJson) {
        Matcher matcher = Pattern.compile("\"client\"\\s*:\\s*\\{[^}]*?\"url\"\\s*:\\s*\"(https://[^\"]+)\"").matcher(versionJson);
        if (matcher.find()) return matcher.group(1);
        return null;
    }
}
