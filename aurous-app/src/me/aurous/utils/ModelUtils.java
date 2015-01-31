package me.aurous.utils;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import me.aurous.ui.models.CSVTableModel;
import me.aurous.ui.models.ImageRendererModel;
import me.aurous.ui.panels.PlayListPanel;
import me.aurous.ui.panels.TabelPanel;
import me.aurous.ui.widgets.SearchWidget;

/**
 * @author Andrew
 *
 */
public class ModelUtils {

	public static class InteractiveRenderer extends DefaultTableCellRenderer {
		/**
		 *
		 */
		private static final long serialVersionUID = 8185342590581318945L;
		protected int interactiveColumn;

		public InteractiveRenderer(final int interactiveColumn) {
			this.interactiveColumn = interactiveColumn;
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table,
				final Object value, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			final Component c = super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);

			setOpaque(true);
			setBackground(new Color(18, 19, 21));
			setForeground(new Color(229, 229, 229));

			if ((column == this.interactiveColumn) && hasFocus) {

			}

			return c;
		}
	}

	public class InteractiveTableModelListener implements TableModelListener {
		@Override
		public void tableChanged(final TableModelEvent evt) {
			if (evt.getType() == TableModelEvent.UPDATE) {
				final int column = evt.getColumn();
				final int row = evt.getFirstRow();
				// System.out.println("row: " + row + " column: " + column);
				table.setColumnSelectionInterval(column + 1, column + 1);
				table.setRowSelectionInterval(row, row);

			}
		}
	}

	public static void loadPlayList(final String fileLocation) {
		try {

			table = TabelPanel.table;
			DefaultTableModel tableModel = TabelPanel.tableModel;
			final String datafile = fileLocation;
			final File playList = new File(datafile);
			final FileReader fin = new FileReader(datafile);

			tableModel = CSVTableModel.createTableModel(playList, fin, null);
			if (Utils.isNull(tableModel)) {
				JOptionPane.showMessageDialog(null,
						"Error loading playlist, corrupted or unfinished.",
						"Error", JOptionPane.ERROR_MESSAGE);

				ModelUtils.loadPlayList(Constants.DATA_PATH
						+ "scripts/blank.plist");
				PlayListPanel.canSetLast = false;
				return;
			} else {
				PlayListPanel.canSetLast = true;
			}
			table.setModel(tableModel);
			hideIndexs(table);

		} catch (final FileNotFoundException e) {
			ModelUtils
			.loadPlayList(Constants.DATA_PATH + "scripts/blank.plist");
		}
	}

	private static void hideIndexs(final JTable table) {
		// table.setTableHeader(null);
		// table.getTableHeader().setReorderingAllowed(false);
		final TableColumn hiddenLink = table.getColumnModel().getColumn(
				LINK_INDEX);
		hiddenLink.setMinWidth(0);
		hiddenLink.setPreferredWidth(0);
		hiddenLink.setMaxWidth(0);
		hiddenLink.setCellRenderer(new InteractiveRenderer(LINK_INDEX));

		final TableColumn hiddenAlbumArt = table.getColumnModel().getColumn(
				ART_INDEX);
		hiddenAlbumArt.setMinWidth(0);
		hiddenAlbumArt.setPreferredWidth(0);
		hiddenAlbumArt.setMaxWidth(0);
		hiddenAlbumArt.setCellRenderer(new InteractiveRenderer(ART_INDEX));

		final TableColumn hiddenDate = table.getColumnModel().getColumn(
				ModelUtils.DATE_INDEX);
		hiddenDate.setMinWidth(0);
		hiddenDate.setPreferredWidth(0);
		hiddenDate.setMaxWidth(0);
		hiddenDate.setCellRenderer(new ModelUtils.InteractiveRenderer(
				ModelUtils.DATE_INDEX));

		final TableColumn hiddenAlbum = table.getColumnModel().getColumn(
				ModelUtils.ALBUM_INDEX);
		hiddenAlbum.setMinWidth(0);
		hiddenAlbum.setPreferredWidth(0);
		hiddenAlbum.setMaxWidth(0);
		hiddenAlbum.setCellRenderer(new ModelUtils.InteractiveRenderer(
				ModelUtils.ALBUM_INDEX));

		final TableColumn hiddenUser = table.getColumnModel().getColumn(
				ModelUtils.OWNER_INDEX);
		hiddenUser.setMinWidth(0);
		hiddenUser.setPreferredWidth(0);
		hiddenUser.setMaxWidth(0);
		hiddenUser.setCellRenderer(new ModelUtils.InteractiveRenderer(
				ModelUtils.OWNER_INDEX));

		for (int row = 0; row < table.getRowCount(); row++) {
			// table.setValueAt(null, row, table.get);
			table.setRowHeight(row, 41);

		}
		table.getColumnModel().getColumn(0).setPreferredWidth(263);
		table.getColumnModel().getColumn(0).setWidth(263);
		table.getColumnModel().getColumn(0)
				.setCellRenderer(new ImageRendererModel());
		table.getTableHeader().setReorderingAllowed(false);
	}

	public static void loadSearchResults(final String searchResults) {
		try {

			final JTable table = SearchWidget.getSearchTable();
			DefaultTableModel tableModel = SearchWidget.getTableModel();
			final String datafile = searchResults;
			final File playList = new File(datafile);

			final FileReader fin = new FileReader(playList);

			tableModel = CSVTableModel.createTableModel(playList, fin, null);
			if (Utils.isNull(tableModel)) {
				JOptionPane.showMessageDialog(null,
						"Error Loading Search Results.", "Error",
						JOptionPane.ERROR_MESSAGE);

				ModelUtils.loadSearchResults(Constants.DATA_PATH
						+ "search/search.blank");
				return;
			} else {

			}
			table.setModel(tableModel);
			final TableColumn hiddenLink = table.getColumnModel().getColumn(3);
			hiddenLink.setMinWidth(0);
			hiddenLink.setPreferredWidth(0);
			hiddenLink.setMaxWidth(0);
			hiddenLink.setCellRenderer(new InteractiveRenderer(3));
			final TableColumn hiddenIDS = table.getColumnModel().getColumn(4);
			hiddenIDS.setMinWidth(0);
			hiddenIDS.setPreferredWidth(0);
			hiddenIDS.setMaxWidth(0);
			hiddenIDS.setCellRenderer(new InteractiveRenderer(4));
			table.getTableHeader().setReorderingAllowed(false);
		} catch (final FileNotFoundException e) {
			ModelUtils.loadSearchResults(Constants.DATA_PATH
					+ "search/search.blank");
		}
	}

	public static final int TITLE_INDEX = 0;
	public static final int ARTIST_INDEX = 1;
	public static final int ALBUM_INDEX = 5;
	public static final int ART_INDEX = 6;
	public static final int TIME_INDEX = 2;
	public static final int DATE_INDEX = 3;
	public static final int OWNER_INDEX = 4;

	public static final int LINK_INDEX = 7;

	public static boolean playListLoaded;

	public static JTable table;

}
