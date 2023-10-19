package com.example.patchnotes;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.dialogs.InputDayOfWeekDialog;
import com.example.patchnotes.dialogs.InputDayPickerDialog;
import com.example.patchnotes.dialogs.InputTimePickerDialog;
import com.example.patchnotes.dialogs.InputYesNoDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class AbstractContentAttributeSetterActivity extends AppCompatActivity implements
        InputYesNoDialog.OnResponseListener, InputDayOfWeekDialog.OnDayOfWeekInputChosenListener,
        InputDayPickerDialog.OnDayPickedListener, InputTimePickerDialog.OnTimePickedListener {

    private static final String SAVED_YEAR = "savedContentSelfYear";
    private static final String SAVED_MONTH = "savedContentSelfMonth";
    private static final String SAVED_DAY = "savedContentSelfDay";
    private static final String SAVED_IS_NAVIGATION_VIEW_OPEN = "savedIsNavigationViewOpen";

    private static final String TAG_SELF_PRIORITY_PERIOD = "tagSelfPriorityPeriodDialogSetter";
    private static final String TAG_SELF_PRIORITY_DAYS = "tagSelfPriorityDaysDialogSetter";
    private static final String TAG_SELF_EXPIRATION_PERIOD = "tagSelfExpirationPeriod";

    private Menu navMenu;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle toRetrieve) {
        this.year = toRetrieve.getInt(SAVED_YEAR);
        this.month = toRetrieve.getInt(SAVED_MONTH);
        this.day = toRetrieve.getInt(SAVED_DAY);

        if (toRetrieve.getBoolean(SAVED_IS_NAVIGATION_VIEW_OPEN)) {
            ((DrawerLayout) findViewById(getDrawerViewId())).openDrawer(findViewById(getNavigationViewId()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        navMenu = getNavigationMenu();
        attachFunctionalityToMenu();

        updatePriorityPeriodPanel();
        updatePriorityDaysPanel();
        updateExpirationPeriodPanel();
    }

    private Menu getNavigationMenu() {
        NavigationView navigationView = findViewById(getNavigationViewId());
        if (navMenu == null) {
            navMenu = initializeNavigationMenu(navigationView);
        }
        return navMenu;
    }

    private Menu initializeNavigationMenu(NavigationView navigationView) {
        navMenu = navigationView.getMenu();
        navMenu.clear();

        addAllMenuItemToMenu(navMenu, getTopPartMenu(), 0);

        navMenu.add(Menu.NONE, getToggleAddDeletePriorityPeriodId(), getHeaderPosAdjust(1), "");
        navMenu.add(Menu.NONE, getEditPriorityPeriodId(), getHeaderPosAdjust(2), getTextPriorityPeriodPanel()[2]);
        navMenu.add(Menu.NONE, Menu.NONE, getHeaderPosAdjust(3), "");

        navMenu.add(Menu.NONE, getToggleAddDeletePriorityDaysId(), getHeaderPosAdjust(4), "");
        navMenu.add(Menu.NONE, getEditPriorityDaysId(), getHeaderPosAdjust(5), getTextPriorityDaysPanel()[2]);
        navMenu.add(Menu.NONE, Menu.NONE, getHeaderPosAdjust(6), "");

        navMenu.add(Menu.NONE, getToggleAddDeleteExpirationPeriodId(), getHeaderPosAdjust(7), "");
        navMenu.add(Menu.NONE, getEditExpirationPeriodId(), getHeaderPosAdjust(8), getTextExpirationPeriodPanel()[2]);
        navMenu.add(Menu.NONE, Menu.NONE, getHeaderPosAdjust(9), "");

        navMenu.findItem(getToggleAddDeletePriorityPeriodId()).setIcon(getAddDeletePriorityPeriodDrawableId());

        navMenu.findItem(getToggleAddDeletePriorityDaysId()).setIcon(getAddDeletePriorityDaysDrawableId());

        navMenu.findItem(getToggleAddDeleteExpirationPeriodId()).setIcon(getAddDeleteExpirationPeriodDrawableId());

        addAllMenuItemToMenu(navMenu, getBottomPartMenu(), getHeaderPosAdjust(10));

        return navMenu;
    }

    private void addAllMenuItemToMenu(Menu menu, MenuItem[] menuItems, int orderShift) {
        if (menuItems != null) {
            for (MenuItem item : menuItems) {
                menu.add(
                        item.getGroupId(),
                        item.getItemId(),
                        item.getOrder() + orderShift,
                        item.getTitle()
                ).setIcon(item.getIcon());
            }
        }
    }

    abstract protected MenuItem[] getTopPartMenu();
    abstract protected MenuItem[] getBottomPartMenu();

    private int getHeaderPosAdjust(int originalIndex) {
        if (getTopPartMenu() == null) {
            return originalIndex;
        } else {
            return getTopPartMenu().length + originalIndex;
        }
    }

    private void attachFunctionalityToMenu() {
        NavigationView navigationView = findViewById(getNavigationViewId());
        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == getToggleAddDeletePriorityPeriodId()) {
                if (getContent().getPriorityPeriod() == null) {
                    showDayTimeSetterDialogs(TAG_SELF_PRIORITY_PERIOD, true);
                } else {
                    showYesNoDialog("", "Remove priority period?", TAG_SELF_PRIORITY_PERIOD);
                }

            } else if (item.getItemId() == getEditPriorityPeriodId()) {
                showDayTimeSetterDialogs(TAG_SELF_PRIORITY_PERIOD, false);

            } else if (item.getItemId() == getToggleAddDeletePriorityDaysId()) {
                if (getContent().getPriorityDaysOfWeek() == null) {
                    showDayPickerDialog(true);
                } else {
                    showYesNoDialog("", "Remove priority days?", TAG_SELF_PRIORITY_DAYS);
                }

            } else if (item.getItemId() == getEditPriorityDaysId()) {
                showDayPickerDialog(false);

            } else if (item.getItemId() == getToggleAddDeleteExpirationPeriodId()) {
                if (getContent().getExpirationPeriod() == null) {
                    showDayTimeSetterDialogs(TAG_SELF_EXPIRATION_PERIOD, true);
                } else {
                    showYesNoDialog("", "Remove expiration period?", TAG_SELF_EXPIRATION_PERIOD);
                }

            } else if (item.getItemId() == getEditExpirationPeriodId()) {
                showDayTimeSetterDialogs(TAG_SELF_EXPIRATION_PERIOD, false);

            } else {
                onUnhandledNavigationOptionsSelected(item);

            }
            return true;
        });
    }

    protected void onUnhandledNavigationOptionsSelected(MenuItem item) {

    }

    private void showDayTimeSetterDialogs(String tag, boolean creatingNew) {
        InputDayPickerDialog dialog = new InputDayPickerDialog();
        dialog.setTag(tag);
        if (creatingNew) {
            dialog.setInitialDayValues(GregorianCalendar.getInstance());
        } else {
            if (tag.equals(TAG_SELF_PRIORITY_PERIOD)) {
                dialog.setInitialDayValues(getContent().getPriorityPeriod());
            } else if (tag.equals(TAG_SELF_EXPIRATION_PERIOD)) {
                dialog.setInitialDayValues(getContent().getExpirationPeriod());
            }
        }
        dialog.show(getSupportFragmentManager(), "getDayDialog");
        this.creatingNew = creatingNew;
    }

    private int year, month, day;
    private boolean creatingNew;

    @CallSuper
    @Override
    public void onDayPicked(int year, int month, int day, String tag) {
        if (tag.equals(TAG_SELF_PRIORITY_PERIOD) || tag.equals(TAG_SELF_EXPIRATION_PERIOD)) {
            this.year = year;
            this.month = month;
            this.day = day;

            InputTimePickerDialog dialog = new InputTimePickerDialog();
            dialog.setTag(tag);
            if (creatingNew) {
                dialog.setInitialTime(GregorianCalendar.getInstance());
            } else {
                if (tag.equals(TAG_SELF_PRIORITY_PERIOD)) {
                    dialog.setInitialTime(getContent().getPriorityPeriod());
                } else {
                    dialog.setInitialTime(getContent().getExpirationPeriod());
                }
            }
            dialog.show(getSupportFragmentManager(), "timeDialog");
        }
    }

    @CallSuper
    @Override
    public void onTimePicked(int hour, int minute, String tag) {
        if (tag.equals(TAG_SELF_PRIORITY_PERIOD) || tag.equals(TAG_SELF_EXPIRATION_PERIOD)) {
            if (tag.equals(TAG_SELF_PRIORITY_PERIOD)) {
                onNewPriorityPeriodChosen(constructCalendarWithValues(hour, minute));
            } else {
                onNewExpirationPeriodChosen(constructCalendarWithValues(hour, minute));
            }
        }
    }

    private Calendar constructCalendarWithValues(int hour, int minute) {
        Calendar returnVal = GregorianCalendar.getInstance();
        returnVal.set(Calendar.YEAR, year);
        returnVal.set(Calendar.MONTH, month);
        returnVal.set(Calendar.DAY_OF_MONTH, day);
        returnVal.set(Calendar.HOUR_OF_DAY, hour);
        returnVal.set(Calendar.MINUTE, minute);
        returnVal.set(Calendar.SECOND, 0);
        returnVal.set(Calendar.MILLISECOND, 0);
        return returnVal;
    }

    private void showDayPickerDialog(boolean creatingNew) {
        InputDayOfWeekDialog dialog = new InputDayOfWeekDialog();
        if (creatingNew) {
            dialog.setInitialSelection(null);
        } else {
            dialog.setInitialSelection(getContent().getPriorityDaysOfWeek());
        }
        dialog.setTag(TAG_SELF_PRIORITY_DAYS);
        dialog.show(getSupportFragmentManager(), "prioDays");
    }

    @CallSuper
    @Override
    public void onDaysOfWeekInputChosen(DaysOfWeek[] daysOfWeeks, String tag) {
        if (tag.equals(TAG_SELF_PRIORITY_DAYS)) {
            onNewPriorityDaysChosen(daysOfWeeks);
        }
    }

    private void showYesNoDialog(String title, String text, String tag) {
        InputYesNoDialog dialog = new InputYesNoDialog();
        dialog.setTitle(title);
        dialog.setText(text);
        dialog.setTag(tag);
        dialog.show(getSupportFragmentManager(), "yesNo");
    }

    @CallSuper
    @Override
    public void onYesSelected(String tag) {
        if (tag.equals(TAG_SELF_PRIORITY_PERIOD)) {
            onNewPriorityPeriodChosen(null);
        } else if (tag.equals(TAG_SELF_PRIORITY_DAYS)) {
            onNewPriorityDaysChosen(null);
        } else if (tag.equals(TAG_SELF_EXPIRATION_PERIOD)) {
            onNewExpirationPeriodChosen(null);
        }
    }

    @Override
    public void onNoSelected(String tag) {}

    @CallSuper
    protected void onNewPriorityPeriodChosen(@Nullable Calendar newTime) {
        updatePriorityPeriodPanel();
    }

    private void updatePriorityPeriodPanel() {
        if (getContent().getPriorityPeriod() == null) {
            navMenu.findItem(getToggleAddDeletePriorityPeriodId()).setTitle(getTextPriorityPeriodPanel()[0]);
            navMenu.findItem(getEditPriorityPeriodId()).setEnabled(false);
        } else {
            navMenu.findItem(getToggleAddDeletePriorityPeriodId()).setTitle(getTextPriorityPeriodPanel()[1]);
            navMenu.findItem(getEditPriorityPeriodId()).setEnabled(true);
        }
    }

    @CallSuper
    protected void onNewPriorityDaysChosen(@Nullable DaysOfWeek[] daysOfWeeks) {
        updatePriorityDaysPanel();
    }

    private void updatePriorityDaysPanel() {
        if (getContent().getPriorityDaysOfWeek() == null) {
            navMenu.findItem(getToggleAddDeletePriorityDaysId()).setTitle(getTextPriorityDaysPanel()[0]);
            navMenu.findItem(getEditPriorityDaysId()).setEnabled(false);
        } else {
            navMenu.findItem(getToggleAddDeletePriorityDaysId()).setTitle(getTextPriorityDaysPanel()[1]);
            navMenu.findItem(getEditPriorityDaysId()).setEnabled(true);
        }
    }

    @CallSuper
    protected void onNewExpirationPeriodChosen(@Nullable Calendar newTime) {
        updateExpirationPeriodPanel();
    }

    private void updateExpirationPeriodPanel() {
        if (getContent().getExpirationPeriod() == null) {
            navMenu.findItem(getToggleAddDeleteExpirationPeriodId()).setTitle(getTextExpirationPeriodPanel()[0]);
            navMenu.findItem(getEditExpirationPeriodId()).setEnabled(false);
        } else {
            navMenu.findItem(getToggleAddDeleteExpirationPeriodId()).setTitle(getTextExpirationPeriodPanel()[1]);
            navMenu.findItem(getEditExpirationPeriodId()).setEnabled(true);
        }
    }

    @Override
    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putInt(SAVED_YEAR, year);
        toSave.putInt(SAVED_MONTH, month);
        toSave.putInt(SAVED_DAY, day);
        toSave.putBoolean(SAVED_IS_NAVIGATION_VIEW_OPEN,
                ((DrawerLayout) findViewById(getDrawerViewId())).isDrawerOpen(findViewById(getNavigationViewId())));
    }

    //

    protected final void openDrawer(boolean animateOpen) {
        NavigationView nav = findViewById(getNavigationViewId());
        DrawerLayout drawer = findViewById(getDrawerViewId());
        if (nav != null && drawer != null) {
            drawer.openDrawer(nav, animateOpen);
        }
    }

    protected final void closeDrawer(boolean animateClose) {
        NavigationView nav = findViewById(getNavigationViewId());
        DrawerLayout drawer = findViewById(getDrawerViewId());
        if (nav != null && drawer != null) {
            drawer.closeDrawer(nav, animateClose);
        }
    }

    //
    @NonNull
    abstract protected Content getContent();
    @IdRes
    abstract protected int getNavigationViewId();
    @IdRes
    abstract protected int getDrawerViewId();

    protected final int getEditPriorityPeriodId() {
        return 10040050;
    }
    protected final int getEditPriorityDaysId() {
        return 10050050;
    }
    protected final int getEditExpirationPeriodId() {
        return 10060050;
    }
    protected final int getToggleAddDeletePriorityPeriodId() {
        return 20040050;
    }
    protected final int getToggleAddDeletePriorityDaysId() {
        return 20050050;
    }
    protected final int getToggleAddDeleteExpirationPeriodId() {
        return 20060050;
    }

    /**
     *
     * @return a String array of size three.
     * <p>Index 0 is text for adding priority period</p>
     * <p>Index 1 is text for removing priority period</p>
     * <p>Index 2 is text for editing priority period</p>
     */
    @NonNull
    protected String[] getTextPriorityPeriodPanel() {
        return new String[] {
                "Add Priority Period",
                "Remove Priority Period",
                "Edit Priority Period"
        };
    }
    /**
     *
     * @return a String array of size three.
     * <p>Index 0 is text for adding priority days</p>
     * <p>Index 1 is text for removing priority days</p>
     * <p>Index 2 is text for editing priority days</p>
     */
    @NonNull
    protected String[] getTextPriorityDaysPanel() {
        return new String[] {
                "Add Priority Days",
                "Remove Priority Days",
                "Edit Priority Days"
        };
    }

    /**
     *
     * @return a String array of size three.
     * <p>Index 0 is text for adding expiration period</p>
     * <p>Index 1 is text for removing expiration period</p>
     * <p>Index 2 is text for editing expiration period</p>
     */
    @NonNull
    protected String[] getTextExpirationPeriodPanel() {
        return new String[] {
                "Add Expiration Period",
                "Remove Expiration Period",
                "Edit Expiration Period"
        };
    }

    @DrawableRes
    protected int getAddDeletePriorityPeriodDrawableId() {
        return R.drawable.baseline_date_range_black_48;
    };
    @DrawableRes
    protected int getAddDeletePriorityDaysDrawableId() {
        return R.drawable.baseline_event_black_48;
    }
    @DrawableRes
    protected int getAddDeleteExpirationPeriodDrawableId() {
        return R.drawable.baseline_auto_delete_black_48;
    }
}
