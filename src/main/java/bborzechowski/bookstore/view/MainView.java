package bborzechowski.bookstore.view;

import bborzechowski.bookstore.model.Book;
import bborzechowski.bookstore.model.Category;
import bborzechowski.bookstore.repository.BookRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Route("")
public class MainView extends VerticalLayout {

    private Binder<Book> binder = new Binder<>();
    private BookRepository bookRepository;
    private HorizontalLayout dataLayout = new HorizontalLayout();
    private TextField txtTitle = new TextField("Title");
    private TextField txtIsbn = new TextField("Isbn");
    private TextField txtAuthor = new TextField("Author");
    private ComboBox<Category> categoryComboBox = new ComboBox<>("Category");
    private Grid<Book> grid = new Grid<>(Book.class,false);
    private Button addButtonn = new Button("Add book", clickEvent -> adBook());

    public MainView(BookRepository bookRepository) {
        this.bookRepository = bookRepository;

        categoryComboBox.setItems(Category.values());
        categoryComboBox.setPlaceholder("Categories");
        dataLayout.add(txtTitle,txtIsbn,txtAuthor,categoryComboBox);

        grid.setItems(bookRepository.findAll());
        grid.addColumns("isbn","title", "author", "category");
        grid.addComponentColumn(this::deleteBook);
        grid.addComponentColumn(this::update);

        add(dataLayout, addButtonn, grid);

    }

    public void  adBook(){

        Book book = new Book();
        book.setTitle(txtTitle.getValue());
        book.setIsbn(txtIsbn.getValue());
        book.setAuthor(txtAuthor.getValue());
        book.setCategory(categoryComboBox.getValue());

        checkEmptyFields(book);
   }

    private Button deleteBook(Book b){

        Button deleteBt = new Button("Delete" );
        Button acceptBt = new Button("Accept");
        acceptBt.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        Button cancelBt = new Button("Cancel");
        cancelBt.addThemeVariants(ButtonVariant.LUMO_ERROR);
        HorizontalLayout buttLayout = new HorizontalLayout();

        buttLayout.add(acceptBt,cancelBt);

        Dialog dialog = new Dialog();
        deleteBt.addClickListener(clickEvent -> {dialog.open();
        dialog.add(new Label("Are you sure you want to permanently delete this book: " + b.getTitle() + " ?"));
        dialog.add(buttLayout);
        acceptBt.addClickListener(clickEvent1 -> {bookRepository.delete(b);
            UI.getCurrent().getPage().reload();
        });
            cancelBt.addClickListener(clickEvent1 -> UI.getCurrent().getPage().reload());
        });
        return deleteBt;

    }

    private Button update(Book b){

        HorizontalLayout buttLayout = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout();
        Button button = new Button("Update");
        Button acceptBt = new Button("Accept");
        Button cancelBt = new Button("Cancel");
        buttLayout.add(acceptBt,cancelBt);

        acceptBt.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        cancelBt.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Dialog dialog = new Dialog();

        TextField isbtTxt = new TextField("Isbn");
        TextField titleTxt = new TextField("Title");
        TextField authorTxt = new TextField("Author");
        isbtTxt.setValue(b.getIsbn());
        titleTxt.setValue(b.getTitle());
        authorTxt.setValue(b.getAuthor());
        ComboBox<Category> categoryComboBox = new ComboBox<>("Category");
        categoryComboBox.setItems(Category.values());
        categoryComboBox.setValue(b.getCategory());
        verticalLayout.add(isbtTxt, titleTxt, authorTxt, categoryComboBox);
        button.addClickListener(clickEvent -> { dialog.open();

            dialog.add(new Label("Make changes to the book"));
            dialog.add(verticalLayout, buttLayout);
            Book findBook = bookRepository.findBookByIdd(b.getId());

            acceptBt.addClickListener(clickEvent1 -> {
                findBook.setIsbn(isbtTxt.getValue());
                findBook.setTitle(titleTxt.getValue());
                findBook.setAuthor(authorTxt.getValue());
                findBook.setCategory(categoryComboBox.getValue());
                bookRepository.save(findBook);
                UI.getCurrent().getPage().reload();
            });
            cancelBt.addClickListener(clickEvent1 -> UI.getCurrent().getPage().reload());
        });
        return button;
    }

    private void checkEmptyFields(Book book){

        if(txtIsbn.isEmpty() || txtTitle.isEmpty() || isTheSameIsbn(book)){
            Div content = new Div();
            content.addClassName("my-style");
            content.setText("Both isbn and title cannot be empty, or isbn is not unique");
            Notification notification = new Notification(content);
            notification.setDuration(3000);
            notification.setPosition(Notification.Position.MIDDLE);

            String styles = ".my-style { "
                    + "  color: red;"
                    + " }";

            StreamRegistration resource = UI.getCurrent().getSession()
                    .getResourceRegistry()
                    .registerResource(new StreamResource("styles.css", () -> {
                        byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                        return new ByteArrayInputStream(bytes);
                    }));
            UI.getCurrent().getPage().addStyleSheet(
                    "base://" + resource.getResourceUri().toString());

            notification.open();
        }

        else {
            bookRepository.save(book);
            UI.getCurrent().getPage().reload();
        }
    }

    private boolean isTheSameIsbn(Book book){

        List<Book> books = bookRepository.findAll();

        for(Book b : books){
          if( b.getIsbn().equals(book.getIsbn())) {
              return true;
          }
        }
        return false;
    }

}
