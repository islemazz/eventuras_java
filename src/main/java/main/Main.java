package main;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import entities.Categorie;
import entities.Event;
import services.ServiceCategorie;
import services.ServiceEvent;

public class Main {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        ServiceCategorie sC = new ServiceCategorie();
        ServiceEvent sE = new ServiceEvent();
        System.out.println("Menu");
        System.out.println("1**Créez votre event**");
        System.out.println("2**Affichez tous les events et categories disponibles**");
        System.out.println("3**Modifier un evenement**");
        System.out.println("4**Créez votre propre catégorie**");
        System.out.println("5**Suprimez un event**");
        System.out.println("6**Supprimez une categorie (Admin only)**");
        System.out.println("7**Modifiez une categorie (Admin only)**");
        try {

            int choice = scanner.nextInt();
            scanner.nextLine();
            scanner.nextLine();
            switch (choice) {

                    case 1://Créer un event

                    // Retrieve and display all categories
                    ArrayList<Categorie> categories = sC.afficherAll();
                    if (categories.isEmpty()) {
                        System.out.println("Il n'y a pas de catégories valides.Ajoutez une:");
                        System.out.println("Entrez le nom de votre catégorie");
                        String nameCategorie = scanner.nextLine();
                        Categorie c = new Categorie(0, nameCategorie);

                        return;
                    }

                    System.out.println("Available Categories:");
                    for (Categorie cat : categories) {
                        System.out.println(cat.getCategory_id() + " - " + cat.getName());
                    }

                    // Prompt user to select a category
                    System.out.print("Enter the ID of the category you want to choose: ");
                    int categoryId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    // Verify if category exists
                    Categorie selectedCategory = null;
                    for (Categorie cat : categories) {
                        if (cat.getCategory_id() == categoryId) {
                            selectedCategory = cat;
                            break;
                        }
                    }
                    if (selectedCategory == null) {
                        System.out.println("Invalid category ID. Exiting...");
                        return;
                    }

                    // Prompt for event details
                    System.out.print("Enter event title: ");
                    String title = scanner.nextLine();

                    System.out.print("Enter event description: ");
                    String description = scanner.nextLine();

                    System.out.print("Enter event date (YYYY-MM-DD): ");
                    String dateStr = scanner.nextLine();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date eventDate;
                    try {
                        eventDate = dateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        System.out.println("Invalid date format. Exiting...");
                        return;
                    }

                    System.out.print("Entrez la location de l'event: ");
                    String location = scanner.nextLine();


                    //Create Event object and insert it into the database
                    Event event = new Event(title, description, eventDate, location, 01, categoryId, null);


                    sE.ajouter(event);
                    System.out.println("Event ajouté avec succès");

                    break;
                case 2: //afficher liste des events et des categories disponibles
                System.out.println(sE.afficherAll());
                System.out.println(sC.afficherAll());
                break;
                case 3://modifier un event
                System.out.println("entrez l'id de l'evenement à modifier");
                int eventId = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Entrez le nouveau titre");
                String titre = scanner.nextLine();

                Event updatedEvent= new Event(eventId,titre);
                sE.update(updatedEvent);
                break;
                case 4:
                System.out.println("Entre le nom de votre catégorie");
                String CategoryName = scanner.nextLine();
                scanner.nextLine();
                Categorie c = new Categorie(CategoryName);
                System.out.println("Category name is " + c.getName());
                sC.ajouter(c);
                break;
                case 5:
                    System.out.println("Entrez l'id de l'event que vous voulez supprimez");
                    int event_id = scanner.nextInt();
                    Event eve = new Event(event_id);
                    sE.delete(eve);
                    break;
                case 6:
                    System.out.println("Entrez l'id de la categorie que vous voulez supprimez");
                    int cat_id = scanner.nextInt();
                    Categorie cat = new Categorie(cat_id);
                    sC.delete(cat);
                    break;
                    case 7:
                        System.out.println("Entrez l'id de la categorie que vous voulez modifiez");
                        int catId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println("Entrez le nom de la categorie que vous voulez modifiez");
                        String catName = scanner.next().trim();
                        Categorie cateo = new Categorie(catId, catName);
                        sC.update(cateo);


            }
        }
        catch (SQLException e) {
            System.out.println(" Error: " + e.getMessage());
            e.printStackTrace();

        }
        scanner.close();

    }

}