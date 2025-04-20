package ad.console;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.net.URL;
import javax.imageio.ImageIO;
import ad.service.MovieRecommendationService;
import ad.model.Movie;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;

public class ConsoleApp {
    private JFrame frame;
    private JComboBox<String> genreBox, decadeBox, languageBox;
    private JPanel resultsPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private MovieRecommendationService service;
    private JProgressBar progressBar;
    private Image backgroundImage;
    
    // Color scheme
    private Color backgroundColor = new Color(22, 25, 37);
    private Color accentColor = new Color(86, 93, 255);
    private Color cardColor = new Color(32, 35, 48);
    private Color textColor = new Color(230, 230, 245);
    private Color subtleTextColor = new Color(180, 180, 200);
    private Color buttonColor = new Color(86, 93, 255);
    private Color buttonHoverColor = new Color(106, 113, 255);
    private Color secondaryButtonColor = new Color(50, 53, 68);
    
    // Rich selection options
    private static final String[] GENRES = {
        "Action", "Adventure", "Animation", "Comedy", "Crime", 
        "Documentary", "Drama", "Family", "Fantasy", "History", 
        "Horror", "Music", "Mystery", "Romance", "Sci-Fi", "Sport", 
        "Thriller", "War", "Western"
    };
    
    private static final String[] DECADES = {
        "2020", "2010", "2000", "1990", "1980", "1970", "1960", "1950", "1940", "1930"
    };
    
    private static final String[] LANGUAGES = {
        "English", "Hindi", "Spanish", "French", "German", 
        "Italian", "Japanese", "Korean", "Chinese", "Russian", 
        "Portuguese", "Arabic", "Turkish"
    };
    
    public ConsoleApp() {
        service = new MovieRecommendationService();
        loadBackgroundImage();
        setCustomLookAndFeel();
        initializeUI();
    }
    
    private void loadBackgroundImage() {
        try {
            // Create an abstract background image programmatically
            backgroundImage = createAbstractBackground(1000, 800);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Image createAbstractBackground(int width, int height) {
        // Create an abstract pattern matching the dark theme
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        
        // Add abstract shapes with transparency
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        
        // Draw some circles and lines in theme colors
        g2d.setColor(accentColor);
        for (int i = 0; i < 8; i++) {
            int size = (int)(Math.random() * 300) + 100;
            int x = (int)(Math.random() * width) - size/2;
            int y = (int)(Math.random() * height) - size/2;
            g2d.fillOval(x, y, size, size);
        }
        
        // Add some abstract lines
        g2d.setStroke(new BasicStroke(3f));
        for (int i = 0; i < 12; i++) {
            g2d.setColor(new Color(
                accentColor.getRed(), 
                accentColor.getGreen(), 
                accentColor.getBlue(), 
                (int)(Math.random() * 40) + 10
            ));
            
            int x1 = (int)(Math.random() * width);
            int y1 = (int)(Math.random() * height);
            int x2 = (int)(Math.random() * width);
            int y2 = (int)(Math.random() * height);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        g2d.dispose();
        return image;
    }
    
    private void setCustomLookAndFeel() {
        try {
            // Set system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize UI elements
            UIManager.put("Panel.background", backgroundColor);
            UIManager.put("OptionPane.background", backgroundColor);
            UIManager.put("Button.background", buttonColor);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.select", buttonHoverColor);
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("ComboBox.background", cardColor);
            UIManager.put("ComboBox.foreground", Color.BLACK); // Changed to black as requested
            UIManager.put("ComboBox.selectionBackground", accentColor);
            UIManager.put("ComboBox.selectionForeground", Color.WHITE);
            UIManager.put("Label.foreground", textColor);
            UIManager.put("TextArea.background", cardColor);
            UIManager.put("TextArea.foreground", textColor);
            UIManager.put("ScrollPane.background", backgroundColor);
            UIManager.put("TextField.background", cardColor);
            UIManager.put("TextField.foreground", textColor);
            UIManager.put("ScrollBar.thumb", secondaryButtonColor);
            UIManager.put("ScrollBar.track", backgroundColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        frame = new JFrame("Random Movie Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new BorderLayout(0, 0));
        
        // Create a content pane with background image
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout(0, 0));
        backgroundPanel.setOpaque(false);
        frame.setContentPane(backgroundPanel);

        JPanel headerPanel = createHeaderPanel();
        frame.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false); // Make transparent to show background
        contentPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel filterPanel = createFilterPanel();
        contentPanel.add(filterPanel, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false); // Make transparent to show background
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        frame.add(contentPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready to recommend movies");
        statusLabel.setForeground(subtleTextColor);
        statusLabel.setBorder(new EmptyBorder(10, 20, 10, 20));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false); // Remove the percentage text
        progressBar.setVisible(false);
        progressBar.setForeground(accentColor);
        progressBar.setBackground(backgroundColor);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(cardColor);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        frame.add(statusPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
    }
    
    private void getRecommendations() {
            statusLabel.setText("Fetching recommendations...");
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);

            resultsPanel.removeAll();
            resultsPanel.revalidate();
            resultsPanel.repaint();

            String genre = getSelectedValue(genreBox);
            String decade = getSelectedValue(decadeBox);
            String language = getSelectedValue(languageBox);
        

        SwingWorker<List<Movie>, Void> worker = new SwingWorker<List<Movie>, Void>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                return service.getRecommendedMovies(genre, decade, language);
            }

            @Override
            protected void done() {
                try {
                    List<Movie> recommendations = get();
                    resultsPanel.removeAll();

                    if (recommendations.isEmpty()) {
                        JLabel noResults = new JLabel("No movies found matching your criteria");
                        noResults.setForeground(textColor);
                        noResults.setAlignmentX(Component.CENTER_ALIGNMENT);
                        noResults.setFont(new Font("SansSerif", Font.BOLD, 16));
                        resultsPanel.add(noResults);
                        statusLabel.setText("No results found");
                    } else {
                        JLabel resultsCountLabel = new JLabel(recommendations.size() + " RECOMMENDATIONS");
                        resultsCountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                        resultsCountLabel.setForeground(textColor);
                        resultsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        resultsPanel.add(resultsCountLabel);
                        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

                        for (Movie movie : recommendations) {
                            JPanel moviePanel = createMoviePanel(movie);
                            moviePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                            resultsPanel.add(moviePanel);
                            resultsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                        }

                        statusLabel.setText("Found " + recommendations.size() + " recommendations");
                    }

                } catch (Exception ex) {
                    JLabel errorLabel = new JLabel("Error: " + ex.getMessage());
                    errorLabel.setForeground(new Color(255, 100, 100));
                    resultsPanel.add(errorLabel);
                    statusLabel.setText("Error occurred during search");
                } finally {
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            }
        };

        worker.execute();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(cardColor);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Movie Recommender");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            createRoundedBorder(cardColor, 10),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // First row - all filters in one line
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel genreLabel = new JLabel("GENRE");
        genreLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        genreLabel.setForeground(subtleTextColor);
        panel.add(genreLabel, gbc);
        
        gbc.gridx = 1;
        genreBox = createStyledComboBox(GENRES, true);
        panel.add(genreBox, gbc);
        
        gbc.gridx = 2;
        JLabel decadeLabel = new JLabel("DECADE");
        decadeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        decadeLabel.setForeground(subtleTextColor);
        panel.add(decadeLabel, gbc);
        
        gbc.gridx = 3;
        decadeBox = createStyledComboBox(DECADES, true);
        panel.add(decadeBox, gbc);
        
        gbc.gridx = 4;
        JLabel languageLabel = new JLabel("LANGUAGE");
        languageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        languageLabel.setForeground(subtleTextColor);
        panel.add(languageLabel, gbc);
        
        gbc.gridx = 5;
        languageBox = createStyledComboBox(LANGUAGES, true);
        panel.add(languageBox, gbc);
        
        // Button row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6; // Span all columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(30, 10, 10, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(cardColor);
        
        JButton searchButton = createStyledButton("Recommend movies!", buttonColor, Color.WHITE);
        searchButton.addActionListener(e -> getRecommendations());
        buttonPanel.add(searchButton);
        
        JButton resetButton = createStyledButton("RESET FILTERS", secondaryButtonColor, textColor);
        resetButton.addActionListener(e -> resetFilters());
        buttonPanel.add(resetButton);
        
        panel.add(buttonPanel, gbc);
        
        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items, boolean addAny) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (addAny) {
            model.addElement("Any");
        }
        for (String item : items) {
            model.addElement(item);
        }
        
        JComboBox<String> comboBox = new JComboBox<>(model);
        comboBox.setBackground(cardColor);
        comboBox.setForeground(Color.BLACK); // Changed to black as requested
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 80)));
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        c.setBackground(accentColor);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(245, 245, 250)); // Soft white background
                        c.setForeground(Color.BLACK);
                    }
                    return c;
                }
            });
     
        
        return comboBox;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (bgColor.equals(buttonColor)) {
                    button.setBackground(buttonHoverColor);
                } else {
                    // For secondary buttons
                    Color brighterColor = new Color(
                        Math.min(bgColor.getRed() + 20, 255),
                        Math.min(bgColor.getGreen() + 20, 255),
                        Math.min(bgColor.getBlue() + 20, 255)
                    );
                    button.setBackground(brighterColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private Border createRoundedBorder(Color color, int radius) {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(radius/2, radius/2, radius/2, radius/2);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }
    
    private DefaultComboBoxModel<String> createComboModel(String[] items, boolean addAny) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        if (addAny) {
            model.addElement("Any");
        }
        for (String item : items) {
            model.addElement(item);
        }
        return model;
    }
    
    private String getSelectedValue(JComboBox<String> comboBox) {
        String selected = (String) comboBox.getSelectedItem();
        return "Any".equals(selected) ? null : selected;
    }
    
    private JPanel createMoviePanel(Movie movie) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            createRoundedBorder(new Color(50, 55, 70), 12),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // Try to load poster if URL is valid
        JPanel posterContainer = new JPanel(new BorderLayout());
        posterContainer.setBackground(cardColor);
        posterContainer.setBorder(BorderFactory.createEmptyBorder());
        
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                ImageIcon posterIcon = loadImageFromUrl(movie.getPosterUrl());
                if (posterIcon != null) {
                    JLabel posterLabel = new JLabel(posterIcon);
                    posterLabel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60), 1));
                    posterContainer.add(posterLabel, BorderLayout.CENTER);
                    panel.add(posterContainer, BorderLayout.WEST);
                }
            } catch (Exception e) {
                // If poster can't be loaded, continue without it
                System.err.println("Could not load poster for " + movie.getTitle() + ": " + e.getMessage());
            }
        }
        
        // Movie info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(cardColor);
        infoPanel.setBorder(new EmptyBorder(0, 5, 0, 10));
        
        // Movie title
        JLabel titleLabel = new JLabel(movie.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Rating indicator
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingPanel.setBackground(cardColor);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel ratingLabel = new JLabel(movie.getRating());
        ratingLabel.setForeground(new Color(255, 215, 0)); // Gold color for rating
        ratingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JLabel durationLabel = new JLabel(" | " + movie.getDuration());
        durationLabel.setForeground(subtleTextColor);
        
        ratingPanel.add(new JLabel("★ "));
        ratingPanel.add(ratingLabel);
        ratingPanel.add(durationLabel);
        infoPanel.add(ratingPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Director
        JPanel directorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        directorPanel.setBackground(cardColor);
        directorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel directorHeaderLabel = new JLabel("Director: ");
        directorHeaderLabel.setForeground(subtleTextColor);
        
        JLabel directorNameLabel = new JLabel(movie.getDirector());
        directorNameLabel.setForeground(textColor);
        
        directorPanel.add(directorHeaderLabel);
        directorPanel.add(directorNameLabel);
        infoPanel.add(directorPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Cast (truncated if too long)
        String castText = movie.getCast();
        if (castText.length() > 100) {
            castText = castText.substring(0, 97) + "...";
        }
        JPanel castPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        castPanel.setBackground(cardColor);
        castPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel castHeaderLabel = new JLabel("Cast: ");
        castHeaderLabel.setForeground(subtleTextColor);
        
        JLabel castNamesLabel = new JLabel(castText);
        castNamesLabel.setForeground(textColor);
        
        castPanel.add(castHeaderLabel);
        castPanel.add(castNamesLabel);
        infoPanel.add(castPanel);
        
        // Description preview (if available)
        if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
            infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            String descPreview = movie.getDescription();
            if (descPreview.length() > 150) {
                descPreview = descPreview.substring(0, 147) + "...";
            }
            JTextArea descArea = new JTextArea(descPreview);
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setEditable(false);
            descArea.setBackground(cardColor);
            descArea.setForeground(new Color(200, 200, 200));
            descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            descArea.setBorder(BorderFactory.createEmptyBorder());
            infoPanel.add(descArea);
        }
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // View details button
        JButton detailsButton = createStyledButton("VIEW DETAILS", accentColor, Color.WHITE);
        detailsButton.setPreferredSize(new Dimension(120, 30));
        detailsButton.addActionListener(e -> showMovieDetails(movie));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(cardColor);
        buttonPanel.add(detailsButton);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private ImageIcon loadImageFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            Image image = ImageIO.read(url);
            // Scale to appropriate size for the UI
            Image scaledImage = image.getScaledInstance(120, 170, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            return null;
        }
    }
    private void showMovieDetails(Movie movie) {
        JDialog dialog = new JDialog(frame, movie.getTitle(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 600);
        dialog.getContentPane().setBackground(backgroundColor);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Top panel with poster and essential info
        JPanel topPanel = new JPanel(new BorderLayout(25, 0));
        topPanel.setBackground(cardColor);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            createRoundedBorder(cardColor, 12),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Try to load poster if available
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                ImageIcon posterIcon = loadDetailedPosterImage(movie.getPosterUrl());
                if (posterIcon != null) {
                    JLabel posterLabel = new JLabel(posterIcon);
                    posterLabel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1));
                    topPanel.add(posterLabel, BorderLayout.WEST);
                }
            } catch (Exception e) {
                // Continue without poster
            }
        }
        
        // Movie essential details panel
        JPanel essentialDetailsPanel = new JPanel();
        essentialDetailsPanel.setLayout(new BoxLayout(essentialDetailsPanel, BoxLayout.Y_AXIS));
        essentialDetailsPanel.setBackground(cardColor);
        
        // Title
        JLabel titleLabel = new JLabel(movie.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        essentialDetailsPanel.add(titleLabel);
        essentialDetailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Rating with star icon
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        ratingPanel.setBackground(cardColor);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel starLabel = new JLabel("★");
        starLabel.setForeground(new Color(255, 215, 0));
        starLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        JLabel ratingValueLabel = new JLabel(movie.getRating());
        ratingValueLabel.setForeground(new Color(255, 215, 0));
        ratingValueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        ratingPanel.add(starLabel);
        ratingPanel.add(ratingValueLabel);
        essentialDetailsPanel.add(ratingPanel);
        essentialDetailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Duration
        JLabel durationLabel = new JLabel("Duration: " + movie.getDuration());
        durationLabel.setForeground(textColor);
        durationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        essentialDetailsPanel.add(durationLabel);
        essentialDetailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Director
        JLabel directorLabel = new JLabel("Director: " + movie.getDirector());
        directorLabel.setForeground(textColor);
        directorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        essentialDetailsPanel.add(directorLabel);
        essentialDetailsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        topPanel.add(essentialDetailsPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with additional info
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(backgroundColor);
        detailsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Cast panel
        JPanel castPanel = new JPanel(new BorderLayout());
        castPanel.setBackground(cardColor);
        castPanel.setBorder(BorderFactory.createCompoundBorder(
            createRoundedBorder(cardColor, 12),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel castTitleLabel = new JLabel("Cast");
        castTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        castTitleLabel.setForeground(subtleTextColor);
        castPanel.add(castTitleLabel, BorderLayout.NORTH);
        
        // Add cast content to a text area with a scroll pane
        JTextArea castTextArea = new JTextArea();
        castTextArea.setText(movie.getCast());
        castTextArea.setLineWrap(true);
        castTextArea.setWrapStyleWord(true);
        castTextArea.setEditable(false);
        castTextArea.setBackground(cardColor);
        castTextArea.setForeground(textColor);
        castTextArea.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JScrollPane castScrollPane = new JScrollPane(castTextArea);
        castScrollPane.setBorder(BorderFactory.createEmptyBorder());
        castScrollPane.setBackground(cardColor);
        
        castPanel.add(castScrollPane, BorderLayout.CENTER);
        castPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        castPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));
        
        detailsPanel.add(castPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Description panel
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(cardColor);
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            createRoundedBorder(cardColor, 12),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel descTitleLabel = new JLabel("Description");
        descTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        descTitleLabel.setForeground(subtleTextColor);
        descPanel.add(descTitleLabel, BorderLayout.NORTH);
        
        // Add description content to a text area with a scroll pane
        JTextArea descTextArea = new JTextArea();
        descTextArea.setText(movie.getDescription());
        descTextArea.setLineWrap(true);
        descTextArea.setWrapStyleWord(true);
        descTextArea.setEditable(false);
        descTextArea.setBackground(cardColor);
        descTextArea.setForeground(textColor);
        descTextArea.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JScrollPane descScrollPane = new JScrollPane(descTextArea);
        descScrollPane.setBorder(BorderFactory.createEmptyBorder());
        descScrollPane.setBackground(cardColor);
        
        descPanel.add(descScrollPane, BorderLayout.CENTER);
        
        detailsPanel.add(descPanel);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        
        JButton closeButton = createStyledButton("CLOSE", buttonColor, Color.WHITE);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    private ImageIcon loadDetailedPosterImage(String urlString) {
        try {
            URL url = new URL(urlString);
            Image image = ImageIO.read(url);
            // Scale to appropriate size for the details dialog
            Image scaledImage = image.getScaledInstance(180, 260, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void resetFilters() {
        genreBox.setSelectedIndex(0);
        decadeBox.setSelectedIndex(0);
        languageBox.setSelectedIndex(0);
        
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
        
        statusLabel.setText("Filters reset. Ready to recommend movies.");
    }
    
    public void display() {
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ConsoleApp app = new ConsoleApp();
                app.display();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting application: " + e.getMessage(), 
                    "Application Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}