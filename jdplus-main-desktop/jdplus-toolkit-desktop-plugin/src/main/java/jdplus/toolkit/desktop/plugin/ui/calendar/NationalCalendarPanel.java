/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.calendar;

import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.toolkit.desktop.plugin.util.IDialogDescriptorProvider;
import jdplus.toolkit.desktop.plugin.util.ListenerState;
import jdplus.toolkit.base.api.timeseries.calendars.*;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.util.Arrays2;
import jdplus.toolkit.base.api.util.Constraint;
import org.openide.DialogDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.util.WeakListeners;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author Philippe Charles
 */
public class NationalCalendarPanel extends JPanel implements ExplorerManager.Provider, IDialogDescriptorProvider {

    // PROPERTIES DEFINITION
    public static final String CALENDAR_NAME_PROPERTY = "calendarName";
    public static final String MEAN_CORRECTION_PROPERTY = "meanCorrection";
    public static final String SPECIAL_DAY_EVENTS_PROPERTY = "specialDayEvents";
    // PROPERTIES
    private String calendarName;
    private List<Holiday> holidays;
    private boolean meanCorrection;
    // OTHER
    final ExplorerManager em;
    final ListOfSpecialDayEvent childFactory;
    final JPopupMenu addPopupMenu;
    final NameTextFieldListener nameTextFieldListener;
    final MeanCheckBoxListener meanListener;
    Action lastUsedAction;

    /**
     * Creates new form NationalCalendarPanel
     */
    public NationalCalendarPanel() {
        this.calendarName = "";
        this.holidays = Collections.emptyList();
        this.meanCorrection = true;

        this.em = new ExplorerManager();
        this.childFactory = new ListOfSpecialDayEvent();

        em.setRootContext(new AbstractNode(Children.create(childFactory, false)));
        em.addVetoableChangeListener(evt -> {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[]) evt.getNewValue();
                removeButton.setEnabled(nodes.length > 0);
            }
        });

        this.addPopupMenu = new JPopupMenu();
        addPopupMenu.add(new AbstractAction("Fixed") {
            @Override
            public void actionPerformed(ActionEvent e) {
                childFactory.beans.add(new FixedEventBean());
                childFactory.refreshData();
                lastUsedAction = this;
            }
        });
        addPopupMenu.add(new AbstractAction("Easter Related") {
            @Override
            public void actionPerformed(ActionEvent e) {
                childFactory.beans.add(new EasterRelatedEventBean());
                childFactory.refreshData();
                lastUsedAction = this;
            }
        });
        addPopupMenu.add(new AbstractAction("Fixed Week") {
            @Override
            public void actionPerformed(ActionEvent e) {
                childFactory.beans.add(new FixedWeekEventBean());
                childFactory.refreshData();
                lastUsedAction = this;
            }
        });
        addPopupMenu.add(new AbstractAction("Special Day") {
            @Override
            public void actionPerformed(ActionEvent e) {
                childFactory.beans.add(new PrespecifiedHolidayBean());
                childFactory.refreshData();
                lastUsedAction = this;
            }
        });
        addPopupMenu.add(new AbstractAction("Single Date") {
            @Override
            public void actionPerformed(ActionEvent e) {
                childFactory.beans.add(new SingleDateBean());
                childFactory.refreshData();
                lastUsedAction = this;
            }
        });

        initComponents();

        addButton.addActionListener(event -> {
            if (lastUsedAction != null) {
                lastUsedAction.actionPerformed(event);
            }
        });

        this.nameTextFieldListener = new NameTextFieldListener();
        this.meanListener = new MeanCheckBoxListener();

        listView1.setShowParentNode(false);
        listView1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        nameTextField.getDocument().addDocumentListener(nameTextFieldListener);
        meanCB.addActionListener(meanListener);
        removeButton.setEnabled(false);

        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case NationalCalendarPanel.CALENDAR_NAME_PROPERTY:
                    onCalendarNameChange();
                    break;
                case NationalCalendarPanel.MEAN_CORRECTION_PROPERTY:
                    onMeanChange();
                    break;
                case NationalCalendarPanel.SPECIAL_DAY_EVENTS_PROPERTY:
                    onSpecialDayEventsChange();
                    break;
            }
        });
    }

    private void updateFromBeans() {
        List<Holiday> tmp = new ArrayList<>();
        for (HasHoliday o : this.childFactory.beans) {
            tmp.add(o.toHoliday());
        }
        setHolidays(tmp);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        addButton = DropDownButtonFactory.createDropDownButton(DemetraIcons.LIST_ADD_16, addPopupMenu);
        removeButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        listView1 = new org.openide.explorer.view.ListView();
        propertySheetView1 = new org.openide.explorer.propertysheet.PropertySheetView();
        meanCB = new javax.swing.JCheckBox();

        jLabel1.setText("Name:");

        jToolBar1.setRollover(true);

        jLabel2.setText("Holidays:");
        jToolBar1.add(jLabel2);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/list-add_16x16.png"))); // NOI18N
        addButton.setToolTipText("");
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(addButton);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jdplus/toolkit/desktop/plugin/icons/list-remove_16x16.png"))); // NOI18N
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(removeButton);

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setLeftComponent(listView1);
        jSplitPane1.setRightComponent(propertySheetView1);

        meanCB.setSelected(true);
        meanCB.setText("Long term mean correction");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                    .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 242, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(meanCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 177, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(4, 4, 4)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(meanCB))
                .add(27, 27, 27)
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        for (Node o : em.getSelectedNodes()) {
            childFactory.beans.remove(o.getLookup().lookup(HasHoliday.class));
        }
        childFactory.refreshData();
    }//GEN-LAST:event_removeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private org.openide.explorer.view.ListView listView1;
    private javax.swing.JCheckBox meanCB;
    private javax.swing.JTextField nameTextField;
    private org.openide.explorer.propertysheet.PropertySheetView propertySheetView1;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    //<editor-fold defaultstate="collapsed" desc="Events handlers">
    protected void onCalendarNameChange() {
        if (nameTextFieldListener.state == ListenerState.READY) {
            nameTextFieldListener.state = ListenerState.SUSPENDED;
            nameTextField.setText(calendarName);
            nameTextFieldListener.state = ListenerState.READY;
        }
    }

    protected void onSpecialDayEventsChange() {
        if (childFactory.state == ListenerState.READY) {
            childFactory.state = ListenerState.SUSPENDED;
            childFactory.beans.clear();
            for (Holiday o : holidays) {
                if (o instanceof FixedDay fixedDay) {
                    childFactory.beans.add(new FixedEventBean(fixedDay, o.getValidityPeriod()));
                } else if (o instanceof EasterRelatedDay easterRelatedDay) {
                    childFactory.beans.add(new EasterRelatedEventBean(easterRelatedDay, o.getValidityPeriod()));
                } else if (o instanceof FixedWeekDay fixedWeekDay) {
                    childFactory.beans.add(new FixedWeekEventBean(fixedWeekDay, o.getValidityPeriod()));
                } else if (o instanceof PrespecifiedHoliday prespecifiedHoliday) {
                    childFactory.beans.add(new PrespecifiedHolidayBean(prespecifiedHoliday));
                } else if (o instanceof SingleDate singleDate) {
                    childFactory.beans.add(new SingleDateBean(singleDate));
                }
            }
            childFactory.refreshData();
            childFactory.state = ListenerState.READY;
        }
    }

    protected void onMeanChange() {
        if (meanListener.state == ListenerState.READY) {
            meanListener.state = ListenerState.SUSPENDED;
            meanCB.setSelected(meanCorrection);
            meanListener.state = ListenerState.READY;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        String old = this.calendarName;
        this.calendarName = calendarName != null ? calendarName : "";
        firePropertyChange(CALENDAR_NAME_PROPERTY, old, this.calendarName);
    }

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> events) {
        List<Holiday> old = this.holidays;
        this.holidays = events != null ? events : Collections.emptyList();
        firePropertyChange(SPECIAL_DAY_EVENTS_PROPERTY, old, this.holidays);
        if (! events.isEmpty()){
            try {
                Children children = em.getExploredContext().getChildren();
                em.setSelectedNodes(new Node[]{children.getNodes()[events.size()-1]});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean isMeanCorrection() {
        return meanCorrection;
    }

    public void setMeanCorrection(boolean mean) {
        boolean old = this.meanCorrection;
        this.meanCorrection = mean;
        firePropertyChange(MEAN_CORRECTION_PROPERTY, old, this.meanCorrection);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    //</editor-fold>

    class ListOfSpecialDayEvent extends ChildFactory<HasHoliday> implements NodeListener {

        public final List<HasHoliday> beans = new ArrayList<>();
        ListenerState state = ListenerState.READY;

        public void refreshData() {
            refresh(true);
            fireDataChange();
        }

        void fireDataChange() {
            if (state == ListenerState.READY) {
                state = ListenerState.SENDING;
                updateFromBeans();
                state = ListenerState.READY;
            }
        }

        @Override
        protected boolean createKeys(List<HasHoliday> toPopulate) {
            toPopulate.addAll(beans);
            return true;
        }

        @Override
        protected Node createNodeForKey(HasHoliday key) {
            Node result;
            if (key instanceof FixedEventBean fixedEventBean) {
                result = new FixedEventNode(fixedEventBean);
            } else if (key instanceof EasterRelatedEventBean easterRelatedEventBean) {
                result = new EasterRelatedEventNode(easterRelatedEventBean);
            } else if (key instanceof FixedWeekEventBean fixedWeekEventBean) {
                result = new FixedWeekEventNode(fixedWeekEventBean);
            } else if (key instanceof PrespecifiedHolidayBean prespecifiedHolidayBean) {
                result = new SpecialEventNode(prespecifiedHolidayBean);
            } else if (key instanceof SingleDateBean singleDateBean) {
                result = new SingleDateNode(singleDateBean);
            } else {
                throw new UnsupportedOperationException();
            }
            result.addNodeListener(WeakListeners.create(NodeListener.class, this, result));
            return result;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p.equals(Node.PROP_DISPLAY_NAME)) {
                fireDataChange();
            }
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }
    }

    private class NameTextFieldListener implements DocumentListener {

        ListenerState state = ListenerState.READY;

        void update() {
            if (state == ListenerState.READY) {
                state = ListenerState.SENDING;
                setCalendarName(nameTextField.getText());
                state = ListenerState.READY;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    }

    @Override
    public DialogDescriptor createDialogDescriptor(String title) {
        return new NationalDialogDescriptor(this, title);
    }

    static class NationalDialogDescriptor extends CustomDialogDescriptor<NationalConstraintData> {

        NationalDialogDescriptor(NationalCalendarPanel p, String title) {
            super(p, title, new NationalConstraintData(p, p.getCalendarName()));
            validate(NationalConstraints.values());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case CALENDAR_NAME_PROPERTY:
                    validate(NationalConstraints.CALENDAR_NAME, NationalConstraints.SPECIAL_DAY_EVENTS);
                    break;
                case SPECIAL_DAY_EVENTS_PROPERTY:
                    validate(NationalConstraints.SPECIAL_DAY_EVENTS, NationalConstraints.CALENDAR_NAME);
                    break;
            }
        }
    }

    private class MeanCheckBoxListener implements ActionListener {

        ListenerState state = ListenerState.READY;

        void update() {
            if (state == ListenerState.READY) {
                state = ListenerState.SENDING;
                setMeanCorrection(meanCB.isSelected());
                state = ListenerState.READY;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            update();
        }
    }

    private static class NationalConstraintData {

        final NationalCalendarPanel panel;
        final String originalName;
        final CalendarManager manager;

        NationalConstraintData(NationalCalendarPanel panel, String originalName) {
            this.panel = panel;
            this.originalName = originalName;
            this.manager = ModellingContext.getActiveContext().getCalendars();
        }
    }

    private enum NationalConstraints implements Constraint<NationalConstraintData> {

        CALENDAR_NAME {
            @Override
            public String check(NationalConstraintData t) {
                String name = t.panel.getCalendarName();
                if (name.isEmpty()) {
                    return "The name of the calendar cannot be empty";
                }
                if (!t.originalName.equals(name) && t.manager.contains(name)) {
                    return "The name of the calendar is already used";
                }
                return null;
            }
        },
        SPECIAL_DAY_EVENTS {
            @Override
            public String check(NationalConstraintData t) {
                List<Holiday> events = t.panel.getHolidays();
                if (new HashSet<>(events).size() != events.size()) {
                    return "There are duplicated events";
                }
                return null;
            }
        }
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (!Arrays2.arrayEquals(oldValue, newValue)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
}
