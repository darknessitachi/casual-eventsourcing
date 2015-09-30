package se.cs;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

public class ItemStockForm extends FormLayout {

    Button save = new Button("Save", this::save);
    TextField name = new TextField("Item name");
    TextField amount = new TextField("Update amount");

    ItemStockUpdate itemStockUpdate;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<ItemStockUpdate> formFieldBindings;

    public ItemStockForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        addComponents(name, amount, save);
    }

    public void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().save(itemStockUpdate);

            itemStockUpdate = null;
            edit(null);

            String msg = String.format("Saved '%s %s'.",
                    name.getValue(), amount.getValue());
            Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    void edit(ItemStockUpdate itemStockUpdate) {
        this.itemStockUpdate = itemStockUpdate;

        if (itemStockUpdate != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(itemStockUpdate, this);
            amount.focus();
        }
        setVisible(itemStockUpdate != null);
    }

    @Override
    public ItemStockUI getUI() {
        return (ItemStockUI) super.getUI();
    }
}
