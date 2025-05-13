package gui.GestionUser;

public final class UserSession {

    private static UserSession instance;

    private int id;
    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String birthday;
    private String gender;
    private String picture;
    private String phonenumber;
    private int level;
    private int role;

    private UserSession(int id, String username, String email, String password,
                        String firstname, String lastname, String birthday,
                        String gender, String picture, String phonenumber,
                        int level, int role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
        this.gender = gender;
        this.picture = picture;
        this.phonenumber = phonenumber;
        this.level = level;
        this.role = role;
    }

    // Crée une nouvelle session ou remplace l'existante
    public static void createSession(int id, String username, String email, String password,
                                     String firstname, String lastname, String birthday,
                                     String gender, String picture, String phonenumber,
                                     int level, int role) {
        instance = new UserSession(id, username, email, password, firstname,
                lastname, birthday, gender, picture,
                phonenumber, level, role);
    }

    public static UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("User session is not initialized.");
        }
        return instance;
    }

    public static void cleanUserSession(){
        instance = null;
    }
    public void setId(int id) { this.id = id; }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setRole(int role) {
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public String getBirthday() { return birthday; }
    public String getGender() { return gender; }
    public String getPicture() { return picture; }
    public String getPhonenumber() { return phonenumber; }
    public int getLevel() { return level; }
    public int getRole() { return role; }

    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", birthday='" + birthday + '\'' +
                ", gender='" + gender + '\'' +
                ", picture='" + picture + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                ", level=" + level +
                ", role=" + role +
                '}';
    }
}
