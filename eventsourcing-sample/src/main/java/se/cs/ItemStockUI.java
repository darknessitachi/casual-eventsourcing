package se.cs;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import se.cs.eventsourcing.domain.store.changeset.ChangeSet;
import se.cs.eventsourcing.domain.store.changeset.ChangeSetRepository;
import se.cs.eventsourcing.domain.store.changeset.StoredEvent;
import se.cs.eventsourcing.domain.store.metadata.KnownMetadata;
import se.cs.eventsourcing.infrastructure.store.InMemoryEventStore;
import se.cs.se.cs.itemstockexample.domain.model.ItemStockStatus;
import se.cs.se.cs.itemstockexample.domain.model.ItemStockStatusRepository;

import javax.servlet.annotation.WebServlet;
import java.util.List;

import static se.cs.eventsourcing.domain.store.metadata.Metadata.withUserReference;

/**
 *
 */
@Theme("valo")
@Widgetset("se.cs.MyAppWidgetset")
public class ItemStockUI extends UI {

    private ItemStockStatusRepository itemStockStatusRepository;
    private ChangeSetRepository changeSetRepository;

    private ItemStockStatus aggregate;
    private String currentUser = "dude123";

    private ItemStockForm form = new ItemStockForm();

    private Button edit = new Button("Edit");
    private Label name;
    private Label amount;
    private Table changeSets = new Table("Change sets");
    private Tree tree = new Tree("Events in change set");


    @Override
    protected void init(VaadinRequest request) {
        InMemoryEventStore store = new InMemoryEventStore();
        changeSetRepository = store;
        itemStockStatusRepository = new ItemStockStatusRepository(store);

        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        aggregate = newItemStockStatus();

        edit.addClickListener(e -> form.edit(new ItemStockUpdate(aggregate.getName(), 1)));
        name = new Label();
        amount = new Label();

        refreshCurrentValues();
    }

    private void buildLayout() {
        VerticalLayout top = new VerticalLayout();
        VerticalLayout bottomLeft = new VerticalLayout();
        VerticalLayout bottomRight = new VerticalLayout();
        HorizontalSplitPanel bottom = new HorizontalSplitPanel(bottomLeft, bottomRight);

        top.setMargin(true);
        VerticalSplitPanel layout = new VerticalSplitPanel(top, bottom);
        layout.setSplitPosition(60, Unit.PERCENTAGE);

        populateTop(top);
        populateBottomLeft(bottomLeft);
        populateBottomRight(bottomRight);

        setContent(layout);
    }

    private void populateTop(AbstractComponentContainer container) {

        VerticalLayout currentState = new VerticalLayout();

        HorizontalLayout nameLayout = new HorizontalLayout();
        nameLayout.addComponent(new Label("<b>Name:</b>&nbsp;", ContentMode.HTML));
        nameLayout.addComponent(name);

        HorizontalLayout amountLayout = new HorizontalLayout();
        amountLayout.addComponent(new Label("<b>Amount in stock:</b>&nbsp;", ContentMode.HTML));
        amountLayout.addComponent(amount);

        currentState.addComponent(nameLayout);
        currentState.addComponent(amountLayout);

        container.addComponent(new Label("<h2>Item stock status</h2>", ContentMode.HTML));
        container.addComponent(currentState);

        container.addComponent(edit);
        container.addComponent(form);
    }

    private void populateBottomLeft(AbstractComponentContainer container) {
        changeSets.setSizeFull();
        changeSets.addContainerProperty("id", Long.class, null);
        changeSets.addContainerProperty("when", String.class, null);
        changeSets.addContainerProperty("user", String.class, null);

        changeSets.addItemClickListener(
                itemClickEvent -> refreshTree(Long.parseLong(itemClickEvent.getItem().getItemProperty("id").getValue().toString())));

        refreshChangeSets();

        container.addComponent(changeSets);
    }

    private void populateBottomRight(AbstractComponentContainer container) {
        tree.setSizeFull();
        container.addComponent(tree);
    }

    void refreshChangeSets() {
        changeSets.clear();

        List<ChangeSet> changeSetList =
                changeSetRepository.getChangeSets(aggregate.getEventStreamId());

        int counter = 1;
        for (ChangeSet changeSet : changeSetList) {
            changeSets.addItem(
                    new Object[]{
                            changeSet.getId(),
                            changeSet.getMetadata().get(KnownMetadata.WHEN.getKey()).getValue(),
                            changeSet.getMetadata().get(KnownMetadata.USER_REFERENCE.getKey()).getValue()
                    }, counter++);
        }
    }

    void refreshTree(long id) {
        tree.removeAllItems();

        ChangeSet changeSet =
                changeSetRepository.getChangeSetById(
                        aggregate.getEventStreamId(), id).get();

        for (StoredEvent event : changeSet.getStoredEvents()) {
            String node = String.format("%s{id=%s}",
                    event.getClass().getSimpleName(),
                    event.getId());

            String leaf = event.getEvent().toString();

            tree.addItem(node);
            tree.addItem(leaf);
            tree.setParent(leaf, node);
            tree.setChildrenAllowed(leaf, false);

            tree.expandItem(node);
        }
    }

    void refreshCurrentValues() {
        this.name.setValue(aggregate.getName());
        this.amount.setValue(String.valueOf(aggregate.getAmount()));
    }

    private ItemStockStatus newItemStockStatus() {
        ItemStockStatus aggregate =
                new ItemStockStatus("Yummy beer", 10);

        itemStockStatusRepository.save(aggregate, withUserReference(currentUser));

        return aggregate;
    }

    void save(ItemStockUpdate update) {

        if (!aggregate.getName().equals(update.getName())) {
            aggregate.changeName(update.getName());
        }

        if (update.getAmount() > 0) {
            aggregate.increase(update.getAmount());
        }
        else if (update.getAmount() < 0) {
            aggregate.decrease(update.getAmount());
        }

        itemStockStatusRepository.save(aggregate, withUserReference(currentUser));

        refreshCurrentValues();
        refreshChangeSets();
    }

    @WebServlet(urlPatterns = "/*", name = "ItemStockUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ItemStockUI.class, productionMode = false)
    public static class ItemStockUIServlet extends VaadinServlet {}
}
