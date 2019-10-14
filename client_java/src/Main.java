import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.put("Table.gridColor", new ColorUIResource(Color.gray));
        javax.swing.SwingUtilities.invokeLater(Main::createGUI);
        javax.swing.SwingUtilities.invokeLater(Main::createGraph);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Функция");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = frame.getContentPane();
        BoxLayout optionsLayout = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
        contentPane.setLayout(optionsLayout);

        JLabel functionLabel = new JLabel("Ваша функция:");
        functionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPane.add(functionLabel);

        final String[] functionList = {
                "Рубцов",
                "Пугачев",
                "Кравец",
                "Баюков"
        };

        final Map<String, Integer> functionToParamNumberMap = new TreeMap<>();
        AtomicInteger paramNumber = new AtomicInteger();
        functionToParamNumberMap.putAll(Arrays.stream(functionList).collect(
                Collectors.toMap(s -> s, s -> paramNumber.incrementAndGet() + 16)
        ));

        System.out.println(functionToParamNumberMap);

        JComboBox<String> functionComboBox = new JComboBox<>(functionList);
        functionComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        functionComboBox.setSelectedItem(null);

        contentPane.add(functionComboBox);

        JLabel paramsLabel = new JLabel("Список параметров:");
        paramsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPane.add(paramsLabel);

        JTable paramsTable = new JTable(0, 2);
        paramsTable.getColumnModel().getColumn(0).setHeaderValue("№");
        paramsTable.getColumnModel().getColumn(0).setMaxWidth(30);
        paramsTable.getColumnModel().getColumn(1).setHeaderValue("Параметр");
        paramsTable.getColumnModel().getColumn(1).setMinWidth(300);
        JScrollPane jScrollPane = new JScrollPane(paramsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        paramsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        contentPane.add(jScrollPane);

        ActionListener functionActionListener = e -> {
            JComboBox comboBox = (JComboBox) e.getSource();
            String item = (String) comboBox.getSelectedItem();
            DefaultTableModel model = (DefaultTableModel) paramsTable.getModel();
            if (model.getRowCount() > 0) {
                for (int i = model.getRowCount() - 1; i > -1; i--) {
                    model.removeRow(i);
                }
            }
            model.setRowCount(functionToParamNumberMap.get(item));
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
                model.setValueAt(UUID.randomUUID(), i, 1);
            }
        };

        functionComboBox.addActionListener(functionActionListener);


        frame.setPreferredSize(new Dimension(400, 600));

        frame.pack();
        frame.setVisible(true);
    }

    private static void createGraph() {
        JFrame frame = new JFrame("График");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 600));
        frame.setResizable(false);

        frame.pack();
        frame.setVisible(true);
    }
}
