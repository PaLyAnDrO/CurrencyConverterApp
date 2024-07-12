import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CurrencyConverterApp extends JFrame {
    protected JLabel labelAmount, labelResult, labelAmountResult, labelCurrentExchangeRate, labelExchangeRate;
    protected JTextField textFieldAmount, textFieldPhoneNumber;
    protected JComboBox<String> comboBoxFrom, comboBoxTo;
    protected JButton buttonConvert, buttonClearHistory, buttonShowExchangeRate;
    protected JTextArea textAreaHistory;
    protected JTabbedPane tabbedPane;

    private static final String TRANSACTION_HISTORY_FILE = "transaction_history.txt";
    private static final String TRANSACTION_COUNTER_FILE = "transaction_counter.txt";

    private static final String[] CURRENCIES = {"USD", "EUR", "PLN", "UAH"};
    private static double[] cashAmounts = {10000.0, 10000.0, 40000.0, 500000.0}; // USD, EUR, PLN, UAH

    private static final double[][] EXCHANGE_RATES = {
            {1.0, 0.92, 3.94, 39.57}, // USD
            {1.08, 1.0, 4.27, 42.85},  // EUR
            {0.25, 0.23, 1.0, 10.03},  // PLN
            {0.025, 0.023, 0.099, 1.0} // UAH
    };
    private static int transactionCounter = loadTransactionCounter();

    public CurrencyConverterApp() {
        setTitle("Currency Converter");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        labelAmount = new JLabel("Amount:");
        labelAmount.setPreferredSize(new Dimension(100, 30));
        labelResult = new JLabel("Result:");
        labelResult.setPreferredSize(new Dimension(100, 30));
        labelCurrentExchangeRate = new JLabel("Exchange rate:");
        labelCurrentExchangeRate.setPreferredSize(new Dimension(100, 30));
        labelAmountResult = new JLabel("");
        labelAmountResult.setPreferredSize(new Dimension(100, 30));
        textFieldAmount = new JTextField(10);
        textFieldAmount.setPreferredSize(new Dimension(200, 30));
        textFieldPhoneNumber = new JTextField(10);
        textFieldPhoneNumber.setPreferredSize(new Dimension(200, 30));

        comboBoxFrom = new JComboBox<>(CURRENCIES);
        comboBoxFrom.setPreferredSize(new Dimension(150, 30));
        comboBoxTo = new JComboBox<>(CURRENCIES);
        comboBoxTo.setPreferredSize(new Dimension(150, 30));
        comboBoxTo.setSelectedItem(CURRENCIES[3]);

        labelExchangeRate = new JLabel(Double.toString(EXCHANGE_RATES[comboBoxFrom.getSelectedIndex()][comboBoxTo.getSelectedIndex()]));
        labelExchangeRate.setPreferredSize(new Dimension(100, 30));

        buttonConvert = new JButton("Convert");
        buttonConvert.setPreferredSize(new Dimension(160, 30));
        buttonConvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertCurrency();
            }
        });

        buttonClearHistory = new JButton("Clear History");
        buttonClearHistory.setPreferredSize(new Dimension(150, 30));
        buttonClearHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearHistory();
            }
        });

        buttonShowExchangeRate = new JButton("Show Exchange Rate");
        buttonShowExchangeRate.setPreferredSize(new Dimension(160,30));
        buttonShowExchangeRate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCurrentExchangeRate();
            }
        });

        textAreaHistory = new JTextArea(10, 30);
        textAreaHistory.setEditable(false);
        JScrollPane scrollPaneHistory = new JScrollPane(textAreaHistory);
        scrollPaneHistory.setPreferredSize(new Dimension(400, 200));

        JPanel panelInput = new JPanel();
        panelInput.setBackground(new Color(64,224,208));
        panelInput.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelInput.add(new JLabel("Phone number:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panelInput.add(textFieldPhoneNumber, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelInput.add(labelAmount, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panelInput.add(textFieldAmount, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panelInput.add(new JLabel("From:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panelInput.add(comboBoxFrom, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panelInput.add(new JLabel("To:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panelInput.add(comboBoxTo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panelInput.add(labelCurrentExchangeRate, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panelInput.add(labelExchangeRate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelInput.add(buttonShowExchangeRate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panelInput.add(labelResult, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        panelInput.add(labelAmountResult, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelInput.add(buttonConvert, gbc);

        JPanel panelHistory = new JPanel();
        panelHistory.setLayout(new BorderLayout());
        panelHistory.add(scrollPaneHistory, BorderLayout.CENTER);
        panelHistory.add(buttonClearHistory, BorderLayout.SOUTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Converter", panelInput);
        tabbedPane.addTab("History", panelHistory);

        add(tabbedPane);
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(textFieldAmount.getText());
            int fromIndex = comboBoxFrom.getSelectedIndex();
            int toIndex = comboBoxTo.getSelectedIndex();
            double result = amount * EXCHANGE_RATES[fromIndex][toIndex];

            if (cashAmounts[toIndex] < result) {
                JOptionPane.showMessageDialog(this, "Insufficient '" + comboBoxTo.getSelectedItem() + "' cash available for this transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirmIndex = JOptionPane.showConfirmDialog(this,"Current exchange rate: " + EXCHANGE_RATES[fromIndex][toIndex] + "\nWould you like to continue?");
            if (confirmIndex == 1 || confirmIndex == 2) {
                return;
            }

            DecimalFormat df = new DecimalFormat("#.##");
            labelAmountResult.setText(df.format(result));

            transactionCounter++;
            saveTransactionCounter(transactionCounter);
            addToHistory(transactionCounter, amount, CURRENCIES[fromIndex], CURRENCIES[toIndex], result);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void addToHistory(int transactionId, double amount, String fromCurrency, String toCurrency, double result) {
        long phoneNumber = Long.parseLong(textFieldPhoneNumber.getText());
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String transaction = "Transaction #" + transactionId + ": " + sdf.format(date) + " - Converted " + amount + " " + fromCurrency + " to " + result + " " + toCurrency + " - Exchange rate:" + EXCHANGE_RATES[comboBoxFrom.getSelectedIndex()][comboBoxTo.getSelectedIndex()] + " | Phone number: " + phoneNumber + "\n";
        JOptionPane.showMessageDialog(this,"Transaction successful, please take your money!", "Transaction" ,JOptionPane.PLAIN_MESSAGE);

        cashAmounts[comboBoxFrom.getSelectedIndex()] += amount;
        cashAmounts[comboBoxTo.getSelectedIndex()] -= result;

        textAreaHistory.append(transaction);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_HISTORY_FILE, true))) {
            writer.write(transaction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void clearHistory() {
        textAreaHistory.setText("");
    }

    public void saveTransactionCounter(int transactionCounter) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_COUNTER_FILE))) {
            writer.write(String.valueOf(transactionCounter));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int loadTransactionCounter() {
        int transactionCounter = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTION_COUNTER_FILE))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                transactionCounter = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return transactionCounter;
    }

    private void showCurrentExchangeRate() {
        labelExchangeRate.setText(Double.toString(EXCHANGE_RATES[comboBoxFrom.getSelectedIndex()][comboBoxTo.getSelectedIndex()]));
    }
}
