package function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;

public class ParamTableWrapper implements IParamTableWrapper {
    public static final String PARAM_NAME_COLUMN_HEADER = "Name (optional)";
    private static final String PARAM_VALUE_COLUMN_HEADER = "Value";

    private final Logger logger = LoggerFactory.getLogger(ParamTableWrapper.class);
    private final JTable paramJTable;

    public ParamTableWrapper(final int paramNumber) {
        paramJTable = new JTable(paramNumber, 2);

        paramJTable.getColumnModel().getColumn(0)
                .setHeaderValue(PARAM_NAME_COLUMN_HEADER);
        paramJTable.getColumnModel().getColumn(0)
                .setMinWidth(150);
        paramJTable.getColumnModel().getColumn(1)
                .setHeaderValue(PARAM_VALUE_COLUMN_HEADER);
        paramJTable.getColumnModel().getColumn(1)
                .setMinWidth(300);

        paramJTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    @Override
    public Iterable<Double> getAllParamValues() {
        final var paramList = new ArrayList<Double>();
        for (int i = 0; i < paramJTable.getRowCount(); i++) {
            try {
                paramList.add(Double.parseDouble((String) paramJTable.getValueAt(i, 1)));
            } catch (NumberFormatException e) {
                logger.error("Invalid parameter in table on index {}", i, e);
                throw new IllegalArgumentException(e);
            } catch (NullPointerException e) {
                logger.error("Empty parameter in table on index {}", i, e);
                throw new IllegalArgumentException(e);
            }
        }
        return paramList;
    }

    public JTable getParamJTable() {
        return paramJTable;
    }
}
